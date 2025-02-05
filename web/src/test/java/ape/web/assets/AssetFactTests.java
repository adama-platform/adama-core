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
package ape.web.assets;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class AssetFactTests {
  @Test
  public void just_bytes() throws Exception {
    AssetUploadBody body = AssetUploadBody.WRAP("XYZ".getBytes(StandardCharsets.UTF_8));
    AssetFact fact = AssetFact.of(body);
    Assert.assertEquals(3, fact.size);
    Assert.assertEquals("5lB11VD5tb+ZkvodcaExvg==", fact.md5);
    Assert.assertEquals("Fl8D+bwAJF//H6j+vvK8eG7KPhF3O4j3BdiLo8zCa2OvtTUCkBO/aCYC/8DqqrSC", fact.sha384);
  }

  @Test
  public void just_file() throws Exception {
    File file = File.createTempFile("ADAMATEST_", "temp_file");
    try {
      Files.writeString(file.toPath(), "ABCDEF");
      AssetUploadBody body = AssetUploadBody.WRAP(file);
      AssetFact fact = AssetFact.of(body);
      Assert.assertEquals(6, fact.size);
      Assert.assertEquals("iCekESKlAouYCMe/hLn89g==", fact.md5);
      Assert.assertEquals("OeDMGwK4wIK2RkPNyuDmGA5GDh9xeCTHixf5RsmGTJDATFUGeyMeabCtW9GdEqBl", fact.sha384);
    } finally {
      file.delete();
    }
  }
}
