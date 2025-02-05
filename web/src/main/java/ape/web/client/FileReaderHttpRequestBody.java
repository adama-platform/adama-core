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
package ape.web.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import ape.common.Hashing;
import ape.common.Hex;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

public class FileReaderHttpRequestBody implements SimpleHttpRequestBody {
  public final String sha256;
  public final long size;
  private final File file;
  private final BufferedInputStream input;

  public FileReaderHttpRequestBody(File file) throws Exception {
    this.file = file;
    BufferedInputStream inputDigest = null;
    MessageDigest digest = Hashing.sha256();
    long _size = 0;
    try {
      inputDigest = new BufferedInputStream(new FileInputStream(file));
      byte[] chunk = new byte[8196];
      int sz = 0;
      while ((sz = inputDigest.read(chunk)) >= 0) {
        digest.update(chunk, 0, sz);
        _size += sz;
      }
      this.sha256 = Hex.of(digest.digest());
      this.size = _size;
    } finally {
      if (inputDigest != null) {
        inputDigest.close();
      }
    }
    this.input = new BufferedInputStream(new FileInputStream(file));
  }

  @Override
  public long size() {
    return this.size;
  }

  @Override
  public int read(byte[] chunk) throws Exception {
    return this.input.read(chunk);
  }

  @Override
  public void finished(boolean success) throws Exception {
    this.input.close();
  }

  @Override
  public void pumpLogEntry(ObjectNode body) {
    body.put("type", "file");
    body.put("size", size);
    body.put("filename", file.getName());
    body.put("sha256", sha256);
  }
}
