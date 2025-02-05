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

import com.fasterxml.jackson.databind.node.ObjectNode;
import ape.common.Json;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class JsonSumTests {
  @Test
  public void top_level() {
    Assert.assertEquals("{\"x\":3}", JsonSum.sum(Json.parseJsonObject("{\"x\":1}"), Json.parseJsonObject("{\"x\":2}")).toString());
  }
  @Test
  public void recurse() {
    Assert.assertEquals("{\"z\":{\"x\":3}}", JsonSum.sum(Json.parseJsonObject("{\"z\":{\"x\":1}}"), Json.parseJsonObject("{\"z\":{\"x\":2}}")).toString());
  }
  @Test
  public void top_level_al() {
    ArrayList<ObjectNode> sum = new ArrayList<>();
    sum.add(Json.parseJsonObject("{\"x\":1}"));
    sum.add(Json.parseJsonObject("{\"x\":2}"));
    Assert.assertEquals("{\"x\":3}", JsonSum.sum(sum).toString());
  }
  @Test
  public void recurse_al() {
    ArrayList<ObjectNode> sum = new ArrayList<>();
    sum.add(Json.parseJsonObject("{\"z\":{\"x\":1}}"));
    sum.add(Json.parseJsonObject("{\"z\":{\"x\":2}}"));
    Assert.assertEquals("{\"z\":{\"x\":3}}", JsonSum.sum(sum).toString());
  }
}
