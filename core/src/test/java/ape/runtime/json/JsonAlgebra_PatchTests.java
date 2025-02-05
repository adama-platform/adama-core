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
package ape.runtime.json;

import org.junit.Assert;
import org.junit.Test;

public class JsonAlgebra_PatchTests {
  @Test
  public void patch_empties() {
    Object target = of("{}");
    Object patch = of("{}");
    Object result = JsonAlgebra.merge(target, patch, false);
    is("{}", result);
  }

  private Object of(String json) {
    return new JsonStreamReader(json).readJavaTree();
  }

  private void is(String json, Object result) {
    JsonStreamWriter writer = new JsonStreamWriter();
    writer.writeTree(result);
    Assert.assertEquals(json, writer.toString());
  }

  @Test
  public void patch_unchanged() {
    Object target = of("{\"x\":123}");
    Object patch = of("{}");
    Object result = JsonAlgebra.merge(target, patch, false);
    is("{\"x\":123}", result);
  }

  @Test
  public void patch_introduce() {
    Object target = of("{}");
    Object patch = of("{\"x\":123}");
    Object result = JsonAlgebra.merge(target, patch, false);
    is("{\"x\":123}", result);
  }

  @Test
  public void patch_delete() {
    Object target = of("{\"x\":123}");
    Object patch = of("{\"x\":null}");
    Object result = JsonAlgebra.merge(target, patch, false);
    is("{}", result);
  }

  @Test
  public void patch_delete_keep() {
    Object target = of("{\"x\":123}");
    Object patch = of("{\"x\":null}");
    Object result = JsonAlgebra.merge(target, patch, true);
    is("{\"x\":null}", result);
  }

  @Test
  public void patch_obj_overwrite() {
    Object target = of("{\"x\":123}");
    Object patch = of("{\"x\":{}}");
    Object result = JsonAlgebra.merge(target, patch, false);
    is("{\"x\":{}}", result);
  }
}
