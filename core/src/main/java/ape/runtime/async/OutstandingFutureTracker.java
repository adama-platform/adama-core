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

import ape.runtime.json.JsonStreamWriter;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.reactives.RxInt32;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * this class has the job of trakcing futures which get created and assigning them persistent ids.
 * This is the class which buffers asks from the code such that we can turn around and ask the
 * people
 */
public class OutstandingFutureTracker {
  public final ArrayList<OutstandingFuture> created;
  public final TimeoutTracker timeouts;
  private final RxInt32 source;
  private int maxId;

  public OutstandingFutureTracker(final RxInt32 source, TimeoutTracker timeouts) {
    this.source = source;
    this.timeouts = timeouts;
    created = new ArrayList<>();
    maxId = source.get();
  }

  /** the code completed, so let's commit our values and clean up! */
  public void commit() {
    if (source.get() != maxId) {
      source.set(maxId);
    }
    created.clear();
  }

  /**
   * dump the viewer's data into the provide node; this is how people learn that they must make a
   * decision
   */
  public void dump(final JsonStreamWriter writer, final NtPrincipal who) {
    writer.writeObjectFieldIntro("outstanding");
    writer.beginArray();
    final var clientsBlocking = new HashSet<NtPrincipal>();
    for (final OutstandingFuture exist : created) {
      if (exist.outstanding()) {
        clientsBlocking.add(exist.who);
        if (exist.who.equals(who)) {
          writer.injectJson(exist.json);
        }
      }
    }
    writer.endArray();
    writer.writeObjectFieldIntro("blockers");
    writer.beginArray();
    for (final NtPrincipal blocker : clientsBlocking) {
      writer.writeNtPrincipal(blocker);
    }
    writer.endArray();
  }

  /**
   * create a future for the given channel and client should the client not already know about one
   */
  public OutstandingFuture make(final String channel, final NtPrincipal client) {
    var newId = source.get() + 1;
    for (final OutstandingFuture exist : created) {
      if (exist.test(channel, client)) {
        return exist;
      }
      if (exist.id >= newId) {
        newId = exist.id + 1;
      }
    }
    if (newId > maxId) {
      maxId = newId;
    }
    final var future = new OutstandingFuture(newId, channel, client);
    created.add(future);
    return future;
  }

  /** reset all the futures, this happens when the code gets reset */
  public void restore() {
    // in hindsight, we may not need this complexity... we MAY just need an index
    // for the client viewers
    for (final OutstandingFuture exist : created) {
      exist.reset();
    }
  }
}
