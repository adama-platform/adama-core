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
package ape.common.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;

/** a callback to isolate a single sub-channel within a channel */
public class Remote implements ByteStream {
  private final HashMap<Integer, ByteStream> streams;
  private final int id;
  private final ChannelHandlerContext context;
  private final Runnable requestFlush;

  public Remote(HashMap<Integer, ByteStream> streams, int id, ChannelHandlerContext context, Runnable requestFlush) {
    this.streams = streams;
    this.id = id;
    this.context = context;
    this.requestFlush = requestFlush;
  }

  @Override
  public void request(int bytes) {
    context.executor().execute(() -> {
      ByteBuf request = Unpooled.buffer();
      request.writeByte(0x30);
      request.writeIntLE(id);
      request.writeIntLE(bytes);
      context.write(request);
      requestFlush.run();
    });
  }

  @Override
  public ByteBuf create(int bestGuessForSize) {
    ByteBuf request = Unpooled.buffer();
    request.writeByte(0x20);
    request.writeIntLE(id);
    return request;
  }

  @Override
  public void next(ByteBuf buf) {
    context.executor().execute(() -> {
      context.write(buf);
      requestFlush.run();
    });
  }

  @Override
  public void completed() {
    context.executor().execute(() -> {
      ByteBuf request = Unpooled.buffer();
      request.writeByte(0x40);
      request.writeIntLE(id);
      context.write(request);
      requestFlush.run();
    });
  }

  @Override
  public void error(int errorCode) {
    context.executor().execute(() -> {
      streams.remove(id);
      ByteBuf request = Unpooled.buffer();
      request.writeByte(0x50);
      request.writeIntLE(id);
      request.writeIntLE(errorCode);
      context.write(request);
      requestFlush.run();
    });
  }
}
