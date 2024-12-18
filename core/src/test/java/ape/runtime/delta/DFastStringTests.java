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

import ape.runtime.json.JsonStreamWriter;
import ape.runtime.json.PrivateLazyDeltaWriter;
import ape.runtime.natives.NtPrincipal;
import org.junit.Assert;
import org.junit.Test;

public class DFastStringTests {
  @Test
  public void flow() {
    final var db = new DFastString();
    final var stream = new JsonStreamWriter();
    final var writer = PrivateLazyDeltaWriter.bind(NtPrincipal.NO_ONE, stream, null, 0);
    db.show("a", writer);
    db.show("a", writer);
    db.show("b", writer);
    db.show("b", writer);
    db.show("c", writer);
    db.show("c", writer);
    db.hide(writer);
    db.hide(writer);
    db.show("d", writer);
    db.show("d", writer);
    db.show("e", writer);
    db.show("e", writer);
    Assert.assertEquals("\"a\"\"b\"\"c\"null\"d\"\"e\"", stream.toString());
    Assert.assertEquals(42, db.__memory());
    db.clear();
  }
}
