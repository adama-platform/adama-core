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
package ape.runtime.sys.web.partial;

import ape.runtime.json.JsonStreamReader;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.sys.web.WebContext;
import ape.runtime.sys.web.WebDelete;
import org.junit.Assert;
import org.junit.Test;

public class WebDeletePartialTests {
  @Test
  public void nulls() {
    WebContext context = new WebContext(NtPrincipal.NO_ONE, "origin", "ip");
    Assert.assertNull(WebDeletePartial.read(new JsonStreamReader("{}")).convert(context));
    Assert.assertNull(WebDeletePartial.read(new JsonStreamReader("{\"junk\":[]}")).convert(context));
        Assert.assertNull(WebDeletePartial.read(new JsonStreamReader("{\"uri\":\"uri\",\"headers\":{}}")).convert(context));
    Assert.assertNull(WebDeletePartial.read(new JsonStreamReader("{\"uri\":\"uri\",\"headers\":\"cake\",\"parameters\":{}}")).convert(context));
    Assert.assertNotNull(WebDeletePartial.read(new JsonStreamReader("{\"uri\":\"uri\",\"headers\":{},\"parameters\":{}}")).convert(context));
  }

  @Test
  public void happy() {
    WebContext context = new WebContext(NtPrincipal.NO_ONE, "origin", "ip");
    WebDelete put = (WebDelete) WebDeletePartial.read(new JsonStreamReader("{\"uri\":\"uri\",\"headers\":{\"x\":\"y\"},\"parameters\":{},\"bodyJson\":\"body\"}")).convert(context);
    Assert.assertNotNull(put);
    Assert.assertEquals("uri", put.uri);
    Assert.assertEquals("y", put.headers.get("x"));
    Assert.assertEquals("{}", put.parameters.json);
  }

}
