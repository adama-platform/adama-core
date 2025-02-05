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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class LocalCacheTests {

  private ArrayList<byte[]> prepare() {
    ArrayList<byte[]> writes = new ArrayList<>();

    {
      Events.Snapshot snap = new Events.Snapshot();
      snap.seq = 1;
      snap.document = "{\"x\":1}";
      snap.history = 10;
      snap.assetBytes = 100;
      ByteBuf buf = Unpooled.buffer();
      EventCodec.write(buf, snap);
      writes.add(ByteArrayHelper.convert(buf));
    }
    {
      Events.Batch batch = new Events.Batch();
      batch.changes = new Events.Change[1];
      batch.changes[0] = new Events.Change();
      batch.changes[0].seq_begin = 2;
      batch.changes[0].seq_end = 4;
      batch.changes[0].active = false;
      batch.changes[0].agent = "agent";
      batch.changes[0].authority = "authority";
      batch.changes[0].dAssetBytes = 100;
      batch.changes[0].redo = "{\"x\":2}";
      batch.changes[0].undo = "undo";
      ByteBuf buf = Unpooled.buffer();
      EventCodec.write(buf, batch);
      writes.add(ByteArrayHelper.convert(buf));
    }
    {
      Events.Change change = new Events.Change();
      change.seq_begin = 5;
      change.seq_end = 5;
      change.active = false;
      change.agent = "agent";
      change.authority = "authority";
      change.dAssetBytes = 1000;
      change.redo = "{\"x\":3}";
      change.undo = "undo";
      ByteBuf buf = Unpooled.buffer();
      EventCodec.write(buf, change);
      writes.add(ByteArrayHelper.convert(buf));
    }
    return writes;
  }

  @Test
  public void account() {
    AssetByteAccountant aba = new AssetByteAccountant();
    for (byte[] write : prepare()) {
      aba.account(write, 1000);
    }
    Assert.assertEquals(3100, aba.getBytes());
  }

  @Test
  public void flow() {
    AtomicInteger finishedCount = new AtomicInteger(0);
    LocalCache cache = new LocalCache() {
      @Override
      public void finished() throws Exception {
        finishedCount.incrementAndGet();
      }
    };
    Assert.assertEquals(0, cache.seq());
    Assert.assertEquals(3, cache.filter(prepare()).size());
    Assert.assertEquals(3, cache.filter(prepare()).size());
    for (byte[] write : prepare()) {
      EventCodec.route(Unpooled.wrappedBuffer(write), cache);
    }
    Assert.assertEquals(0, cache.filter(prepare()).size());
  }

  @Test
  public void restore_nukes_filter() {
    AtomicInteger finishedCount = new AtomicInteger(0);
    LocalCache cache = new LocalCache() {
      @Override
      public void finished() throws Exception {
        finishedCount.incrementAndGet();
      }
    };
    Assert.assertEquals(0, cache.seq());
    ArrayList<byte[]> list = prepare();
    {
      Events.Recover change = new Events.Recover();
      change.seq = 100;
      change.agent = "cake";
      change.authority = "ninja";
      change.document = "{\"z\":100}";
      ByteBuf buf = Unpooled.buffer();
      EventCodec.write(buf, change);
      list.add(ByteArrayHelper.convert(buf));
    }
    ArrayList<byte[]> filtered = cache.filter(list);
    Assert.assertEquals(1, filtered.size());

    for (byte[] write : filtered) {
      EventCodec.route(Unpooled.wrappedBuffer(write), cache);
    }
    Assert.assertEquals("{\"z\":100}", cache.build().patch);
  }

  @Test
  public void recover_recovers() {
    AtomicInteger finishedCount = new AtomicInteger(0);
    LocalCache cache = new LocalCache() {
      @Override
      public void finished() throws Exception {
        finishedCount.incrementAndGet();
      }
    };
    Assert.assertEquals(0, cache.seq());
    ArrayList<byte[]> list = prepare();
    {
      Events.Recover change = new Events.Recover();
      change.seq = 100;
      change.agent = "cake";
      change.authority = "ninja";
      change.document = "{\"z\":100}";
      ByteBuf buf = Unpooled.buffer();
      EventCodec.write(buf, change);
      list.add(ByteArrayHelper.convert(buf));
    }
    for (byte[] write : list) {
      EventCodec.route(Unpooled.wrappedBuffer(write), cache);
    }
    RestoreDebuggerStdErr.print(list);
    Assert.assertEquals("{\"z\":100}", cache.build().patch);
  }
}
