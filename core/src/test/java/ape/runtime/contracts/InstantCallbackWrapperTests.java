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
package ape.runtime.contracts;

import ape.common.ErrorCodeException;
import org.junit.Assert;
import org.junit.Test;

public class InstantCallbackWrapperTests {
  @Test
  public void happy() {
    InstantCallbackWrapper wrapper = new InstantCallbackWrapper();
    Assert.assertNull(wrapper.convert());
    wrapper.success("xyz");
    Assert.assertEquals("xyz", wrapper.convert().result);
  }

  @Test
  public void sad() {
    InstantCallbackWrapper wrapper = new InstantCallbackWrapper();
    Assert.assertNull(wrapper.convert());
    wrapper.failure(new ErrorCodeException(-13));
    Assert.assertEquals(-13, (int) wrapper.convert().failureCode);
  }
}
