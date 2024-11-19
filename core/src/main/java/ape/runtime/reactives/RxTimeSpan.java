/*
* Adama Platform and Language
* Copyright (C) 2021 - 2025 by Adama Platform Engineering, LLC
* 
* This program is free software for non-commercial purposes: 
* you can redistribute it and/or modify it under the terms of the 
* GNU Affero General Public License as published by the Free Software Foundation,
* either version 3 of the License, or (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
* 
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package ape.runtime.reactives;

import ape.runtime.contracts.CanGetAndSet;
import ape.runtime.contracts.RxParent;
import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.natives.NtTimeSpan;

/** a reactive time span measured in seconds */
public class RxTimeSpan extends RxBase implements CanGetAndSet<NtTimeSpan>, Comparable<RxTimeSpan> {
  private NtTimeSpan backup;
  private NtTimeSpan value;

  public RxTimeSpan(final RxParent parent, final NtTimeSpan value) {
    super(parent);
    backup = value;
    this.value = value;
  }

  @Override
  public void __commit(String name, JsonStreamWriter forwardDelta, JsonStreamWriter reverseDelta) {
    if (__isDirty()) {
      forwardDelta.writeObjectFieldIntro(name);
      forwardDelta.writeNtTimeSpan(value);
      reverseDelta.writeObjectFieldIntro(name);
      reverseDelta.writeNtTimeSpan(backup);
      backup = value;
      __lowerDirtyCommit();
    }
  }

  @Override
  public void __dump(final JsonStreamWriter writer) {
    writer.writeNtTimeSpan(value);
  }

  @Override
  public void __insert(final JsonStreamReader reader) {
    backup = reader.readNtTimeSpan();
    value = backup;
  }

  @Override
  public void __patch(JsonStreamReader reader) {
    set(reader.readNtTimeSpan());
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
    return super.__memory() + backup.memory() + value.memory() + 16;
  }

  @Override
  public NtTimeSpan get() {
    return value;
  }

  @Override
  public void set(final NtTimeSpan value) {
    this.value = value;
    __raiseDirty();
  }

  @Override
  public int compareTo(RxTimeSpan o) {
    return value.compareTo(o.value);
  }
}
