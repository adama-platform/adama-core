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
import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.natives.NtMatrix3;

/** a reactive 3x3 matrix */
public class RxMatrix3 extends RxBase implements CanGetAndSet<NtMatrix3> {
    protected NtMatrix3 backup;
    protected NtMatrix3 value;

    public RxMatrix3(final RxParent owner, final NtMatrix3 value) {
        super(owner);
        backup = value;
        this.value = value;
    }

    @Override
    public void __commit(String name, JsonStreamWriter forwardDelta, JsonStreamWriter reverseDelta) {
        if (__isDirty()) {
            forwardDelta.writeObjectFieldIntro(name);
            forwardDelta.writeNtMatrix3(value);
            reverseDelta.writeObjectFieldIntro(name);
            reverseDelta.writeNtMatrix3(backup);
            backup = value;
            __lowerDirtyCommit();
        }
    }

    @Override
    public void __dump(final JsonStreamWriter writer) {
        writer.writeNtMatrix3(value);
    }

    @Override
    public void __insert(final JsonStreamReader reader) {
        backup = reader.readNtMatrix3();
        value = backup;
    }

    @Override
    public void __patch(JsonStreamReader reader) {
        set(reader.readNtMatrix3());
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
            return super.__memory() + backup.memory() + value.memory() + 32L;
    }

    @Override
    public NtMatrix3 get() {
        return value;
    }

    @Override
    public void set(final NtMatrix3 value) {
        if (this.value.equals(value)) {
            return;
        }
        this.value = value;
        __raiseDirty();
    }
}
