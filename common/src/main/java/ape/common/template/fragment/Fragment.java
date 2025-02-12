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
package ape.common.template.fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.PrimitiveIterator;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/** a simplified templating language inspired by mustache */
public class Fragment {
  public final FragmentType type;
  public final String[] text;

  public Fragment(FragmentType type, String... text) {
    if (text.length == 0) {
      this.type = FragmentType.Text;
      this.text = new String[]{"["};
      return;
    }
    this.type = type;
    this.text = text;
  }

  /** parse the template */
  public static ArrayList<Fragment> parse(String template) {
    ArrayList<Fragment> results = new ArrayList<>();
    IntStream codepoints = template.codePoints();
    PrimitiveIterator.OfInt it = codepoints.iterator();
    StringBuilder current = new StringBuilder();
    State state = State.Text;
    Runnable cut = () -> {
      String r = current.toString();
      current.setLength(0);
      if (r.length() > 0) {
        results.add(new Fragment(FragmentType.Text, r));
      }
    };
    Supplier<Fragment> scan = () -> {
      ArrayList<String> parts = new ArrayList<>();
      StringBuilder part = new StringBuilder();
      FragmentType ty = FragmentType.Expression;
      boolean first = true;
      while (it.hasNext()) {
        int ecp = it.next();
        switch (ecp) {
          case '#':
            if (first) {
              ty = FragmentType.If;
              break;
            }
          case '^':
            if (first) {
              ty = FragmentType.IfNot;
              break;
            }
          case '/':
            if (first) {
              ty = FragmentType.End;
              break;
            }
          case ']':
          case '(':
          case ')':
          case '|':
          case '>':
          case '<':
          case '+':
          case '*':
          case '-':
            String partial = part.toString().trim();
            if (partial.length() > 0) {
              parts.add(partial);
            }
            part.setLength(0);
            if (ecp == ']') {
              if (!(it.hasNext() && it.next() == ']')) {
                throw new RuntimeException("']' encountered without additional ']' during scan");
              }
              return new Fragment(ty, parts.toArray(new String[parts.size()]));
            } else {
              parts.add("" + Character.toString(ecp));
            }
            break;
          default:
            part.append(Character.toChars(ecp));
            break;
        }
        first = false;
      }
      throw new RuntimeException("scan() failed due to missing ']'");
    };
    while (it.hasNext()) {
      int cp = it.next();
      switch (state) {
        case Text: {
          if (cp == '[') {
            state = State.FirstEscape;
          } else {
            current.append(Character.toChars(cp));
          }
          break;
        }
        case FirstEscape: {
          state = State.Text;
          if (cp == '[') {
            cut.run();
            results.add(scan.get());
          } else {
            current.append('[');
            current.append(Character.toChars(cp));
          }
          break;
        }
      }
    }
    if (state == State.FirstEscape) {
      current.append('[');
    }
    cut.run();
    return results;
  }

  @Override
  public String toString() {
    return type + ":" + Arrays.toString(text);
  }

  private enum State {
    Text, FirstEscape
  }
}
