/**
 * MIT License
 * 
 * Copyright (C) 2021 - 2025 by Adama Platform Engineering, LLC
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ape.runtime.data.managed;

import ape.ErrorCodes;
import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.NamedRunnable;
import ape.runtime.contracts.DeleteTask;
import ape.runtime.data.BackupResult;
import ape.runtime.data.DocumentLocation;
import ape.runtime.data.Key;
import ape.runtime.data.LocationType;
import ape.runtime.data.*;

import java.util.ArrayList;

public class Machine {
  private final Key key;
  private final Base base;
  private State state;
  private ArrayList<Action> actions;
  private boolean closed;
  private int pendingWrites;
  private Runnable cancelArchive;
  private int writesInFlight;
  private String lastArchiveKey;
  private boolean attemptClose;
  private boolean deleted;

  public Machine(Key key, Base base) {
    this.key = key;
    this.base = base;
    this.state = State.Unknown;
    this.actions = null;
    this.closed = false;
    this.pendingWrites = 0;
    this.cancelArchive = null;
    this.writesInFlight = 0;
    this.lastArchiveKey = null;
    this.attemptClose = false;
    this.deleted = false;
  }

  private void queue(Action action) {
    if (actions == null) {
      actions = new ArrayList<>();
    }
    actions.add(action);
  }

  private void failQueueWhileInExecutor(ErrorCodeException ex) {
    if (actions != null) {
      ArrayList<Action> tokill = actions;
      actions = null;
      for (Action action : tokill) {
        action.callback.failure(ex);
      }
    }
  }

  private void executeClosed() {
    closed = true;
    base.finder.free(key, new Callback<Void>() {
      @Override
      public void success(Void value) {
        base.reportSuccess();
        base.executor.execute(new NamedRunnable("machine-archive-freed") {
          @Override
          public void execute() throws Exception {
            base.documents.remove(key);
            base.data.delete(key, DeleteTask.TRIVIAL, Callback.DONT_CARE_VOID);
          }
        });
      }

      @Override
      public void failure(ErrorCodeException ex) {
        base.executor.schedule(new NamedRunnable("machine-free-retry") {
          @Override
          public void execute() throws Exception {
            executeClosed();
          }
        }, base.reportFailureGetRetryBackoff());
      }
    });
  }

  private void archive_Success(BackupResult result) {
    base.executor.execute(new NamedRunnable("machine-archive-success") {
      @Override
      public void execute() throws Exception {
        if (lastArchiveKey != null) {
          base.data.cleanUp(key, lastArchiveKey);
        }
        lastArchiveKey = result.archiveKey;
        if (deleted) {
          base.data.cleanUp(key, lastArchiveKey);
          failQueueWhileInExecutor(new ErrorCodeException(ErrorCodes.MANAGED_STORAGE_DELETED));
          return;
        }
        cancelArchive = null;
        pendingWrites -= writesInFlight;
        writesInFlight = 0;
        if (pendingWrites > 0) {
          scheduleArchiveWhileInExecutor(false);
        } else if (attemptClose) {
          executeClosed();
        }
      }
    });
  }

  private void archive_Failure(Exception ex, BackupResult result) {
    base.executor.execute(new NamedRunnable("machine-archive-failure") {
      @Override
      public void execute() throws Exception {
        if (result != null) {
          base.data.cleanUp(key, result.archiveKey);
        }
        cancelArchive = null;
        if (!(closed || deleted)) {
          scheduleArchiveWhileInExecutor(true);
        }
      }
    });
  }

  private void archiveWhileInExecutor() {
    base.data.backup(key, new Callback<>() {
      @Override
      public void success(BackupResult result) {
        base.finder.backup(key, result, new Callback<Void>() {
          @Override
          public void success(Void value) {
            archive_Success(result);
          }

          @Override
          public void failure(ErrorCodeException ex) {
            archive_Failure(ex, result);
          }
        });
      }

      @Override
      public void failure(ErrorCodeException ex) {
        archive_Failure(ex, null);
      }
    });
  }

  private void scheduleArchiveWhileInExecutor(boolean dueToFailure) {
    if (cancelArchive == null) {
      writesInFlight = pendingWrites;
      cancelArchive = base.executor.schedule(new NamedRunnable("machine-archive") {
        @Override
        public void execute() throws Exception {
          archiveWhileInExecutor();
        }
      }, dueToFailure ? base.reportFailureGetRetryBackoff() : base.archiveTimeMilliseconds);
    }
  }

  private void find_FoundMachine(boolean postRestore) {
    base.executor.execute(new NamedRunnable("machine-found-machine") {
      @Override
      public void execute() throws Exception {
        if (deleted) {
          state = State.Unknown;
          failQueueWhileInExecutor(new ErrorCodeException(ErrorCodes.MANAGED_STORAGE_DELETED));
          return;
        }
        if (closed || attemptClose) {
          state = State.Unknown;
          base.documents.remove(key);
          failQueueWhileInExecutor(new ErrorCodeException(ErrorCodes.MANAGED_STORAGE_CLOSED_BEFORE_FOUND));
          return;
        }
        state = State.OnMachine;
        // since we found it on the machine, we _may_ have local changes
        if (!postRestore) {
          pendingWrites++;
        }
        ArrayList<Action> toact = actions;
        actions = null;
        for (Action action : toact) {
          action.action.run();
        }
        if (pendingWrites > 0) {
          scheduleArchiveWhileInExecutor(false);
        }
      }
    });
  }

  private void restore_Failed(ErrorCodeException ex) {
    base.executor.execute(new NamedRunnable("machine-restoring-failed") {
      @Override
      public void execute() throws Exception {
        state = State.Unknown;
        failQueueWhileInExecutor(ex);
        base.documents.remove(key);
      }
    });
  }

  private void find_Restore(String archiveKey) {
    base.executor.execute(new NamedRunnable("machine-found-archive") {
      @Override
      public void execute() throws Exception {
        state = State.Restoring;
        base.data.restore(key, archiveKey, new Callback<Void>() {
          @Override
          public void success(Void value) {
            base.finder.bind(key, new Callback<Void>() {
              @Override
              public void success(Void value) {
                find_FoundMachine(true);
              }

              @Override
              public void failure(ErrorCodeException ex) {
                restore_Failed(ex);
              }
            });
          }

          @Override
          public void failure(ErrorCodeException ex) {
            restore_Failed(ex);
          }
        });
      }
    });
  }

  private void find() {
    state = State.Finding;
    base.finder.find(key, new Callback<>() {
      @Override
      public void success(DocumentLocation found) {
        base.executor.execute(new NamedRunnable("got-find-result") {
          @Override
          public void execute() throws Exception {
            lastArchiveKey = found.archiveKey;
            if ("".equals(lastArchiveKey)) {
              lastArchiveKey = null;
            }
            if (found.location == LocationType.Machine) {
              if (found.region.equals(base.region) && found.machine.equals(base.machine)) {
                find_FoundMachine(false);
              } else {
                failQueueWhileInExecutor(new ErrorCodeException(ErrorCodes.MANAGED_STORAGE_WRONG_MACHINE));
              }
            } else {
              if (lastArchiveKey == null) {
                failQueueWhileInExecutor(new ErrorCodeException(ErrorCodes.MANAGED_STORAGE_NULL_ARCHIVE, key.space + "/" + key.key));
              } else {
                find_Restore(lastArchiveKey);
              }
            }
          }
        });
      }

      @Override
      public void failure(ErrorCodeException ex) {
        base.executor.execute(new NamedRunnable("machine-find-failure") {
          @Override
          public void execute() throws Exception {
            base.documents.remove(key);
            failQueueWhileInExecutor(ex);
          }
        });
      }
    });
  }

  public void write(Action action) {
    if (attemptClose) {
      attemptClose = false;
    }
    if (closed) {
      action.callback.failure(new ErrorCodeException(ErrorCodes.MANAGED_STORAGE_WRITE_FAILED_CLOSED));
      base.documents.remove(key);
      return;
    }
    pendingWrites++;
    switch (state) {
      case Unknown:
        find();
      case Finding:
      case Restoring:
        queue(action);
        return;
      case OnMachine:
        action.action.run();
        scheduleArchiveWhileInExecutor(false);
    }
  }

  public void read(Action action) {
    if (attemptClose) {
      attemptClose = false;
    }
    if (closed) {
      action.callback.failure(new ErrorCodeException(ErrorCodes.MANAGED_STORAGE_READ_FAILED_CLOSED));
      base.documents.remove(key);
      return;
    }
    switch (state) {
      case Unknown:
        find();
      case Finding:
      case Restoring:
        queue(action);
        return;
      case OnMachine:
        action.action.run();
    }
  }

  private void signalClose(boolean shed) {
    attemptClose = true;
    if (state == State.Unknown) {
      closed = true;
      base.documents.remove(key);
      return;
    }
    if (state == State.OnMachine) {
      if (pendingWrites == 0) {
        executeClosed();
      } else if (shed) {
        if (cancelArchive != null) {
          cancelArchive.run();
          cancelArchive = null;
        }
        scheduleArchiveWhileInExecutor(true);
      }
    }
  }

  public void shed() {
    signalClose(true);
  }

  public void close() {
    signalClose(false);
  }

  public void delete() {
    attemptClose = false;
    closed = true;
    deleted = true;
    base.documents.remove(key);
    if (lastArchiveKey != null) {
      base.data.cleanUp(key, lastArchiveKey);
      lastArchiveKey = null;
    }
  }
}
