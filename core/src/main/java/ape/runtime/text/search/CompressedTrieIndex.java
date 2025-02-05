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
package ape.runtime.text.search;

import java.util.PrimitiveIterator;
import java.util.TreeMap;
import java.util.TreeSet;

/** this is a simple trie for mapping code points that are compressed via a hash into keys */
public class CompressedTrieIndex {
  private final TreeMap<Integer, CompressedTrieIndex> children;
  private TreeSet<Integer> keys;

  public CompressedTrieIndex() {
    this.children = new TreeMap<>();
    this.keys = null;
  }

  private CompressedTrieIndex next(int k) {
    CompressedTrieIndex val = children.get(k);
    if (val == null) {
      val = new CompressedTrieIndex();
      children.put(k, val);
    }
    return val;
  }

  private CompressedTrieIndex of(String word) {
    CompressedTrieIndex root = this;
    PrimitiveIterator.OfInt it = word.codePoints().iterator();
    while (it.hasNext()) {
      int val = it.nextInt();
      if (it.hasNext()) {
        val += 32831 * it.nextInt();
      }
      if (it.hasNext()) {
        val += 52347 * it.nextInt();
      }
      root = next(val);
    }
    return root;
  }

  public void map(String word, int key) {
    CompressedTrieIndex index = of(word);
    if (index.keys == null) {
      index.keys = new TreeSet<>();
    }
    index.keys.add(key);
  }

  public void unmap(String word, int key) {
    CompressedTrieIndex index = of(word);
    if (index.keys != null) {
      index.keys.remove(key);
      if (index.keys.size() == 0) {
        index.keys = null;
        // TODO: need to clean up the parents
      }
    }
  }

  public TreeSet<Integer> keysOf(String word) {
    CompressedTrieIndex index = of(word);
    return of(word).keys;
  }
}
