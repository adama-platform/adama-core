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
package ape.common.capacity;

import org.junit.Assert;
import org.junit.Test;

public class LoadEventTests {
  @Test
  public void flow() {
    StringBuilder sb = new StringBuilder();
    LoadEvent le = new LoadEvent("test", 0.5, (b) -> sb.append(b ? "START" : "STOP"));
    le.at(.4);
    le.at(.4);
    le.at(.4);
    le.at(.6);
    le.at(.6);
    le.at(.6);
    le.at(.4);
    le.at(.4);
    le.at(.4);
    le.at(.6);
    le.at(.4);
    Assert.assertEquals("STARTSTOPSTARTSTOP", sb.toString());
  }
}
