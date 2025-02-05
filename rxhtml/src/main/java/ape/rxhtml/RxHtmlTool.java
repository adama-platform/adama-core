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

import ape.rxhtml.server.ServerPageShell;
import ape.rxhtml.server.ServerSideTargetBuilder;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ape.common.Json;
import ape.common.template.Parser;
import ape.common.template.Settings;
import ape.common.template.tree.T;
import ape.rxhtml.routing.Target;
import ape.rxhtml.routing.Instructions;
import ape.rxhtml.routing.Table;
import ape.rxhtml.template.Environment;
import ape.rxhtml.template.Root;
import ape.rxhtml.template.Shell;
import ape.rxhtml.template.config.ShellConfig;
import ape.rxhtml.typing.ViewSchemaBuilder;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/** the rxhtml tool for converting rxhtml into javascript templates */
public class RxHtmlTool {
  public static RxHtmlBundle convertStringToTemplateForest(String str, File types, ShellConfig config) {
    Environment env = Environment.fresh(config.feedback, config.environment);
    TypeChecker.typecheck(str, types, config.feedback);
    Document document = Loader.parseForest(str, config.feedback, ProductionMode.Web);
    Root.start(env, buildCustomJavaScript(document));
    String style = buildInternStyle(document);
    ArrayList<String> defaultRedirects = getDefaultRedirect(document);
    Shell shell = new Shell(config);
    shell.scan(document);
    ViewSchemaBuilder vb = new ViewSchemaBuilder(document, config.feedback);
    for (Element element : document.getElementsByTag("template")) {
      Root.template(env.element(element, true, null));
    }
    Table table = new Table();
    ArrayList<String> urisToMapToEntire = new ArrayList<>();
    for (Element element : document.getElementsByTag("page")) {
      urisToMapToEntire.add(element.attr("uri"));
      Root.page(env.element(element, true, null), defaultRedirects);
    }
    // TODO: do warnings about cross-page linking, etc...
    String javascript = Root.finish(env);

    // build the table
    TreeMap<String, String> entireHeaders = new TreeMap<>();
    entireHeaders.put("content-type", "text/html; charset=UTF-8");
    entireHeaders.put("cache-control", "public, max-age=" + config.cacheMaxAgeSeconds + ", min-fresh=" + config.cacheMaxAgeSeconds);
    Target entire = new Target(200, entireHeaders, shell.makeShell(javascript, style).getBytes(StandardCharsets.UTF_8), null);
    for (String uri : urisToMapToEntire) {
      table.add(Instructions.parse(uri), entire);
    }
    for (Element element : document.getElementsByTag("static-rewrite")) {
      String uri = element.attr("uri");
      String location = element.attr("location");
      int status = "302".equals(element.attr("status")) ? 302 : 301;
      if (location != null) {
        table.add(Instructions.parse(uri), createRedirectRule(status, location));
      }
    }
    ServerPageShell serverPageShell = ServerPageShell.of(document, config.feedback, config.environment);

    String redirectPathNoAuth = null;
    for (Element element : document.getElementsByTag("server-page")) {
      if (element.hasAttr("default-redirect-source") && element.hasAttr("uri")) {
        redirectPathNoAuth = element.attr("uri");
      }
    }

    for (Element element : document.getElementsByTag("server-page")) {
      String uri = element.attr("uri");
      String redirectPathIfNoPrinciple = null;
      if (element.hasAttr("authenticate")) {
        redirectPathIfNoPrinciple = redirectPathNoAuth;
      }
      table.add(Instructions.parse(uri), ServerSideTargetBuilder.build(serverPageShell, element, redirectPathIfNoPrinciple));
    }
    Diagnostics diagnostics = new Diagnostics(env.getCssFreq(), env.tasks, vb.results, javascript.length());
    return new RxHtmlBundle(javascript, style, shell, diagnostics, table);
  }

  public static Target createRedirectRule(int status, String location) {
    final T locationTemplate = Parser.parse(location);
    return new Target(999, null, null, (t, cap) -> {
      TreeMap<String, String> headers = new TreeMap<>();
      StringBuilder result = new StringBuilder();
      ObjectNode params = Json.newJsonObject();
      for (Map.Entry<String, String> c : cap.entrySet()) {
        params.put(c.getKey(), c.getValue());
      }
      locationTemplate.render(new Settings(false), params, result);
      headers.put("location", result.toString());
      return new Target(status, headers, null, null);
    });
  }

  public static String buildCustomJavaScript(Document document) {
    StringBuilder customjs = new StringBuilder();
    ArrayList<Element> axe = new ArrayList<>();
    for (Element element : document.getElementsByTag("script")) {
      if (element.hasAttr("is-custom")) {
        customjs.append(element.html().trim());
        axe.add(element);
      }
    }
    for (Element toAxe : axe) {
      toAxe.remove();
    }
    return customjs.toString();
  }

  public static String buildInternStyle(Document document) {
    ArrayList<Element> axe = new ArrayList<>();
    StringBuilder style = new StringBuilder();
    for (Element element : document.getElementsByTag("style")) {
      style.append(element.html().trim()).append(" ");
      axe.add(element);
    }
    for (Element toAxe : axe) {
      toAxe.remove();
    }
    return style.toString().trim();
  }

  public static ArrayList<String> getDefaultRedirect(Document document) {
    ArrayList<String> defaults = new ArrayList<>();
    for (Element element : document.getElementsByTag("page")) {
      if (element.hasAttr("default-redirect-source")) {
        defaults.add(Instructions.parse(element.attr("uri")).formula);
      }
    }
    return defaults;
  }
}
