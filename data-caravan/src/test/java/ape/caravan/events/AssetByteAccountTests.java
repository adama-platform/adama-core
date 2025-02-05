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
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.natives.NtAsset;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class AssetByteAccountTests {


  private ArrayList<byte[]> prepare() {
    ArrayList<byte[]> writes = new ArrayList<>();

    {
      JsonStreamWriter writer = new JsonStreamWriter();
      writer.beginObject();
      writer.writeObjectFieldIntro("asset");
      writer.writeNtAsset(new NtAsset("id-0", "name.asset", "binary", 0, "", ""));
      writer.endObject();
      Events.Snapshot snap = new Events.Snapshot();
      snap.seq = 1;
      snap.document = writer.toString();
      snap.history = 10;
      ByteBuf buf = Unpooled.buffer();
      EventCodec.write(buf, snap);
      writes.add(ByteArrayHelper.convert(buf));
    }
    {
      JsonStreamWriter writer = new JsonStreamWriter();
      writer.beginObject();
      writer.writeObjectFieldIntro("asset");
      writer.writeNtAsset(new NtAsset("id-1", "name.asset", "binary", 0, "", ""));
      writer.endObject();
      Events.Batch batch = new Events.Batch();
      batch.changes = new Events.Change[1];
      batch.changes[0] = new Events.Change();
      batch.changes[0].seq_begin = 2;
      batch.changes[0].seq_end = 4;
      batch.changes[0].active = false;
      batch.changes[0].agent = "agent";
      batch.changes[0].authority = "authority";
      batch.changes[0].dAssetBytes = 100;
      batch.changes[0].redo = writer.toString();
      batch.changes[0].undo = writer.toString();
      ByteBuf buf = Unpooled.buffer();
      EventCodec.write(buf, batch);
      writes.add(ByteArrayHelper.convert(buf));
    }
    {
      JsonStreamWriter writer = new JsonStreamWriter();
      writer.beginObject();
      writer.writeObjectFieldIntro("asset");
      writer.writeNtAsset(new NtAsset("id-2", "name.asset", "binary", 0, "", ""));
      writer.endObject();

      Events.Change change = new Events.Change();
      change.seq_begin = 5;
      change.seq_end = 5;
      change.active = false;
      change.agent = "agent";
      change.authority = "authority";
      change.dAssetBytes = 100;
      change.redo = writer.toString();
      change.undo = writer.toString();
      ByteBuf buf = Unpooled.buffer();
      EventCodec.write(buf, change);
      writes.add(ByteArrayHelper.convert(buf));
    }
    {
      JsonStreamWriter writer = new JsonStreamWriter();
      writer.beginObject();
      writer.writeObjectFieldIntro("asset");
      writer.writeNtAsset(new NtAsset("id-3", "name.asset", "binary", 0, "", ""));
      writer.endObject();
      Events.Recover recover = new Events.Recover();
      recover.seq = 10;
      recover.document = writer.toString();
      recover.authority = "boss";
      recover.agent = "n00b";
      ByteBuf buf = Unpooled.buffer();
      EventCodec.write(buf, recover);
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
    Assert.assertEquals(1000, aba.getBytes());
  }
}
