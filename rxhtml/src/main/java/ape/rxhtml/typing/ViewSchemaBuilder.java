/*
* Adama Platform and Language
* Copyright (C) 2021 - 2025 by Adama Platform Engineering, LLC
* 
* This program is free software for non-commercial purposes: 
* you can redistribute it and/or modify it under the terms of the 
* GNU Affero General Public License as published by the Free Software Foundation,
* either version 3 of the License, or (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
* 
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package ape.rxhtml.typing;

import com.fasterxml.jackson.databind.node.ObjectNode;
import ape.common.Json;
import ape.rxhtml.acl.Parser;
import ape.rxhtml.acl.commands.Command;
import ape.rxhtml.atl.ParseException;
import ape.rxhtml.routing.Instructions;
import ape.rxhtml.template.Base;
import ape.rxhtml.template.config.Feedback;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Stack;
import java.util.TreeMap;

/** construct a view schema */
public class ViewSchemaBuilder {
  private final Document document;
  private final Feedback feedback;
  private final TreeMap<String, Elements> templates;
  private Stack<Elements> fragments;

  public final ObjectNode results;

  public ViewSchemaBuilder(Document document, Feedback feedback) {
    this.document = document;
    this.feedback = feedback;
    this.templates = new TreeMap<>();
    for (Element element : document.getElementsByTag("template")) {
      templates.put(element.attr("name"), element.children());
    }
    this.fragments = new Stack<>();
    this.results = Json.newJsonObject();
    for (Element element : document.getElementsByTag("page")) {
      Instructions uriInstructions = Instructions.parse(element.attr("uri"));
      String normalizedUri = uriInstructions.normalized;
      ViewScope vs = ViewScope.makeRoot();
      vs.types.putAll(uriInstructions.types);
      buildSchema(element.children(), vs);
      vs.fill(results.putObject(normalizedUri));
    }
  }

  public void buildSchema(Elements children, ViewScope schema) {
    if (children == null) {
      return;
    }
    for (Element child : children) {
      if ("fragment".equalsIgnoreCase(child.tagName())) {
        buildSchema(fragments.peek(), schema);
        continue;
      }
      if ("lookup".equalsIgnoreCase(child.tagName())) {
        schema.write(child.attr("path"), "lookup", true);
        continue;
      }
      boolean expand = child.hasAttr("rx:expand-view-state");
      ViewScope current = schema;
      if (expand) {
        if (child.hasAttr("rx:scope")) {
          current.write(child.attr("rx:scope"), "object", false);
          current = current.eval(child.attr("rx:scope"));
        }
        if (child.hasAttr("rx:iterate")) {
          current.write(child.attr("rx:iterate"), "list", false);
          current = current.eval(child.attr("rx:iterate")).child("#items");
        }
      }
      for (String ifvariant : new String[] { "rx:if", "rx:ifnot" }) {
        if (child.hasAttr(ifvariant)) {
          String path = child.attr(ifvariant);
          int kEq = path.indexOf('=');
          if (kEq > 0) {
            String pathL = path.substring(0, kEq);
            String pathR = path.substring(kEq + 1);
            current.write(pathL, "cmpval", false);
            current.write(pathR, "cmpval", false);
          } else {
            current.write(path, "bool", false);
          }
        }
      }
      // TODO: IF/IFNOT (kind of tricky)
      for (String event : Base.EVENTS) {
        if (child.hasAttr("rx:" + event)) {
          try {
            for (Command command : Parser.parse(child.attr("rx:" + event))) {
              command.writeTypes(current);
            }
          } catch (ParseException pe) {
          }
        }
      }
      for(Attribute attr : child.attributes()) {
        try {
          ape.rxhtml.atl.Parser.parse(attr.getValue()).writeTypes(current);
        } catch (ParseException pe) {
        }
      }
      if (child.hasAttr("rx:template")) {
        fragments.push(child.children());
        buildSchema(templates.get(child.attr("rx:template")), current);
        fragments.pop();
      } else {
        buildSchema(child.children(), current);
      }
    }
  }
}
