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

import ape.web.service.WebConfig;

/**
 * Factory for creating Transform instances based on content type and arguments.
 * Currently supports PNG and JPEG image transformations. Returns null for
 * unsupported content types, allowing callers to fall back to direct streaming.
 */
public class TransformFactory {
  public static Transform make(WebConfig config, String contentType, String args) {
    if (args == null || args.isEmpty()) {
      return null;
    }
    if (args.length() > config.maxTransformInstructionLength) {
      return null;
    }
    ImageTransform transform;
    switch (contentType) {
      case "image/png":
        transform = new ImageTransform(config, "png", args);
        break;
      case "image/jpeg":
        transform = new ImageTransform(config, "jpg", args);
        break;
      case "image/gif":
        transform = new ImageTransform(config, "gif", args);
        break;
      default:
        return null;
    }
    if (!transform.isValid()) {
      return null;
    }
    return transform;
  }
}
