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
package ape.translator.env.topo;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TopologicalSortTests {

  private static Set<String> D(String... keys) {
    HashSet<String> result = new HashSet<>();
    for (String key : keys) {
      result.add(key);
    }
    return result;
  }

  @Test
  public void linear_out_of_order() {
    TopologicalSort<String> ts = new TopologicalSort<>();
    ts.add("c", "vc", D("b"));
    ts.add("b", "vb", D("a"));
    ts.add("a", "va", null);
    ArrayList<String> sorted = ts.sort();
    Assert.assertEquals("va", sorted.get(0));
    Assert.assertEquals("vb", sorted.get(1));
    Assert.assertEquals("vc", sorted.get(2));
  }

  @Test
  public void complex() {
    TopologicalSort<String> ts = new TopologicalSort<>();
    ts.add("c", "vc", D("b", "d"));
    ts.add("b", "vb", D("a", "e"));
    ts.add("a", "va", null);
    ts.add("d", "vd", D("a"));
    ts.add("e", "ve", null);
    ArrayList<String> sorted = ts.sort();
    Assert.assertEquals("va", sorted.get(0));
    Assert.assertEquals("vd", sorted.get(1));
    Assert.assertEquals("ve", sorted.get(2));
    Assert.assertEquals("vb", sorted.get(3));
    Assert.assertEquals("vc", sorted.get(4));
  }

  @Test
  public void cycles() {
    TopologicalSort<String> ts = new TopologicalSort<>();
    ts.add("c", "vc", D("b"));
    ts.add("b", "vb", D("a"));
    ts.add("a", "va", D("c"));
    ArrayList<String> sorted = ts.sort();
    Assert.assertEquals("va", sorted.get(0));
    Assert.assertEquals("vb", sorted.get(1));
    Assert.assertEquals("vc", sorted.get(2));
    Assert.assertFalse(ts.cycles().isEmpty());
  }

  @Test
  public void linear() {
    TopologicalSort<String> ts = new TopologicalSort<>();
    ts.add("a", "va", null);
    ts.add("b", "vb", D("a"));
    ts.add("c", "vc", D("b"));
    ArrayList<String> sorted = ts.sort();
    Assert.assertEquals("va", sorted.get(0));
    Assert.assertEquals("vb", sorted.get(1));
    Assert.assertEquals("vc", sorted.get(2));
    Assert.assertTrue(ts.cycles().isEmpty());
  }
}
