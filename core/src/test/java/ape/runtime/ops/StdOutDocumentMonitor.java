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
package ape.runtime.ops;

import ape.runtime.contracts.DocumentMonitor;

import java.util.ArrayList;
import java.util.HashMap;

/** a monitor which dumps fun information out to stdout */
public class StdOutDocumentMonitor implements DocumentMonitor {
  public HashMap<String, TableRegister> stats;

  public StdOutDocumentMonitor() {
    stats = new HashMap<>();
  }

  @Override
  public void assertFailureAt(final int startLine, final int startPosition, final int endLine, final int endLinePosition, final int total, final int failures) {
  }

  @Override
  public void goodwillFailureAt(final int startLine, final int startPosition, final int endLine, final int endLinePosition) {
  }

  @Override
  public void pop(final long time, final boolean exception) {
    System.out.println("POP:" + exception);
  }

  @Override
  public void push(final String label) {
    System.out.println("PUSH:" + label);
  }

  public void dump() {
    final var items = new ArrayList<>(stats.values());
    items.sort((a, b) -> {
      final var s0 = Integer.compare(a.total, b.total);
      if (s0 == 0) {
        return Integer.compare(a.effectiveness, b.effectiveness);
      }
      return s0;
    });
    System.out.println("table,column,calls,total,effectiveness,%");
    for (final TableRegister tr : items) {
      System.out.println(tr.tableName + "," + tr.colummName + "," + tr.calls + "," + tr.total + "," + tr.effectiveness + "," + tr.effectiveness / (double) tr.total);
    }
  }

  public static class TableRegister {
    public final String colummName;
    public final String tableName;
    public int calls;
    public int effectiveness;
    public int total;

    public TableRegister(final String tableName, final String colummName) {
      this.tableName = tableName;
      this.colummName = colummName;
      total = 0;
      effectiveness = 0;
      calls = 0;
    }
  }
}
