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
import ape.common.ErrorCodeException;
import ape.common.gossip.Engine;
import ape.common.gossip.EngineTests;
import ape.common.metrics.NoOpMetricsFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class NetGossipTests {
  @Test
  public void flow() throws Exception {
    NetBase A = new NetBase(new NetMetrics(new NoOpMetricsFactory()), NetSuiteTests.identity(), 2, 4);
    NetBase B = new NetBase(new NetMetrics(new NoOpMetricsFactory()), NetSuiteTests.identity(), 2, 4);
    Assert.assertTrue(A.alive());
    Assert.assertTrue(B.alive());
    int portA = (int) (45000 + Math.random() * 2000);

    Engine a = A.startGossiping();
    Engine b = B.startGossiping();

    EngineTests.TargetCollectorAsserter target = new EngineTests.TargetCollectorAsserter();
    Runnable second = target.latchAt(2);
    b.subscribe("adama", target);

    AtomicReference<Runnable> aHB = new AtomicReference<>();
    {
      CountDownLatch aHBlatch = new CountDownLatch(1);
      a.createLocalApplicationHeartbeat("adama", 5000, 5001, new Consumer<Runnable>() {
        @Override
        public void accept(Runnable runnable) {
          aHB.set(runnable);
          aHBlatch.countDown();
        }
      });
      Assert.assertTrue(aHBlatch.await(10000, TimeUnit.MILLISECONDS));
    }
    aHB.get().run();

    A.serve(portA, (upstream) -> {
      return new ByteStream() {
        @Override
        public void request(int bytes) {
        }

        @Override
        public ByteBuf create(int bestGuessForSize) {
          throw new UnsupportedOperationException();
        }

        @Override
        public void next(ByteBuf buf) {
          System.err.println("GOT BYTES");
        }

        @Override
        public void completed() {
          System.err.println("COMPLETED");
        }

        @Override
        public void error(int errorCode) {
          System.err.println("ERRORCODE");
        }
      };
    });
    AtomicBoolean needConnect = new AtomicBoolean(true);
    int attempts = 5;
    while (needConnect.get() && attempts-- > 0) {
      CountDownLatch connectStatus = new CountDownLatch(1);
      B.connect("127.0.0.1:" + portA, new Lifecycle() {
        @Override
        public void connected(ChannelClient channel) {
          needConnect.set(false);
          connectStatus.countDown();
          System.err.println("CONNECTED CLIENT");
        }

        @Override
        public void failed(ErrorCodeException ex) {
          connectStatus.countDown();
          System.err.println("FAILED:" + ex.code);
        }

        @Override
        public void disconnected() {
          System.err.println("DISCONNECTED");
        }
      });
      Assert.assertTrue(connectStatus.await(5000, TimeUnit.MILLISECONDS));
    }

    second.run();
    Assert.assertEquals("127.0.0.1:5000", target.logAt(1));
  }
}
