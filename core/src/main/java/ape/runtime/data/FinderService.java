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

import java.util.List;

/** an interface to describe how to find a specific document by key */
public interface FinderService extends SimpleFinderService {

  /** take over for the key */
  void bind(Key key, Callback<Void> callback);

  /** release the machine for the given key */
  void free(Key key, Callback<Void> callback);

  /** set a backup copy while still active on machine */
  void backup(Key key, BackupResult result, Callback<Void> callback);

  /** mark the key for deletion */
  void markDelete(Key key, Callback<Void> callback);

  /** signal that deletion has been completed */
  void commitDelete(Key key, Callback<Void> callback);

  /** list all items on a host */
  void list(Callback<List<Key>> callback);

  /** list all items on a host */
  void listDeleted(Callback<List<Key>> callback);

}
