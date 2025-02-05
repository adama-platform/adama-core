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

import ape.rxhtml.template.Environment;
import ape.rxhtml.template.Root;
import ape.rxhtml.template.config.Feedback;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class CapacitorJSShell {
  private String domainOverride;
  private boolean devmode;
  private final Feedback feedback;
  private boolean multiDomain;
  private String startingPath;
  private String betaSuffix;
  private String prodSuffix;

  public CapacitorJSShell(Feedback feedback) {
    this.feedback = feedback;
    this.domainOverride = null;
    this.devmode = false;
    this.multiDomain = false;
    this.startingPath = null;
  }

  public void enableDevMode() {
    this.devmode = true;
  }

  public void setDomain(String domainOverride) {
    this.domainOverride = domainOverride;
    this.betaSuffix = domainOverride;
    this.prodSuffix = domainOverride;
  }

  public void setMultiDomain(String startingPath, String betaSuffix, String prodSuffix) {
    this.multiDomain = true;
    this.startingPath = startingPath;
    this.betaSuffix = betaSuffix;
    this.prodSuffix = prodSuffix;
  }


  public String make(String forest) throws Exception {
    StringBuilder sb = new StringBuilder();
    Document document = Loader.parseForest(forest, feedback, ProductionMode.MobileApp);
    Element mobileShell = findMobileShell(document);
    String workerIdentity = mobileShell.hasAttr("worker-identity-name") ? mobileShell.attr("worker-identity-name") : "default";
    sb.append("<!DOCTYPE html>\n<html");
    if (mobileShell.hasAttr("html-class")) {
      sb.append(" class=\"").append(mobileShell.attr("html-class")).append("\"");
    }
    sb.append(">\n");
    sb.append(" <head>\n");
    String defaultTitle = null;
    for (Element element : mobileShell.getElementsByTag("title")) {
      defaultTitle = element.text();
    }
    if (defaultTitle != null) {
      sb.append("  <title>").append(defaultTitle).append("</title>\n");
    }
    for (Element element : mobileShell.getElementsByTag("meta")) {
      sb.append("  ").append(element.toString()).append("\n");
    }
    for (Element element : mobileShell.getElementsByTag("link")) {
      sb.append("  ").append(element.toString()).append("\n");
    }
    if (devmode) {
      sb.append("  <script src=\"/connection.js\"></script>").append("\n");
      sb.append("  <script src=\"/tree.js\"></script>").append("\n");
      sb.append("  <script src=\"/rxhtml.js\"></script>").append("\n");
    } else {
      sb.append("  <script src=\"/libadama.js\"></script>").append("\n");
    }
    sb.append("  <script src=\"/rxcapacitor.js\"></script>").append("\n");
    for (Element element : mobileShell.getElementsByTag("script")) {
      sb.append("  ").append(element.toString()).append("\n");
    }
    Environment env = Environment.fresh(feedback, "mobile");
    Root.start(env, RxHtmlTool.buildCustomJavaScript(document));
    for (Element element : document.getElementsByTag("template")) {
      // TODO: detect if this template is used by any mobile pages
      Root.template(env.element(element, true, null));
    }
    ArrayList<String> defaultRedirects = RxHtmlTool.getDefaultRedirect(document);
    for (Element element : document.getElementsByTag("page")) {
      // TODO: discriminate for a mobile page (and also, get a dependency tree of templates)
      Root.page(env.element(element, true, null), defaultRedirects);
    }
    String javascript = Root.finish(env).trim();
    sb.append("  <script>\n").append(javascript).append("\n  </script>\n");
    String internStyle = RxHtmlTool.buildInternStyle(document).trim();
    if (internStyle.length() > 0) {
      sb.append("  <style>\n").append(internStyle).append("\n </style>\n");
    }
    sb.append(" </head>\n");
    sb.append("<body></body>\n<script>\n");
    if (multiDomain) {
      sb.append("  RxHTML.mobileInitMultiDomain(\"").append(startingPath).append("\",\"").append(betaSuffix).append("\",\"").append(prodSuffix).append("\");\n");
    } else {
      sb.append("  RxHTML.mobileInit(\"").append(domainOverride).append("\");\n");
      sb.append("  RxHTML.init();\n");
    }
    sb.append("  LinkCapacitor(RxHTML, \"").append(workerIdentity).append("\");\n");
    sb.append("</script>\n</html>\n");
    return sb.toString();
  }

  public static Element findMobileShell(Document document) throws Exception {
    Elements elements = document.getElementsByTag("mobile-shell");
    if (elements.size() == 1) {
      return elements.get(0);
    }
    throw new Exception("failed to find a solo <mobile-shell> element");
  }
}
