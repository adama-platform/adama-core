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

import ape.ErrorCodes;
import ape.common.ErrorCodeException;
import ape.web.service.WebConfig;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.regex.Pattern;

/** Image transformation processor supporting resize, crop, filters, format conversion, and quality control. */
public class ImageTransform implements Transform {
  private final WebConfig config;
  private final String inputFormat;
  private String outputFormat;
  private Integer desiredWidth;
  private Integer desiredHeight;
  private String resizeAlgorithm;
  private String invalidMessage;
  private Object hintKeyInterpolation;
  private Color background;
  private boolean grayscale;
  private boolean sepia;
  private boolean invert;
  private boolean flipH;
  private boolean flipV;
  private boolean blur;
  private boolean sharpen;
  private int brightness;
  private int jpegQuality;

  public ImageTransform(WebConfig config, String format, String args) {
    this.config = config;
    this.inputFormat = format;
    this.outputFormat = format;
    this.desiredWidth = null;
    this.desiredHeight = null;
    this.resizeAlgorithm = "fc";
    this.invalidMessage = null;
    this.grayscale = false;
    this.sepia = false;
    this.invert = false;
    this.flipH = false;
    this.flipV = false;
    this.blur = false;
    this.sharpen = false;
    this.brightness = 0;
    this.jpegQuality = -1;
    this.hintKeyInterpolation = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
    this.background = new Color(255, 255, 255, 0);
    StringBuilder errors = new StringBuilder();
    for (String cmd : args.split(Pattern.quote("_"))) {
      if (cmd.startsWith("w") && cmd.length() > 1 && Character.isDigit(cmd.charAt(1))) {
        try {
          desiredWidth = Integer.parseInt(cmd.substring(1));
          if (desiredWidth <= 0) {
            errors.append("[width must be positive]");
            desiredWidth = null;
          } else {
            desiredWidth = Math.min(desiredWidth, config.maxTransformDimension);
          }
        } catch (Exception ex) {
          errors.append("[width parameter is not int]");
        }
      } else if (cmd.startsWith("h") && cmd.length() > 1 && Character.isDigit(cmd.charAt(1))) {
        try {
          desiredHeight = Integer.parseInt(cmd.substring(1));
          if (desiredHeight <= 0) {
            errors.append("[height must be positive]");
            desiredHeight = null;
          } else {
            desiredHeight = Math.min(desiredHeight, config.maxTransformDimension);
          }
        } catch (Exception ex) {
          errors.append("[height parameter is not int]");
        }
      } else if (cmd.startsWith("q") && cmd.length() > 1 && Character.isDigit(cmd.charAt(1))) {
        try {
          jpegQuality = Integer.parseInt(cmd.substring(1));
          if (jpegQuality < 1 || jpegQuality > 100) {
            errors.append("[quality must be 1-100]");
            jpegQuality = -1;
          }
        } catch (Exception ex) {
          errors.append("[quality parameter is not int]");
        }
      } else if (cmd.startsWith("b") && cmd.length() > 1 && (Character.isDigit(cmd.charAt(1)) || cmd.charAt(1) == '-')) {
        try {
          brightness = Integer.parseInt(cmd.substring(1));
          if (brightness < -100 || brightness > 100) {
            errors.append("[brightness must be -100 to 100]");
            brightness = 0;
          }
        } catch (Exception ex) {
          errors.append("[brightness parameter is not int]");
        }
      } else {
        switch (cmd) {
          case "bl":
            hintKeyInterpolation = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
            break;
          case "bc":
            hintKeyInterpolation = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
            break;
          case "nn":
            hintKeyInterpolation = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
            break;
          case "sq":
          case "fc":
          case "crop":
          case "cover":
          case "band":
            resizeAlgorithm = cmd;
            break;
          case "grey":
          case "gray":
            grayscale = true;
            break;
          case "sepia":
            sepia = true;
            break;
          case "invert":
            invert = true;
            break;
          case "fliph":
            flipH = true;
            break;
          case "flipv":
            flipV = true;
            break;
          case "blur":
            blur = true;
            break;
          case "sharpen":
            sharpen = true;
            break;
          case "topng":
            outputFormat = "png";
            break;
          case "tojpg":
            outputFormat = "jpg";
            break;
          case "togif":
            outputFormat = "gif";
            break;
        }
      }
    }
    invalidMessage = errors.toString();
    if (invalidMessage.length() == 0) {
      invalidMessage = null;
    }
  }

  @Override
  public String outputContentType() {
    switch (outputFormat) {
      case "png":
        return "image/png";
      case "jpg":
        return "image/jpeg";
      case "gif":
        return "image/gif";
      default:
        return "image/" + outputFormat;
    }
  }

  public boolean isValid() {
    return invalidMessage == null;
  }

  public String getInvalidMessage() {
    return invalidMessage;
  }

  public void execute(InputStream input, File output) throws Exception {
    // Read image dimensions BEFORE fully decoding to prevent decompression bombs.
    // A small compressed image (e.g. 50KB PNG) can decode to enormous pixel buffers;
    // checking dimensions via ImageReader metadata avoids allocating that memory.
    ImageInputStream iis = ImageIO.createImageInputStream(input);
    if (iis == null) {
      throw new Exception("Failed to create image input stream");
    }
    BufferedImage src;
    try {
      Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
      if (!readers.hasNext()) {
        throw new Exception("Failed to read image: unsupported or corrupt input");
      }
      ImageReader reader = readers.next();
      try {
        reader.setInput(iis);
        int preWidth = reader.getWidth(0);
        int preHeight = reader.getHeight(0);
        long sourcePixels = (long) preWidth * preHeight;
        if (sourcePixels > config.maxSourcePixels) {
          throw new ErrorCodeException(ErrorCodes.ASSET_TRANSFORM_SOURCE_TOO_LARGE);
        }
        src = reader.read(0);
      } finally {
        reader.dispose();
      }
    } finally {
      iis.close();
    }
    if (src == null) {
      throw new Exception("Failed to read image: unsupported or corrupt input");
    }
    if (desiredHeight != null && desiredWidth == null) {
      desiredWidth = Math.min((int)((long) src.getWidth() * desiredHeight / src.getHeight()), config.maxTransformDimension);
    } else if (desiredHeight == null && desiredWidth != null) {
      desiredHeight = Math.min((int)((long) src.getHeight() * desiredWidth / src.getWidth()), config.maxTransformDimension);
    } else if (desiredWidth == null && desiredHeight == null) {
      desiredWidth = Math.min(src.getWidth(), config.maxTransformDimension);
      desiredHeight = Math.min(src.getHeight(), config.maxTransformDimension);
    }
    if (desiredWidth <= 0) desiredWidth = 1;
    if (desiredHeight <= 0) desiredHeight = 1;

    // Apply grayscale to source
    if (grayscale) {
      src = applyGrayscale(src);
    }

    // Apply sepia to source
    if (sepia) {
      src = applySepia(src);
    }

    // Apply invert to source
    if (invert) {
      src = applyInvert(src);
    }

    // Apply brightness to source
    if (brightness != 0) {
      src = applyBrightness(src, brightness);
    }

    // Determine output image type
    int destType = outputFormat.equals("jpg") ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;

    // Resize
    BufferedImage dest;
    switch (resizeAlgorithm) {
      case "sq":
        dest = resizeSquish(src, desiredWidth, desiredHeight, destType);
        break;
      case "crop":
      case "cover":
        dest = resizeCover(src, desiredWidth, desiredHeight, destType);
        break;
      case "band":
        dest = resizeBand(src, desiredWidth, desiredHeight, destType);
        break;
      case "fc":
      default:
        dest = resizeFitCenter(src, desiredWidth, desiredHeight, destType);
        break;
    }

    // Apply flip
    if (flipH) {
      dest = applyFlipH(dest);
    }
    if (flipV) {
      dest = applyFlipV(dest);
    }

    // Apply blur
    if (blur) {
      dest = applyBlur(dest);
    }

    // Apply sharpen
    if (sharpen) {
      dest = applySharpen(dest);
    }

    // Write output
    writeImage(dest, outputFormat, jpegQuality, output);
  }

  private BufferedImage applyGrayscale(BufferedImage src) {
    WritableRaster raster = src.getRaster();
    int bands = raster.getNumBands();
    int w = src.getWidth();
    int h = src.getHeight();
    int[] pixel = new int[bands];
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        raster.getPixel(x, y, pixel);
        int rgbBands = Math.min(bands, 3);
        int sum = 0;
        for (int i = 0; i < rgbBands; i++) {
          sum += pixel[i];
        }
        int gray = sum / rgbBands;
        for (int i = 0; i < rgbBands; i++) {
          pixel[i] = gray;
        }
        raster.setPixel(x, y, pixel);
      }
    }
    return src;
  }

  private BufferedImage applySepia(BufferedImage src) {
    WritableRaster raster = src.getRaster();
    int bands = raster.getNumBands();
    int w = src.getWidth();
    int h = src.getHeight();
    int[] pixel = new int[bands];
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        raster.getPixel(x, y, pixel);
        int r = pixel[0];
        int g = bands > 1 ? pixel[1] : r;
        int b = bands > 2 ? pixel[2] : r;
        int newR = Math.min(255, (int)(r * 0.393 + g * 0.769 + b * 0.189));
        int newG = Math.min(255, (int)(r * 0.349 + g * 0.686 + b * 0.168));
        int newB = Math.min(255, (int)(r * 0.272 + g * 0.534 + b * 0.131));
        pixel[0] = newR;
        if (bands > 1) pixel[1] = newG;
        if (bands > 2) pixel[2] = newB;
        raster.setPixel(x, y, pixel);
      }
    }
    return src;
  }

  private BufferedImage applyInvert(BufferedImage src) {
    WritableRaster raster = src.getRaster();
    int bands = raster.getNumBands();
    int w = src.getWidth();
    int h = src.getHeight();
    int[] pixel = new int[bands];
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        raster.getPixel(x, y, pixel);
        int rgbBands = Math.min(bands, 3);
        for (int i = 0; i < rgbBands; i++) {
          pixel[i] = 255 - pixel[i];
        }
        raster.setPixel(x, y, pixel);
      }
    }
    return src;
  }

  private BufferedImage applyBrightness(BufferedImage src, int brightness) {
    float scale = 1.0f + brightness / 100.0f;
    // Ensure we only process RGB bands (not alpha)
    int bands = src.getRaster().getNumBands();
    if (bands == 4) {
      float[] scales = {scale, scale, scale, 1.0f};
      float[] offsets = {0, 0, 0, 0};
      RescaleOp op = new RescaleOp(scales, offsets, null);
      return op.filter(src, null);
    } else {
      RescaleOp op = new RescaleOp(scale, 0, null);
      return op.filter(src, null);
    }
  }

  private BufferedImage resizeSquish(BufferedImage src, int w, int h, int destType) {
    BufferedImage dest = new BufferedImage(w, h, destType);
    Graphics2D g2d = dest.createGraphics();
    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hintKeyInterpolation);
    try {
      g2d.drawImage(src, 0, 0, w, h, null);
    } finally {
      g2d.dispose();
    }
    return dest;
  }

  private BufferedImage resizeFitCenter(BufferedImage src, int w, int h, int destType) {
    BufferedImage dest = new BufferedImage(w, h, destType);
    Graphics2D g2d = dest.createGraphics();
    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hintKeyInterpolation);
    try {
      g2d.setColor(background);
      g2d.fillRect(0, 0, w, h);
      if (src.getWidth() > 0 && src.getHeight() > 0) {
        if ((long) w * src.getHeight() < (long) h * src.getWidth()) {
          int newHeight = src.getHeight() * w / src.getWidth();
          g2d.drawImage(src, 0, (h - newHeight) / 2, w, newHeight, null);
        } else {
          int newWidth = src.getWidth() * h / src.getHeight();
          g2d.drawImage(src, (w - newWidth) / 2, 0, newWidth, h, null);
        }
      }
    } finally {
      g2d.dispose();
    }
    return dest;
  }

  private BufferedImage resizeCover(BufferedImage src, int w, int h, int destType) {
    BufferedImage dest = new BufferedImage(w, h, destType);
    Graphics2D g2d = dest.createGraphics();
    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hintKeyInterpolation);
    try {
      int srcW = src.getWidth();
      int srcH = src.getHeight();
      // Scale to cover: pick the larger scale factor
      double scaleX = (double) w / srcW;
      double scaleY = (double) h / srcH;
      double scale = Math.max(scaleX, scaleY);
      int scaledW = (int) Math.round(srcW * scale);
      int scaledH = (int) Math.round(srcH * scale);
      int offsetX = (w - scaledW) / 2;
      int offsetY = (h - scaledH) / 2;
      g2d.drawImage(src, offsetX, offsetY, scaledW, scaledH, null);
    } finally {
      g2d.dispose();
    }
    return dest;
  }

  private BufferedImage resizeBand(BufferedImage src, int w, int h, int destType) {
    // Crop a horizontal band from the vertical center, then resize to target
    int srcW = src.getWidth();
    int srcH = src.getHeight();
    // Calculate the band height based on target aspect ratio
    double targetRatio = (double) w / h;
    int bandH = (int) Math.round(srcW / targetRatio);
    if (bandH > srcH) bandH = srcH;
    int bandY = (srcH - bandH) / 2;
    BufferedImage band = src.getSubimage(0, bandY, srcW, bandH);
    return resizeSquish(band, w, h, destType);
  }

  private BufferedImage applyFlipH(BufferedImage src) {
    int w = src.getWidth();
    int h = src.getHeight();
    BufferedImage dest = new BufferedImage(w, h, src.getType());
    Graphics2D g2d = dest.createGraphics();
    try {
      AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
      tx.translate(-w, 0);
      g2d.setTransform(tx);
      g2d.drawImage(src, 0, 0, null);
    } finally {
      g2d.dispose();
    }
    return dest;
  }

  private BufferedImage applyFlipV(BufferedImage src) {
    int w = src.getWidth();
    int h = src.getHeight();
    BufferedImage dest = new BufferedImage(w, h, src.getType());
    Graphics2D g2d = dest.createGraphics();
    try {
      AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
      tx.translate(0, -h);
      g2d.setTransform(tx);
      g2d.drawImage(src, 0, 0, null);
    } finally {
      g2d.dispose();
    }
    return dest;
  }

  private BufferedImage applyBlur(BufferedImage src) {
    float[] kernel = {
      1f/16f, 2f/16f, 1f/16f,
      2f/16f, 4f/16f, 2f/16f,
      1f/16f, 2f/16f, 1f/16f
    };
    ConvolveOp op = new ConvolveOp(new Kernel(3, 3, kernel), ConvolveOp.EDGE_NO_OP, null);
    return op.filter(src, null);
  }

  private BufferedImage applySharpen(BufferedImage src) {
    float[] kernel = {
       0f, -1f,  0f,
      -1f,  5f, -1f,
       0f, -1f,  0f
    };
    ConvolveOp op = new ConvolveOp(new Kernel(3, 3, kernel), ConvolveOp.EDGE_NO_OP, null);
    return op.filter(src, null);
  }

  private void writeImage(BufferedImage img, String format, int quality, File output) throws Exception {
    if (format.equals("jpg") && quality > 0) {
      Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
      if (!writers.hasNext()) {
        throw new Exception("No JPEG writer available");
      }
      ImageWriter writer = writers.next();
      ImageWriteParam param = writer.getDefaultWriteParam();
      param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
      param.setCompressionQuality(quality / 100.0f);
      try (ImageOutputStream ios = ImageIO.createImageOutputStream(output)) {
        writer.setOutput(ios);
        // Ensure no alpha for JPEG
        BufferedImage toWrite = ensureNoAlpha(img);
        writer.write(null, new IIOImage(toWrite, null, null), param);
      } finally {
        writer.dispose();
      }
    } else {
      BufferedImage toWrite = img;
      if (format.equals("jpg")) {
        toWrite = ensureNoAlpha(img);
      }
      ImageIO.write(toWrite, format, output);
    }
  }

  private BufferedImage ensureNoAlpha(BufferedImage img) {
    if (img.getType() == BufferedImage.TYPE_INT_RGB) {
      return img;
    }
    BufferedImage noAlpha = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
    Graphics2D g = noAlpha.createGraphics();
    try {
      g.setColor(Color.WHITE);
      g.fillRect(0, 0, img.getWidth(), img.getHeight());
      g.drawImage(img, 0, 0, null);
    } finally {
      g.dispose();
    }
    return noAlpha;
  }
}
