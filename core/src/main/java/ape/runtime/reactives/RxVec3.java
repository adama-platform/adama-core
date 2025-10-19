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
import ape.runtime.natives.NtVec3;

/** a reactive 3d vector */
public class RxVec3 extends RxBase implements CanGetAndSet<NtVec3>, Comparable<RxVec3> {
    protected NtVec3 backup;
    protected NtVec3 value;

    public RxVec3(final RxParent owner, final NtVec3 value) {
        super(owner);
        backup = value;
        this.value = value;
    }

    @Override
    public void __commit(String name, JsonStreamWriter forwardDelta, JsonStreamWriter reverseDelta) {
        if (__isDirty()) {
            forwardDelta.writeObjectFieldIntro(name);
            forwardDelta.writeNtVec3(value);
            reverseDelta.writeObjectFieldIntro(name);
            reverseDelta.writeNtVec3(backup);
            backup = value;
            __lowerDirtyCommit();
        }
    }

    @Override
    public void __dump(final JsonStreamWriter writer) {
        writer.writeNtVec3(value);
    }

    @Override
    public void __insert(final JsonStreamReader reader) {
        backup = reader.readNtVec3();
        value = backup;
    }

    @Override
    public void __patch(JsonStreamReader reader) {
        set(reader.readNtVec3());
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
    public NtVec3 get() {
        return value;
    }

    @Override
    public void set(final NtVec3 value) {
        if (this.value.equals(value)) {
            return;
        }
        this.value = value;
        __raiseDirty();
    }

    @Override
    public int compareTo(RxVec3 o) {
        return value.compareTo(o.value);
    }
}
