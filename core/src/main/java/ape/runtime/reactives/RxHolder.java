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
package ape.runtime.reactives;

import ape.runtime.contracts.CanGetAndSet;
import ape.runtime.contracts.RxParent;
import ape.runtime.json.JsonAlgebra;
import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.natives.NtMessageBase;

import java.util.function.Supplier;

public class RxHolder<Ty extends NtMessageBase> extends RxBase implements CanGetAndSet<Ty> {
  private final Supplier<Ty> maker;
  private Ty backup;
  private Ty value;

  public RxHolder(final RxParent parent, Supplier<Ty> maker) {
    super(parent);
    backup = maker.get();
    this.value = backup;
    this.maker = maker;
  }

  @Override
  public void __commit(String name, JsonStreamWriter forwardDelta, JsonStreamWriter reverseDelta) {
    if (__isDirty()) {
      forwardDelta.writeObjectFieldIntro(name);
      reverseDelta.writeObjectFieldIntro(name);
      Object from = new JsonStreamReader(backup.to_dynamic().json).readJavaTree();
      Object to = new JsonStreamReader(value.to_dynamic().json).readJavaTree();
      JsonAlgebra.writeObjectFieldDelta(from, to, forwardDelta);
      JsonAlgebra.writeObjectFieldDelta(to, from, reverseDelta);
      backup = value;
      __lowerDirtyCommit();
    }
  }

  @Override
  public void __dump(final JsonStreamWriter writer) {
    writer.injectJson(value.to_dynamic().json);
  }

  @Override
  public void __insert(final JsonStreamReader reader) {
    backup = maker.get();
    backup.__ingest(reader);
    value = backup;
  }

  @Override
  public void __patch(JsonStreamReader reader) {
    JsonStreamWriter cloneOld = new JsonStreamWriter();
    value.__writeOut(cloneOld);
    value = maker.get();
    value.__ingest(new JsonStreamReader(cloneOld.toString()));
    value.__ingest(reader);
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
    return super.__memory() + backup.__memory() + value.__memory() + 16;
  }

  @Override
  public Ty get() {
    return value;
  }

  public Ty write() {
    __raiseDirty();
    return value;
  }

  @Override
  public void set(final Ty value) {
    this.value = value;
    __raiseDirty();
  }
}
