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
package ape.runtime.async;

import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.reactives.RxInt32;
import org.junit.Assert;
import org.junit.Test;

public class IdHistoryLogTests {
  @Test
  public void simple() {
    IdHistoryLog log = new IdHistoryLog();
    RxInt32 base = new RxInt32(null, 0);
    Assert.assertEquals(1, log.next(base));
    Assert.assertEquals(2, log.next(base));
    Assert.assertEquals(3, log.next(base));
    log.revert();
    Assert.assertEquals(1, log.next(base));
    Assert.assertEquals(2, log.next(base));
    Assert.assertEquals(3, log.next(base));
    Assert.assertTrue(log.resetDirtyGetPriorDirty());
    Assert.assertFalse(log.resetDirtyGetPriorDirty());
    Assert.assertEquals(4, log.next(base));
    Assert.assertTrue(log.resetDirtyGetPriorDirty());
    Assert.assertFalse(log.resetDirtyGetPriorDirty());
    Assert.assertEquals(5, log.next(base));
    Assert.assertEquals(6, log.next(base));
    JsonStreamWriter writer = new JsonStreamWriter();
    log.dump(writer);
    Assert.assertEquals("[1,2,3,4,5,6]", writer.toString());
  }

  @Test
  public void recover() {
    IdHistoryLog log = IdHistoryLog.read(new JsonStreamReader("[4,5,6]"));
    RxInt32 base = new RxInt32(null, 0);
    Assert.assertEquals(4, log.next(base));
    Assert.assertEquals(5, log.next(base));
    Assert.assertEquals(6, log.next(base));
    Assert.assertEquals(7, log.next(base)); // questionable
  }

  @Test
  public void junk() {
    Assert.assertFalse(IdHistoryLog.read(new JsonStreamReader("{}")).has());
  }
}
