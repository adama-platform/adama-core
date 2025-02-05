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
package ape.runtime.natives;

import ape.runtime.async.*;
import ape.runtime.exceptions.AbortMessageException;
import ape.runtime.exceptions.ComputeBlockedException;
import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.natives.algo.HashBuilder;
import ape.runtime.reactives.RxInt32;
import ape.runtime.reactives.RxInt64;
import org.junit.Assert;
import org.junit.Test;

public class NtChannelTests {
  public static final NtMessageBase DEMO = new NtMessageBase() {
    @Override
    public void __writeOut(JsonStreamWriter writer) {
      writer.beginObject();
      writer.endObject();
    }

    @Override
    public void __ingest(JsonStreamReader reader) {
      reader.skipValue();
    }

    @Override
    public void __hash(HashBuilder __hash) {
      __hash.hashString("demo");
    }

    @Override
    public int[] __getIndexValues() {
      return new int[0];
    }

    @Override
    public String[] __getIndexColumns() {
      return new String[0];
    }

    @Override
    public long __memory() {
      return 128;
    }

    @Override
    public void __parsed() throws AbortMessageException {
    }
  };

  public OutstandingFutureTracker makeFutures() {
    final var src = new RxInt32(null, 42);
    final var futures = new OutstandingFutureTracker(src, new TimeoutTracker(new RxInt64(null, 0)));
    return futures;
  }

  @Test
  public void flow1() {
    final var futures = makeFutures();
    final var sink = new Sink<String>("channel");
    sink.enqueue(new AsyncTask(0, 1, NtPrincipal.NO_ONE, null, "channel", 0, "origin", "ip","message", new IdHistoryLog()), "X");
    final var channel = new NtChannel<>(futures, sink);
    final var future = channel.fetchItem(NtPrincipal.NO_ONE);
    Assert.assertTrue(future.exists());
  }

  @Test
  public void flow2() {
    final var futures = makeFutures();
    final var sink = new Sink<String>("channel");
    sink.enqueue(new AsyncTask(0, 2, NtPrincipal.NO_ONE, null, "channel", 0, "origin", "ip","message", new IdHistoryLog()), "X");
    final var channel = new NtChannel<>(futures, sink);
    final var future = channel.fetchArray(NtPrincipal.NO_ONE);
    Assert.assertTrue(future.exists());
  }

  @Test
  public void flow_choose_nope() {
    final var futures = makeFutures();
    final var sink = new Sink<String>("channel");
    final var channel = new NtChannel<>(futures, sink);
    Assert.assertFalse(channel.choose(NtPrincipal.NO_ONE, new NtMessageBase[0], 3).await().has());
  }

  @Test
  public void flow_choose_options_available() {
    final var futures = makeFutures();
    final var sink = new Sink<String>("channel");
    sink.enqueue(new AsyncTask(0, 3, NtPrincipal.NO_ONE, null, "channel", 0, "origin", "ip","message", new IdHistoryLog()), "X");
    final var channel = new NtChannel<>(futures, sink);
    final var future = channel.choose(NtPrincipal.NO_ONE, new NtMessageBase[] {DEMO, DEMO}, 2);
    future.await();
  }

  @Test
  public void flow_choose_options_nothing_available() {
    final var futures = makeFutures();
    final var sink = new Sink<String>("channel");
    final var channel = new NtChannel<>(futures, sink);
    final var future = channel.choose(NtPrincipal.NO_ONE, new NtMessageBase[] {DEMO, DEMO}, 2);
    try {
      future.await();
      Assert.fail();
    } catch (final ComputeBlockedException cbe) {
    }
  }

  @Test
  public void flow_decide_nope() {
    final var futures = makeFutures();
    final var sink = new Sink<String>("channel");
    final var channel = new NtChannel<>(futures, sink);
    Assert.assertFalse(channel.decide(NtPrincipal.NO_ONE, new NtMessageBase[0]).await().has());
  }

  @Test
  public void flow_decide_options_available() {
    final var futures = makeFutures();
    final var sink = new Sink<String>("channel");
    sink.enqueue(new AsyncTask(0, 1, NtPrincipal.NO_ONE, 123, "channel", 0, "origin", "ip","message", new IdHistoryLog()), "X");
    final var channel = new NtChannel<>(futures, sink);
    final var future = channel.decide(NtPrincipal.NO_ONE, new NtMessageBase[] {DEMO, DEMO});
    future.await();
  }

  @Test
  public void flow_decide_options_nothing_available() {
    final var futures = makeFutures();
    final var sink = new Sink<String>("channel");
    final var channel = new NtChannel<>(futures, sink);
    final var future = channel.decide(NtPrincipal.NO_ONE, new NtMessageBase[] {DEMO, DEMO});
    try {
      future.await();
      Assert.fail();
    } catch (final ComputeBlockedException cbe) {
    }
  }

  @Test
  public void flow_nope() {
    final var futures = makeFutures();
    final var sink = new Sink<String>("channel");
    final var channel = new NtChannel<>(futures, sink);
    final var future = channel.fetchItem(NtPrincipal.NO_ONE);
    Assert.assertFalse(future.exists());
    try {
      future.await();
      Assert.fail();
    } catch (final ComputeBlockedException bce) {
    }
  }

  @Test
  public void flow1_timeout() {
    final var futures = makeFutures();
    final var sink = new Sink<String>("channel");
    sink.enqueue(new AsyncTask(0, 1, NtPrincipal.NO_ONE, null, "channel", 0, "origin", "ip","message", new IdHistoryLog()), "X");
    final var channel = new NtChannel<>(futures, sink);
    final var future = channel.fetchTimeoutItem(NtPrincipal.NO_ONE, 1000.0);
    Assert.assertTrue(future.exists());
    Assert.assertTrue(future.await().has());
  }

  @Test
  public void flow2_timeout() {
    final var futures = makeFutures();
    final var sink = new Sink<String>("channel");
    sink.enqueue(new AsyncTask(0, 2, NtPrincipal.NO_ONE, null, "channel", 0, "origin", "ip","message", new IdHistoryLog()), "X");
    final var channel = new NtChannel<>(futures, sink);
    final var future = channel.fetchTimeoutArray(NtPrincipal.NO_ONE, 1000);
    Assert.assertTrue(future.exists());
    Assert.assertTrue(future.await().has());
  }

  @Test
  public void flow1_timeout_expired() {
    final var src = new RxInt32(null, 42);
    final var futures = new OutstandingFutureTracker(src, new TimeoutTracker(new RxInt64(null, 0)));

    final var sink = new Sink<String>("channel");
    final var channel = new NtChannel<>(futures, sink);
    final var futureToss = channel.fetchTimeoutItem(NtPrincipal.NO_ONE, 1000.0);
    Assert.assertFalse(futureToss.exists());

    src.__revert();
    sink.clear();
    futures.restore();

    futures.timeouts.time.set(5000 * 1000);
    final var future = channel.fetchTimeoutItem(NtPrincipal.NO_ONE, 1000.0);
    Assert.assertTrue(future.exists());
    Assert.assertFalse(future.await().has());
  }

  @Test
  public void flow2_timeout_expired() {
    final var src = new RxInt32(null, 42);
    final var futures = new OutstandingFutureTracker(src, new TimeoutTracker(new RxInt64(null, 0)));

    final var sink = new Sink<String>("channel");
    final var channel = new NtChannel<>(futures, sink);
    final var futureToss = channel.fetchTimeoutArray(NtPrincipal.NO_ONE, 1000.0);
    Assert.assertFalse(futureToss.exists());

    src.__revert();
    sink.clear();
    futures.restore();

    futures.timeouts.time.set(5000 * 1000);
    final var future = channel.fetchTimeoutArray(NtPrincipal.NO_ONE, 1000.0);
    Assert.assertTrue(future.exists());
    Assert.assertFalse(future.await().has());
  }
}
