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
package ape.runtime.delta;

import ape.runtime.contracts.DeltaNode;
import ape.runtime.json.PrivateLazyDeltaWriter;
import ape.runtime.natives.NtDateTime;

/** a datetime that respects privacy */
public class DDateTime implements DeltaNode {
  private NtDateTime prior;

  public DDateTime() {
    prior = null;
  }

  /** the double is no longer visible (was made private) */
  public void hide(final PrivateLazyDeltaWriter writer) {
    if (prior != null) {
      writer.writeNull();
      prior = null;
    }
  }

  @Override
  public void clear() {
    prior = null;
  }

  /** memory usage */
  @Override
  public long __memory() {
    return (prior != null ? prior.memory() : 0) + 32;
  }

  /** the double is visible, so show changes */
  public void show(final NtDateTime value, final PrivateLazyDeltaWriter writer) {
    if (!value.equals(prior)) {
      writer.writeNtDateTime(value);
    }
    prior = value;
  }
}
