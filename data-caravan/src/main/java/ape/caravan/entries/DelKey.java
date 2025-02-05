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
package ape.caravan.entries;

import io.netty.buffer.ByteBuf;
import ape.caravan.contracts.WALEntry;
import ape.runtime.data.Key;

import java.nio.charset.StandardCharsets;

public class DelKey implements WALEntry<DelKey> {
  public final byte[] space;
  public final byte[] key;

  public DelKey(Key key) {
    this.space = key.space.getBytes(StandardCharsets.UTF_8);
    this.key = key.key.getBytes(StandardCharsets.UTF_8);
  }

  private DelKey(byte[] space, byte[] key) {
    this.space = space;
    this.key = key;
  }

  @Override
  public void write(ByteBuf buf) {
    buf.writeByte(0x36);
    buf.writeIntLE(space.length);
    buf.writeBytes(space);
    buf.writeIntLE(key.length);
    buf.writeBytes(key);
  }

  public static DelKey readAfterTypeId(ByteBuf buf) {
    int sizeSpace = buf.readIntLE();
    byte[] bytesSpace = new byte[sizeSpace];
    buf.readBytes(bytesSpace);
    int sizeKey = buf.readIntLE();
    byte[] bytesKey = new byte[sizeKey];
    buf.readBytes(bytesKey);
    return new DelKey(bytesSpace, bytesKey);
  }

  public Key of() {
    return new Key(new String(this.space, StandardCharsets.UTF_8), new String(this.key, StandardCharsets.UTF_8));
  }
}
