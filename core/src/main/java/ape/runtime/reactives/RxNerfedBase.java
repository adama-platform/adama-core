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

import ape.runtime.contracts.RxParent;
import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;

/** a lame version of RxBase to leverage RxBase without any associated data persisted */
public class RxNerfedBase extends RxBase {
  protected RxNerfedBase(RxParent __parent) {
    super(__parent);
  }

  @Override
  public void __commit(String name, JsonStreamWriter forwardDelta, JsonStreamWriter reverseDelta) {}

  @Override
  public void __dump(JsonStreamWriter writer) {}

  @Override
  public void __insert(JsonStreamReader reader) {
    reader.skipValue();
  }

  @Override
  public void __patch(JsonStreamReader reader) {
    reader.skipValue();
  }

  @Override
  public void __revert() {}
}
