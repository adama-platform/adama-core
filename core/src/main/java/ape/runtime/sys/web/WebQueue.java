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
package ape.runtime.sys.web;

import ape.runtime.async.EphemeralFuture;
import ape.runtime.async.IdHistoryLog;
import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.reactives.RxInt32;
import ape.runtime.contracts.DelayParent;
import ape.runtime.remote.RxCache;
import ape.runtime.sys.LivingDocument;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/** the web queue represents inflight web actions. */
public class WebQueue implements Iterable<Map.Entry<Integer, WebQueueItem>> {
  // for generating ids for the web tasks
  private final RxInt32 webTaskId;
  // the items in the "queue"
  private final LinkedHashMap<Integer, WebQueueItem> items;
  // whether or not something in the queue is dirty
  private boolean dirty;

  public WebQueue(RxInt32 webTaskId) {
    this.webTaskId = webTaskId;
    this.items = new LinkedHashMap<>();
    this.dirty = false;
  }

  /** queue a work item */
  public void queue(WebContext context, WebItem item, EphemeralFuture<WebResponse> future, RxCache cache, IdHistoryLog log, DelayParent parent) {
    int taskId = webTaskId.bumpUpPre();
    WebQueueItem wqi = new WebQueueItem(taskId, context, item, cache, log, future);
    items.put(taskId, wqi);
    bind(parent, wqi);
    dirty = true;
  }

  /** helper: bind a parent to an item getting dirty */
  private void bind(DelayParent parent, WebQueueItem wqi) {
    parent.bind(() -> {
      if (wqi.state == WebQueueState.Steady) {
        wqi.state = WebQueueState.Dirty;
      }
      dirty = true;
    });
  }

  /** cancel all items in the queue */
  public void cancel() {
    Iterator<Map.Entry<Integer, WebQueueItem>> it = items.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<Integer, WebQueueItem> val = it.next();
      WebQueueItem item = val.getValue();
      if (item.future != null) {
        item.future.cancel();
      }
      it.remove();
    }
  }

  /** force a change */
  public void dirty() {
    this.dirty = true;
  }

  /** write deltas out */
  public void commit(JsonStreamWriter forward, JsonStreamWriter reverse) {
    if (dirty) {
      forward.writeObjectFieldIntro("__webqueue");
      reverse.writeObjectFieldIntro("__webqueue");
      forward.beginObject();
      reverse.beginObject();

      // dump all new
      Iterator<Map.Entry<Integer, WebQueueItem>> it = items.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry<Integer, WebQueueItem> val = it.next();
        WebQueueItem item = val.getValue();
        item.advanceStateDirtiness();
        switch (item.state) {
          case Dirty:
            item.commit(val.getKey(), forward, reverse);
            item.state = WebQueueState.Steady;
            break;
          case Created:
            forward.writeObjectFieldIntro(val.getKey());
            item.dump(forward);
            reverse.writeObjectFieldIntro(val.getKey());
            reverse.writeNull();
            item.state = WebQueueState.Steady;
            break;
          case Remove:
            forward.writeObjectFieldIntro(val.getKey());
            forward.writeNull();
            reverse.writeObjectFieldIntro(val.getKey());
            item.dump(reverse);
            it.remove();
            break;
        }
      }
      forward.endObject();
      reverse.endObject();
    }
  }

  /** dump the state out */
  public void dump(JsonStreamWriter writer) {
    writer.writeObjectFieldIntro("__webqueue");
    writer.beginObject();
    Iterator<Map.Entry<Integer, WebQueueItem>> it = items.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<Integer, WebQueueItem> val = it.next();
      writer.writeObjectFieldIntro(val.getKey());
      val.getValue().dump(writer);
    }
    writer.endObject();
  }

  /** restore/path state from the reader */
  public void hydrate(JsonStreamReader reader, LivingDocument root) {
    if (reader.testLackOfNull()) {
      if (reader.startObject()) {
        while (reader.notEndOfObject()) {
          final var taskId = Integer.parseInt(reader.fieldName());
          if (reader.testLackOfNull()) {
            WebQueueItem prior = items.get(taskId);
            if (prior != null) {
              prior.patch(reader);
            } else {
              DelayParent parent = new DelayParent();
              RxCache cache = new RxCache(root, parent);
              prior = WebQueueItem.from(taskId, reader, cache);
              if (prior != null) {
                items.put(taskId, prior);
                bind(parent, prior);
              }
            }
          } else {
            items.remove(taskId);
          }
        }
      }
    } else {
      items.clear();
    }
  }

  @Override
  public Iterator<Map.Entry<Integer, WebQueueItem>> iterator() {
    return items.entrySet().iterator();
  }

  /** how many items remain in the queue */
  public int size() {
    return items.size();
  }
}
