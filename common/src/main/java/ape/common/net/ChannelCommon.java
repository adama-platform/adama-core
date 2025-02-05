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
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.ScheduledFuture;
import ape.ErrorCodes;
import ape.common.gossip.Engine;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** since both the server and client share some common things, we group them here */
public abstract class ChannelCommon extends ChannelInboundHandlerAdapter {
  protected final HashMap<Integer, ByteStream> streams;
  protected final Engine gossipEngine;
  private final int initialId;
  protected ScheduledFuture<?> scheduledFlush;
  private int nextId;

  public ChannelCommon(int initialId, Engine gossipEngine) {
    this.initialId = initialId;
    this.scheduledFlush = null;
    this.streams = new HashMap<>();
    this.nextId = initialId;
    this.gossipEngine = gossipEngine;
  }

  public int makeId() {
    while (streams.containsKey(nextId)) {
      nextId += 2;
    }
    int id = nextId;
    nextId += 2;
    if (nextId >= 4194304) {
      nextId = initialId;
    }
    return id;
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    ctx.executor().execute(() -> {
      if (scheduledFlush != null) {
        scheduledFlush.cancel(true);
        scheduledFlush = null;
      }
      for (Map.Entry<Integer, ByteStream> stream : streams.entrySet()) {
        stream.getValue().error(ErrorCodes.NET_DISCONNECT);
      }
      streams.clear();
    });
  }

  protected void routeCommon(byte type, int id, ByteBuf inBuffer, ChannelHandlerContext ctx) {
    switch (type) {
      case 0x20: {
        ByteStream stream = streams.get(id);
        if (stream != null) {
          stream.next(inBuffer);
        }
      }
      break;
      case 0x30: {
        ByteStream stream = streams.get(id);
        if (stream != null) {
          stream.request(inBuffer.readIntLE());
        }
      }
      break;
      case 0x40: {
        ByteStream stream = streams.remove(id);
        if (stream != null) {
          stream.completed();
        }
        sendConfirmRemoval(ctx, id);
      }
      break;
      case 0x50: {
        ByteStream stream = streams.remove(id);
        if (stream != null) {
          stream.error(inBuffer.readIntLE());
        }
      }
      break;
      case 0x60: {
        ByteStream stream = streams.remove(id);
        if (stream != null) {
          stream.completed();
        }
      }
      break;
    }
  }

  protected void sendConfirmRemoval(ChannelHandlerContext context, int id) {
    context.executor().execute(() -> {
      ByteBuf buffer = Unpooled.buffer();
      buffer.writeByte(0x60);
      buffer.writeIntLE(id);
      context.write(buffer);
      flushFromWithinContextExecutor(context);
    });
  }

  protected void flushFromWithinContextExecutor(ChannelHandlerContext context) {
    if (scheduledFlush != null) {
      return;
    }
    scheduledFlush = context.executor().schedule(() -> {
      context.flush();
      scheduledFlush = null;
    }, 50000, TimeUnit.NANOSECONDS);
  }
}
