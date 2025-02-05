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
package ape.rxhtml.template;

import ape.common.Escaping;
import ape.rxhtml.atl.ParseException;
import ape.rxhtml.atl.Parser;
import ape.rxhtml.atl.tree.Tree;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;

public class RxObject {
  public final String rxObj;
  public final boolean delayed;
  private final Environment env;
  private final ArrayList<Attribute> attributes;

  public RxObject(Environment env, String... names) {
    this.env = env;
    this.attributes = new ArrayList<>();
    rxObj = env.pool.ask();
    env.writeElementDebugIfTest();
    env.writer.tab().append("var ").append(rxObj).append("=$.RX([");
    ArrayList<String> attrToRemove = new ArrayList<>();
    boolean addedUnder = false;
    boolean _delayed = false;
    for (String attrName : names) {
      String nameToUse = attrName;
      if (nameToUse.startsWith("parameter:")) {
        nameToUse = nameToUse.substring(10);
        attrToRemove.add(attrName);
      } else if (nameToUse.startsWith("search:")) {
        nameToUse = nameToUse.substring(7);
        attrToRemove.add(attrName);
      }
      nameToUse = nameToUse.replaceAll(Pattern.quote(":"), "_");
      if (env.element.hasAttr(attrName)) {
        try {
          Tree tree = Parser.parse(env.element.attr(attrName));
          if (tree.variables().size() > 0) {
            if (!addedUnder) {
              addedUnder = true;
            } else {
              env.writer.append(",");
            }
            env.writer.append("'").append(nameToUse).append("'");
          }
        } catch (ParseException pe) {
          env.feedback.warn(env.element, "attribute '" + attrName + "' has parser errors; " + pe.getMessage());
        }
      }
    }
    env.writer.append("]);").newline();
    for (String attrName : names) {
      String nameToUse = attrName;
      if (nameToUse.startsWith("parameter:")) {
        nameToUse = nameToUse.substring(10);
      } else if (nameToUse.startsWith("search:")) {
        nameToUse = nameToUse.substring(7);
      }
      nameToUse = nameToUse.replaceAll(Pattern.quote(":"), "_");
      if (env.element.hasAttr(attrName)) {
        String value = env.element.attr(attrName);
        try {
          Tree tree = Parser.parse(value);
          if (tree.hasAuto()) {
            env.feedback.warn(env.element, attrName + " has an auto variance which is not allowed in this context");
          }
          Map<String, String> vars = tree.variables();
          if (vars.size() > 0) {
            for (Map.Entry<String, String> ve : vars.entrySet()) {
              StatePath path = StatePath.resolve(ve.getValue(), env.stateVar);
              String subItem = env.pool.ask();
              env.writer.tab().append("$.Y2(").append(path.command).append(",").append(rxObj).append(",'").append(nameToUse).append("','").append(path.name).append("',").append("function(").append(subItem).append(") {").tabUp().newline();
              env.writer.tab().append(rxObj).append(".").append(nameToUse).append("=").append(tree.js(env.contextOf(attrName), subItem)).newline();
              env.writer.tab().append(rxObj).append(".__();").tabDown().newline();
              env.writer.tab().append("});").newline();
              env.pool.give(subItem);
            }
            _delayed = true;
          } else {
            env.writer.tab().append(rxObj).append(".").append(nameToUse).append("='").append(new Escaping(value).switchQuotes().go()).append("';").newline();
          }
        } catch (ParseException pe) {
          env.feedback.warn(env.element, "attribute '" + attrName + "' has parse exception; " + pe.getMessage());
        }
      } else {
        env.writer.tab().append(rxObj).append(".").append(nameToUse).append("=true;").newline();
      }
    }
    this.delayed = _delayed;
    for (String param : attrToRemove) {
      env.element.removeAttr(param);
    }
  }

  public static String[] pullParameters(Element element) {
    ArrayList<String> params = new ArrayList<>();
    for (Attribute attr : element.attributes()) {
      if (attr.getKey().startsWith("parameter:")) {
        params.add(attr.getKey());
      }
    }
    return params.toArray(new String[params.size()]);
  }

  public void finish() {
    if (!delayed) {
      env.writer.tab().append(rxObj).append(".__();").newline();
    }
  }
}
