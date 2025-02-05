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

import java.io.IOException;

/** combine multiple storages into one */
public class SequenceStorage implements Storage {
  private final Storage[] storages;
  private final long size;

  public SequenceStorage(Storage... storages) {
    this.storages = storages;
    long _size = 0;
    for (Storage storage : storages) {
      _size += storage.size();
    }
    this.size = _size;
  }

  @Override
  public long size() {
    return size;
  }

  @Override
  public void write(Region region, byte[] mem) {
    long at = region.position;
    for (Storage storage : storages) {
      if (at < storage.size()) {
        storage.write(new Region(at, region.size), mem);
        return;
      }
      at -= storage.size();
    }
  }

  @Override
  public byte[] read(Region region) {
    long at = region.position;
    for (Storage storage : storages) {
      if (at < storage.size()) {
        return storage.read(new Region(at, region.size));
      }
      at -= storage.size();
    }
    return null;
  }

  @Override
  public void flush() throws IOException {
    for (Storage storage : storages) {
      storage.flush();
    }
  }

  @Override
  public void close() throws IOException {
    for (Storage storage : storages) {
      storage.close();
    }
  }
}
