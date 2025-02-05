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

/** Since a restore has both deltas and snapshots, we need the snapshots to reset the counter */
public class AssetByteAccountant implements EventCodec.HandlerEvent {
  private long bytes;
  private boolean snapshot;
  private int minimumSeq;

  public AssetByteAccountant() {
    this.bytes = 0;
    this.snapshot = false;
    this.minimumSeq = Integer.MAX_VALUE;
  }

  public long getBytes() {
    return bytes;
  }

  public boolean hasThereBeenDataloss() {
    return minimumSeq > 1 && !snapshot;
  }

  public void account(byte[] entry, long size) {
    EventCodec.route(Unpooled.wrappedBuffer(entry), this);
    bytes += size;
  }

  @Override
  public void handle(Events.Snapshot payload) {
    bytes = 0;
    snapshot = true;
  }

  @Override
  public void handle(Events.Batch payload) {
    for (Events.Change change : payload.changes) {
      handle(change);
    }
  }

  @Override
  public void handle(Events.Change payload) {
    if (payload.seq_begin < minimumSeq) {
      minimumSeq = payload.seq_begin;
      bytes += payload.dAssetBytes;
    }
  }

  @Override
  public void handle(Events.Recover payload) {
    bytes = 0;
    snapshot = true;
    minimumSeq = payload.seq;
  }
}
