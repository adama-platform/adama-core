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

import ape.ErrorCodes;
import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.TimeSource;
import ape.runtime.contracts.AutoMorphicAccumulator;
import ape.runtime.contracts.DeleteTask;
import ape.runtime.json.JsonAlgebra;
import ape.runtime.natives.NtPrincipal;

import java.util.*;
import java.util.concurrent.Executor;

/**
 * provides a canonical "in-memory" service for backing Adama. Beyond providing a simple way to
 * benchmark the stack above Adama, this should be a super fast version.
 */
public class InMemoryDataService implements DataService {

  private final HashMap<Key, InMemoryDocument> datum;
  private final TimeSource time;
  private final Executor executor;

  public InMemoryDataService(Executor executor, TimeSource time) {
    this.datum = new HashMap<>();
    this.executor = executor;
    this.time = time;
  }

  @Override
  public void get(Key key, Callback<LocalDocumentChange> callback) {
    executor.execute(() -> {
      InMemoryDocument document = datum.get(key);
      int reads = 0;
      if (document == null) {
        callback.failure(new ErrorCodeException(ErrorCodes.UNIVERSAL_LOOKUP_FAILED));
        return;
      }
      AutoMorphicAccumulator<String> merge = JsonAlgebra.mergeAccumulator();
      for (RemoteDocumentUpdate update : document.updates) {
        merge.next(update.redo);
        reads++;
      }
      callback.success(new LocalDocumentChange(merge.finish(), reads, document.seq));
    });
  }

  @Override
  public void initialize(Key key, RemoteDocumentUpdate patch, Callback<Void> callback) {
    executor.execute(() -> {
      if (datum.containsKey(key)) {
        callback.failure(new ErrorCodeException(ErrorCodes.UNIVERSAL_INITIALIZE_FAILURE));
        return;
      }
      InMemoryDocument document = new InMemoryDocument();
      document.seq = patch.seqEnd;
      document.updates.add(patch);
      datum.put(key, document);
      callback.success(null);
    });
  }

  @Override
  public void patch(Key key, RemoteDocumentUpdate[] patches, Callback<Void> callback) {
    executor.execute(() -> {
      InMemoryDocument document = datum.get(key);
      if (document == null) {
        callback.failure(new ErrorCodeException(ErrorCodes.INMEMORY_DATA_PATCH_CANT_FIND_DOCUMENT));
        return;
      }
      if (patches[0].seqBegin != document.seq + 1) {
        callback.failure(new ErrorCodeException(ErrorCodes.UNIVERSAL_PATCH_FAILURE_HEAD_SEQ_OFF));
        return;
      }
      document.seq = patches[patches.length - 1].seqEnd;
      Collections.addAll(document.updates, patches);
      if (patches[patches.length - 1].requiresFutureInvalidation) {
        document.active = true;
        document.timeToWake = patches[patches.length - 1].whenToInvalidateMilliseconds + time.nowMilliseconds();
      } else {
        document.active = false;
        document.timeToWake = 0L;
      }
      callback.success(null);
    });
  }

  @Override
  public void compute(Key key, ComputeMethod method, int seq, Callback<LocalDocumentChange> callback) {
    executor.execute(() -> {
      InMemoryDocument document = datum.get(key);
      if (document == null) {
        callback.failure(new ErrorCodeException(ErrorCodes.INMEMORY_DATA_COMPUTE_CANT_FIND_DOCUMENT));
        return;
      }
      if (method == ComputeMethod.HeadPatch) {
        AutoMorphicAccumulator<String> redo = JsonAlgebra.mergeAccumulator();
        int reads = 0;
        // get items in order
        for (RemoteDocumentUpdate update : document.updates) {
          if (update.seqBegin > seq) {
            redo.next(update.redo);
            reads++;
          }
        }
        if (redo.empty()) {
          callback.failure(new ErrorCodeException(ErrorCodes.INMEMORY_DATA_COMPUTE_PATCH_NOTHING_TODO));
          return;
        }
        callback.success(new LocalDocumentChange(redo.finish(), reads, document.seq));
        return;
      }
      if (method == ComputeMethod.Rewind) {
        Stack<RemoteDocumentUpdate> toUndo = new Stack<>();
        int reads = 0;
        // get items in order
        for (RemoteDocumentUpdate update : document.updates) {
          if (update.seqBegin >= seq) {
            toUndo.push(update);
            reads++;
          }
        }
        // walk them backwards to build appropriate undo
        AutoMorphicAccumulator<String> undo = JsonAlgebra.mergeAccumulator();
        while (!toUndo.empty()) {
          undo.next(toUndo.pop().undo);
        }
        if (undo.empty()) {
          callback.failure(new ErrorCodeException(ErrorCodes.INMEMORY_DATA_COMPUTE_REWIND_NOTHING_TODO));
          return;
        }
        callback.success(new LocalDocumentChange(undo.finish(), reads, document.seq));
        return;
      }

      callback.failure(new ErrorCodeException(ErrorCodes.INMEMORY_DATA_COMPUTE_INVALID_METHOD));
    });
  }

  @Override
  public void delete(Key key, DeleteTask task, Callback<Void> callback) {
    executor.execute(() -> {
      InMemoryDocument document = datum.remove(key);
      if (document == null) {
        callback.failure(new ErrorCodeException(ErrorCodes.INMEMORY_DATA_DELETE_CANT_FIND_DOCUMENT));
      } else {
        task.executeAfterMark(callback);
      }
    });
  }

  @Override
  public void snapshot(Key key, DocumentSnapshot snapshot, Callback<Integer> callback) {
    executor.execute(() -> {
      InMemoryDocument document = datum.get(key);
      if (document != null) {
        callback.success(document.compact(snapshot.history));
      } else {
        callback.failure(new ErrorCodeException(ErrorCodes.INMEMORY_DATA_COMPACT_CANT_FIND_DOCUMENT));
      }
    });
  }

  @Override
  public void shed(Key key) {
    close(key, Callback.DONT_CARE_VOID);
  }

  @Override
  public void inventory(Callback<Set<Key>> callback) {
    executor.execute(() -> callback.success(new TreeSet<>(datum.keySet())));
  }

  @Override
  public void close(Key key, Callback<Void> callback) {
    executor.execute(() -> {
      InMemoryDocument document = datum.remove(key);
      callback.success(null);
    });
  }

  @Override
  public void recover(Key key, DocumentRestore restore, Callback<Void> callback) {
    executor.execute(() -> {
      InMemoryDocument document = datum.get(key);
      if (document == null) {
        callback.failure(new ErrorCodeException(ErrorCodes.INMEMORY_DATA_RESTORE_CANT_FIND_DOCUMENT));
        return;
      }
      document.updates.clear();
      document.seq = restore.seq;
      document.updates.add(new RemoteDocumentUpdate(restore.seq, restore.seq, restore.who, "{\"restore\":" + restore.seq + "}", restore.document, "{}", true, 0, 0, UpdateType.Restore));
      callback.success(null);
    });
  }

  private static class InMemoryDocument {
    private final ArrayList<RemoteDocumentUpdate> updates;
    private boolean active;
    private long timeToWake;
    private int seq;

    public InMemoryDocument() {
      this.updates = new ArrayList<>();
      this.active = false;
      this.timeToWake = 0;
      this.seq = 0;
    }

    public int compact(int history) {
      int toCompact = updates.size() - history;
      if (toCompact > 1) {
        AutoMorphicAccumulator<String> mergeRedo = JsonAlgebra.mergeAccumulator();
        AutoMorphicAccumulator<String> mergeUndo = JsonAlgebra.mergeAccumulator();
        Stack<String> undo = new Stack<>();
        long assetBytes = 0;
        for (int k = 0; k < toCompact; k++) {
          RemoteDocumentUpdate update = updates.remove(0);
          assetBytes += update.assetBytes;
          mergeRedo.next(update.redo);
          undo.push(update.undo);
        }
        while (!undo.empty()) {
          mergeUndo.next(undo.pop());
        }
        RemoteDocumentUpdate newHead = new RemoteDocumentUpdate(0, 0, NtPrincipal.NO_ONE, "{}", mergeRedo.finish(), mergeUndo.finish(), false, 0, assetBytes, UpdateType.CompactedResult);
        updates.add(0, newHead);
        return toCompact - 1;
      }
      return 0;
    }
  }
}
