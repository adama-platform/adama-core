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
import ape.runtime.natives.NtResult;

import java.util.function.Supplier;

/** a maybe wrapper that will respect privacy and sends state to client only on changes */
public class DResult<dTy extends DeltaNode> implements DeltaNode {
  private dTy cache;
  private Boolean failed;
  private String message;
  private Integer code;

  public DResult() {
    this.cache = null;
    this.failed = null;
    this.message = null;
    this.code = null;
  }

  /** get or make the cached delta (see CodeGenDeltaClass) */
  public dTy get(final Supplier<dTy> maker) {
    if (cache == null) {
      cache = maker.get();
    }
    return cache;
  }

  /** start showing the result(s) */
  public PrivateLazyDeltaWriter show(NtResult<?> result, PrivateLazyDeltaWriter writer) {
    final var obj = writer.planObject();
    if (failed == null || result.failed() != failed) {
      obj.planField("failed").writeBool(result.failed());
      this.failed = result.failed();
    }
    if (message == null || !result.message().equals(message)) {
      obj.planField("message").writeString(result.message());
      this.message = result.message();
    }
    if (code == null || result.code() != code) {
      obj.planField("code").writeInt(result.code());
      this.code = result.code();
    }
    return obj.planField("result");
  }

  /** the maybe is either no longer visible (was made private or isn't present) */
  public void hide(final PrivateLazyDeltaWriter writer) {
    if (cache != null) {
      writer.writeNull();
      cache = null;
    }
  }

  @Override
  public void clear() {
    cache = null;
  }

  /** memory usage */
  @Override
  public long __memory() {
    return 40 + (cache != null ? cache.__memory() : 0);
  }
}
