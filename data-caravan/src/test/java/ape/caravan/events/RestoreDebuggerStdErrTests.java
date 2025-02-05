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
package ape.caravan.events;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.util.ArrayList;

public class RestoreDebuggerStdErrTests {
  @Test
  public void batch() {
    ArrayList<byte[]> writes = new ArrayList<>();
    Events.Batch batch = new Events.Batch();
    batch.changes = new Events.Change[2];
    batch.changes[0] = new Events.Change();
    batch.changes[0].seq_begin = 2;
    batch.changes[0].seq_end = 4;
    batch.changes[0].active = false;
    batch.changes[0].agent = "agent";
    batch.changes[0].authority = "authority";
    batch.changes[0].dAssetBytes = 100;
    batch.changes[0].redo = "{\"x\":2}";
    batch.changes[0].undo = "undo";

    batch.changes[1] = new Events.Change();
    batch.changes[1].seq_begin = 4;
    batch.changes[1].seq_end = 5;
    batch.changes[1].active = false;
    batch.changes[1].agent = "agent";
    batch.changes[1].authority = "authority";
    batch.changes[1].dAssetBytes = 100;
    batch.changes[1].redo = "{\"x\":3}";
    batch.changes[1].undo = "undo";

    ByteBuf buf = Unpooled.buffer();
    EventCodec.write(buf, batch);
    writes.add(ByteArrayHelper.convert(buf));
    RestoreDebuggerStdErr.print(writes);
  }
}
