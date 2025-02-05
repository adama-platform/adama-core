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
package ape.caravan.data;

import ape.caravan.index.Region;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/** implementation of a Storage using a memory mapped file */
public class MemoryMappedFileStorage implements Storage {
  private final RandomAccessFile storage;
  private final MappedByteBuffer memory;
  private final long size;
  private boolean dirty;

  public MemoryMappedFileStorage(File storeFile, long size) throws IOException {
    this.storage = new RandomAccessFile(storeFile, "rw");
    storage.setLength(size);
    if (storeFile.exists()) {
      storeFile.setWritable(true, false);
    }
    this.size = size;
    this.memory = storage.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, size);
    this.dirty = false;
  }

  @Override
  public long size() {
    return size;
  }

  @Override
  public void write(Region region, byte[] mem) {
    dirty = true;
    memory.slice().position((int) region.position).put(mem);
  }

  @Override
  public byte[] read(Region region) {
    byte[] mem = new byte[region.size];
    memory.slice().position((int) region.position).get(mem);
    return mem;
  }

  @Override
  public void flush() throws IOException {
    if (dirty) {
      memory.force();
    }
  }

  @Override
  public void close() throws IOException {
    storage.close();
  }
}
