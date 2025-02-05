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
package ape.rxhtml.preprocess.expand;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;

public class StaticConfig {
  public final String name;
  public final String push;
  public final String order;
  public final String code;
  public final String children;
  public final String parent;
  public final String id;

  public StaticConfig(Element element) {
    String _name = null;
    String _push = "path";
    String _order = "ordering";
    String _code = "code";
    String _children = "children";
    String _parent = "parent";
    String _id = "id";
    if (element != null) {
      for (Attribute attribute : element.attributes()) {
        if (attribute.hasDeclaredValue() || attribute.getValue() == null) {
          continue;
        }
        String val = attribute.getValue().trim();
        switch (attribute.getKey()) {
          case "push":
            _push = val;
            break;
          case "name":
            _name = val;
            break;
          case "order":
            _order = val;
            break;
          case "code":
            _code = val;
            break;
          case "children":
            _children = val;
            break;
          case "parent":
            _parent = val;
            break;
          case "id":
            _id = val;
            break;
        }
      }
    }
    this.name = _name;
    this.push = _push;
    this.order = _order;
    this.code = _code;
    this.children = _children;
    this.parent = _parent;
    this.id = _id;
  }
}
