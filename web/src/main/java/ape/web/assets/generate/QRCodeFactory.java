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

import io.nayuki.qrcodegen.QrCode;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

/** Generates QR code PNG images on the fly from a URL string. */
public class QRCodeFactory {

  /** Absolute upper bound on generated QR image dimensions in pixels. */
  private static final int MAX_QR_SIZE = 1024;

  /** Generate a QR code PNG for the given URL at the specified pixel dimensions. */
  public static byte[] generate(String url, int sizeAsk) throws Exception {
    QrCode qr = QrCode.encodeText(url, QrCode.Ecc.MEDIUM);
    int qrSize = qr.size;
    int size = qrSize;
    if (qrSize / 4 <= sizeAsk && sizeAsk <= qrSize * 64) {
      size = sizeAsk;
    }
    // Enforce absolute upper bound to prevent excessive memory allocation
    size = Math.min(size, MAX_QR_SIZE);
    BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
    // scale: map each QR module to a proportional block of pixels
    double scaleX = (double) size / qrSize;
    double scaleY = (double) size / qrSize;
    for (int y = 0; y < size; y++) {
      for (int x = 0; x < size; x++) {
        int moduleX = (int) (x / scaleX);
        int moduleY = (int) (y / scaleY);
        // clamp to valid range
        if (moduleX >= qrSize) moduleX = qrSize - 1;
        if (moduleY >= qrSize) moduleY = qrSize - 1;
        boolean dark = qr.getModule(moduleX, moduleY);
        image.setRGB(x, y, dark ? 0x000000 : 0xFFFFFF);
      }
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(image, "png", baos);
    return baos.toByteArray();
  }
}
