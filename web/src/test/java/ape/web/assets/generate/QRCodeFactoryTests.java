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
package ape.web.assets.generate;

import org.junit.Assert;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

public class QRCodeFactoryTests {

  @Test
  public void generateBasic() throws Exception {
    byte[] png = QRCodeFactory.generate("https://example.com", 256);
    Assert.assertNotNull(png);
    Assert.assertTrue(png.length > 0);
    // verify it's a valid PNG that can be read back
    BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
    Assert.assertNotNull(image);
    Assert.assertEquals(256, image.getWidth());
    Assert.assertEquals(256, image.getHeight());
  }

  @Test
  public void generateSmall() throws Exception {
    byte[] png = QRCodeFactory.generate("https://example.com", 64);
    Assert.assertNotNull(png);
    BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
    Assert.assertEquals(64, image.getWidth());
    Assert.assertEquals(64, image.getHeight());
  }
}
