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

import java.io.ByteArrayInputStream;

/** a simplified http body */
public interface SimpleHttpRequestBody {
  SimpleHttpRequestBody EMPTY = new SimpleHttpRequestBody() {
    @Override
    public long size() {
      return 0;
    }

    @Override
    public int read(byte[] chunk) throws Exception {
      return 0;
    }

    @Override
    public void finished(boolean success) throws Exception {
    }


    @Override
    public void pumpLogEntry(ObjectNode body) {
      body.put("type", "empty");
      body.put("size", 0);
    }
  };

  static SimpleHttpRequestBody WRAP(byte[] bytes) {
    ByteArrayInputStream memory = new ByteArrayInputStream(bytes);
    return new SimpleHttpRequestBody() {
      @Override
      public long size() {
        return bytes.length;
      }

      @Override
      public int read(byte[] chunk) throws Exception {
        return memory.read(chunk);
      }

      @Override
      public void finished(boolean success) throws Exception {
        memory.close();
      }

      @Override
      public void pumpLogEntry(ObjectNode body) {
        body.put("type", "byte-array");
        body.put("size", bytes.length);
      }
    };
  }

  /** the size of the body */
  long size();

  /** read a chunk */
  int read(byte[] chunk) throws Exception;

  /** the body is finished being read */
  void finished(boolean success) throws Exception;

  /** fill a log entry */
  void pumpLogEntry(ObjectNode body);
}
