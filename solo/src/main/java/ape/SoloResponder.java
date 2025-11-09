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
package ape;

import ape.api.*;
import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.Json;
import ape.runtime.contracts.Streamback;
import ape.runtime.data.Key;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.sys.ConnectionMode;
import ape.runtime.sys.CoreRequestContext;
import ape.runtime.sys.CoreService;
import ape.runtime.sys.CoreStream;
import ape.web.io.ConnectionContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/** adapating the WebSocket json request to json responder */
public class SoloResponder extends GeneratedSoloRouterBase {
  private final ConnectionContext context;
  private final CoreService service;
  private final ConcurrentHashMap<Long, LocalStream> streams;

  public SoloResponder(ConnectionContext context, CoreService service) {
    this.context = context;
    this.service = service;
    this.streams = new ConcurrentHashMap<>();
  }

  public static NtPrincipal principalOf(String identity) {
    if (identity.startsWith("document/")) {
      String[] parts = identity.split(Pattern.quote("/"));
      return new NtPrincipal(parts[3], "doc/" + parts[1] + "/" + parts[2]);
    }
    if (identity.startsWith("anonymous:")) {
      return new NtPrincipal(identity.substring(10), "anonymous");
    }
    return NtPrincipal.NO_ONE;
  }

  private CoreRequestContext contextOf(String identity, String key) {
    return new CoreRequestContext(principalOf(identity), context.origin, context.remoteIp, key);
  }

  @Override
  public void handle_Probe(long requestId, String identity, SimpleResponder responder) {
    responder.complete();
  }

  @Override
  public void handle_DocumentCreate(long requestId, String identity, String space, String key, String entropy, ObjectNode arg, SimpleResponder responder) {
    responder.error(new ErrorCodeException(-2));
  }

  @Override
  public void handle_MessageDirectSend(long requestId, String identity, String space, String key, String channel, JsonNode message, SeqResponder responder) {
    responder.error(new ErrorCodeException(-2));
  }

  @Override
  public void handle_MessageDirectSendOnce(long requestId, String identity, String space, String key, String dedupe, String channel, JsonNode message, SeqResponder responder) {
    responder.error(new ErrorCodeException(-2));
  }

  @Override
  public void handle_ConnectionCreate(long requestId, String identity, String space, String key, ObjectNode viewerState, DataResponder responder) {
    CoreRequestContext ctx = contextOf(identity, key);
    final LocalStream local = new LocalStream(new Key(space, key));
    streams.put(requestId, local);
    Streamback back = new Streamback() {
      @Override
      public void onSetupComplete(CoreStream stream) {
        local.setup(stream);
      }

      @Override
      public void traffic(String trafficHint) {
      }

      @Override
      public void status(StreamStatus status) {
      }

      @Override
      public void next(String data) {
        ObjectNode delta = Json.parseJsonObject(data);
        responder.next(delta);
        JsonNode force = delta.get("force-disconnect");
        if (force != null && force.isBoolean() && force.booleanValue()) {
          responder.error(new ErrorCodeException(ErrorCodes.AUTH_DISCONNECTED));
          local.close();
        }
      }

      @Override
      public void failure(ErrorCodeException exception) {
      }
    };
    service.connect(ctx, local.key, viewerState != null ? viewerState.toString() : "{}", ConnectionMode.Full, back);
  }

  @Override
  public void handle_ConnectionSend(long requestId, Long connection, String channel, JsonNode message, SeqResponder responder) {
    LocalStream local = streams.get(connection);
    if (local != null) {
      local.send(channel, null, message, responder);
    } else {
      responder.error(new ErrorCodeException(-1));
    }
  }

  @Override
  public void handle_ConnectionSendOnce(long requestId, Long connection, String channel, String dedupe, JsonNode message, SeqResponder responder) {
    LocalStream local = streams.get(connection);
    if (local != null) {
      local.send(channel, null, message, responder);
    } else {
      responder.error(new ErrorCodeException(-1));
    }
  }

  @Override
  public void handle_ConnectionCanAttach(long requestId, Long connection, YesResponder responder) {
    responder.complete(false);
  }

  @Override
  public void handle_ConnectionAttach(long requestId, Long connection, String assetId, String filename, String contentType, Long size, String digestMd5, String digestSha384, SeqResponder responder) {
    responder.error(new ErrorCodeException(-1));
  }

  @Override
  public void handle_ConnectionUpdate(long requestId, Long connection, ObjectNode viewerState, SimpleResponder responder) {
    LocalStream local = streams.get(connection);
    if (local != null) {
      local.update(viewerState, responder);
    } else {
      responder.error(new ErrorCodeException(-1));
    }
  }

  @Override
  public void handle_ConnectionEnd(long requestId, Long connection, SimpleResponder responder) {
    LocalStream local = streams.remove(connection);
    if (local != null) {
      local.close();
    }
  }

  public void kill() {
    for (LocalStream stream : streams.values()) {
      stream.close();
    }
    streams.clear();
  }

  private class LocalStream {
    public final Key key;
    private final ArrayList<Consumer<Boolean>> queue;
    private CoreStream ref;
    private boolean closed;

    public LocalStream(Key key) {
      this.key = key;
      this.ref = null;
      this.closed = false;
      this.queue = new ArrayList<>();
    }

    public synchronized void setup(CoreStream stream) {
      if (closed) {
        stream.close();
      } else {
        ref = stream;
        for (Consumer<Boolean> pending : queue) {
          pending.accept(true);
        }
        queue.clear();
      }
    }

    public synchronized void update(ObjectNode entry, SimpleResponder responder) {
      if (ref != null) {
        ref.update(entry.toString(), wrap(responder));
      } else {
        queue.add((success) -> {
          if (success) {
            ref.update(entry.toString(), wrap(responder));
          } else {
            responder.error(new ErrorCodeException(-1));
          }
        });
      }
    }

    public synchronized void send(String channel, String marker, JsonNode message, SeqResponder responder) {
      if (ref != null) {
        ref.send(channel, marker, message.toString(), wrap(responder));
      } else {
        if (closed) {
          responder.error(new ErrorCodeException(-1));
        } else {
          queue.add((success) -> {
            if (success) {
              ref.send(channel, marker, message.toString(), wrap(responder));
            } else {
              responder.error(new ErrorCodeException(-1));
            }
          });
        }
      }
    }

    public synchronized void close() {
      closed = true;
      if (ref != null) {
        for (Consumer<Boolean> item : queue) {
          item.accept(false);
        }
        queue.clear();
        ref.close();
        ref = null;
      }
    }

    private Callback<Integer> wrap(SeqResponder responder) {
      return new Callback<Integer>() {
        @Override
        public void success(Integer v) {
          responder.complete(v);
        }

        @Override
        public void failure(ErrorCodeException ex) {
          responder.error(ex);
        }
      };
    }

    private Callback<Void> wrap(SimpleResponder responder) {
      return new Callback<Void>() {
        @Override
        public void success(Void value) {
          responder.complete();
        }

        @Override
        public void failure(ErrorCodeException ex) {
          responder.error(ex);
        }
      };
    }
  }
}
