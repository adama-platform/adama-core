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
package ape.caravan.entries;

import io.netty.buffer.ByteBuf;
import ape.caravan.contracts.WALEntry;
import ape.caravan.index.Heap;
import ape.caravan.index.Index;
import ape.caravan.index.KeyMap;

public class OrganizationSnapshot implements WALEntry {
  private final Heap heap;
  private final Index index;
  private final KeyMap keymap;

  public OrganizationSnapshot(Heap heap, Index index, KeyMap keymap) {
    this.heap = heap;
    this.index = index;
    this.keymap = keymap;
  }

  public static void populateAfterTypeId(ByteBuf buf, Heap heap, Index index, KeyMap keymap) {
    heap.load(buf);
    index.load(buf);
    keymap.load(buf);
  }

  @Override
  public void write(ByteBuf buf) {
    buf.writeByte(0x57);
    heap.snapshot(buf);
    index.snapshot(buf);
    keymap.snapshot(buf);
  }
}
