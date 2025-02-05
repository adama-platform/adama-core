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
package ape.caravan.events;

import io.netty.buffer.Unpooled;
import ape.caravan.contracts.ByteArrayStream;
import ape.runtime.contracts.AutoMorphicAccumulator;
import ape.runtime.data.LocalDocumentChange;
import ape.runtime.json.JsonAlgebra;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class LocalCache implements ByteArrayStream, EventCodec.HandlerEvent {
  private final ArrayList<SeqString> redos;
  private final ArrayDeque<SeqString> undos;
  public int currentAppendIndex;
  public SeqString document;
  private int seq;

  public LocalCache() {
    this.document = null;
    this.redos = new ArrayList<>();
    this.undos = new ArrayDeque<>();
    this.seq = 0;
  }

  public int seq() {
    return seq;
  }

  @Override
  public void next(int appendIndex, byte[] value, int seq, long assetBytes) throws Exception {
    this.currentAppendIndex = appendIndex;
    EventCodec.route(Unpooled.wrappedBuffer(value), this);
  }

  @Override
  public void handle(Events.Snapshot payload) {
    document = new SeqString(payload.seq, payload.document);
    Iterator<SeqString> it = redos.iterator();
    while (it.hasNext()) {
      if (it.next().seq + payload.history <= payload.seq) {
        it.remove();
      }
    }
    while (undos.size() > payload.history) {
      undos.removeLast();
    }
  }

  public int getMinimumHistoryToPreserve() {
    return 1 + redos.size();
  }

  @Override
  public void handle(Events.Batch payload) {
    for (Events.Change change : payload.changes) {
      handle(change);
    }
  }

  @Override
  public void handle(Events.Change change) {
    redos.add(new SeqString(change.seq_end, change.redo));
    undos.addFirst(new SeqString(change.seq_begin, change.undo));
    seq = change.seq_end;
  }

  @Override
  public void handle(Events.Recover payload) {
    redos.clear();
    undos.clear();
    seq = payload.seq;
    document = new SeqString(seq, payload.document);
  }

  public ArrayList<byte[]> filter(ArrayList<byte[]> writes) {
    ArrayList<byte[]> reduced = new ArrayList<>();
    AtomicInteger checkSeq = new AtomicInteger(seq);
    for (byte[] write : writes) {
      EventCodec.route(Unpooled.wrappedBuffer(write), new EventCodec.HandlerEvent() {
        @Override
        public void handle(Events.Snapshot payload) {
          if (checkSeq.get() <= payload.seq || checkSeq.get() == 0) {
            reduced.add(write);
            checkSeq.set(payload.seq);
          }
        }

        @Override
        public void handle(Events.Batch payload) {
          if (checkSeq.get() + 1 == payload.changes[0].seq_begin || checkSeq.get() == 0) {
            reduced.add(write);
            checkSeq.set(payload.changes[payload.changes.length - 1].seq_end);
          }
        }

        @Override
        public void handle(Events.Change payload) {
          if (checkSeq.get() + 1 == payload.seq_begin || checkSeq.get() == 0) {
            reduced.add(write);
            checkSeq.set(payload.seq_end);
          }
        }

        @Override
        public void handle(Events.Recover payload) {
          reduced.clear();
          reduced.add(write);
          checkSeq.set(payload.seq);
        }
      });
    }
    return reduced;
  }

  public boolean check(int newSeq) {
    return seq + 1 == newSeq;
  }

  public boolean snapshotInvalid(int snapshotSeq) {
    if (seq == 0) {
      return false;
    }
    // we don't allow future snapshots to happen
    return seq < snapshotSeq;
  }

  public String computeHeadPatch(int seqGoal) {
    AutoMorphicAccumulator<String> merger = JsonAlgebra.mergeAccumulator(true);
    for (SeqString ss : redos) {
      if (ss.seq > seqGoal) {
        merger.next(ss.data);
      }
    }
    if (merger.empty()) {
      return null;
    }
    return merger.finish();
  }

  public String computeRewind(int seqGoal) {
    AutoMorphicAccumulator<String> merger = JsonAlgebra.mergeAccumulator(true);
    Iterator<SeqString> it = undos.iterator();
    int last = -1;
    while (it.hasNext()) {
      SeqString ss = it.next();
      if (ss.seq >= seqGoal) {
        merger.next(ss.data);
        last = ss.seq;
      }
    }
    if (merger.empty() || last > seqGoal) {
      return null;
    }
    return merger.finish();
  }

  public LocalDocumentChange build() {
    AutoMorphicAccumulator<String> merger = JsonAlgebra.mergeAccumulator();
    int count = 0;
    int seqAt = -1;
    if (document != null) {
      count++;
      seqAt = document.seq;
      merger.next(document.data);
    }
    for (SeqString ss : redos) {
      count++;
      if (ss.seq > seqAt) {
        merger.next(ss.data);
        seqAt = ss.seq;
      }
    }
    if (merger.empty()) {
      return null;
    }
    return new LocalDocumentChange(merger.finish(), count, seq);
  }

  private class SeqString {
    private final int seq;
    private final String data;

    public SeqString(int seq, String data) {
      this.seq = seq;
      this.data = data;
    }
  }
}
