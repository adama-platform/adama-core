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
package ape.runtime.data.managed;

import ape.common.NamedRunnable;
import ape.common.SimpleExecutor;
import ape.runtime.data.ArchivingDataService;
import ape.runtime.data.FinderService;
import ape.runtime.data.Key;
import ape.runtime.data.PostDocumentDelete;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/** shared state between the machine and the managed data service */
public class Base {
  public final FinderService finder;
  public final ArchivingDataService data;
  public final PostDocumentDelete delete;
  public final String region;
  public final String machine;
  public final HashMap<Key, Machine> documents;
  public final SimpleExecutor executor;
  public final int archiveTimeMilliseconds;
  private final AtomicInteger failureBackoff;

  public Base(FinderService finder, ArchivingDataService data, final PostDocumentDelete delete, String region, String machine, SimpleExecutor executor, int archiveTimeMilliseconds) {
    this.finder = finder;
    this.data = data;
    this.delete = delete;
    this.region = region;
    this.machine = machine;
    this.documents = new HashMap<>();
    this.executor = executor;
    this.archiveTimeMilliseconds = archiveTimeMilliseconds;
    this.failureBackoff = new AtomicInteger(1);
  }

  /** jump into a state machine for a given key */
  public void on(Key key, Consumer<Machine> action) {
    executor.execute(new NamedRunnable("managed-on") {
      @Override
      public void execute() throws Exception {
        Machine machine = documents.get(key);
        if (machine == null) {
          machine = new Machine(key, Base.this);
          documents.put(key, machine);
        }
        action.accept(machine);
      }
    });
  }

  public void reportSuccess() {
    this.failureBackoff.set(Math.max(100, (int) (failureBackoff.get() * Math.random())));
  }

  public int reportFailureGetRetryBackoff() {
    int prior = failureBackoff.get();
    this.failureBackoff.set(Math.min(10000, (int) (prior * (1.0 + Math.random()))) + 100);
    return prior;
  }

}
