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
import ape.common.Callback;
import ape.common.gossip.Engine;

import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Netty channel handler for client-side connections.
 * Manages multiplexed byte streams over a single TCP connection,
 * handles connection lifecycle events, supports gossip protocol
 * exchanges, and routes incoming messages to appropriate stream handlers.
 */
public class ChannelClient extends ChannelCommon {
  public final String host;
  public final int port;
  private final Lifecycle lifecycle;
  private final HashMap<Integer, Consumer<Boolean>> initiations;
  private final Engine gossipEngine;
  private ChannelHandlerContext context;
  private Runnable unregister;

  public ChannelClient(String host, int port, Lifecycle lifecycle, Engine gossipEngine) {
    super(1, gossipEngine);
    this.host = host;
    this.port = port;
    this.lifecycle = lifecycle;
    this.initiations = new HashMap<>();
    this.gossipEngine = gossipEngine;
    this.unregister = null;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    this.context = ctx;
    lifecycle.connected(this);
    unregister = gossipEngine.registerClient(this);
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    ByteBuf inBuffer = (ByteBuf) msg;
    byte type = inBuffer.readByte();
    int id = inBuffer.readIntLE();
    if (type == 0x10) {
      Consumer<Boolean> callback = initiations.remove(id);
      if (callback != null) {
        callback.accept(true);
      }
    } else {
      routeCommon(type, id, inBuffer, ctx);
    }
    inBuffer.release();
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    super.channelInactive(ctx);
    for (Consumer<Boolean> initiation : initiations.values()) {
      initiation.accept(false);
    }
    initiations.clear();
    lifecycle.disconnected();
    if (unregister != null) {
      unregister.run();
      unregister = null;
    }
  }

  public void close() {
    context.executor().execute(() -> {
      context.close();
    });
  }

  public void gossip() {
    context.executor().execute(() -> {
      int id = makeId();
      Engine.Exchange exchange = gossipEngine.client();
      streams.put(id, exchange);
      ByteBuf buffer = Unpooled.buffer();
      buffer.writeByte(0x11);
      buffer.writeIntLE(id);
      initiations.put(id, (success) -> {
        if (success) {
          exchange.start(new Remote(streams, id, context, () -> {
            flushFromWithinContextExecutor(context);
          }));
        }
      });
      context.write(buffer);
      flushFromWithinContextExecutor(context);
    });
  }

  public void open(ByteStream downstream, Callback<ByteStream> opened) {
    context.executor().execute(() -> {
      int id = makeId();
      streams.put(id, downstream);
      ByteBuf buffer = Unpooled.buffer();
      buffer.writeByte(0x10);
      buffer.writeIntLE(id);
      initiations.put(id, (success) -> {
        if (success) {
          opened.success(new Remote(streams, id, context, () -> {
            flushFromWithinContextExecutor(context);
          }));
        }
      });
      context.write(buffer);
      flushFromWithinContextExecutor(context);
    });
  }
}
