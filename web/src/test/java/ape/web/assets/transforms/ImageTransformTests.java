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

import ape.common.ConfigObject;
import ape.common.Json;
import ape.web.service.WebConfig;
import org.junit.Assert;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class ImageTransformTests {
  private static WebConfig defaultConfig() {
    return new WebConfig(new ConfigObject(Json.newJsonObject()));
  }

  private static BufferedImage createTestImage(int w, int h, int type) {
    BufferedImage img = new BufferedImage(w, h, type);
    Graphics2D g = img.createGraphics();
    try {
      // Red quadrant top-left
      g.setColor(Color.RED);
      g.fillRect(0, 0, w / 2, h / 2);
      // Green quadrant top-right
      g.setColor(Color.GREEN);
      g.fillRect(w / 2, 0, w / 2, h / 2);
      // Blue quadrant bottom-left
      g.setColor(Color.BLUE);
      g.fillRect(0, h / 2, w / 2, h / 2);
      // Yellow quadrant bottom-right
      g.setColor(Color.YELLOW);
      g.fillRect(w / 2, h / 2, w / 2, h / 2);
    } finally {
      g.dispose();
    }
    return img;
  }

  private static InputStream toInputStream(BufferedImage img, String format) throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(img, format, baos);
    return new ByteArrayInputStream(baos.toByteArray());
  }

  private static BufferedImage executeTransform(String format, String args, BufferedImage src) throws Exception {
    ImageTransform it = new ImageTransform(defaultConfig(), format, args);
    File tempFile = File.createTempFile("img_test_", "." + format);
    tempFile.deleteOnExit();
    try {
      it.execute(toInputStream(src, format), tempFile);
      return ImageIO.read(tempFile);
    } finally {
      tempFile.delete();
    }
  }

  private static File executeTransformToFile(String format, String args, BufferedImage src, String inputFormat) throws Exception {
    ImageTransform it = new ImageTransform(defaultConfig(), inputFormat, args);
    File tempFile = File.createTempFile("img_test_", ".out");
    tempFile.deleteOnExit();
    it.execute(toInputStream(src, inputFormat), tempFile);
    return tempFile;
  }

  // --- Output content type tests ---

  @Test
  public void outputContentType_png() {
    ImageTransform it = new ImageTransform(defaultConfig(), "png", "w100");
    Assert.assertEquals("image/png", it.outputContentType());
  }

  @Test
  public void outputContentType_jpg() {
    ImageTransform it = new ImageTransform(defaultConfig(), "jpg", "w100");
    Assert.assertEquals("image/jpeg", it.outputContentType());
  }

  @Test
  public void outputContentType_gif() {
    ImageTransform it = new ImageTransform(defaultConfig(), "gif", "w100");
    Assert.assertEquals("image/gif", it.outputContentType());
  }

  @Test
  public void outputContentType_formatConversion_topng() {
    ImageTransform it = new ImageTransform(defaultConfig(), "jpg", "topng");
    Assert.assertEquals("image/png", it.outputContentType());
  }

  @Test
  public void outputContentType_formatConversion_tojpg() {
    ImageTransform it = new ImageTransform(defaultConfig(), "png", "tojpg");
    Assert.assertEquals("image/jpeg", it.outputContentType());
  }

  @Test
  public void outputContentType_formatConversion_togif() {
    ImageTransform it = new ImageTransform(defaultConfig(), "png", "togif");
    Assert.assertEquals("image/gif", it.outputContentType());
  }

  // --- Resize algorithm tests ---

  @Test
  public void resize_squish() throws Exception {
    BufferedImage src = createTestImage(200, 100, BufferedImage.TYPE_INT_RGB);
    BufferedImage result = executeTransform("png", "w50_h80_sq", src);
    Assert.assertEquals(50, result.getWidth());
    Assert.assertEquals(80, result.getHeight());
  }

  @Test
  public void resize_fitCenter() throws Exception {
    BufferedImage src = createTestImage(200, 100, BufferedImage.TYPE_INT_RGB);
    BufferedImage result = executeTransform("png", "w100_h100_fc", src);
    Assert.assertEquals(100, result.getWidth());
    Assert.assertEquals(100, result.getHeight());
  }

  @Test
  public void resize_crop() throws Exception {
    BufferedImage src = createTestImage(200, 100, BufferedImage.TYPE_INT_RGB);
    BufferedImage result = executeTransform("png", "w50_h50_crop", src);
    Assert.assertEquals(50, result.getWidth());
    Assert.assertEquals(50, result.getHeight());
  }

  @Test
  public void resize_cover() throws Exception {
    BufferedImage src = createTestImage(200, 100, BufferedImage.TYPE_INT_RGB);
    BufferedImage result = executeTransform("png", "w50_h50_cover", src);
    Assert.assertEquals(50, result.getWidth());
    Assert.assertEquals(50, result.getHeight());
  }

  @Test
  public void resize_band() throws Exception {
    BufferedImage src = createTestImage(200, 200, BufferedImage.TYPE_INT_RGB);
    BufferedImage result = executeTransform("png", "w100_h50_band", src);
    Assert.assertEquals(100, result.getWidth());
    Assert.assertEquals(50, result.getHeight());
  }

  @Test
  public void resize_onlyWidth() throws Exception {
    BufferedImage src = createTestImage(200, 100, BufferedImage.TYPE_INT_RGB);
    BufferedImage result = executeTransform("png", "w100", src);
    Assert.assertEquals(100, result.getWidth());
    Assert.assertEquals(50, result.getHeight());
  }

  @Test
  public void resize_onlyHeight() throws Exception {
    BufferedImage src = createTestImage(200, 100, BufferedImage.TYPE_INT_RGB);
    BufferedImage result = executeTransform("png", "h50", src);
    Assert.assertEquals(100, result.getWidth());
    Assert.assertEquals(50, result.getHeight());
  }

  @Test
  public void resize_noDimensions() throws Exception {
    BufferedImage src = createTestImage(200, 100, BufferedImage.TYPE_INT_RGB);
    BufferedImage result = executeTransform("png", "fc", src);
    Assert.assertEquals(200, result.getWidth());
    Assert.assertEquals(100, result.getHeight());
  }

  // --- Grayscale tests ---

  @Test
  public void grayscale_rgb() throws Exception {
    BufferedImage src = createTestImage(100, 100, BufferedImage.TYPE_INT_RGB);
    BufferedImage result = executeTransform("png", "gray", src);
    Assert.assertNotNull(result);
    Assert.assertEquals(100, result.getWidth());
    // Verify a pixel is grayscale (R==G==B)
    int rgb = result.getRGB(25, 25); // should be in the red quadrant, now gray
    int r = (rgb >> 16) & 0xFF;
    int g = (rgb >> 8) & 0xFF;
    int b = rgb & 0xFF;
    Assert.assertEquals(r, g);
    Assert.assertEquals(g, b);
  }

  @Test
  public void grayscale_rgba() throws Exception {
    BufferedImage src = createTestImage(100, 100, BufferedImage.TYPE_INT_ARGB);
    BufferedImage result = executeTransform("png", "gray", src);
    Assert.assertNotNull(result);
    Assert.assertEquals(100, result.getWidth());
    Assert.assertEquals(100, result.getHeight());
  }

  // --- Filter tests ---

  @Test
  public void filter_sepia() throws Exception {
    BufferedImage src = createTestImage(100, 100, BufferedImage.TYPE_INT_RGB);
    BufferedImage result = executeTransform("png", "sepia", src);
    Assert.assertNotNull(result);
    Assert.assertEquals(100, result.getWidth());
  }

  @Test
  public void filter_invert() throws Exception {
    BufferedImage src = createTestImage(100, 100, BufferedImage.TYPE_INT_RGB);
    // Get original pixel
    int origRgb = src.getRGB(25, 25);
    BufferedImage result = executeTransform("png", "invert", src);
    int newRgb = result.getRGB(25, 25);
    // Inverted pixel should differ from original
    Assert.assertNotEquals(origRgb, newRgb);
  }

  @Test
  public void filter_fliph() throws Exception {
    BufferedImage src = createTestImage(100, 100, BufferedImage.TYPE_INT_RGB);
    BufferedImage result = executeTransform("png", "fliph", src);
    Assert.assertNotNull(result);
    Assert.assertEquals(100, result.getWidth());
    Assert.assertEquals(100, result.getHeight());
  }

  @Test
  public void filter_flipv() throws Exception {
    BufferedImage src = createTestImage(100, 100, BufferedImage.TYPE_INT_RGB);
    BufferedImage result = executeTransform("png", "flipv", src);
    Assert.assertNotNull(result);
    Assert.assertEquals(100, result.getWidth());
    Assert.assertEquals(100, result.getHeight());
  }

  @Test
  public void filter_blur() throws Exception {
    BufferedImage src = createTestImage(100, 100, BufferedImage.TYPE_INT_RGB);
    BufferedImage result = executeTransform("png", "blur", src);
    Assert.assertNotNull(result);
    Assert.assertEquals(100, result.getWidth());
  }

  @Test
  public void filter_sharpen() throws Exception {
    BufferedImage src = createTestImage(100, 100, BufferedImage.TYPE_INT_RGB);
    BufferedImage result = executeTransform("png", "sharpen", src);
    Assert.assertNotNull(result);
    Assert.assertEquals(100, result.getWidth());
  }

  @Test
  public void filter_brightness_positive() throws Exception {
    BufferedImage src = createTestImage(100, 100, BufferedImage.TYPE_INT_RGB);
    BufferedImage result = executeTransform("png", "b50", src);
    Assert.assertNotNull(result);
    Assert.assertEquals(100, result.getWidth());
  }

  @Test
  public void filter_brightness_negative() throws Exception {
    BufferedImage src = createTestImage(100, 100, BufferedImage.TYPE_INT_RGB);
    BufferedImage result = executeTransform("png", "b-50", src);
    Assert.assertNotNull(result);
    Assert.assertEquals(100, result.getWidth());
  }

  @Test
  public void filter_brightness_rgba() throws Exception {
    BufferedImage src = createTestImage(100, 100, BufferedImage.TYPE_INT_ARGB);
    BufferedImage result = executeTransform("png", "b50", src);
    Assert.assertNotNull(result);
  }

  // --- Format conversion tests ---

  @Test
  public void formatConversion_pngToJpg() throws Exception {
    BufferedImage src = createTestImage(100, 100, BufferedImage.TYPE_INT_RGB);
    ImageTransform it = new ImageTransform(defaultConfig(), "png", "tojpg");
    Assert.assertEquals("image/jpeg", it.outputContentType());
    File tempFile = File.createTempFile("img_test_", ".jpg");
    tempFile.deleteOnExit();
    try {
      it.execute(toInputStream(src, "png"), tempFile);
      BufferedImage result = ImageIO.read(tempFile);
      Assert.assertNotNull(result);
      Assert.assertEquals(100, result.getWidth());
    } finally {
      tempFile.delete();
    }
  }

  @Test
  public void formatConversion_jpgToPng() throws Exception {
    BufferedImage src = createTestImage(100, 100, BufferedImage.TYPE_INT_RGB);
    ImageTransform it = new ImageTransform(defaultConfig(), "jpg", "topng");
    Assert.assertEquals("image/png", it.outputContentType());
    File tempFile = File.createTempFile("img_test_", ".png");
    tempFile.deleteOnExit();
    try {
      it.execute(toInputStream(src, "jpg"), tempFile);
      BufferedImage result = ImageIO.read(tempFile);
      Assert.assertNotNull(result);
      Assert.assertEquals(100, result.getWidth());
    } finally {
      tempFile.delete();
    }
  }

  @Test
  public void formatConversion_pngToGif() throws Exception {
    BufferedImage src = createTestImage(100, 100, BufferedImage.TYPE_INT_RGB);
    ImageTransform it = new ImageTransform(defaultConfig(), "png", "togif");
    Assert.assertEquals("image/gif", it.outputContentType());
    File tempFile = File.createTempFile("img_test_", ".gif");
    tempFile.deleteOnExit();
    try {
      it.execute(toInputStream(src, "png"), tempFile);
      BufferedImage result = ImageIO.read(tempFile);
      Assert.assertNotNull(result);
      Assert.assertEquals(100, result.getWidth());
    } finally {
      tempFile.delete();
    }
  }

  // --- JPEG quality tests ---

  @Test
  public void jpegQuality_lowVsHigh() throws Exception {
    BufferedImage src = createTestImage(200, 200, BufferedImage.TYPE_INT_RGB);
    File lowQ = executeTransformToFile("jpg", "q10_tojpg", src, "png");
    File highQ = executeTransformToFile("jpg", "q90_tojpg", src, "png");
    try {
      Assert.assertTrue("Low quality should produce smaller file", lowQ.length() < highQ.length());
    } finally {
      lowQ.delete();
      highQ.delete();
    }
  }

  @Test
  public void jpegQuality_withResize() throws Exception {
    BufferedImage src = createTestImage(200, 200, BufferedImage.TYPE_INT_RGB);
    ImageTransform it = new ImageTransform(defaultConfig(), "jpg", "w100_q50");
    File tempFile = File.createTempFile("img_test_", ".jpg");
    tempFile.deleteOnExit();
    try {
      it.execute(toInputStream(src, "jpg"), tempFile);
      BufferedImage result = ImageIO.read(tempFile);
      Assert.assertNotNull(result);
      Assert.assertEquals(100, result.getWidth());
    } finally {
      tempFile.delete();
    }
  }

  // --- GIF input test ---

  @Test
  public void gifInput() throws Exception {
    BufferedImage src = createTestImage(100, 100, BufferedImage.TYPE_INT_RGB);
    ImageTransform it = new ImageTransform(defaultConfig(), "gif", "w50");
    File tempFile = File.createTempFile("img_test_", ".gif");
    tempFile.deleteOnExit();
    try {
      it.execute(toInputStream(src, "gif"), tempFile);
      BufferedImage result = ImageIO.read(tempFile);
      Assert.assertNotNull(result);
      Assert.assertEquals(50, result.getWidth());
    } finally {
      tempFile.delete();
    }
  }

  // --- Interpolation hint tests ---

  @Test
  public void interpolation_bilinear() throws Exception {
    BufferedImage src = createTestImage(100, 100, BufferedImage.TYPE_INT_RGB);
    BufferedImage result = executeTransform("png", "w50_bl", src);
    Assert.assertEquals(50, result.getWidth());
  }

  @Test
  public void interpolation_bicubic() throws Exception {
    BufferedImage src = createTestImage(100, 100, BufferedImage.TYPE_INT_RGB);
    BufferedImage result = executeTransform("png", "w50_bc", src);
    Assert.assertEquals(50, result.getWidth());
  }

  @Test
  public void interpolation_nearestNeighbor() throws Exception {
    BufferedImage src = createTestImage(100, 100, BufferedImage.TYPE_INT_RGB);
    BufferedImage result = executeTransform("png", "w50_nn", src);
    Assert.assertEquals(50, result.getWidth());
  }

  // --- Combination tests ---

  @Test
  public void combo_grayscaleAndResize() throws Exception {
    BufferedImage src = createTestImage(200, 200, BufferedImage.TYPE_INT_RGB);
    BufferedImage result = executeTransform("png", "w100_gray", src);
    Assert.assertEquals(100, result.getWidth());
    int rgb = result.getRGB(25, 25);
    int r = (rgb >> 16) & 0xFF;
    int g = (rgb >> 8) & 0xFF;
    int b = rgb & 0xFF;
    Assert.assertEquals(r, g);
    Assert.assertEquals(g, b);
  }

  @Test
  public void combo_multipleFilters() throws Exception {
    BufferedImage src = createTestImage(100, 100, BufferedImage.TYPE_INT_RGB);
    BufferedImage result = executeTransform("png", "gray_fliph_blur", src);
    Assert.assertNotNull(result);
    Assert.assertEquals(100, result.getWidth());
  }

  @Test
  public void combo_resizeAndConvert() throws Exception {
    BufferedImage src = createTestImage(200, 200, BufferedImage.TYPE_INT_RGB);
    ImageTransform it = new ImageTransform(defaultConfig(), "png", "w100_tojpg");
    Assert.assertEquals("image/jpeg", it.outputContentType());
    File tempFile = File.createTempFile("img_test_", ".jpg");
    tempFile.deleteOnExit();
    try {
      it.execute(toInputStream(src, "png"), tempFile);
      BufferedImage result = ImageIO.read(tempFile);
      Assert.assertNotNull(result);
      Assert.assertEquals(100, result.getWidth());
    } finally {
      tempFile.delete();
    }
  }

  // --- Error handling tests ---

  @Test
  public void error_nullImage() {
    ImageTransform it = new ImageTransform(defaultConfig(), "png", "w100");
    File tempFile;
    try {
      tempFile = File.createTempFile("img_test_", ".png");
      tempFile.deleteOnExit();
      it.execute(new ByteArrayInputStream(new byte[]{0, 1, 2, 3}), tempFile);
      Assert.fail("Should have thrown exception for corrupt input");
    } catch (Exception ex) {
      Assert.assertTrue(ex.getMessage().contains("Failed to read image"));
    }
  }

  // --- Dimension clamping test ---

  @Test
  public void dimension_clamped() throws Exception {
    BufferedImage src = createTestImage(100, 100, BufferedImage.TYPE_INT_RGB);
    // Default max is 2048, so requesting 5000 should be clamped
    BufferedImage result = executeTransform("png", "w5000", src);
    Assert.assertTrue(result.getWidth() <= 2048);
  }

  // --- Invert with RGBA ---

  @Test
  public void invert_rgba() throws Exception {
    BufferedImage src = createTestImage(100, 100, BufferedImage.TYPE_INT_ARGB);
    BufferedImage result = executeTransform("png", "invert", src);
    Assert.assertNotNull(result);
  }

  // --- Sepia with RGBA ---

  @Test
  public void sepia_rgba() throws Exception {
    BufferedImage src = createTestImage(100, 100, BufferedImage.TYPE_INT_ARGB);
    BufferedImage result = executeTransform("png", "sepia", src);
    Assert.assertNotNull(result);
  }

  // --- Band with tall image ---

  @Test
  public void band_tallImage() throws Exception {
    BufferedImage src = createTestImage(100, 400, BufferedImage.TYPE_INT_RGB);
    BufferedImage result = executeTransform("png", "w100_h50_band", src);
    Assert.assertEquals(100, result.getWidth());
    Assert.assertEquals(50, result.getHeight());
  }
}
