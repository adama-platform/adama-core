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

import io.netty.buffer.Unpooled;

import java.util.ArrayList;

public class RestoreDebuggerStdErr implements EventCodec.HandlerEvent {
  @Override
  public void handle(Events.Snapshot payload) {
    System.err.println("Snapshot["+payload.seq+"]:" + payload.document);
  }

  @Override
  public void handle(Events.Batch payload) {
    if (payload.changes.length == 1) {
      handle(payload.changes[0]);
      return;
    }
    System.err.println("Batch:" + payload.changes.length);
    int index = 0;
    for (Events.Change change : payload.changes) {
      System.err.print("[" + index + "] ");
      handle(change);
      index++;
    }
  }

  @Override
  public void handle(Events.Change payload) {
    System.err.println("Change[" + payload.seq_begin + "->" + payload.seq_end + "," + payload.dAssetBytes + "] = " + payload.redo);
  }

  @Override
  public void handle(Events.Recover payload) {
    System.err.println("Recover[" + payload.seq + "]");
  }

  public static void print(ArrayList<byte[]> writes) {
    for (byte[] write : writes) {
      EventCodec.route(Unpooled.wrappedBuffer(write), new RestoreDebuggerStdErr());
    }
  }
}
