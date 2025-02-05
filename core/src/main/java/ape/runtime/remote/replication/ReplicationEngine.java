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
package ape.runtime.remote.replication;

import ape.common.Callback;
import ape.common.SimpleExecutor;
import ape.runtime.contracts.DeleteTask;
import ape.runtime.contracts.RxParent;
import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.natives.NtToDynamic;
import ape.runtime.reactives.RxInt64;
import ape.runtime.reactives.RxLazy;
import ape.runtime.sys.LivingDocument;

import java.util.*;

/** engine to replicate data */
public class ReplicationEngine implements DeleteTask  {
  private final LivingDocument document;
  private final ArrayList<RxReplicationStatus> tasks;
  private final HashMap<TombStone, RxReplicationStatus> deleting;

  public ReplicationEngine(LivingDocument parent) {
    this.document = parent;
    this.tasks = new ArrayList<>();
    this.deleting = new HashMap<>();
  }

  public void link(RxReplicationStatus status, RxLazy<? extends NtToDynamic> value) {
    tasks.add(status);
    value.__subscribe(status);
    status.linkToValue(value);
  }

  public void load(JsonStreamReader reader, RxInt64 documentTime) {
    if (reader.startObject()) {
      while (reader.notEndOfObject()) {
        String name = reader.fieldName();
        if (reader.testLackOfNull()) {
          TombStone ts = TombStone.read(reader);
          RxReplicationStatus status = new RxReplicationStatus(RxParent.DEAD, documentTime, ts.service, ts.method);
          status.linkToTombstone(ts, document);
          deleting.put(ts, status);
        }
      }
    }
  }

  public void dump(JsonStreamWriter writer) {
    writer.beginObject();
    for (Map.Entry<TombStone, RxReplicationStatus> entry : deleting.entrySet()) {
      writer.writeObjectFieldIntro(entry.getKey().md5);
      entry.getKey().dump(writer);
    }
    writer.endObject();
  }

  public void commit(JsonStreamWriter forwardDelta, JsonStreamWriter reverseDelta) {
    ArrayList<TombStone> toAdd = new ArrayList<>();
    ArrayList<String> toDelete = new ArrayList<>();

    { // make progress on items being deleted
      Iterator<Map.Entry<TombStone, RxReplicationStatus>> itDeleting = deleting.entrySet().iterator();
      while (itDeleting.hasNext()) {
        Map.Entry<TombStone, RxReplicationStatus> pair = itDeleting.next();
        pair.getValue().progress(document);
        if (pair.getValue().isGone()) {
          toDelete.add(pair.getKey().md5);
          itDeleting.remove();
        }
      }
    }

    { // make progress on items that are live
      Iterator<RxReplicationStatus> it = tasks.iterator();
      while (it.hasNext()) {
        RxReplicationStatus status = it.next();
        status.progress(document);
        if (status.requiresTombstone()) {
          it.remove();
          TombStone ts = status.toTombStone();
          if (ts != null) {
            deleting.put(ts, status);
            toAdd.add(ts);
          }
        }
      }
    }

    if (toAdd.size() > 0 || toDelete.size() > 0) {
      // there is no reverse/undo for replication
      reverseDelta.writeObjectFieldIntro("__replication");
      reverseDelta.beginObject();
      reverseDelta.endObject();

      forwardDelta.writeObjectFieldIntro("__replication");
      forwardDelta.beginObject();
      for (String del : toDelete) { // remove the tombstones for successful deletions
        forwardDelta.writeObjectFieldIntro(del);
        forwardDelta.writeNull();
      }
      for (TombStone ts : toAdd) { // persist the tombstone for things being deleted
        forwardDelta.writeObjectFieldIntro(ts.md5);
        ts.dump(forwardDelta);
      }
      forwardDelta.endObject();
    }
  }

  public void signalDurableAndExecute(SimpleExecutor executor) {
    for (RxReplicationStatus status : deleting.values()) {
      status.signalDurableAndExecute(executor);
    }
    for (RxReplicationStatus status : tasks) {
      status.signalDurableAndExecute(executor);
    }
  }

  @Override
  public void executeAfterMark(Callback<Void> callback) {
    // This is very tricky! We just marked the document as deleted, so now we need to execute all the things.
    // TODO: execute deletes
    callback.success(null);
  }
}
