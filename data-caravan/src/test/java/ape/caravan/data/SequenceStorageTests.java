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
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class SequenceStorageTests {

  @Test
  public void flow() throws Exception {
    File fileToUse1 = File.createTempFile("adama_", "storage1");
    File fileToUse2 = File.createTempFile("adama_", "storage2");
    MemoryMappedFileStorage storage1 = new MemoryMappedFileStorage(fileToUse1, 512);
    MemoryMappedFileStorage storage2 = new MemoryMappedFileStorage(fileToUse2, 8196);
    SequenceStorage storage = new SequenceStorage(storage1, storage2);
    Assert.assertEquals(8196 + 512, storage.size());
    {
      storage.write(new Region(8, 2), "Hi".getBytes(StandardCharsets.UTF_8));
      byte[] read = storage.read(new Region(8, 2));
      Assert.assertEquals("Hi", new String(read, StandardCharsets.UTF_8));
    }
    {
      storage.write(new Region(1024, 2), "Hi".getBytes(StandardCharsets.UTF_8));
      byte[] read = storage.read(new Region(1024, 2));
      Assert.assertEquals("Hi", new String(read, StandardCharsets.UTF_8));
    }
    Assert.assertNull(storage.read(new Region(100000, 2)));
    storage.flush();
    storage.close();
    fileToUse1.delete();
    fileToUse2.delete();
  }
}
