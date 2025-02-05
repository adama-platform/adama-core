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
package ape.common.keys;

import ape.ErrorCodes;
import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.NamedRunnable;
import ape.common.SimpleExecutor;

import java.security.PrivateKey;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/** store private keys in memory for nearly forever. TODO: expire at some point... maybe */
public abstract class PrivateKeyCache {
  private final ConcurrentHashMap<SpaceKeyIdPair, PrivateKey> keys;
  private final SimpleExecutor executor;

  public PrivateKeyCache(SimpleExecutor executor) {
    this.keys = new ConcurrentHashMap<>();
    this.executor = executor;
  }

  public void get(String space, int keyId, Callback<PrivateKey> callback) {
    SpaceKeyIdPair pair = new SpaceKeyIdPair(space, keyId);
    PrivateKey immediate = keys.get(pair);
    if (immediate != null) {
      callback.success(immediate);
      return;
    }
    executor.execute(new NamedRunnable("find-private-key") {
      @Override
      public void execute() throws Exception {
        PrivateKey result = keys.get(pair);
        if (result != null) {
          callback.success(result);
          return;
        }
        result = find(pair);
        if (result == null) {
          callback.failure(new ErrorCodeException(ErrorCodes.PRIVATE_KEY_NOT_FOUND));
          return;
        }
        keys.put(pair, result);
        callback.success(result);
      }
    });
  }

  protected abstract PrivateKey find(SpaceKeyIdPair pair);

  public static class SpaceKeyIdPair {
    public final String space;
    public final int id;

    public SpaceKeyIdPair(String space, int id) {
      this.space = space;
      this.id = id;
    }

    @Override
    public int hashCode() {
      return Objects.hash(space, id);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      SpaceKeyIdPair that = (SpaceKeyIdPair) o;
      return id == that.id && Objects.equals(space, that.space);
    }
  }
}
