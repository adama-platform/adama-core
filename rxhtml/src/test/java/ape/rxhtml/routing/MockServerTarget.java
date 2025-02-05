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
package ape.rxhtml.routing;

import ape.common.Callback;
import ape.rxhtml.server.RemoteInlineResolver;
import ape.rxhtml.server.ServerSideTarget;

import java.util.List;
import java.util.TreeMap;

public class MockServerTarget implements ServerSideTarget {
  @Override
  public void get(RemoteInlineResolver resolver, String agent, String authority, String space, String uri, TreeMap<String, List<String>> query, Callback<Target> callback) {
    callback.success(new Target(200, new TreeMap<>(), "Hi".getBytes(), null));
  }

  @Override
  public void post(RemoteInlineResolver resolver, String agent, String authority, String space, String uri, TreeMap<String, List<String>> query, TreeMap<String, List<String>> body, Callback<Target> callback) {
    callback.success(new Target(200, new TreeMap<>(), "Hi Post".getBytes(), null));
  }

  @Override
  public long memory() {
    return 1024;
  }
}
