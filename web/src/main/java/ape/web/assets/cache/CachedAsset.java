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
package ape.web.assets.cache;

import ape.common.SimpleExecutor;
import ape.common.cache.Measurable;
import ape.web.assets.AssetStream;

/**
 * Cache entry interface for stored assets supporting concurrent requestors.
 * Implements Measurable for LRU cache size tracking. First requestor triggers
 * actual fetch via returned AssetStream; subsequent requestors attach to the
 * in-progress stream and receive data as it arrives.
 */
public interface CachedAsset extends Measurable {
  /** where the cached code is running */
  public SimpleExecutor executor();

  /** attach the stream to the cache; this returns a non-null valid when the cache needs to be filled */
  public AssetStream attachWhileInExecutor(AssetStream attach);

  /** the cached item needs to be removed */
  public void evict();
}
