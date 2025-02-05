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

import ape.runtime.async.OutstandingFutureTracker;
import ape.runtime.async.SimpleFuture;
import ape.runtime.async.Sink;
import ape.runtime.async.Timeout;
import ape.runtime.json.JsonStreamWriter;

/** a channel */
public class NtChannel<T> {
  public final Sink<T> sink;
  public final OutstandingFutureTracker tracker;

  public NtChannel(final OutstandingFutureTracker tracker, final Sink<T> sink) {
    this.tracker = tracker;
    this.sink = sink;
  }

  /** from a list of options, choose $limit of them */
  public SimpleFuture<NtMaybe<T>> choose(final NtPrincipal who, final NtMessageBase[] optionsRaw, final int limit) {
    final var actualLimit = Math.min(limit, optionsRaw.length);
    if (actualLimit == 0) {
      return new SimpleFuture<>(sink.channel, who, new NtMaybe<>());
    }
    final var oldFuture = tracker.make(sink.channel, who);
    final var writer = new JsonStreamWriter();
    writer.beginObject();
    writer.writeObjectFieldIntro("id");
    writer.writeInteger(oldFuture.id);
    writer.writeObjectFieldIntro("channel");
    writer.writeFastString(oldFuture.channel);
    writer.writeObjectFieldIntro("array");
    writer.writeBoolean(true);
    writer.writeObjectFieldIntro("min");
    writer.writeInteger(limit);
    writer.writeObjectFieldIntro("max");
    writer.writeInteger(limit);
    writer.writeObjectFieldIntro("distinct");
    writer.writeBoolean(true);
    writer.writeObjectFieldIntro("options");
    writer.beginArray();
    for (final NtMessageBase option : optionsRaw) {
      option.__writeOut(writer);
    }
    writer.endArray();
    writer.endObject();
    oldFuture.json = writer.toString();
    final var future = sink.dequeueMaybe(who);
    if (future.exists()) {
      oldFuture.take();
    }
    return future;
  }

  /** from a list of options, pick one of them */
  public SimpleFuture<NtMaybe<T>> decide(final NtPrincipal who, final NtMessageBase[] optionsRaw) {
    if (optionsRaw.length == 0) {
      return new SimpleFuture<>(sink.channel, who, new NtMaybe<>());
    }
    final var oldFuture = tracker.make(sink.channel, who);
    final var writer = new JsonStreamWriter();
    writer.beginObject();
    writer.writeObjectFieldIntro("id");
    writer.writeInteger(oldFuture.id);
    writer.writeObjectFieldIntro("channel");
    writer.writeFastString(oldFuture.channel);
    writer.writeObjectFieldIntro("array");
    writer.writeBoolean(false);
    writer.writeObjectFieldIntro("min");
    writer.writeInteger(1);
    writer.writeObjectFieldIntro("max");
    writer.writeInteger(1);
    writer.writeObjectFieldIntro("distinct");
    writer.writeBoolean(true);
    writer.writeObjectFieldIntro("options");
    writer.beginArray();
    for (final NtMessageBase option : optionsRaw) {
      option.__writeOut(writer);
    }
    writer.endArray();
    writer.endObject();
    oldFuture.json = writer.toString();
    final var future = sink.dequeueMaybe(who);
    if (future.exists()) {
      oldFuture.take();
    }
    return future;
  }

  /** ask the user for one item, blocks entire universe */
  public SimpleFuture<T> fetchItem(final NtPrincipal who) {
    return fetch(who, false);
  }

  /** ask the user for item/items */
  public SimpleFuture<T> fetch(final NtPrincipal who, boolean array) {
    final var oldFuture = tracker.make(sink.channel, who);
    final var writer = new JsonStreamWriter();
    writer.beginObject();
    writer.writeObjectFieldIntro("id");
    writer.writeInteger(oldFuture.id);
    writer.writeObjectFieldIntro("channel");
    writer.writeFastString(oldFuture.channel);
    writer.writeObjectFieldIntro("array");
    writer.writeBoolean(array);
    writer.endObject();
    oldFuture.json = writer.toString();
    final var future = sink.dequeue(who);
    if (future.exists()) {
      oldFuture.take();
    }
    return future;
  }

  /** ask the user for one array of items, blocks entire universe */
  public SimpleFuture<T> fetchArray(final NtPrincipal who) {
    return fetch(who, true);
  }

  public SimpleFuture<NtMaybe<T>> fetchTimeout(final NtPrincipal who, boolean array, double timeout) {
    final var oldFuture = tracker.make(sink.channel, who);
    Timeout to = tracker.timeouts.create(oldFuture.id, timeout);
    final var writer = new JsonStreamWriter();
    writer.beginObject();
    writer.writeObjectFieldIntro("id");
    writer.writeInteger(oldFuture.id);
    writer.writeObjectFieldIntro("channel");
    writer.writeFastString(oldFuture.channel);
    if (to != null) {
      writer.writeObjectFieldIntro("timeout");
      writer.beginObject();
      writer.writeObjectFieldIntro("started");
      writer.writeLong(to.timestamp);
      writer.writeObjectFieldIntro("seconds");
      writer.writeDouble(to.timeoutSeconds);
      writer.endObject();
    }
    writer.writeObjectFieldIntro("array");
    writer.writeBoolean(array);
    writer.endObject();
    oldFuture.json = writer.toString();

    // when does the timeout occur
    long limit = to.timestamp + (long) (to.timeoutSeconds * 1000);

    // we establish an exclusive timeline for the channel such that only the first message in the window belongs to this request
    final var future = sink.dequeueIf(who, limit);

    // it exists, so let's return it!
    if (future.exists()) {
      oldFuture.take();
      return new SimpleFuture<>(future.channel, future.who, new NtMaybe<>(future.await()));
    }

    // if the request is expired
    boolean expired = limit <= tracker.timeouts.time.get();
    if (expired) {
      // return an empty maybe to indicate a timeout
      return new SimpleFuture<>(future.channel, future.who, new NtMaybe<>());
    } else {
      // otherwise, let null indicates a future compute blocked
      return new SimpleFuture<>(future.channel, future.who, null);
    }
  }

  public SimpleFuture<NtMaybe<T>> fetchTimeoutItem(final NtPrincipal who, double timeout) {
    return fetchTimeout(who, false, timeout);
  }

  public SimpleFuture<NtMaybe<T>> fetchTimeoutArray(final NtPrincipal who, double timeout) {
    return fetchTimeout(who, true, timeout);
  }
}
