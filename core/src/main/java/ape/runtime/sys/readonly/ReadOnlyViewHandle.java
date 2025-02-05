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
