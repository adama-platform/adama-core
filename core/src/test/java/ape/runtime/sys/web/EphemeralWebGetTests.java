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
package ape.runtime.sys.web;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.runtime.natives.NtDynamic;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.contracts.DelayParent;
import ape.runtime.remote.RxCache;
import org.junit.Test;

import java.util.TreeMap;

public class EphemeralWebGetTests {
  @Test
  public void trivial() {
    EphemeralWebGet ewg = new EphemeralWebGet(new RxCache(null, null), new WebGet(new WebContext(NtPrincipal.NO_ONE, "origin", "ip"), "uri", new TreeMap<>(), new NtDynamic("{}")), new Callback<WebResponse>() {
      @Override
      public void success(WebResponse value) {

      }

      @Override
      public void failure(ErrorCodeException ex) {

      }
    }, new DelayParent());
  }
}
