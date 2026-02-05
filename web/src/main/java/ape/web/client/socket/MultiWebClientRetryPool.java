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
package ape.web.client.socket;

import ape.common.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ape.ErrorCodes;
import ape.common.queue.ItemAction;
import ape.common.queue.ItemQueue;
import ape.web.client.WebClientBase;
import ape.web.contracts.WebJsonStream;
import ape.web.contracts.WebLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Resilient WebSocket connection pool with automatic reconnection and load balancing.
 * Maintains multiple connections to a single endpoint with configurable connection count.
 * Handles disconnections with exponential backoff retry, queues requests during reconnection,
 * and supports both request-response and streaming communication patterns.
 */
public class MultiWebClientRetryPool {
  private static final Logger LOG = LoggerFactory.getLogger(MultiWebClientRetryPool.class);
  private final Random rng;
  private final WebClientBase base;
  private final MultiWebClientRetryPoolMetrics metrics;
  private final MultiWebClientRetryPoolConfig config;
  private final AtomicBoolean alive;
  private final String endpoint;
  private final SimpleExecutor executor;
  private final WebSocketPoolEndpoint[] connections;
  private final ConnectionReady ready;

  public MultiWebClientRetryPool(SimpleExecutor executor, WebClientBase base, MultiWebClientRetryPoolMetrics metrics, MultiWebClientRetryPoolConfig config, ConnectionReady ready, String endpoint) {
    this.executor = executor;
    this.base = base;
    this.metrics = metrics;
    this.config = config;
    this.ready = ready;
    this.endpoint = endpoint;
    this.rng = new Random();
    this.alive = new AtomicBoolean(true);
    this.connections = new WebSocketPoolEndpoint[config.connectionCount];
    for (int k = 0; k < connections.length; k++) {
      connections[k] = new WebSocketPoolEndpoint(config.maxInflight, config.findTimeout);
    }
  }

  private class WebSocketPoolEndpoint {
    private final ItemQueue<WebClientConnection> queue;

    private WebSocketPoolEndpoint(int bound, int timeout) {
      this.queue = new ItemQueue<>(executor, bound, timeout);
      base.open(endpoint, new WebLifecycle() {
        private int backoff = 0;
        @Override
        public void connected(WebClientConnection connection, String version) {
          metrics.inflight.up();
          backoff = 0;
          executor.execute(new NamedRunnable("connected") {
            @Override
            public void execute() throws Exception {
              ready.test(connection, new Callback<>() {
                @Override
                public void success(Void value) {
                  queue.ready(connection);
                }

                @Override
                public void failure(ErrorCodeException ex) {
                  failure(ex);
                  connection.close();
                }
              });
            }
          });
        }

        @Override
        public void ping(int latency) {
          if (latency > 500) {
            metrics.slow.run();
          }
        }

        @Override
        public void failure(Throwable t) {
          if (!StartUp.hasRecentlyStartedUp()) {
            LOG.error("mwcr-failure: {}", t.getMessage());
          }
          metrics.failure.run();
        }

        @Override
        public void disconnected() {
          metrics.inflight.down();
          metrics.disconnected.run();
          WebLifecycle self = this;
          if (alive.get()) {
            backoff = Math.min(config.maxBackoff, backoff > 0 ? (rng.nextInt(backoff) + backoff + 1) : 1);
            executor.execute(new NamedRunnable("disconnected") {
              @Override
              public void execute() throws Exception {
                queue.unready();
                executor.schedule(new NamedRunnable("retry") {
                  @Override
                  public void execute() throws Exception {
                    if (alive.get()) {
                      base.open(endpoint, self);
                    }
                  }
                }, backoff);
              }
            });
          }
        }
      });
    }
  }

  public void get(Callback<WebClientConnection> callback) {
    int id = rng.nextInt(connections.length); // TODO: consider power of two load balancing
    connections[id].queue.add(new ItemAction<>(ErrorCodes.WEBBASE_CONNECT_TIMEOUT, ErrorCodes.WEBBASE_CONNECT_REJECTED, metrics.queue.start()) {
      @Override
      protected void executeNow(WebClientConnection item) {
        callback.success(item);
      }

      @Override
      protected void failure(int code) {
        callback.failure(new ErrorCodeException(code));
      }
    });
  }

  public <T> void requestResponse(ObjectNode request, Function<ObjectNode, T> transform, Callback<T> callback) {
    get(new Callback<WebClientConnection>() {
      @Override
      public void success(WebClientConnection connection) {
        connection.requestResponse(request, transform, callback);
      }

      @Override
      public void failure(ErrorCodeException ex) {
        callback.failure(ex);
      }
    });
  }

  public <C, T> void requestStream(ObjectNode request, BiFunction<WebClientConnection, Integer, C> shared, Function<ObjectNode, T> transform, Callback<C> created, Stream<T> streamback) {
    get(new Callback<WebClientConnection>() {
      @Override
      public void success(WebClientConnection connection) {
        int id = connection.execute(request, new WebJsonStream() {
          @Override
          public void data(int connection, ObjectNode node) {
            streamback.next(transform.apply(node));
          }

          @Override
          public void complete() {
            streamback.complete();
          }

          @Override
          public void failure(int code) {
            streamback.failure(new ErrorCodeException(code));
          }
        });
        created.success(shared.apply(connection, id));
      }

      @Override
      public void failure(ErrorCodeException ex) {
        created.failure(ex);
        streamback.failure(ex);
      }
    });
  }

  public <C, T> void requestStream(ObjectNode request,  Function<ObjectNode, T> transform, Stream<T> streamback) {
    get(new Callback<WebClientConnection>() {
      @Override
      public void success(WebClientConnection connection) {
        int id = connection.execute(request, new WebJsonStream() {
          @Override
          public void data(int connection, ObjectNode node) {
            streamback.next(transform.apply(node));
          }

          @Override
          public void complete() {
            streamback.complete();
          }

          @Override
          public void failure(int code) {
            streamback.failure(new ErrorCodeException(code));
          }
        });
      }

      @Override
      public void failure(ErrorCodeException ex) {
        streamback.failure(ex);
      }
    });
  }

  public void shutdown() {
    alive.set(false);
    executor.execute(new NamedRunnable("shutdown") {
      @Override
      public void execute() throws Exception {
        for (int k = 0; k < connections.length; k++) {
          WebClientConnection connection = connections[k].queue.nuke();
          if (connection != null) {
            connection.close();
          }
        }
      }
    });
  }
}
