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
package ape.runtime.mocks;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.runtime.contracts.AdamaStream;

public class MockAdamaStream implements AdamaStream {
  private final StringBuilder writer;

  public MockAdamaStream() {
    this.writer = new StringBuilder();
  }

  @Override
  public String toString() {
    return writer.toString();
  }

  @Override
  public void update(String newViewerState, Callback<Void> callback) {
    writer.append("UPDATE:" + newViewerState + "\n");
    callback.success(null);
  }

  @Override
  public void send(String channel, String marker, String message, Callback<Integer> callback) {
    writer.append("SEND:" + channel + "/" + marker + "/" + message + "\n");
    if (channel.equals("failure")) {
      callback.failure(new ErrorCodeException(-1));
    } else {
      callback.success(123);
    }
  }

  @Override
  public void canAttach(Callback<Boolean> callback) {
    writer.append("CANATTACH\n");
    callback.success(true);
    callback.failure(new ErrorCodeException(-2));
  }

  @Override
  public void attach(String id, String name, String contentType, long size, String md5, String sha384, Callback<Integer> callback) {
    writer.append("ATTACH:" + id + "/" + name + "/" + contentType + "/" + size + "/" + md5 + "/" + sha384 + "\n");
    if (id.equals("failure")) {
      callback.failure(new ErrorCodeException(-2));
    } else {
      callback.success(1);
    }
  }

  @Override
  public void close() {
    writer.append("CLOSE\n");
  }
}
