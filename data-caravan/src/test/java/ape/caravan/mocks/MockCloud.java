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
package ape.caravan.mocks;

import ape.caravan.contracts.Cloud;
import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.runtime.data.Key;

import java.io.File;
import java.util.HashMap;

public class MockCloud implements Cloud {
  private final File path;
  private final HashMap<String, File> map;

  public MockCloud() {
    try {
      path = new File(File.createTempFile("adama_abc", "def").getParentFile(), "_ADAMA_TEMP_" + System.currentTimeMillis());
      path.mkdir();
      map = new HashMap<>();
      Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
        @Override
        public void run() {
          for (File file : path.listFiles()) {
            file.delete();
          }
          path.delete();
        }
      }));
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void delete(Key key, String archiveKey, Callback<Void> callback) {
    File found = map.remove(archiveKey);
    if (found != null) {
      found.delete();
    }
  }

  @Override
  public File path() {
    return path;
  }

  @Override
  public void exists(Key key, String archiveKey, Callback<Void> callback) {
    File found = map.get(archiveKey);
    if (found == null) {
      callback.failure(new ErrorCodeException(-102));
    } else {
      callback.success(null);
    }
  }

  @Override
  public void restore(Key key, String archiveKey, Callback<File> callback) {
    if (archiveKey.equals("notfound")) {
      callback.success(new File(path, "nope"));
      return;
    }
    File found = map.get(archiveKey);
    if (found == null) {
      callback.failure(new ErrorCodeException(-102));
    } else {
      callback.success(found);
    }
  }

  @Override
  public void backup(Key key, File archiveFile, Callback<Void> callback) {
    map.put(archiveFile.getName(), archiveFile);
    callback.success(null);
  }
}
