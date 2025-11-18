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
import ape.runtime.stdlib.bonus.BasicRoomMazeGenerator;
import ape.translator.reflect.HiddenTypes2;

import java.util.Random;

public class LibMazes {
  public static @HiddenTypes2(class1 = Integer.class, class2 = Boolean.class) NtGrid<Integer, Boolean> basic_v0(long seed, int w, int h, int generations, int min, int max, int margin) {
    NtGrid<Integer, Boolean> grid = new NtGrid<>();
    BasicRoomMazeGenerator.generate(new Random(seed), w, h, generations, min, max, margin, (x, y) -> {
      grid.storage.put(new Pair<>(x, y), true);
    });
    return grid;
  }
}
