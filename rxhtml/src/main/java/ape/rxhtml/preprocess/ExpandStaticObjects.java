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
package ape.rxhtml.preprocess;

import ape.rxhtml.preprocess.expand.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ape.common.Json;
import ape.rxhtml.preprocess.expand.*;
import ape.rxhtml.template.config.Feedback;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

public class ExpandStaticObjects {


  public static void expand(Document document, Feedback feedback) {
    // for generating unique throw-away values
    AtomicInteger genId = new AtomicInteger(1);

    { // phase 0: static tree cloning and stamping
      // extract all static-tree fragment elements
      HashMap<String, Element> treeFragments = new HashMap<>();
      for (Element element : document.getElementsByTag("static-tree-fragment")) {
        String name = element.attr("name");
        if (name != null) {
          treeFragments.put(name, element.clone());
        }
        element.remove();
      }

      // execute replacements
      for (Element element : document.getElementsByTag("static-tree-replace")) {
        String name = element.attr("name");
        if (name != null) {
          Element fragment = treeFragments.get(name);
          if (fragment != null) {
            Replacement.replace(element, fragment.clone().childNodes());
          }
        } else {
          element.remove();
        }
      }
    }

    // extract the configs from the document and provide a way to find/invent them
    HashMap<String, StaticConfig> configs = Extraction.staticConfigs(document);
    Function<String, StaticConfig> findConfig = (String name) -> {
      StaticConfig found = configs.get(name);
      if (found == null) {
        found = new StaticConfig(null);
        configs.put(name, found);
      }
      return found;
    };

    // convert the pages into object writes
    HashMap<String, ArrayList<ObjectWrite>> writes = new HashMap<>();

    Consumer<Element> handlePage = (page) -> {
      HashMap<String, HashMap<String, String>> properties = Extraction.propertiesByObject(page, feedback, genId);
      for (Map.Entry<String, HashMap<String, String>> entry : properties.entrySet()) {
        ArrayList<ObjectWrite> writesToObject = writes.get(entry.getKey());
        if (writesToObject == null) {
          writesToObject = new ArrayList<>();
          writes.put(entry.getKey(), writesToObject);
        }
        writesToObject.add(new ObjectWrite(findConfig.apply(entry.getKey()), entry.getValue()));
      }
    };
    for (Element page : document.getElementsByTag("page")) {
      handlePage.accept(page);
    }
    for (Element page : document.getElementsByTag("pseudo-page")) {
      handlePage.accept(page);
    }

    // assemble the objects
    TreeMap<String, ObjectNode> staticObjects = new TreeMap<>();
    for (Map.Entry<String, ArrayList<ObjectWrite>> entry : writes.entrySet()) {
      ArrayList<ObjectWrite> objectWrites = entry.getValue();
      objectWrites.sort(Comparator.comparing(o -> o.ordering));
      ObjectNode rootObject = Json.newJsonObject();
      staticObjects.put(entry.getKey(), rootObject);
      ArrayNode root = rootObject.putArray("pages");
      HashMap<String, ObjectNode> parents = new HashMap<>();
      // establish parents
      for (ObjectWrite write : objectWrites) {
        ObjectNode child = write.convertToNode();
        if (write.id != null) {
          parents.put(write.id, child);
        }
      }
      // construct the hierarchy
      for (ObjectWrite write : objectWrites) {
        if (write.parent != null) {
          ObjectNode parent = parents.get(write.parent);
          if (parent != null) {
            if (parent.has(write.config.children)) {
              ((ArrayNode) parent.get(write.config.children)).add(write.convertToNode());
            } else {
              parent.putArray(write.config.children).add(write.convertToNode());
              parent.put("has_" + write.config.children, true);
            }
          }
        } else {
          root.add(write.convertToNode());
        }
      }
    }
    staticObjects.putAll(Extraction.staticObjects(document));

    for (Element element : document.getElementsByTag("static-expand")) {
      String source = element.attr("source");
      if (source != null && staticObjects.containsKey(source)) {
        ObjectNode node = staticObjects.get(source);
        Expand.expand(element, source, node);
      }
    }
  }
}
