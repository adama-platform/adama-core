/**
 * MIT License
 * 
 * Copyright (C) 2021 - 2025 by Adama Platform Engineering, LLC
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ape.web.firewall;

import org.junit.Assert;
import org.junit.Test;

public class WebRequestShieldTests {
  @Test
  public void coverage() {
    Assert.assertFalse(WebRequestShield.block("/.well-known"));
    Assert.assertTrue(WebRequestShield.block("/.git/"));
    Assert.assertTrue(WebRequestShield.block("/CSS/"));
    Assert.assertTrue(WebRequestShield.block("/Portal/"));
    Assert.assertTrue(WebRequestShield.block("/actuator/"));
    Assert.assertTrue(WebRequestShield.block("/api/"));
    Assert.assertTrue(WebRequestShield.block("/cgi-bin/"));
    Assert.assertTrue(WebRequestShield.block("/docs/"));
    Assert.assertTrue(WebRequestShield.block("/ecp/"));
    Assert.assertTrue(WebRequestShield.block("/owa/"));
    Assert.assertTrue(WebRequestShield.block("/scripts/"));
    Assert.assertTrue(WebRequestShield.block("/vendor/"));
    Assert.assertFalse(WebRequestShield.block("/my/name/is/ninja/"));
    Assert.assertTrue(WebRequestShield.block("/d/"));
    Assert.assertTrue(WebRequestShield.block("/portal/"));
    Assert.assertTrue(WebRequestShield.block("/remote/"));
    Assert.assertTrue(WebRequestShield.block("/.aws/"));
  }
}
