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
import ape.common.codec.Helper;
import ape.common.net.ByteStream;
import ape.caravan.events.Events.Recover;
import ape.caravan.events.Events.Snapshot;
import ape.caravan.events.Events.Batch;
import ape.caravan.events.Events.Change;

public class EventCodec {

  public static abstract class StreamEvent implements ByteStream {
    public abstract void handle(Recover payload);

    public abstract void handle(Snapshot payload);

    public abstract void handle(Batch payload);

    public abstract void handle(Change payload);

    @Override
    public void request(int bytes) {
    }

    @Override
    public ByteBuf create(int size) {
      return Unpooled.buffer();
    }

    @Override
    public void next(ByteBuf buf) {
      switch (buf.readIntLE()) {
        case 80:
          handle(readBody_80(buf, new Recover()));
          return;
        case 48:
          handle(readBody_48(buf, new Snapshot()));
          return;
        case 32:
          handle(readBody_32(buf, new Batch()));
          return;
        case 16:
          handle(readBody_16(buf, new Change()));
          return;
      }
    }
  }

  public static interface HandlerEvent {
    public void handle(Recover payload);
    public void handle(Snapshot payload);
    public void handle(Batch payload);
    public void handle(Change payload);
  }

  public static void route(ByteBuf buf, HandlerEvent handler) {
    switch (buf.readIntLE()) {
      case 80:
        handler.handle(readBody_80(buf, new Recover()));
        return;
      case 48:
        handler.handle(readBody_48(buf, new Snapshot()));
        return;
      case 32:
        handler.handle(readBody_32(buf, new Batch()));
        return;
      case 16:
        handler.handle(readBody_16(buf, new Change()));
        return;
    }
  }


  public static Recover read_Recover(ByteBuf buf) {
    switch (buf.readIntLE()) {
      case 80:
        return readBody_80(buf, new Recover());
    }
    return null;
  }


  private static Recover readBody_80(ByteBuf buf, Recover o) {
    o.seq = buf.readIntLE();
    o.document = Helper.readString(buf);
    o.agent = Helper.readString(buf);
    o.authority = Helper.readString(buf);
    return o;
  }

  public static Snapshot read_Snapshot(ByteBuf buf) {
    switch (buf.readIntLE()) {
      case 48:
        return readBody_48(buf, new Snapshot());
    }
    return null;
  }


  private static Snapshot readBody_48(ByteBuf buf, Snapshot o) {
    o.seq = buf.readIntLE();
    o.history = buf.readIntLE();
    o.document = Helper.readString(buf);
    o.assetBytes = buf.readLongLE();
    return o;
  }

  public static Batch read_Batch(ByteBuf buf) {
    switch (buf.readIntLE()) {
      case 32:
        return readBody_32(buf, new Batch());
    }
    return null;
  }


  private static Batch readBody_32(ByteBuf buf, Batch o) {
    o.changes = Helper.readArray(buf, (n) -> new Change[n], () -> read_Change(buf));
    return o;
  }

  public static Change read_Change(ByteBuf buf) {
    switch (buf.readIntLE()) {
      case 16:
        return readBody_16(buf, new Change());
    }
    return null;
  }


  private static Change readBody_16(ByteBuf buf, Change o) {
    o.agent = Helper.readString(buf);
    o.authority = Helper.readString(buf);
    o.seq_begin = buf.readIntLE();
    o.seq_end = buf.readIntLE();
    o.request = Helper.readString(buf);
    o.redo = Helper.readString(buf);
    o.undo = Helper.readString(buf);
    o.active = buf.readBoolean();
    o.delay = buf.readIntLE();
    o.dAssetBytes = buf.readLongLE();
    return o;
  }

  public static void write(ByteBuf buf, Recover o) {
    if (o == null) {
      buf.writeIntLE(0);
      return;
    }
    buf.writeIntLE(80);
    buf.writeIntLE(o.seq);
    Helper.writeString(buf, o.document);;
    Helper.writeString(buf, o.agent);;
    Helper.writeString(buf, o.authority);;
  }

  public static void write(ByteBuf buf, Snapshot o) {
    if (o == null) {
      buf.writeIntLE(0);
      return;
    }
    buf.writeIntLE(48);
    buf.writeIntLE(o.seq);
    buf.writeIntLE(o.history);
    Helper.writeString(buf, o.document);;
    buf.writeLongLE(o.assetBytes);
  }

  public static void write(ByteBuf buf, Batch o) {
    if (o == null) {
      buf.writeIntLE(0);
      return;
    }
    buf.writeIntLE(32);
    Helper.writeArray(buf, o.changes, (item) -> write(buf, item));
  }

  public static void write(ByteBuf buf, Change o) {
    if (o == null) {
      buf.writeIntLE(0);
      return;
    }
    buf.writeIntLE(16);
    Helper.writeString(buf, o.agent);;
    Helper.writeString(buf, o.authority);;
    buf.writeIntLE(o.seq_begin);
    buf.writeIntLE(o.seq_end);
    Helper.writeString(buf, o.request);;
    Helper.writeString(buf, o.redo);;
    Helper.writeString(buf, o.undo);;
    buf.writeBoolean(o.active);
    buf.writeIntLE(o.delay);
    buf.writeLongLE(o.dAssetBytes);
  }
}
