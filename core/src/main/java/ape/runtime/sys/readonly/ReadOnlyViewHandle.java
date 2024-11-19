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
package ape.runtime.sys.readonly;

import ape.common.NamedRunnable;
import ape.common.SimpleExecutor;
import ape.runtime.json.JsonStreamReader;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.sys.StreamHandle;

/** a way to control the stream after the fact */
public class ReadOnlyViewHandle {
  private final NtPrincipal who;
  ReadOnlyLivingDocument document;
  private final StreamHandle handle;
  private final SimpleExecutor executor;

  public ReadOnlyViewHandle(NtPrincipal who, ReadOnlyLivingDocument document, StreamHandle handle, SimpleExecutor executor) {
    this.who = who;
    this.document = document;
    this.handle = handle;
    this.executor = executor;
  }

  public void update(String update) {
    executor.execute(new NamedRunnable("update-ro-view") {
      @Override
      public void execute() throws Exception {
        handle.ingestViewUpdate(new JsonStreamReader(update));
        document.forceUpdate(who);
      }
    });
  }

  public void close() {
    executor.execute(new NamedRunnable("update-ro-view") {
      @Override
      public void execute() throws Exception {
        handle.kill();
        document.garbageCollectViewsFor(who);
        handle.disconnect();
      }
    });
  }
}
