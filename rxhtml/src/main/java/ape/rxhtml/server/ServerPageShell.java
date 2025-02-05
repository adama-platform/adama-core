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
package ape.rxhtml.server;

import ape.rxhtml.template.config.Feedback;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.QueryStringEncoder;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** the shell over a server generated page */
public class ServerPageShell {
  private Element shell;
  public final String endpoint;
  public final TreeMap<String, List<LinkRewrite>> rewrites;

  private ServerPageShell(Element shell, String endpoint, TreeMap<String, List<LinkRewrite>> rewrites) {
    this.shell = shell;
    this.endpoint = endpoint;
    this.rewrites = rewrites;
  }

  public static ServerPageShell of(Document document, Feedback feedback, String environment) {
    Elements shells = document.getElementsByTag("server-shell");
    Element _shell = null;
    for (Element shell : shells) {
      if (_shell == null) {
        _shell = shell;
      } else {
        feedback.warn(shell, "extra shell, not used");
      }
    }
    String _endpoint = null;
    for (Element endpoint : document.getElementsByTag("remote-config")) {
      if (endpoint.hasAttr("endpoint")) {
        if (endpoint.hasAttr("env")) {
          if (endpoint.attr("env").equals(environment)) {
            _endpoint = endpoint.attr("endpoint");
          }
        } else {
          _endpoint = endpoint.attr("endpoint");
        }
      }
    }
    TreeMap<String, List<LinkRewrite>> rewrites = new TreeMap<>();
    for (Element rewrite : document.getElementsByTag("remote-rewrite")) {
      String src = rewrite.attr("src");
      String dest = rewrite.attr("dest");
      List<LinkRewrite> prior = rewrites.get(src);
      if (prior == null) {
        prior = new ArrayList<>();
        rewrites.put(src, prior);
      }
      prior.add(new LinkRewrite(dest));
    }
    return new ServerPageShell(_shell, _endpoint, rewrites);
  }

  public String rewriteHref(String href) {
    QueryStringDecoder decoded = new QueryStringDecoder(href);
    TreeMap<String, List<String>> map = new TreeMap<>();
    map.putAll(decoded.parameters());
    List<LinkRewrite> candidates = rewrites.get(decoded.path());
    if (candidates != null) {
      LinkRewrite winner = null;
      int winnerScore = 0;
      for (LinkRewrite candidate : candidates) {
        int candidateScore = candidate.score(map);
        if (candidateScore > winnerScore) {
          winnerScore = candidateScore;
          winner = candidate;
        }
      }
      if (winner != null) {
        String uri = winner.eval(map);
        if (map.size() > 0) {
          QueryStringEncoder encoder = new QueryStringEncoder(uri);
          for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            for (String value : entry.getValue()) {
              encoder.addParam(entry.getKey(), value);
            }
          }
          uri = encoder.toString();
        }
        return uri;
      }
    }
    return href;
  }

  public void rewriteInline(Element body) {
    for (Element link : body.getElementsByTag("a")) {
      if (link.hasAttr("href")) {
        link.attr("href", rewriteHref(link.attr("href")));
      }
    }
    for (Element link : body.getElementsByTag("form")) {
      if (link.hasAttr("action")) {
        link.attr("action", rewriteHref(link.attr("action")));
      }
    }
  }

  public void injectFormHandles(Element body, String pathUsed) {
    for (Element form : body.getElementsByTag("form")) {
      Element input = form.appendElement("input");
      input.attr("name", "__path");
      input.attr("type", "hidden");
      if (form.hasAttr("action")) {
        input.attr("value", form.attr("action"));
      } else {
        input.attr("value", pathUsed);
      }
    }
  }

  public String wrap(String titleOverride, Element body) {
    StringBuilder sb = new StringBuilder();
    if (shell != null) {
      sb.append("<!DOCTYPE html>\n<html");
      if (shell.hasAttr("html-class")) {
        sb.append(" class=\"").append(shell.attr("html-class")).append("\"");
      }
      sb.append(">\n<head>");
      String defaultTitle = null;
      for (Element element : shell.getElementsByTag("title")) {
          defaultTitle = element.text();
      }
      if (titleOverride != null) {
        defaultTitle = titleOverride;
      }
      if (defaultTitle != null) {
        sb.append("<title>").append(defaultTitle).append("</title>");
      }
      for (Element element : shell.getElementsByTag("meta")) {
        sb.append(element);
      }
      for (Element element : shell.getElementsByTag("link")) {
        sb.append(element);
      }
      for (Element element : shell.getElementsByTag("script")) {
        sb.append(element);
      }
    } else {
      sb.append("<!DOCTYPE html>\n<html>\n<head>");
    }
    if (shell != null) {
      sb.append("<body");
      if (shell.hasAttr("body-class")) {
        sb.append(" class=\"").append(shell.attr("body-class")).append("\"");
      }
      sb.append(">");
    } else {
      sb.append("<body>");
    }
    sb.append(body.html());
    sb.append("</body></html>");
    return sb.toString();
  }
}
