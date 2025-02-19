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
package ape.runtime.natives;

import ape.runtime.mocks.MockMessage;
import ape.runtime.natives.algo.CanWriteCSV;
import ape.runtime.natives.algo.MessageCSVWriter;
import org.junit.Assert;
import org.junit.Test;

public class NtTableTests {
  @Test
  public void flow() {
    final var table = new NtTable<>(MockMessage::new);
    new NtTable<>(table);
    table.make();
    Assert.assertEquals(1, table.size());
    table.make();
    Assert.assertEquals(2, table.size());
    table.delete();
    Assert.assertEquals(0, table.size());
    Assert.assertEquals(0, table.iterate(false).size());
    table.make();
    table.make();
    table.make();
    Assert.assertEquals(3, table.iterate(false).size());
    table.__raiseInvalid();
  }

  @Test
  public void csv() {
    final var table = new NtTable<>(MockMessage::new);
    new NtTable<>(table);
    table.make();
    String csv = table.to_csv("x,y", (i) -> (new CanWriteCSV() {
      @Override
      public void __write_csv_row(MessageCSVWriter __writer) {
        __writer.write("x");
      }
    }));
    Assert.assertEquals("x,y\r\n" + "x", csv);
  }
}
