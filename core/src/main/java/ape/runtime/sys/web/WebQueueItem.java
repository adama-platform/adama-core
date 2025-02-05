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
import ape.runtime.remote.RxCache;
import ape.runtime.sys.web.partial.WebPartial;

/** an item within the queue for processing web tasks */
public class WebQueueItem {
  public final int id;
  public final WebContext context;
  public final RxCache cache;
  public final WebItem item;
  public final EphemeralFuture<WebResponse> future;
  public WebQueueState state;
  public final IdHistoryLog log;

  public WebQueueItem(int id, WebContext context, WebItem item, RxCache cache, IdHistoryLog log, EphemeralFuture<WebResponse> future) {
    this.id = id;
    this.context = context;
    this.item = item;
    this.cache = cache;
    this.future = future;
    this.log = log;
    this.state = WebQueueState.Created;
  }

  public static WebQueueItem from(int taskId, JsonStreamReader reader, RxCache cache) {
    if (reader.startObject()) {
      WebContext _context = null;
      WebPartial _item_partial = null;
      IdHistoryLog _log = null;
      while (reader.notEndOfObject()) {
        switch (reader.fieldName()) {
          case "cache":
            cache.__insert(reader);
            break;
          case "context":
            _context = WebContext.readFromObject(reader);
            break;
          case "item":
            _item_partial = WebPartial.read(reader);
            break;
          case "log":
            _log = IdHistoryLog.read(reader);
            break;
          default:
            reader.skipValue();
        }
      }
      if (_log == null) {
        _log = new IdHistoryLog();
      }
      return new WebQueueItem(taskId, _context, _item_partial.convert(_context), cache, _log,null);
    } else {
      reader.skipValue();
    }
    return null;
  }

  public void commit(int key, JsonStreamWriter forward, JsonStreamWriter reverse) {
    boolean isCacheDirty = cache.__isDirty();
    boolean isLogDirty = log.resetDirtyGetPriorDirty();

    if (isCacheDirty || isLogDirty) {
      forward.writeObjectFieldIntro("" + key);
      forward.beginObject();
      reverse.writeObjectFieldIntro("" + key);
      reverse.beginObject();
      if (isCacheDirty) {
        cache.__commit("cache", forward, reverse);
      }
      if (isLogDirty && log.has()) {
        forward.writeObjectFieldIntro("log");
        log.dump(forward);
      }
      forward.endObject();
      reverse.endObject();
    }
  }

  public void advanceStateDirtiness() {
    if (state == WebQueueState.Steady) {
      // we think we are steady, but something else has happened causing us to be dirty
      if (log.isDirty()) {
        state = WebQueueState.Dirty;
      }
    }
  }

  public void dump(JsonStreamWriter writer) {
    writer.beginObject();
    writer.writeObjectFieldIntro("context");
    context.writeAsObject(writer);
    writer.writeObjectFieldIntro("item");
    item.writeAsObject(writer);
    writer.writeObjectFieldIntro("cache");
    cache.__dump(writer);
    if (log.has()) {
      writer.writeObjectFieldIntro("log");
      log.dump(writer);
      log.resetDirtyGetPriorDirty();
    }
    writer.endObject();
  }

  public void patch(JsonStreamReader reader) {
    if (reader.startObject()) {
      while (reader.notEndOfObject()) {
        String fieldName = reader.fieldName();
        if ("cache".equals(fieldName)) {
          cache.__patch(reader);
        } else if ("log".equals(fieldName)) {
          log.readInline(reader);
        } else {
          reader.skipValue();
        }
      }
    } else {
      reader.skipValue();
    }
  }
}
