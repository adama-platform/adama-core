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
import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.MachineIdentity;
import ape.common.metrics.NoOpMetricsFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class NetSuiteTests {

  @Test
  public void happy() throws Exception {
    NetBase base = new NetBase(new NetMetrics(new NoOpMetricsFactory()), identity(), 2, 4);
    try {
      ServerHandle handle = base.serve(25001, new Handler() {
        @Override
        public ByteStream create(ByteStream upstream) {
          return new ByteStream() {
            @Override
            public void request(int bytes) {
              System.err.println("server|Request=" + bytes);
              upstream.request(bytes * 2);
            }

            @Override
            public ByteBuf create(int bestGuessForSize) { // Not used on server side
              return null;
            }

            @Override
            public void next(ByteBuf buf) {
              int val = buf.readIntLE();
              System.err.println("Server|Data=" + val);
              ByteBuf toSend = upstream.create(4);
              toSend.writeIntLE(val);
              upstream.next(toSend);
            }

            @Override
            public void completed() {
              System.err.println("server|Done");
            }

            @Override
            public void error(int errorCode) {
              System.err.println("server|Error:" + errorCode);
            }
          };
        }
      });
      try {
        AtomicReference<ChannelClient> theClient = new AtomicReference<>();
        CountDownLatch phases = new CountDownLatch(102);
        Thread thread = new Thread(() -> handle.waitForEnd());
        thread.start();
        System.err.println("Server running");
        base.connect("127.0.0.1:25001", new Lifecycle() {
          int attemptsLeft = 3;

          @Override
          public void connected(ChannelClient channel) {
            theClient.set(channel);
            System.err.println("client connected");
            channel.open(new ByteStream() {
              @Override
              public void request(int bytes) {
                System.err.println("client|Request=" + bytes);
                phases.countDown();
              }

              @Override
              public ByteBuf create(int bestGuessForSize) {
                return null;
              }

              @Override
              public void next(ByteBuf buf) {
                int value = buf.readIntLE();
                System.err.println("client|Data=" + value);
                phases.countDown();
              }

              @Override
              public void completed() {
                System.err.println("Completed");
              }

              @Override
              public void error(int errorCode) {
                System.err.println("Client error:" + +errorCode);
              }
            }, new Callback<ByteStream>() {
              @Override
              public void success(ByteStream value) {
                value.request(1000);
                for (int k = 0; k < 100; k++) {
                  ByteBuf toSend = value.create(4);
                  toSend.writeIntLE(42 + k);
                  value.next(toSend);
                }
                value.completed();
                phases.countDown(); // GOT CONNECTED
              }

              @Override
              public void failure(ErrorCodeException ex) {
                ex.printStackTrace();
              }
            });
          }

          @Override
          public void failed(ErrorCodeException ex) {
            if (attemptsLeft-- > 0) {
              base.connect("127.0.0.1:25001", this);
            }
          }

          @Override
          public void disconnected() {

          }
        });
        Assert.assertTrue(phases.await(2000, TimeUnit.MILLISECONDS));
        theClient.get().close();
      } finally {
        handle.kill();
      }
    } finally {
      base.shutdown();
      CountDownLatch latchFailed = new CountDownLatch(1);
      base.connect("127.0.0.1:25001", new Lifecycle() {
        @Override
        public void connected(ChannelClient channel) {

        }

        @Override
        public void failed(ErrorCodeException ex) {
          latchFailed.countDown();
        }

        @Override
        public void disconnected() {

        }
      });
      Assert.assertTrue(latchFailed.await(1000, TimeUnit.MILLISECONDS));
    }
    base.waitForShutdown();
  }

  public static MachineIdentity identity() throws Exception {
    for (String search : new String[]{"./", "../", "./grpc/"}) {
      String candidate = search + "localhost.identity";
      File file = new File(candidate);
      if (file.exists()) {
        return MachineIdentity.fromFile(candidate);
      }
    }
    throw new NullPointerException("could not find identity.localhost");
  }

  @Test
  public void sad_remote_error() throws Exception {
    NetBase base = new NetBase(new NetMetrics(new NoOpMetricsFactory()), identity(), 2, 4);
    try {
      ServerHandle handle = base.serve(25002, upstream -> new ByteStream() {
        @Override
        public void request(int bytes) {
          System.err.println("server|Request=" + bytes);
          upstream.request(bytes * 2);
        }

        @Override
        public ByteBuf create(int bestGuessForSize) { // Not used on server side
          return null;
        }

        @Override
        public void next(ByteBuf buf) {
          int val = buf.readIntLE();
          System.err.println("Server|Data=" + val);
          if (val == 50) {
            upstream.error(7209550);
          } else {
            ByteBuf toSend = upstream.create(4);
            toSend.writeIntLE(val);
            upstream.next(toSend);
          }
        }

        @Override
        public void completed() {
          System.err.println("server|Done");
        }

        @Override
        public void error(int errorCode) {
          System.err.println("server|Error:" + errorCode);
        }
      });
      try {
        CountDownLatch phases = new CountDownLatch(10);
        Thread thread = new Thread(() -> handle.waitForEnd());
        thread.start();
        System.err.println("Server running");
        base.connect("127.0.0.1:25002", new Lifecycle() {
          int attemptsLeft = 3;

          @Override
          public void connected(ChannelClient channel) {
            System.err.println("client connected");
            channel.open(new ByteStream() {
              @Override
              public void request(int bytes) {
                System.err.println("client|Request=" + bytes);
                phases.countDown();
              }

              @Override
              public ByteBuf create(int bestGuessForSize) {
                return null;
              }

              @Override
              public void next(ByteBuf buf) {
                int value = buf.readIntLE();
                System.err.println("client|Data=" + value);
                phases.countDown();
              }

              @Override
              public void completed() {
                System.err.println("Completed");
              }

              @Override
              public void error(int errorCode) {
                System.err.println("Client error:" + +errorCode);
                phases.countDown();
              }
            }, new Callback<ByteStream>() {
              @Override
              public void success(ByteStream value) {
                value.request(1000);
                for (int k = 0; k < 100; k++) {
                  ByteBuf toSend = value.create(4);
                  toSend.writeIntLE(42 + k);
                  value.next(toSend);
                }
                value.completed();
                phases.countDown(); // GOT CONNECTED
              }

              @Override
              public void failure(ErrorCodeException ex) {

              }
            });
          }

          @Override
          public void failed(ErrorCodeException ex) {
            if (attemptsLeft-- > 0) {
              base.connect("127.0.0.1:25001", this);
            }
          }

          @Override
          public void disconnected() {

          }
        });
        Assert.assertTrue(phases.await(2000, TimeUnit.MILLISECONDS));
      } finally {
        handle.kill();
      }
    } finally {
      base.shutdown();
    }
    base.waitForShutdown();
  }
}
