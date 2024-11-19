/*
* Adama Platform and Language
* Copyright (C) 2021 - 2025 by Adama Platform Engineering, LLC
* 
* This program is free software for non-commercial purposes: 
* you can redistribute it and/or modify it under the terms of the 
* GNU Affero General Public License as published by the Free Software Foundation,
* either version 3 of the License, or (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
* 
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
