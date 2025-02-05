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
package ape.web.assets.transforms;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;

public class ImageTransformTests {
  private static File imageRoot() {
    File file = new File("images");
    if (file.exists() && file.isDirectory()) {
      return file;
    }
    throw new UnsupportedOperationException("failed to find images directory:" + file.getAbsolutePath());
  }

  private static void transformForAudit(String file, String transform) throws Exception {
    ImageTransform it = new ImageTransform("png", transform);
    FileInputStream input = new FileInputStream(new File(imageRoot(), file));
    File tempDir = new File(imageRoot(), "output");
    tempDir.mkdirs();
    try {
      File output = new File(tempDir, "result_" + transform + "_" + file);
      if (output.exists()) {
        output.delete();
      }
      it.execute(input, output);
    } finally {
      input.close();
    }
  }

  private void battery(String file) throws Exception{
    transformForAudit(file, "gray");
    transformForAudit(file, "w100_h50_fc");
    transformForAudit(file, "w50_h100_fc");
    transformForAudit(file, "w50_sq");
    transformForAudit(file, "h50_sq");
  }

  @Test
  public void square() throws Exception{
    battery("square.png");
  }

  @Test
  public void tall() throws Exception{
    battery("tall.png");
  }

  @Test
  public void wide() throws Exception{
    battery("wide.png");
  }

  // test runner for validating things
  /*
  @Test
  public void flow_new_stuff() throws Exception{
    ImageTransform it = new ImageTransform("png", "w500_sq_gray");
    FileInputStream input = new FileInputStream(new File(imageRoot(), "wide.png"));
    try {
      File output = new File(imageRoot(), "bleeding.png");
      try {
        it.execute(input, output);
      } finally {
        // output.delete();
      }
    } finally {
      input.close();
    }
  }
  */
}
