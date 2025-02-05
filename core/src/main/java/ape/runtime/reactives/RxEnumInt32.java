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

import ape.runtime.contracts.RxParent;
import ape.runtime.json.JsonStreamReader;

import java.util.function.Function;

/** a reactive 32-bit integer (int) used by enums with the ability to correct invalid values */
public class RxEnumInt32 extends RxInt32 {
  private final Function<Integer, Integer> fixer;

  public RxEnumInt32(RxParent parent, int value, Function<Integer, Integer> fixer) {
    super(parent, value);
    this.fixer = fixer;
  }

  @Override
  public void __insert(JsonStreamReader reader) {
    super.__insert(reader);
    this.backup = fixer.apply(this.backup);
    this.value = fixer.apply(this.value);
  }

  @Override
  public void __patch(JsonStreamReader reader) {
    set(reader.readInteger());
  }

  @Override
  public void forceSet(int id) {
    super.forceSet(fixer.apply(id));
  }

  @Override
  public void set(Integer value) {
    super.set(fixer.apply(value));
  }
}
