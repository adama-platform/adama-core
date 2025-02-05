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
package ape.rxhtml;

import ape.rxhtml.routing.Instructions;
import ape.rxhtml.template.config.Feedback;
import ape.rxhtml.typing.RxRootEnvironment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.util.TreeSet;

/** entry point for the type checker */
public class TypeChecker {
  /** Given a bundled forest, produce feedback for the developer */
  public static void typecheck(String forest, File input, Feedback feedback) {
    Document document = Loader.parseForest(forest, feedback, ProductionMode.Web);
    warnDuplicatePages(document, feedback);
    if (input != null && input.exists() && input.isDirectory()) {
      RxRootEnvironment env = new RxRootEnvironment(forest, input, feedback);
      env.check();
    }
  }

  public static void warnDuplicatePages(Document document, Feedback feedback) {
    TreeSet<String> paths = new TreeSet<>();
    for (Element element : document.getElementsByTag("page")) {
      if (!element.hasAttr("uri")) {
        feedback.warn(element, "page is missing a uri");
        continue;
      }
      String normalizedUri = Instructions.parse(element.attr("uri")).normalized;
      if (paths.contains(normalizedUri)) {
        feedback.warn(element, "page has duplicate path of '" + normalizedUri + "'");
      }
      paths.add(normalizedUri);
    }
  }
}
