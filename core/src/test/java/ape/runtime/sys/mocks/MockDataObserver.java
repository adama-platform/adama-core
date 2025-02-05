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
package ape.runtime.sys.mocks;

import ape.common.ErrorCodeException;
import ape.runtime.data.DataObserver;

import java.util.ArrayList;

public class MockDataObserver implements DataObserver {
  public ArrayList<String> writes = new ArrayList<>();

  @Override
  public String machine() {
    return "mock";
  }

  @Override
  public synchronized void start(String snapshot) {
    writes.add("START:" + snapshot);
  }

  @Override
  public synchronized void change(String delta) {
    writes.add("DELTA:" + delta);
  }

  @Override
  public synchronized void failure(ErrorCodeException exception) {
    writes.add("FAILURE:" + exception.code);
  }

  public void dump(String intro) {
    System.err.println("MockDataObserver:" + intro);
    int at = 0;
    for (String write : writes) {
      System.err.println(at + "|" + write);
      at++;
    }
  }
}
