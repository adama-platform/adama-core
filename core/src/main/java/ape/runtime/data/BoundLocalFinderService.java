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
package ape.runtime.data;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.NamedRunnable;
import ape.common.SimpleExecutor;

import java.util.HashMap;
import java.util.List;

/** a local cache of items bound to the current host */
public class BoundLocalFinderService implements FinderService {
  private final FinderService global;
  public final String region;
  public final String machine;
  private final HashMap<Key, DocumentLocation> cacheLocked;
  private final SimpleExecutor executor;

  public BoundLocalFinderService(SimpleExecutor executor, FinderService global, String region, String machine) {
    this.executor = executor;
    this.global = global;
    this.region = region;
    this.machine = machine;
    this.cacheLocked = new HashMap<>();
  }

  @Override
  public void find(Key key, Callback<DocumentLocation> callback) {
    executor.execute(new NamedRunnable("find") {
      @Override
      public void execute() throws Exception {
        DocumentLocation cached = cacheLocked.get(key);
        if (cached != null) {
          callback.success(cached);
          return;
        }
        global.find(key, callback);
      }
    });
  }

  @Override
  public void bind(Key key, Callback<Void> callback) {
    global.bind(key, new Callback<Void>() {
      @Override
      public void success(Void value) {
        // do this in a different thread to avoid a deadlock
        executor.execute(new NamedRunnable("find-after-bind") {
          @Override
          public void execute() throws Exception {
            global.find(key, new Callback<DocumentLocation>() {
              @Override
              public void success(DocumentLocation result) {
                executor.execute(new NamedRunnable("write-cache") {
                  @Override
                  public void execute() throws Exception {
                    cacheLocked.put(key, result);
                  }
                });
                callback.success(null);
              }

              @Override
              public void failure(ErrorCodeException ex) {
                // technically, the bind was a success; we just couldn't cache it
                callback.success(null);
              }
            });
          }
        });
      }

      @Override
      public void failure(ErrorCodeException ex) {
        callback.failure(ex);
      }
    });
  }

  @Override
  public void free(Key key, Callback<Void> callback) {
    executor.execute(new NamedRunnable("cache-free") {
      @Override
      public void execute() throws Exception {
        cacheLocked.remove(key);
        global.free(key, callback);
      }
    });
  }

  @Override
  public void backup(Key key, BackupResult result, Callback<Void> callback) {
    executor.execute(new NamedRunnable("cache-backup") {
      @Override
      public void execute() throws Exception {
        DocumentLocation location = cacheLocked.get(key);
        if (location != null) {
          cacheLocked.put(key, new DocumentLocation(location.id, location.location, location.region, location.machine, result.archiveKey, location.deleted));
        }
        global.backup(key, result, callback);
      }
    });
  }

  @Override
  public void markDelete(Key key, Callback<Void> callback) {
    global.markDelete(key, callback);
  }

  @Override
  public void commitDelete(Key key, Callback<Void> callback) {
    executor.execute(new NamedRunnable("cache-commit-delete") {
      @Override
      public void execute() throws Exception {
        cacheLocked.remove(key);
        global.commitDelete(key, callback);
      }
    });
  }

  @Override
  public void list(Callback<List<Key>> callback) {
    global.list(callback);
  }

  @Override
  public void listDeleted(Callback<List<Key>> callback) {
    global.listDeleted(callback);
  }
}
