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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.InputStream;
import java.util.regex.Pattern;

/**
 * Image transformation processor supporting resize, crop, and color operations.
 * Parses underscore-delimited arguments: w{width}, h{height}, resize algorithms
 * (fc=fit-center, sq=squish, crop), interpolation hints (bl=bilinear, bc=bicubic,
 * nn=nearest-neighbor), and grayscale conversion. Uses Java AWT for processing.
 */
public class ImageTransform implements Transform {
  private final String format;
  private Integer desiredWidth;
  private Integer desiredHeight;
  private String resizeAlgorithm;
  private String invalidMessage;
  private Object hintKeyInterpolation;
  private Color background;
  private boolean grayscale;

  public ImageTransform(String format, String args) {
    this.format = format;
    this.desiredWidth = null;
    this.desiredHeight = null;
    this.resizeAlgorithm = "fc"; // fit and center
    this.invalidMessage = null;
    this.grayscale = false;
    this.hintKeyInterpolation = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
    this.background = new Color(255, 255, 255, 0);
    StringBuilder errors = new StringBuilder();
    for (String cmd : args.split(Pattern.quote("_"))) {
      if (cmd.startsWith("w")) {
        try {
          desiredWidth = Integer.parseInt(cmd.substring(1));
        } catch (Exception ex) {
          errors.append("[width parameter is not int]");
        }
      } else if (cmd.startsWith("h")) {
        try {
          desiredHeight = Integer.parseInt(cmd.substring(1));
        } catch (Exception ex) {
          errors.append("[height parameter is not int]");
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
          case "sq": // squish
          case "fc": // fit and center
          case "crop": //
            resizeAlgorithm = cmd;
            break;
          case "grey":
          case "gray":
            grayscale = true;
            break;
        }
      }
    }
    invalidMessage = errors.toString();
    if (invalidMessage.length() == 0) {
      invalidMessage = null;
    }
  }

  public void execute(InputStream input, File output) throws Exception {
    BufferedImage src = ImageIO.read(input);
    if (desiredHeight != null && desiredWidth == null) {
      desiredWidth = src.getWidth() * desiredHeight / src.getHeight();
    } else if (desiredHeight == null && desiredWidth != null) {
      desiredHeight = src.getHeight() * desiredWidth / src.getWidth();
    } else if (desiredWidth == null && desiredHeight == null) {
      desiredWidth = src.getWidth();
      desiredHeight = src.getHeight();
    }

    if (grayscale) {
      int[] pixels = new int[src.getWidth() * src.getHeight() * 3];
      WritableRaster raster = src.getRaster();
      raster.getPixels(0, 0, src.getWidth(), src.getHeight(), pixels);
      for (int k = 0; k + 2 < pixels.length; k += 3) {
        int s = pixels[k] + pixels[k + 1] + pixels[k + 2];
        s /= 3;
        pixels[k] = s;
        pixels[k + 1] = s;
        pixels[k + 2] = s;
      }
      raster.setPixels(0, 0, src.getWidth(), src.getHeight(), pixels);
    }


    BufferedImage dest = new BufferedImage(desiredWidth, desiredHeight, src.getType());
    Graphics2D g2d = dest.createGraphics();
    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hintKeyInterpolation);

    try {
      switch (resizeAlgorithm) {
        case "sq": // squish
          g2d.drawImage(src, 0, 0, desiredWidth, desiredHeight, null);
          break;
        case "fc": // fit and center
          g2d.setColor(background);
          g2d.fillRect(0, 0, desiredWidth, desiredHeight);
          if (src.getWidth() > 0 && src.getHeight() > 0) {
            if (desiredWidth * src.getHeight() < desiredHeight * src.getWidth()) { // dW / sW < dH / sH without division
              int newHeight = src.getHeight() * desiredWidth / src.getWidth();
              g2d.drawImage(src, 0, (desiredHeight - newHeight) / 2, desiredWidth, newHeight, null);
            } else {
              int newWidth = src.getWidth() * desiredHeight / src.getHeight();
              g2d.drawImage(src, (desiredWidth - newWidth) / 2, 0, newWidth, desiredHeight, null);
            }
          }
      }
      ImageIO.write(dest, format, output);
    } finally {
      g2d.dispose();
    }
  }
}
