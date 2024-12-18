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
package ape.runtime.text.search;

import org.junit.Assert;
import org.junit.Test;

import java.util.TreeSet;

public class CompressedTrieIndexTests {
  @Test
  public void flow() {
    CompressedTrieIndex index = new CompressedTrieIndex();
    index.map("theword", 42);
    index.map("theword", 23);
    TreeSet<Integer> keys = index.keysOf("theword");
    Assert.assertTrue(keys.contains(42));
    Assert.assertTrue(keys.contains(23));
    Assert.assertFalse(keys.contains(-1));
    index.unmap("theword", 42);
    index.unmap("theword", 23);
    Assert.assertFalse(keys.contains(42));
    Assert.assertFalse(keys.contains(23));
  }
}
