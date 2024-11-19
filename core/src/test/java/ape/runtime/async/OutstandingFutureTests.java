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

import ape.runtime.natives.NtPrincipal;
import org.junit.Assert;
import org.junit.Test;

public class OutstandingFutureTests {
  @Test
  public void flow() {
    final var fut = new OutstandingFuture(1, "ch", NtPrincipal.NO_ONE);
    Assert.assertFalse(fut.test("ch", NtPrincipal.NO_ONE));
    Assert.assertTrue(fut.outstanding());
    fut.take();
    Assert.assertFalse(fut.outstanding());
    fut.reset();
    Assert.assertFalse(fut.outstanding());
    Assert.assertTrue(fut.test("ch", NtPrincipal.NO_ONE));
    Assert.assertFalse(fut.test("ch2", NtPrincipal.NO_ONE));
    Assert.assertTrue(fut.outstanding());
    Assert.assertFalse(fut.test("ch", NtPrincipal.NO_ONE));
    Assert.assertFalse(fut.test("ch2", NtPrincipal.NO_ONE));
  }
}
