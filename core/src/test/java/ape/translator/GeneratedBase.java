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
package ape.translator;

import ape.support.testgen.TestForge;
import org.junit.Assert;

import java.io.File;

public class GeneratedBase {
  public String generateTestOutput(boolean emission, String className, String filename) {
    File file = new File(filename);
    Assert.assertTrue(file.exists());
    Assert.assertTrue(file.getParentFile().isDirectory());
    try {
      String output =
          TestForge.forge(emission, className, file.toPath(), file.getParentFile().toPath());
      String[] lines = output.split("\n");
      for (int k = 0; k < lines.length; k++) {
        lines[k] = lines[k].stripTrailing();
      }
      StringBuilder reconstructed = new StringBuilder();
      reconstructed.append(lines[0]);
      for (int k = 1; k < lines.length; k++) {
        reconstructed.append("\n").append(lines[k]);
      }
      return reconstructed.toString();
    } catch (Throwable t) {
      t.printStackTrace();
      return "FailedDueToException";
    }
  }

  public void assertTODOFree(String live) {
    Assert.assertFalse(live.contains("TODO"));
  }

  public void assertExceptionFree(String live) {
    if (live.contains("!!EXCEPTION!!")) {
      Assert.fail("contains an exception: " + live);
    }
    if (live.contains("FAILED PRIVATE VIEW DUE TO:180978")) {
      Assert.fail("failed due to private view having an exception!");
    }
  }

  public void assertGoodWillHappy(String live) {
    Assert.assertFalse(live.contains("GOODWILL EXHAUSTED"));
  }

  public void assertEmissionGood(String live) {
    Assert.assertTrue(live.contains("Emission Success, Yay"));
  }

  public void assertLivePass(String live) {
    if (!(live.endsWith("Success"))) {
      Assert.fail("Does not end with 'Success':" + live);
    }
  }

  public void assertNoFormatException(String live) {
    Assert.assertFalse(live.contains("FORMAT-EXCEPTION"));
  }

  public void assertLiveFail(String live) {
    Assert.assertTrue(!live.endsWith("Success"));
  }

  public void assertStable(String live, StringBuilder gold) {
    Assert.assertEquals(gold.toString(), live);
  }

  public void assertNotTerribleLineNumbers(String live) {
    if (live.contains("2147483647")) {
      System.err.println(live);
    }
    Assert.assertFalse(live.contains("2147483647"));
  }
}
