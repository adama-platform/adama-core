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
package ape.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ape.common.*;
import ape.web.io.*;
import ape.ErrorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GeneratedSoloRouterBase {
  private static final Logger ACCESS_LOG = LoggerFactory.getLogger("access");
  private static final JsonLogger SOLO_ACCESS_LOG = (item) -> ACCESS_LOG.debug(item.toString());

  public abstract void handle_Probe(long requestId, String identity, SimpleResponder responder);

  public abstract void handle_DocumentCreate(long requestId, String identity, String space, String key, String entropy, ObjectNode arg, SimpleResponder responder);

  public abstract void handle_MessageDirectSend(long requestId, String identity, String space, String key, String channel, JsonNode message, SeqResponder responder);

  public abstract void handle_MessageDirectSendOnce(long requestId, String identity, String space, String key, String dedupe, String channel, JsonNode message, SeqResponder responder);

  public abstract void handle_ConnectionCreate(long requestId, String identity, String space, String key, ObjectNode viewerState, DataResponder responder);

  public abstract void handle_ConnectionSend(long requestId, Long connection, String channel, JsonNode message, SeqResponder responder);

  public abstract void handle_ConnectionSendOnce(long requestId, Long connection, String channel, String dedupe, JsonNode message, SeqResponder responder);

  public abstract void handle_ConnectionCanAttach(long requestId, Long connection, YesResponder responder);

  public abstract void handle_ConnectionAttach(long requestId, Long connection, String assetId, String filename, String contentType, Long size, String digestMd5, String digestSha384, SeqResponder responder);

  public abstract void handle_ConnectionUpdate(long requestId, Long connection, ObjectNode viewerState, SimpleResponder responder);

  public abstract void handle_ConnectionEnd(long requestId, Long connection, SimpleResponder responder);

  public void route(JsonRequest request, JsonResponder responder) {
    try {
      long requestId = request.id();
      String method = request.method();
      ObjectNode _accessLogItem = Json.newJsonObject();
      _accessLogItem.put("method", method);
      _accessLogItem.put("requestId", requestId);
      _accessLogItem.put("@timestamp", LogTimestamp.now());
      request.dumpIntoLog(_accessLogItem);
      switch (method) {
        case "probe":
          handle_Probe(requestId, //
            request.getString("identity", true, 458759), //
            new SimpleResponder(new LoggedProxyResponder(responder, _accessLogItem, SOLO_ACCESS_LOG)));
          return;
        case "document/create":
          _accessLogItem.put("space", request.getStringNormalize("space", true, 461828));
          _accessLogItem.put("key", request.getString("key", true, 466947));
          _accessLogItem.put("entropy", request.getString("entropy", false, 0));
          handle_DocumentCreate(requestId, //
            request.getString("identity", true, 458759), //
            request.getStringNormalize("space", true, 461828), //
            request.getString("key", true, 466947), //
            request.getString("entropy", false, 0), //
            request.getObject("arg", true, 461826), //
            new SimpleResponder(new LoggedProxyResponder(responder, _accessLogItem, SOLO_ACCESS_LOG)));
          return;
        case "message/direct-send":
          _accessLogItem.put("space", request.getStringNormalize("space", true, 461828));
          _accessLogItem.put("key", request.getString("key", true, 466947));
          _accessLogItem.put("channel", request.getString("channel", true, 454659));
          handle_MessageDirectSend(requestId, //
            request.getString("identity", true, 458759), //
            request.getStringNormalize("space", true, 461828), //
            request.getString("key", true, 466947), //
            request.getString("channel", true, 454659), //
            request.getJsonNode("message", true, 425987), //
            new SeqResponder(new LoggedProxyResponder(responder, _accessLogItem, SOLO_ACCESS_LOG)));
          return;
        case "message/direct-send-once":
          _accessLogItem.put("space", request.getStringNormalize("space", true, 461828));
          _accessLogItem.put("key", request.getString("key", true, 466947));
          _accessLogItem.put("dedupe", request.getString("dedupe", false, 0));
          _accessLogItem.put("channel", request.getString("channel", true, 454659));
          handle_MessageDirectSendOnce(requestId, //
            request.getString("identity", true, 458759), //
            request.getStringNormalize("space", true, 461828), //
            request.getString("key", true, 466947), //
            request.getString("dedupe", false, 0), //
            request.getString("channel", true, 454659), //
            request.getJsonNode("message", true, 425987), //
            new SeqResponder(new LoggedProxyResponder(responder, _accessLogItem, SOLO_ACCESS_LOG)));
          return;
        case "connection/create":
          _accessLogItem.put("space", request.getStringNormalize("space", true, 461828));
          _accessLogItem.put("key", request.getString("key", true, 466947));
          handle_ConnectionCreate(requestId, //
            request.getString("identity", true, 458759), //
            request.getStringNormalize("space", true, 461828), //
            request.getString("key", true, 466947), //
            request.getObject("viewer-state", false, 0), //
            new DataResponder(new LoggedProxyResponder(responder, _accessLogItem, SOLO_ACCESS_LOG)));
          return;
        case "connection/send":
          _accessLogItem.put("channel", request.getString("channel", true, 454659));
          handle_ConnectionSend(requestId, //
            request.getLong("connection", true, 405505), //
            request.getString("channel", true, 454659), //
            request.getJsonNode("message", true, 425987), //
            new SeqResponder(new LoggedProxyResponder(responder, _accessLogItem, SOLO_ACCESS_LOG)));
          return;
        case "connection/send-once":
          _accessLogItem.put("channel", request.getString("channel", true, 454659));
          _accessLogItem.put("dedupe", request.getString("dedupe", false, 0));
          handle_ConnectionSendOnce(requestId, //
            request.getLong("connection", true, 405505), //
            request.getString("channel", true, 454659), //
            request.getString("dedupe", false, 0), //
            request.getJsonNode("message", true, 425987), //
            new SeqResponder(new LoggedProxyResponder(responder, _accessLogItem, SOLO_ACCESS_LOG)));
          return;
        case "connection/can-attach":
          handle_ConnectionCanAttach(requestId, //
            request.getLong("connection", true, 405505), //
            new YesResponder(new LoggedProxyResponder(responder, _accessLogItem, SOLO_ACCESS_LOG)));
          return;
        case "connection/attach":
          _accessLogItem.put("asset-id", request.getString("asset-id", true, 476156));
          _accessLogItem.put("filename", request.getString("filename", true, 470028));
          _accessLogItem.put("content-type", request.getString("content-type", true, 455691));
          _accessLogItem.put("size", request.getLong("size", true, 477179));
          _accessLogItem.put("digest-md5", request.getString("digest-md5", true, 445437));
          _accessLogItem.put("digest-sha384", request.getString("digest-sha384", true, 406525));
          handle_ConnectionAttach(requestId, //
            request.getLong("connection", true, 405505), //
            request.getString("asset-id", true, 476156), //
            request.getString("filename", true, 470028), //
            request.getString("content-type", true, 455691), //
            request.getLong("size", true, 477179), //
            request.getString("digest-md5", true, 445437), //
            request.getString("digest-sha384", true, 406525), //
            new SeqResponder(new LoggedProxyResponder(responder, _accessLogItem, SOLO_ACCESS_LOG)));
          return;
        case "connection/update":
          handle_ConnectionUpdate(requestId, //
            request.getLong("connection", true, 405505), //
            request.getObject("viewer-state", false, 0), //
            new SimpleResponder(new LoggedProxyResponder(responder, _accessLogItem, SOLO_ACCESS_LOG)));
          return;
        case "connection/end":
          handle_ConnectionEnd(requestId, //
            request.getLong("connection", true, 405505), //
            new SimpleResponder(new LoggedProxyResponder(responder, _accessLogItem, SOLO_ACCESS_LOG)));
          return;
      }
      responder.error(new ErrorCodeException(ErrorCodes.API_METHOD_NOT_FOUND));
    } catch (ErrorCodeException ex) {
      responder.error(ex);
    }
  }
}
