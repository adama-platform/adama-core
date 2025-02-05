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
package ape.common.codec;

import io.netty.buffer.ByteBuf;
import ape.common.codec.CodecCodeGenTests.TestClassA;
import ape.common.codec.CodecCodeGenTests.TestClassB;
import ape.common.net.ByteStream;

public class GeneratedCodecMe {

  public static void route(ByteBuf buf, HandlerX handler) {
    switch (buf.readIntLE()) {
      case 123:
        handler.handle(readBody_123(buf, new TestClassA()));
        return;
      case 42:
        handler.handle(readBody_42(buf, new TestClassA()));
        return;
      case 4242:
        handler.handle(readBody_4242(buf, new TestClassB()));
    }
  }

  private static TestClassA readBody_123(ByteBuf buf, TestClassA o) {
    o.x = buf.readIntLE();
    o.str = Helper.readString(buf);
    o.w = buf.readDoubleLE();
    o.sssshort = buf.readShortLE();
    o.bbb = buf.readBoolean();
    o.strarr = Helper.readStringArray(buf);
    return o;
  }

  private static TestClassA readBody_42(ByteBuf buf, TestClassA o) {
    o.x = buf.readIntLE();
    o.z = buf.readDoubleLE();
    o.w = buf.readDoubleLE();
    o.sssshort = buf.readShortLE();
    o.bbb = buf.readBoolean();
    o.strarr = Helper.readStringArray(buf);
    return o;
  }

  private static TestClassB readBody_4242(ByteBuf buf, TestClassB o) {
    o.x = buf.readIntLE();
    o.embed = read_TestClassA(buf);
    o.lng = buf.readLongLE();
    o.arr = Helper.readArray(buf, (n) -> new TestClassA[n], () -> read_TestClassA(buf));
    return o;
  }

  public static TestClassA read_TestClassA(ByteBuf buf) {
    switch (buf.readIntLE()) {
      case 123:
        return readBody_123(buf, new TestClassA());
      case 42:
        return readBody_42(buf, new TestClassA());
    }
    return null;
  }

  public static void route(ByteBuf buf, HandlerY handler) {
    if (buf.readIntLE() == 4242) {
      handler.handle(readBody_4242(buf, new TestClassB()));
    }
  }

  public static TestClassA readRegister_TestClassA(ByteBuf buf, TestClassA o) {
    switch (buf.readIntLE()) {
      case 123:
        return readBody_123(buf, o);
      case 42:
        return readBody_42(buf, o);
    }
    return null;
  }

  public static TestClassB read_TestClassB(ByteBuf buf) {
    if (buf.readIntLE() == 4242) {
      return readBody_4242(buf, new TestClassB());
    }
    return null;
  }

  public static void write(ByteBuf buf, TestClassB o) {
    if (o == null) {
      buf.writeIntLE(0);
      return;
    }
    buf.writeIntLE(4242);
    buf.writeIntLE(o.x);
    write(buf, o.embed);
    buf.writeLongLE(o.lng);
    Helper.writeArray(buf, o.arr, (item) -> write(buf, item));
  }

  public static void write(ByteBuf buf, TestClassA o) {
    if (o == null) {
      buf.writeIntLE(0);
      return;
    }
    buf.writeIntLE(123);
    buf.writeIntLE(o.x);
    Helper.writeString(buf, o.str);
    buf.writeDoubleLE(o.w);
    buf.writeShortLE(o.sssshort);
    buf.writeBoolean(o.bbb);
    Helper.writeStringArray(buf, o.strarr);
  }

  public interface HandlerX {
    void handle(TestClassA payload);

    void handle(TestClassB payload);
  }


  public interface HandlerY {
    void handle(TestClassB payload);
  }

  public static abstract class StreamX implements ByteStream {
    @Override
    public void request(int bytes) {
    }

    @Override
    public ByteBuf create(int size) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void next(ByteBuf buf) {
      switch (buf.readIntLE()) {
        case 123:
          handle(readBody_123(buf, new TestClassA()));
          return;
        case 42:
          handle(readBody_42(buf, new TestClassA()));
          return;
        case 4242:
          handle(readBody_4242(buf, new TestClassB()));
      }
    }

    public abstract void handle(TestClassA payload);

    public abstract void handle(TestClassB payload);
  }

  public static abstract class StreamY implements ByteStream {
    @Override
    public void request(int bytes) {
    }

    @Override
    public ByteBuf create(int size) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void next(ByteBuf buf) {
      if (buf.readIntLE() == 4242) {
        handle(readBody_4242(buf, new TestClassB()));
      }
    }

    public abstract void handle(TestClassB payload);
  }
}
