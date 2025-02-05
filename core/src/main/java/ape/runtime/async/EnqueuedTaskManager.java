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

import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.natives.NtDynamic;
import ape.runtime.natives.NtPrincipal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

/** the enqueued task manager is for future execution of messages when the system is settled */
public class EnqueuedTaskManager {
  private final LinkedList<EnqueuedTask> active;
  private final ArrayList<EnqueuedTask> pending;
  private final ArrayList<EnqueuedTask> completed;

  public EnqueuedTaskManager() {
    this.active = new LinkedList<>();
    this.pending = new ArrayList<>();
    this.completed = new ArrayList<>();
  }

  public void hydrate(JsonStreamReader reader) {
    HashSet<Integer> present = new HashSet<>();
    for (EnqueuedTask task : active) {
      present.add(task.messageId);
    }
    if (reader.startObject()) {
      while (reader.notEndOfObject()) {
        int messageId = Integer.parseInt(reader.fieldName());
        String _channel = null;
        NtPrincipal _who = null;
        NtDynamic _message = null;
        int viewId = -1;
        if (reader.startObject()) {
          while (reader.notEndOfObject()) {
            switch (reader.fieldName()) {
              case "who":
                _who = reader.readNtPrincipal();
                break;
              case "channel":
                _channel = reader.readString();
                break;
              case "message":
                _message = reader.readNtDynamic();
                break;
              case "view_id":
                viewId = reader.readInteger();
                break;
              default:
                reader.skipValue();
            }
          }
          if (!present.contains(messageId)) {
            active.add(new EnqueuedTask(messageId, _who, _channel, viewId, _message));
          }
        } else {
          reader.skipValue();
        }
      }
    } else {
      reader.skipValue();
    }
  }

  public void commit(JsonStreamWriter forward, JsonStreamWriter reverse) {
    if (pending.size() > 0 || completed.size() > 0) {
      forward.writeObjectFieldIntro("__enqueued");
      forward.beginObject();
      reverse.writeObjectFieldIntro("__enqueued");
      reverse.beginObject();
      for (EnqueuedTask task : pending) {
        forward.writeObjectFieldIntro(task.messageId);
        task.writeTo(forward);
        reverse.writeObjectFieldIntro(task.messageId);
        reverse.writeNull();
        active.add(task);
      }
      for (EnqueuedTask task : completed) {
        forward.writeObjectFieldIntro(task.messageId);
        forward.writeNull();
        reverse.writeObjectFieldIntro(task.messageId);
        task.writeTo(reverse);
      }
      pending.clear();
      completed.clear();
      forward.endObject();
      reverse.endObject();
    }
  }

  public void revert() {
    pending.clear();
  }

  public void dump(JsonStreamWriter writer) {
    if (active.size() > 0) {
      writer.writeObjectFieldIntro("__enqueued");
      writer.beginObject();
      for (EnqueuedTask task : active) {
        writer.writeObjectFieldIntro(task.messageId);
        task.writeTo(writer);
      }
      writer.endObject();
    }
  }

  public void add(EnqueuedTask task) {
    pending.add(task);
  }

  public boolean readyForTransfer() {
    return pending.size() == 0 && !active.isEmpty();
  }

  public EnqueuedTask transfer() {
    EnqueuedTask task = active.removeFirst();
    completed.add(task);
    return task;
  }

  public int size() {
    return pending.size() + active.size();
  }
}
