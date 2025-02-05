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

import ape.runtime.contracts.AsyncAction;
import ape.runtime.exceptions.AbortMessageException;
import ape.runtime.exceptions.RetryProgressException;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.natives.NtMessageBase;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.sys.CoreRequestContext;

/**
 * a task is a wrapper around a message, it is used to track the lifecycle of the message and delay
 * executing code from the living document.
 */
public class AsyncTask {
  public final String channel;
  public final Integer viewId; // note: we don't persist this as it is ephemeral
  public final Object message;
  public final int messageId;
  public final int docSeq;
  public final long timestamp;
  public final NtPrincipal who;
  public final String origin;
  public final String ip;
  private boolean aborted;
  private AsyncAction action;
  public final IdHistoryLog log;
  private boolean used;

  /** Construct the task around a message */
  public AsyncTask(final int messageId, final int docSeq, final NtPrincipal who, final Integer viewId, final String channel, final long timestamp, final String origin, String ip, final Object message, IdHistoryLog log) {
    this.messageId = messageId;
    this.docSeq = docSeq;
    this.who = who;
    this.viewId = viewId;
    this.channel = channel;
    this.timestamp = timestamp;
    this.origin = origin;
    this.ip = ip;
    this.message = message;
    this.log = log;
    action = null;
    aborted = false;
    used = false;
  }

  public CoreRequestContext context(String key) {
    return new CoreRequestContext(who, origin, ip, key);
  }

  /** dump to a Json Stream Writer */
  public void dump(final JsonStreamWriter writer) {
    writer.beginObject();
    writer.writeObjectFieldIntro("who");
    writer.writeNtPrincipal(who);
    writer.writeObjectFieldIntro("channel");
    writer.writeFastString(channel);
    writer.writeObjectFieldIntro("seq");
    writer.writeInteger(docSeq);
    writer.writeObjectFieldIntro("timestamp");
    writer.writeLong(timestamp);
    writer.writeObjectFieldIntro("origin");
    writer.writeFastString(origin);
    writer.writeObjectFieldIntro("ip");
    writer.writeFastString(ip);
    writer.writeObjectFieldIntro("message");
    if (message instanceof NtMessageBase) {
      ((NtMessageBase) message).__writeOut(writer);
    } else if (message instanceof NtMessageBase[]) {
      final var msgs = (NtMessageBase[]) message;
      writer.beginArray();
      for (final NtMessageBase msg : msgs) {
        msg.__writeOut(writer);
      }
      writer.endArray();
    }
    if (log.has()) {
      writer.writeObjectFieldIntro("log");
      log.dump(writer);
    }
    writer.endObject();
  }

  /** execute the task */
  public boolean execute() throws RetryProgressException {
    // we must have either an action and not be aborted
    if (action != null && !aborted) {
      try {
        action.execute(); // compute
        markUsed();
        return true;
      } catch (final AbortMessageException aborted) {
        // this did not go so well
        this.aborted = true;
        this.used = true;
        throw new RetryProgressException(this);
      }
    }
    return false;
  }

  /** the underlying message has been consumed */
  public void markUsed() {
    used = true;
  }

  /** the usage was reverted */
  public void resetUsed() {
    if (!aborted) {
      used = false;
    }
  }

  public boolean isUsed() {
    return used;
  }

  /**
   * associate code to run on this task. This is done within the generated code to invert the
   * execution flow.
   */
  public void setAction(final AsyncAction action) {
    this.action = action;
  }
}
