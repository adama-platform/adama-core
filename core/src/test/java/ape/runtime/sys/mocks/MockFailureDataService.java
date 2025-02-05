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

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.runtime.contracts.DeleteTask;
import ape.runtime.data.*;
import ape.runtime.data.*;

import java.util.Set;

public class MockFailureDataService implements DataService {
  public boolean crashScan = false;

  @Override
  public void get(Key key, Callback<LocalDocumentChange> callback) {
    callback.failure(new ErrorCodeException(999));
  }

  @Override
  public void initialize(Key key, RemoteDocumentUpdate patch, Callback<Void> callback) {
    callback.failure(new ErrorCodeException(999));
  }

  @Override
  public void patch(Key key, RemoteDocumentUpdate[] patches, Callback<Void> callback) {
    callback.failure(new ErrorCodeException(999));
  }

  @Override
  public void compute(
      Key key, ComputeMethod method, int seq, Callback<LocalDocumentChange> callback) {
    callback.failure(new ErrorCodeException(999));
  }

  @Override
  public void delete(Key key, DeleteTask task, Callback<Void> callback) {
    callback.failure(new ErrorCodeException(999));
  }

  @Override
  public void snapshot(Key key, DocumentSnapshot snapshot, Callback<Integer> callback) {
    callback.failure(new ErrorCodeException(912));
  }

  @Override
  public void shed(Key key) {
  }

  @Override
  public void recover(Key key, DocumentRestore restore, Callback<Void> callback) {
    callback.failure(new ErrorCodeException(-42));
  }

  @Override
  public void inventory(Callback<Set<Key>> callback) {
    callback.failure(new ErrorCodeException(123));
  }

  @Override
  public void close(Key key, Callback<Void> callback) {
    callback.failure(new ErrorCodeException(1231));
  }
}
