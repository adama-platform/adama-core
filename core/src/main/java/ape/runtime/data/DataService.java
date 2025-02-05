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
package ape.runtime.data;

import ape.common.Callback;
import ape.runtime.contracts.DeleteTask;

import java.util.Set;

/** the contract for the data service */
public interface DataService {

  /** Download the entire object and return the entire json */
  void get(Key key, Callback<LocalDocumentChange> callback);

  /** write the first entry for the document */
  void initialize(Key key, RemoteDocumentUpdate patch, Callback<Void> callback);

  /** Apply a series of patches to the document using rfc7396 */
  void patch(Key key, RemoteDocumentUpdate[] patches, Callback<Void> callback);

  /** Compute the change the state of the document to the indicated seq by the given client */
  void compute(Key key, ComputeMethod method, int seq, Callback<LocalDocumentChange> callback);

  /** Delete the document given by the ID */
  void delete(Key key, DeleteTask task, Callback<Void> callback);

  /** Snapshot the state of the document */
  void snapshot(Key key, DocumentSnapshot snapshot, Callback<Integer> callback);

  /** a command from on-high to shed the key from the machine */
  void shed(Key key);

  /** recover a document from a snapshot */
  void recover(Key key, DocumentRestore restore, Callback<Void> callback);

  /** list all the keys in storage */
  void inventory(Callback<Set<Key>> callback);

  /** close the storage */
  void close(Key key, Callback<Void> callback);
}
