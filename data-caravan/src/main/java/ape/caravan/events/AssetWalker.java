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
import ape.runtime.json.JsonStreamReader;

import java.util.ArrayList;
import java.util.HashSet;

/** walk assets within a restore file and produce the ids */
public class AssetWalker implements EventCodec.HandlerEvent {
  private final HashSet<String> ids;

  public AssetWalker() {
    this.ids = new HashSet<>();
  }

  /** entry point: give it a list of writes and get a list of asset ids */
  public static HashSet<String> idsOf(ArrayList<byte[]> writes) {
    AssetWalker walker = new AssetWalker();
    for (byte[] write : writes) {
      EventCodec.route(Unpooled.wrappedBuffer(write), walker);
    }
    return walker.ids;
  }

  @Override
  public void handle(Events.Snapshot payload) {
    scanJson(payload.document);
  }

  @Override
  public void handle(Events.Batch payload) {
    for (Events.Change change : payload.changes) {
      handle(change);
    }
  }

  @Override
  public void handle(Events.Change payload) {
    scanJson(payload.redo);
    scanJson(payload.undo);
  }

  @Override
  public void handle(Events.Recover payload) {
    scanJson(payload.document);
  }

  public void scanJson(String json) {
    new JsonStreamReader(json).populateGarbageCollectedIds(ids);
  }
}
