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

public class TransformFactoryTests {
  private static WebConfig defaultConfig() {
    return new WebConfig(new ConfigObject(Json.newJsonObject()));
  }

  @Test
  public void make_png() {
    Transform t = TransformFactory.make(defaultConfig(), "image/png", "w100");
    Assert.assertNotNull(t);
    Assert.assertEquals("image/png", t.outputContentType());
  }

  @Test
  public void make_jpeg() {
    Transform t = TransformFactory.make(defaultConfig(), "image/jpeg", "w100");
    Assert.assertNotNull(t);
    Assert.assertEquals("image/jpeg", t.outputContentType());
  }

  @Test
  public void make_gif() {
    Transform t = TransformFactory.make(defaultConfig(), "image/gif", "w100");
    Assert.assertNotNull(t);
    Assert.assertEquals("image/gif", t.outputContentType());
  }

  @Test
  public void make_unsupported_returns_null() {
    Assert.assertNull(TransformFactory.make(defaultConfig(), "application/pdf", "w100"));
    Assert.assertNull(TransformFactory.make(defaultConfig(), "text/plain", "w100"));
    Assert.assertNull(TransformFactory.make(defaultConfig(), "video/mp4", "w100"));
  }

  @Test
  public void make_png_with_format_conversion() {
    Transform t = TransformFactory.make(defaultConfig(), "image/png", "tojpg");
    Assert.assertNotNull(t);
    Assert.assertEquals("image/jpeg", t.outputContentType());
  }

  @Test
  public void make_jpeg_with_format_conversion() {
    Transform t = TransformFactory.make(defaultConfig(), "image/jpeg", "topng");
    Assert.assertNotNull(t);
    Assert.assertEquals("image/png", t.outputContentType());
  }

  @Test
  public void make_gif_with_format_conversion() {
    Transform t = TransformFactory.make(defaultConfig(), "image/gif", "tojpg");
    Assert.assertNotNull(t);
    Assert.assertEquals("image/jpeg", t.outputContentType());
  }
}
