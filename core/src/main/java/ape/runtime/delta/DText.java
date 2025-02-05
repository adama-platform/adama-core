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
package ape.runtime.delta;

import ape.runtime.contracts.DeltaNode;
import ape.runtime.json.PrivateLazyDeltaWriter;
import ape.runtime.text.RxText;
import ape.runtime.text.SeqString;

/** a document synchronized by deltas using CodeMirror's format */
public class DText implements DeltaNode {
  private boolean initialized;
  private int seq;
  private int gen;

  public DText() {
    initialized = false;
    seq = 0;
    gen = 0;
  }

  /** the string is no longer visible (was made private) */
  public void hide(final PrivateLazyDeltaWriter writer) {
    if (initialized) {
      writer.writeNull();
      initialized = false;
      seq = 0;
      gen = 0;
    }
  }

  @Override
  public void clear() {
    initialized = false;
    seq = 0;
    gen = 0;
  }

  /** memory usage */
  @Override
  public long __memory() {
    return 64;
  }

  public void show(final RxText value, final PrivateLazyDeltaWriter writer) {
    if (value.current().gen != gen) {
      this.initialized = false;
      this.seq = 0;
      this.gen = value.current().gen;
    }
    PrivateLazyDeltaWriter obj = writer.planObject();
    if (initialized) {
      int start = seq;
      String change;
      while ((change = value.current().changes.get(seq)) != null) {
        obj.planField("" + seq).writeString(change);
        seq++;
      }
      while ((change = value.current().uncommitedChanges.get(seq)) != null) {
        obj.planField("" + seq).writeString(change);
        seq++;
      }
      if (start != seq) {
        obj.planField("$s").writeInt(seq);
      }
    } else {
      initialized = true;
      SeqString val = value.current().get();
      obj.planField("$g").writeInt(gen);
      obj.planField("$i").writeString(val.value);
      obj.planField("$s").writeInt(val.seq);
      seq = val.seq;
    }
    obj.end();
  }
}
