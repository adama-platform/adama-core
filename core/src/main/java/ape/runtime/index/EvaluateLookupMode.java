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
package ape.runtime.index;

import ape.runtime.contracts.IndexQuerySet;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/** shared library for evaluating the look up mode against an index */
public class EvaluateLookupMode {

  /** get the index */
  public static <Ty> TreeSet<Ty> of(TreeMap<Integer, TreeSet<Ty>> index, final int at, IndexQuerySet.LookupMode mode) {
    switch (mode) {
      case LessThan: {
        TreeSet<Ty> values = new TreeSet<>();
        Iterator<Map.Entry<Integer, TreeSet<Ty>>> it = index.entrySet().iterator();
        while (it.hasNext()) {
          Map.Entry<Integer, TreeSet<Ty>> entry = it.next();
          if (entry.getKey() < at) {
            values.addAll(entry.getValue());
          } else {
            break;
          }
        }
        if (values.size() == 0) {
          return null;
        }
        return values;
      }
      case LessThanOrEqual: {
        TreeSet<Ty> values = new TreeSet<>();
        Iterator<Map.Entry<Integer, TreeSet<Ty>>> it = index.entrySet().iterator();
        while (it.hasNext()) {
          Map.Entry<Integer, TreeSet<Ty>> entry = it.next();
          if (entry.getKey() <= at) {
            values.addAll(entry.getValue());
          } else {
            break;
          }
        }
        if (values.size() == 0) {
          return null;
        }
        return values;
      }
      case GreaterThan: {
        TreeSet<Ty> values = new TreeSet<>();
        Iterator<Map.Entry<Integer, TreeSet<Ty>>> it = index.descendingMap().entrySet().iterator();
        while (it.hasNext()) {
          Map.Entry<Integer, TreeSet<Ty>> entry = it.next();
          if (entry.getKey() > at) {
            values.addAll(entry.getValue());
          } else {
            break;
          }
        }
        if (values.size() == 0) {
          return null;
        }
        return values;
      }
      case GreaterThanOrEqual: {
        TreeSet<Ty> values = new TreeSet<>();
        Iterator<Map.Entry<Integer, TreeSet<Ty>>> it = index.descendingMap().entrySet().iterator();
        while (it.hasNext()) {
          Map.Entry<Integer, TreeSet<Ty>> entry = it.next();
          if (entry.getKey() >= at) {
            values.addAll(entry.getValue());
          } else {
            break;
          }
        }
        if (values.size() == 0) {
          return null;
        }
        return values;
      }
      default:
        return index.get(at);
    }
  }

}
