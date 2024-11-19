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
package ape.runtime.async;

import ape.runtime.exceptions.ComputeBlockedException;
import ape.runtime.natives.NtPrincipal;

/** represents a future which holds a value which */
public class SimpleFuture<T> {
  public final String channel;
  public final NtPrincipal who;
  private final T value;

  public SimpleFuture(final String channel, final NtPrincipal who, final T value) {
    this.channel = channel;
    this.who = who;
    this.value = value;
  }

  /** the code is asking for it now */
  public T await() throws ComputeBlockedException {
    if (value != null) {
      return value;
    }
    throw new ComputeBlockedException(who, channel);
  }

  /** does the value for the future exist in the moment at this time */
  public boolean exists() {
    return value != null;
  }
}
