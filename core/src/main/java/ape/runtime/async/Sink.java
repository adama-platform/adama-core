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
package ape.runtime.async;

import ape.runtime.natives.NtMaybe;
import ape.runtime.natives.NtPrincipal;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Per-user message queue for channel-based async communication.
 * Each channel has a Sink that maintains separate queues per user (principal).
 * Messages are enqueued via AsyncTask and dequeued as SimpleFuture results.
 * Supports conditional dequeue by timestamp and maybe-wrapped extraction.
 * Used by generated channel handlers to implement Adama's async messaging.
 */
public class Sink<T> {
  /** the communication channel for the sink */
  public final String channel;
  /** the various queues per users */
  private final HashMap<NtPrincipal, ClientChannelQueue> queues;

  /** construct the sink for the particular channel */
  public Sink(final String channel) {
    this.channel = channel;
    this.queues = new HashMap<>();
  }

  /** remove all items from the queue */
  public void clear() {
    queues.clear();
  }

  /** dequeue a message for a particular user; the future may not have a value */
  public SimpleFuture<T> dequeue(final NtPrincipal who) {
    final var queue = queueFor(who);
    T value = null;
    if (!queue.queue.isEmpty()) {
      ClientChannelQueuePair pair = queue.queue.remove(0);
      pair.task.markUsed();
      value = pair.item;
    }
    return new SimpleFuture<>(channel, who, value);
  }

  /** get the queue for the particular user */
  private ClientChannelQueue queueFor(final NtPrincipal value) {
    var queue = queues.get(value);
    if (queue == null) {
      /** create on-demand */
      queue = new ClientChannelQueue();
      queues.put(value, queue);
    }
    return queue;
  }

  /** dequeue a message for a particular user against a timestamp limit; the future may not have a value */
  public SimpleFuture<T> dequeueIf(final NtPrincipal who, long timestampLimit) {
    final var queue = queueFor(who);
    T value = null;
    if (!queue.queue.isEmpty()) {
      ClientChannelQueuePair pair = queue.queue.get(0);
      if (pair.task.timestamp <= timestampLimit) {
        queue.queue.remove(0);
        pair.task.markUsed();
        value = pair.item;
      }
    }
    return new SimpleFuture<>(channel, who, value);
  }

  /** dequeue a message for a particular user; the future may not have a value */
  public SimpleFuture<NtMaybe<T>> dequeueMaybe(final NtPrincipal who) {
    final var queue = queueFor(who);
    NtMaybe<T> value = null;
    if (!queue.queue.isEmpty()) {
      ClientChannelQueuePair pair = queue.queue.remove(0);
      value = new NtMaybe<>(pair.item);
      pair.task.markUsed();
    }
    return new SimpleFuture<>(channel, who, value);
  }

  /** enqueue the given task and message; the task has the user in it */
  public void enqueue(final AsyncTask task, final T message) {
    queueFor(task.who).queue.add(new ClientChannelQueuePair(message, task));
  }

  private class ClientChannelQueuePair {
    public final T item;
    public final AsyncTask task;

    public ClientChannelQueuePair(T item, AsyncTask task) {
      this.item = item;
      this.task = task;
    }
  }

  /** a queue for a particular user */
  private class ClientChannelQueue {
    private final ArrayList<ClientChannelQueuePair> queue;

    private ClientChannelQueue() {
      this.queue = new ArrayList<>();
    }
  }
}
