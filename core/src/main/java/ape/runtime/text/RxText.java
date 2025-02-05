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
package ape.runtime.text;

import ape.runtime.contracts.RxParent;
import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.natives.NtDynamic;
import ape.runtime.reactives.RxBase;
import ape.runtime.reactives.RxInt32;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/** a text field within a document */
public class RxText extends RxBase {
  private final RxInt32 gen;
  private Text backup;
  private Text value;

  public RxText(final RxParent parent, RxInt32 gen) {
    super(parent);
    this.gen = gen;
    this.backup = new Text(gen.bumpUpPost());
    this.value = this.backup;
  }

  public Text current() {
    return value;
  }

  @Override
  public void __commit(String name, JsonStreamWriter forwardDelta, JsonStreamWriter reverseDelta) {
    if (__isDirty()) {
      if (backup == value && value.uncommitedChanges.size() > 0 && !value.upgraded) {
        __commitJustChanges(name, forwardDelta, reverseDelta);
      } else {
        __commitFullDiff(name, forwardDelta, reverseDelta);
      }
      backup = value;
      __lowerDirtyCommit();
    }
  }

  private void __commitJustChanges(String name, JsonStreamWriter forwardDelta, JsonStreamWriter reverseDelta) {
    forwardDelta.writeObjectFieldIntro(name);
    forwardDelta.beginObject();
    forwardDelta.writeObjectFieldIntro("changes");
    forwardDelta.beginObject();
    reverseDelta.writeObjectFieldIntro(name);
    reverseDelta.beginObject();
    reverseDelta.writeObjectFieldIntro("changes");
    reverseDelta.beginObject();
    for (Map.Entry<Integer, String> change : value.uncommitedChanges.entrySet()) {
      forwardDelta.writeObjectFieldIntro(change.getKey());
      forwardDelta.injectJson(change.getValue());
      reverseDelta.writeObjectFieldIntro(change.getKey());
      reverseDelta.writeNull();
    }
    value.commit();
    forwardDelta.endObject();
    forwardDelta.endObject();
    reverseDelta.endObject();
    reverseDelta.endObject();
  }

  private void __commitFullDiff(String name, JsonStreamWriter forwardDelta, JsonStreamWriter reverseDelta) {
    forwardDelta.writeObjectFieldIntro(name);
    forwardDelta.beginObject();
    reverseDelta.writeObjectFieldIntro(name);
    reverseDelta.beginObject();

    forwardDelta.writeObjectFieldIntro("fragments");
    reverseDelta.writeObjectFieldIntro("fragments");
    forwardDelta.beginObject();
    reverseDelta.beginObject();
    __commitDiffMap(backup.fragments, value.fragments, forwardDelta, reverseDelta);
    forwardDelta.endObject();
    reverseDelta.endObject();

    forwardDelta.writeObjectFieldIntro("order");
    reverseDelta.writeObjectFieldIntro("order");
    forwardDelta.beginObject();
    reverseDelta.beginObject();
    __commitDiffMap(backup.order, value.order, forwardDelta, reverseDelta);
    forwardDelta.endObject();
    reverseDelta.endObject();

    value.commit();

    forwardDelta.writeObjectFieldIntro("changes");
    reverseDelta.writeObjectFieldIntro("changes");
    forwardDelta.beginObject();
    reverseDelta.beginObject();
    __commitDiffMap(backup.changes, value.changes, forwardDelta, reverseDelta);
    forwardDelta.endObject();
    reverseDelta.endObject();

    if (backup.seq != value.seq) {
      forwardDelta.writeObjectFieldIntro("seq");
      forwardDelta.writeInteger(value.seq);
      reverseDelta.writeObjectFieldIntro("seq");
      reverseDelta.writeInteger(backup.seq);
    }

    if (backup.gen != value.gen) {
      forwardDelta.writeObjectFieldIntro("gen");
      forwardDelta.writeInteger(value.gen);
      reverseDelta.writeObjectFieldIntro("gen");
      reverseDelta.writeInteger(backup.gen);
    }

    forwardDelta.endObject();
    reverseDelta.endObject();

    backup = value;
  }

  private static <T> void __commitDiffMap(HashMap<T, String> prior, HashMap<T, String> next, JsonStreamWriter forwardDelta, JsonStreamWriter reverseDelta) {
    HashSet<T> nuke = new HashSet<>(prior.keySet());
    for (Map.Entry<T, String> entryNew : next.entrySet()) {
      nuke.remove(entryNew.getKey());
      String old = prior.get(entryNew.getKey());
      if (!entryNew.getValue().equals(old)) {
        forwardDelta.writeObjectFieldIntro(entryNew.getKey());
        forwardDelta.writeString(entryNew.getValue());
        reverseDelta.writeObjectFieldIntro(entryNew.getKey());
        if (old == null) {
          reverseDelta.writeNull();
        } else {
          reverseDelta.writeString(old);
        }
      }
    }
    for (T nukeKey : nuke) {
      forwardDelta.writeObjectFieldIntro(nukeKey);
      forwardDelta.writeNull();
      reverseDelta.writeObjectFieldIntro(nukeKey);
      reverseDelta.writeString(prior.get(nukeKey));
    }
  }

  @Override
  public void __dump(JsonStreamWriter writer) {
    value.write(writer);
  }

  @Override
  public void __insert(JsonStreamReader reader) {
    backup = new Text(reader, gen.bumpUpPost());
    value = backup;
  }

  @Override
  public void __patch(JsonStreamReader reader) {
    if (value == backup) {
      value = backup.forkValue();
    }
    value.patch(reader, gen.bumpUpPost());
    __raiseDirty();
  }

  @Override
  public void __revert() {
    if (__isDirty()) {
      value = backup;
      __lowerDirtyRevert();
    }
  }

  @Override
  public long __memory() {
    if (backup == value) {
      return super.__memory() + value.memory();
    } else {
      return super.__memory() + value.memory() + backup.memory();
    }
  }

  public boolean append(int seq, NtDynamic changes) {
    if (value.append(seq, changes.json)) {
      __raiseDirty();
      return true;
    }
    return false;
  }

  public void set(String str) {
    if (value == backup) {
      value = backup.forkValue();
    }
    this.value.set(str, gen.bumpUpPost());
    __raiseDirty();
  }

  public void compact(double ratio) {
    if (value == backup) {
      value = backup.forkValue();
    }
    value.compact(ratio);
    __raiseDirty();
  }

  public String get() {
    return value.get().value;
  }
}
