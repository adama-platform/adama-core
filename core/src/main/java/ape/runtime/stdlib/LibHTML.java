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

import ape.runtime.json.JsonStreamWriter;
import ape.runtime.natives.NtDynamic;
import ape.runtime.natives.NtMaybe;
import ape.translator.reflect.Extension;
import ape.translator.reflect.HiddenType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.PrimitiveIterator;

/**
 * Standard library for HTML/JSON bidirectional conversion in Adama documents.
 * Converts HTML fragments to a JSON tree representation (tag/attributes/children)
 * and back. Also provides HTML attribute value escaping for safe output.
 */
public class LibHTML {

  // support: which HTML5 tags self close
  private static boolean __selfClosing(String tag) {
    switch (tag.toLowerCase(Locale.ENGLISH).trim()) {
      case "area":
      case "base":
      case "br":
      case "col":
      case "embed":
      case "hr":
      case "img":
      case "input":
      case "link":
      case "meta":
      case "param":
      case "source":
      case "track":
      case "wbr":
        return true;
      default:
        return false;
    }
  }

  // the recursive conversion function (HTML -> JSON)
  private static void __convertElement(Element element, JsonStreamWriter writer) {
    // <element>
    writer.beginObject();
    // [tag name]
    writer.writeObjectFieldIntro("t");
    writer.writeString(element.tagName());

    // [attributes]
    if (element.attributes().size() > 0) {
      writer.writeObjectFieldIntro("a");
      writer.beginObject();
      for (Attribute attribute : element.attributes()) {
        writer.writeObjectFieldIntro(attribute.getKey());
        if (attribute.hasDeclaredValue()) {
          writer.writeString(attribute.getValue());
        } else {
          writer.writeNull();
        }
      }
      writer.endObject();
    }

    if (!__selfClosing(element.tagName()) || element.childNodes().size() > 0) {
      // [children]
      writer.writeObjectFieldIntro("c");
      writer.beginArray();
      for (Node child : element.childNodes()) {
        if (child instanceof Element) {
          __convertElement((Element) child, writer);
        } else if (child instanceof TextNode) {
          writer.writeString(((TextNode) child).text());
        } // don't care about other things at this time
      }
      writer.endArray();
    }
    // </element>
    writer.endObject();
  }

  /** Convert an HTML string to a JSON tree by extracting the element matching rootTag. Returns empty if zero or multiple matches. */
  @Extension
  public static @HiddenType(clazz = NtDynamic.class) NtMaybe<NtDynamic> convertHTMLtoJSON(String html, String rootTag) {
    Document doc = Jsoup.parse(html);
    Elements elements = doc.getElementsByTag(rootTag);
    if (elements.size() == 1) {
      JsonStreamWriter writer = new JsonStreamWriter();
      __convertElement(elements.get(0), writer);
      return new NtMaybe<>(new NtDynamic(writer.toString()));
    }
    return new NtMaybe<>();
  }

  /** Escape special characters in a string for safe use as an HTML attribute value. Escapes &lt; &gt; &amp; &quot; &apos;. */
  @Extension
  public static String escapeHTMLAttributeValue(String value) {
    StringBuilder result = new StringBuilder();
    PrimitiveIterator.OfInt it = value.codePoints().iterator();
    while (it.hasNext()) {
      int cp = it.nextInt();
      switch (cp) {
        case '"':
          result.append("&quot;");
          break;
        case '\'':
          result.append("&apos;");
          break;
        case '&':
          result.append("&amp;");
          break;
        case '<':
          result.append("&lt;");
          break;
        case '>':
          result.append("&gt;");
          break;
        default:
          result.append(Character.toString(cp));
      }
    }
    return result.toString();
  }

  // the recursive conversion function (JSON -> HTML)
  private static void __convertTree(Object tree, StringBuilder html) {
    if (tree == null) {
      return;
    }
    if (tree instanceof Map) {
      Map<String, Object> element = (Map<String, Object>) tree;
      Object tagName = element.get("t");
      if (tagName != null && tagName instanceof String) {
        html.append("<").append(tagName);
        Object attrRaw = element.get("a");
        if (attrRaw != null && attrRaw instanceof Map) {
          Map<String, Object> attributes = (Map<String, Object>) attrRaw;
          for(Map.Entry<String, Object> attr : attributes.entrySet()) {
            html.append(" ").append(attr.getKey());
            Object valRaw = attr.getValue();
            if (valRaw != null && valRaw instanceof String) {
              String value = (String) valRaw;
              html.append("=\"").append(escapeHTMLAttributeValue(value)).append("\"");
            }
          }
        }
        Object children = element.get("c");
        if (children == null) {
          html.append(" />");
        } else {
          html.append(">");
          __convertTree(element.get("c"), html);
          html.append("</").append(tagName).append(">");
        }
      }
    } else if (tree instanceof ArrayList) {
      ArrayList<Object> children = (ArrayList<Object>) tree;
      for (Object child : children) {
        __convertTree(child, html);
      }
    } else {
      html.append(tree);
    }
  }

  /** Convert a JSON tree (NtDynamic with tag/attributes/children structure) back into an HTML string. */
  @Extension
  public static String convertJSONtoHTML(NtDynamic json) {
    StringBuilder html = new StringBuilder();
    __convertTree(json.cached(), html);
    return html.toString();
  }
}
