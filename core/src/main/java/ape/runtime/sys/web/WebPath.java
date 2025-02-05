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
package ape.runtime.sys.web;

import ape.common.cache.Measurable;

import java.util.ArrayList;

/** Tear down a URI into fragments */
public class WebPath implements Measurable {
  public String uri;
  public WebFragment[] fragments;

  public WebPath(String uri) {
    this.uri = uri;
    ArrayList<WebFragment> fragments = new ArrayList<>();
    int at = uri.indexOf('/') + 1; // skip the first slash
    while (at > 0) {
      int slashAt = uri.indexOf('/', at);
      if (slashAt >= 0) {
        fragments.add(new WebFragment(uri, uri.substring(at, slashAt), at));
        at = slashAt + 1;
      } else {
        fragments.add(new WebFragment(uri, uri.substring(at), at));
        at = -1;
      }
    }
    this.fragments = fragments.toArray(new WebFragment[fragments.size()]);
  }

  public WebFragment at(int k) {
    if (k < fragments.length) {
      return fragments[k];
    }
    return null;
  }

  public int size() {
    return fragments.length;
  }

  @Override
  public long measure() {
    return uri.length() * 3L + 32L * fragments.length + 64;
  }
}
