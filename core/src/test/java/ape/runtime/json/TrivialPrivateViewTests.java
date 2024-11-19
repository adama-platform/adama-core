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
package ape.runtime.json;

import ape.runtime.contracts.Perspective;
import ape.runtime.natives.NtPrincipal;
import org.junit.Assert;
import org.junit.Test;

public class TrivialPrivateViewTests {
  @Test
  public void flow() {
    TrivialPrivateView tpv = new TrivialPrivateView(1, NtPrincipal.NO_ONE, Perspective.DEAD);
    tpv.ingest(new JsonStreamReader("{}"));
    tpv.dumpViewer(new JsonStreamWriter());
    Assert.assertEquals(1, tpv.getViewId());
    Assert.assertEquals(1024, tpv.memory());
    tpv.update(new JsonStreamWriter());
    Assert.assertFalse(tpv.hasRead());
  }
}
