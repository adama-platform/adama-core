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

import ape.common.Platform;
import ape.rxhtml.template.config.ShellConfig;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/** a class for pulling out the first and (hopefully) only shell */
public class Shell {
  private final ShellConfig config;
  private Element shell;

  public Shell(ShellConfig config) {
    this.config = config;
    this.shell = null;
  }

  public void scan(Document document) {
    for (Element element : document.getElementsByTag("shell")) {
      if (this.shell == null) {
        this.shell = element;
      } else {
        config.feedback.warn(element, "A duplicate shell was found");
      }
    }
  }

  public String makeShell(String javascript, String style) {
    StringBuilder sb = new StringBuilder();
    StringBuilder scripts = new StringBuilder();
    boolean worker = false;
    String workerIdentity = "default";
    if (shell != null) {
      worker = "true".equalsIgnoreCase(shell.attr("worker"));
      sb.append("<!DOCTYPE html>\n<html");
      if (shell.hasAttr("html-class")) {
        sb.append(" class=\"").append(shell.attr("html-class")).append("\"");
      }
      if (shell.hasAttr("worker-identity-name")) {
        workerIdentity = shell.attr("worker-identity-name");
      }
      sb.append(">\n<head>");
      String defaultTitle = null;
      for (Element element : shell.getElementsByTag("title")) {
        if (config.includeInShell(element)) {
          defaultTitle = element.text();
        }
      }
      if (defaultTitle != null) {
        sb.append("<title>").append(defaultTitle).append("</title>");
      }
      for (Element element : shell.getElementsByTag("meta")) {
        if (config.includeInShell(element)) {
          sb.append(element);
        }
      }
      for (Element element : shell.getElementsByTag("link")) {
        if (config.includeInShell(element)) {
          sb.append(element);
        }
      }
      for (Element element : shell.getElementsByTag("script")) {
        if (config.includeInShell(element)) {
          scripts.append(element);
        }
      }
    } else {
      sb.append("<!DOCTYPE html>\n<html>\n<head>");
    }
    if (config.useLocalAdamaJavascript) {
      sb.append("<script src=\"/" + System.currentTimeMillis() + "/devlibadama.js\"></script>");
    } else {
      sb.append("<script src=\"/libadama.js/"+ config.version +".js\"></script>");
    }
    sb.append("<script>\n\n").append(javascript).append("\n\n</script>");
    sb.append("<style>\n\n").append(style).append("\n\n</style>");
    sb.append(scripts);
    sb.append("</head>");
    if (shell != null) {
      sb.append("<body");
      if (shell.hasAttr("body-class")) {
        sb.append(" class=\"").append(shell.attr("body-class")).append("\"");
      }
      sb.append(">");
    } else {
      sb.append("<body>");
    }
    sb.append("</body><script>\n");
    sb.append("  RxHTML.init();\n");
    if (worker) {
      if (config.useLocalAdamaJavascript) {
        sb.append("  RxHTML.worker(\""+workerIdentity+"\",\"/" + Platform.JS_VERSION + "/devlibadama-worker.js\",'").append(Platform.JS_VERSION).append("');\n");
      } else {
        sb.append("  RxHTML.worker(\""+workerIdentity+"\",\"/libadama-worker.js/" + Platform.JS_VERSION + ".js\",'").append(Platform.JS_VERSION).append("');\n");
      }
    }
    sb.append("</script></html>");
    return sb.toString();
  }
}
