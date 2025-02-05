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

import ape.runtime.natives.NtPrincipal;
import org.junit.Assert;
import org.junit.Test;

public class RemoteDocumentUpdateTests {
  private static final RemoteDocumentUpdate UPDATE_1 = new RemoteDocumentUpdate(1, 1, NtPrincipal.NO_ONE, "REQUEST", "{\"x\":1}", "{\"x\":0}", false, 0, 100, UpdateType.AddUserData);
  private static final RemoteDocumentUpdate UPDATE_2 = new RemoteDocumentUpdate(2, 2, null, "REQUEST", "{\"x\":2}", "{\"x\":1}", true, 0, 100, UpdateType.Invalidate);
  private static final RemoteDocumentUpdate UPDATE_3 = new RemoteDocumentUpdate(3, 3, null, "REQUEST", "{\"x\":3}", "{\"x\":2}", true, 0, 100, UpdateType.Invalidate);
  private static final RemoteDocumentUpdate UPDATE_4 = new RemoteDocumentUpdate(4, 4, null, "REQUEST", "{\"x\":4}", "{\"x\":3}", true, 0, 100, UpdateType.AddUserData);
  private static final RemoteDocumentUpdate UPDATE_5 = new RemoteDocumentUpdate(5, 5, null, "REQUEST", "{\"x\":5}", "{\"x\":4}", true, 0, 100, UpdateType.Invalidate);
  private static final RemoteDocumentUpdate UPDATE_6 = new RemoteDocumentUpdate(6, 6, null, "REQUEST", "{\"x\":6}", "{\"x\":5}", true, 0, 100, UpdateType.AddUserData);

  @Test
  public void merging() {
    RemoteDocumentUpdate[] input = new RemoteDocumentUpdate[]{UPDATE_1, UPDATE_2, UPDATE_3, UPDATE_4, UPDATE_5, UPDATE_6};
    RemoteDocumentUpdate[] output = RemoteDocumentUpdate.compact(input);
    Assert.assertEquals(3, output.length);
    Assert.assertEquals(1, output[0].seqBegin);
    Assert.assertEquals(3, output[0].seqEnd);
    Assert.assertEquals(4, output[1].seqBegin);
    Assert.assertEquals(5, output[1].seqEnd);
    Assert.assertEquals(6, output[2].seqBegin);
    Assert.assertEquals(6, output[2].seqEnd);
    Assert.assertEquals("{\"x\":3}", output[0].redo);
    Assert.assertEquals("{\"x\":0}", output[0].undo);
    Assert.assertEquals("{\"x\":5}", output[1].redo);
    Assert.assertEquals("{\"x\":3}", output[1].undo);
    Assert.assertEquals("{\"x\":6}", output[2].redo);
    Assert.assertEquals("{\"x\":5}", output[2].undo);
  }

  @Test
  public void trivial_merge() {
    RemoteDocumentUpdate[] input = new RemoteDocumentUpdate[]{UPDATE_1};
    Assert.assertTrue(input == RemoteDocumentUpdate.compact(input));
  }
}
