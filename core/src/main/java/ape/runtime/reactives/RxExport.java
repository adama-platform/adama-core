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

import ape.common.ErrorCodeException;
import ape.common.SimpleCancel;
import ape.common.Stream;
import ape.runtime.contracts.RxParent;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.natives.NtMessageBase;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.sys.CoreRequestContext;

import java.util.Set;

/** Ability to export a message as a stream */
public abstract class RxExport<Viewer> implements RxParent, SimpleCancel {
  public final RxGuard __guard;
  private boolean __alive;
  protected final CoreRequestContext __context;
  protected final NtPrincipal __who;
  protected final Viewer __viewer;
  private Stream<String> __stream;
  private boolean __dirty;

  public RxExport(CoreRequestContext context, Viewer viewer, Stream<String> stream) {
    this.__guard = new RxGuard(this);
    this.__context = context;
    this.__viewer = viewer;
    this.__who = context.who;
    this.__stream = stream;
    this.__alive = true;
  }

  public void error(ErrorCodeException ex) {
    if (__alive) {
      __stream.failure(ex);
      __alive = false;
    }
  }

  public abstract NtMessageBase compute();

  @Override
  public void __raiseDirty() {
    __dirty = true;
  }

  @Override
  public boolean __isAlive() {
    return __alive;
  }

  public boolean __ping() {
    if (__alive) {
      if (__dirty) {
        deliver();
        __dirty = false;
      }
      return true;
    }
    return false;
  }

  public void deliver() {
    __guard.__lowerInvalid();
    JsonStreamWriter writer = new JsonStreamWriter();
    compute().__writeOut(writer);
    __stream.next(writer.toString());
  }

  @Override
  public void cancel() {
    __alive = false;
  }

  @Override
  public void __cost(int cost) {
    // transfer cost?
  }

  @Override
  public void __invalidateUp() {
    __raiseDirty();
  }

  @Override
  public void __settle(Set<Integer> viewers) {
  }
}
