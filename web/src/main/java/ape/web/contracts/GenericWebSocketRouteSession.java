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
package ape.web.contracts;

import ape.common.ErrorCodeException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.ChannelHandlerContext;

/**
 * Per-connection WebSocket session interface for JSON-RPC style protocols.
 * Supports configurable keepalive, ping/pong for latency measurement,
 * graceful disconnect notification, and JSON message handling.
 * Abstracts protocol specifics so WebSocketHandler can manage any JSON protocol.
 */
public interface GenericWebSocketRouteSession {

  /** should there be a keep-alive executed on the route */
  public boolean enableKeepAlive();

  /** send a keep alive disconnect notice (if it is enabled and supported, return true -> emit metrics) */
  public boolean sendKeepAliveDisconnect(ChannelHandlerContext ctx);

  /** send a ping to the route (return true -> emit metrics) */
  public boolean sendPing(ChannelHandlerContext ctx);

  /** kill the route */
  public void kill();

  /** execute a keepalive against the route to see if it died internally */
  public boolean keepalive();

  /** handle the JSON input and yield over to the context for glory and fun */
  public void handle(ObjectNode requestNode, ChannelHandlerContext ctx) throws ErrorCodeException;
}
