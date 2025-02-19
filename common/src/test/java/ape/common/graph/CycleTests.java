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
package ape.common.graph;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class CycleTests {
  @Test
  public void empty() {
    Assert.assertNull(Cycle.detect(new HashMap<>()));
  }

  @Test
  public void look_back() {
    TreeMap<String, Set<String>> graph = new TreeMap<>();
    graph.put("A", D("B", "C"));
    graph.put("B", D("C", "Y"));
    graph.put("X", D("Y", "Z"));
    graph.put("Y", D("Z"));
    Assert.assertNull(Cycle.detect(graph));
  }

  private static Set<String> D(String... ds) {
    TreeSet<String> depends = new TreeSet<>();
    Collections.addAll(depends, ds);
    return depends;
  }

  @Test
  public void sample_1_nocycle() {
    HashMap<String, Set<String>> graph = new HashMap<>();
    graph.put("A", D("B"));
    graph.put("B", D("C"));
    graph.put("X", D("Y"));
    graph.put("Y", D("Z"));
    Assert.assertNull(Cycle.detect(graph));
  }

  @Test
  public void sample_2_cycle() {
    HashMap<String, Set<String>> graph = new HashMap<>();
    graph.put("A", D("B"));
    graph.put("B", D("A"));
    Assert.assertEquals("A, B, A", Cycle.detect(graph));
  }

  @Test
  public void sample_3_cycle() {
    HashMap<String, Set<String>> graph = new HashMap<>();
    graph.put("A", D("A"));
    Assert.assertEquals("A, A", Cycle.detect(graph));
  }

  @Test
  public void sample_4_cycle() {
    HashMap<String, Set<String>> graph = new HashMap<>();
    graph.put("X", D("Y"));
    graph.put("Y", D("Z"));
    graph.put("Y", D("A"));
    graph.put("A", D("B"));
    graph.put("B", D("Y"));
    Assert.assertEquals("A, B, Y, A", Cycle.detect(graph));
  }

  @Test
  public void sample_5_nocycle_big() {
    HashMap<String, Set<String>> graph = new HashMap<>();
    graph.put("A", D("B"));
    graph.put("A", D("C"));
    graph.put("A", D("D"));
    graph.put("A", D("E"));
    graph.put("A", D("F"));
    graph.put("A", D("G"));
    graph.put("B", D("C"));
    graph.put("B", D("D"));
    graph.put("B", D("E"));
    graph.put("B", D("F"));
    graph.put("B", D("G"));
    graph.put("C", D("D"));
    graph.put("C", D("E"));
    graph.put("C", D("F"));
    graph.put("C", D("G"));
    graph.put("D", D("E"));
    graph.put("E", D("F"));
    Assert.assertNull(Cycle.detect(graph));
  }
}
