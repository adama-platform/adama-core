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
import ape.runtime.json.JsonStreamWriter;

/**
 * Reactive string optimized for pre-validated/escaped content.
 * Extends RxString but uses writeFastString() during serialization,
 * which skips JSON escape processing. Use for strings known to be
 * safe (no control characters or quotes needing escape), providing
 * faster JSON output at the cost of correctness guarantees.
 */
public class RxFastString extends RxString {
  public RxFastString(final RxParent owner, final String value) {
    super(owner, value);
  }

  @Override
  public void __commit(String name, JsonStreamWriter forwardDelta, JsonStreamWriter reverseDelta) {
    if (__isDirty()) {
      forwardDelta.writeObjectFieldIntro(name);
      forwardDelta.writeFastString(value);
      reverseDelta.writeObjectFieldIntro(name);
      reverseDelta.writeFastString(backup);
      backup = value;
      __lowerDirtyCommit();
    }
  }

  @Override
  public void __dump(final JsonStreamWriter writer) {
    writer.writeFastString(value);
  }
}
