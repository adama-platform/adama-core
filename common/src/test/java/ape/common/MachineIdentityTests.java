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
package ape.common;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

public class MachineIdentityTests {
  @Test
  public void io() throws Exception {
    File trust = File.createTempFile("ADAMATEST_", "trust");
    File cert = File.createTempFile("ADAMATEST_", "cert");
    File key = File.createTempFile("ADAMATEST_", "key");
    File json = File.createTempFile("ADAMATEST_", "identity");
    Files.writeString(trust.toPath(), "X");
    Files.writeString(cert.toPath(), "Y");
    Files.writeString(key.toPath(), "Z");
    String str = MachineIdentity.convertToJson("x", trust, cert, key);
    Files.writeString(json.toPath(), str);
    MachineIdentity identity = MachineIdentity.fromFile(json.getAbsolutePath());
    Assert.assertEquals("x", identity.ip);
    Assert.assertEquals("X", of(identity.getTrust()));
    Assert.assertEquals("Y", of(identity.getCert()));
    Assert.assertEquals("Z", of(identity.getKey()));
  }

  public String of(InputStream stream) throws Exception {
    StringBuilder sb = new StringBuilder();
    byte[] chunk = new byte[4096];
    int rd;
    while ((rd = stream.read(chunk)) > 0) {
      sb.append(new String(chunk, 0, rd));
    }
    return sb.toString();
  }

  @Test
  public void coverage() throws Exception {
    try {
      new MachineIdentity("{}");
      Assert.fail();
    } catch (Exception ex) {
      Assert.assertTrue(ex.getMessage().contains("ip"));
    }
    try {
      new MachineIdentity("{\"ip\":\"x\"}");
      Assert.fail();
    } catch (Exception ex) {
      Assert.assertTrue(ex.getMessage().contains("key"));
    }
    try {
      new MachineIdentity("{\"ip\":\"x\",\"key\":\"y\"}");
      Assert.fail();
    } catch (Exception ex) {
      Assert.assertTrue(ex.getMessage().contains("cert"));
    }
    try {
      new MachineIdentity("{\"ip\":\"x\",\"key\":\"y\",\"cert\":\"z\"}");
      Assert.fail();
    } catch (Exception ex) {
      Assert.assertTrue(ex.getMessage().contains("trust"));
    }
    MachineIdentity identity = new MachineIdentity("{\"ip\":\"x\",\"key\":\"y\",\"cert\":\"z\",\"trust\":\"w\"}");
    identity.getCert();
    identity.getKey();
    identity.getCert();
    Assert.assertEquals("x", identity.ip);
  }
}
