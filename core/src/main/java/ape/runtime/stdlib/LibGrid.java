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
package ape.runtime.stdlib;

import ape.common.Pair;
import ape.runtime.natives.NtGrid;
import ape.translator.reflect.Skip;

import java.util.Arrays;
import java.util.Map;

public class LibGrid {
  @Skip
  public static <O> NtGrid<Integer, O> rotate(NtGrid<Integer, O> grid, int turns) {
    int real_turns = turns % 4;
    if (real_turns == 0) {
      return new NtGrid<>(grid); //  no turns
    }
    NtGrid<Integer, O> next = new NtGrid<>(grid);
    for (Map.Entry<Pair<Integer>, O> entry : grid.storage.entrySet()) {
      int x = entry.getKey().x;
      int y = entry.getKey().y;
      switch (turns) {
        case 1: // (x + y i) * i = -y + x i
          next.storage.put(new Pair<>(-y, x), entry.getValue());
          break;
        case 2: // (x + y i) * (i * i) = -x - y i
          next.storage.put(new Pair<>(-x, -y), entry.getValue());
          break;
        case 3: // (x + y i) * (i * i * i) = y - x i
          next.storage.put(new Pair<>(y, -x), entry.getValue());
          break;
      }
    }
    return next;
  }

  @Skip
  public static <O> NtGrid<Integer, O> smash(NtGrid<Integer, O> grid) {
    int min_x = Integer.MAX_VALUE;
    int min_y = Integer.MAX_VALUE;
    for (Map.Entry<Pair<Integer>, O> entry : grid.storage.entrySet()) {
      Pair<Integer> p = entry.getKey();
      min_x = Math.min(min_x, p.x);
      min_y = Math.min(min_y, p.y);
    }
    NtGrid<Integer, O> next = new NtGrid<>(grid);
    for (Map.Entry<Pair<Integer>, O> entry : grid.storage.entrySet()) {
      Pair<Integer> p = entry.getKey();
      next.storage.put(new Pair<>(p.x - min_x, p.y - min_y), entry.getValue());
    }
    return next;
  }

  @Skip
  public static <O> NtGrid<Integer, O> translate(NtGrid<Integer, O> grid, int u, int v) {
    NtGrid<Integer, O> next = new NtGrid<>(grid);
    for (Map.Entry<Pair<Integer>, O> entry : grid.storage.entrySet()) {
      Pair<Integer> p = entry.getKey();
      next.storage.put(new Pair<>(p.x + u, p.y + v), entry.getValue());
    }
    return next;
  }

  @Skip
  public static boolean[] flatten(NtGrid<Integer, Boolean> grid, boolean defaultValue) {
    if (grid.size() == 0) {
      return new boolean[0];
    }
    boolean[] result = new boolean[grid.width() * grid.height()];
    int mx = grid.minX();
    int my = grid.minY();
    Arrays.fill(result, defaultValue);
    for (Map.Entry<Pair<Integer>, Boolean> entry : grid.storage.entrySet()) {
      Pair<Integer> p = entry.getKey();
      result[(p.x - mx) + (p.y - my) * grid.width()] = entry.getValue();
    }
    return result;
  }

  @Skip
  public static int[] flatten(NtGrid<Integer, Integer> grid, int defaultValue) {
    if (grid.size() == 0) {
      return new int[0];
    }
    int[] result = new int[grid.width() * grid.height()];
    int mx = grid.minX();
    int my = grid.minY();
    Arrays.fill(result, defaultValue);
    for (Map.Entry<Pair<Integer>, Integer> entry : grid.storage.entrySet()) {
      Pair<Integer> p = entry.getKey();
      result[(p.x - mx) + (p.y - my) * grid.width()] = entry.getValue();
    }
    return result;
  }

  @Skip
  public static double[] flatten(NtGrid<Integer, Double> grid, double defaultValue) {
    if (grid.size() == 0) {
      return new double[0];
    }
    double[] result = new double[grid.width() * grid.height()];
    int mx = grid.minX();
    int my = grid.minY();
    Arrays.fill(result, defaultValue);
    for (Map.Entry<Pair<Integer>, Double> entry : grid.storage.entrySet()) {
      Pair<Integer> p = entry.getKey();
      result[(p.x - mx) + (p.y - my) * grid.width()] = entry.getValue();
    }
    return result;
  }
}
