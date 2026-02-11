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
package ape.web.features;

import ape.web.client.SimpleHttpRequest;
import ape.web.client.SimpleHttpRequestBody;
import ape.web.client.StringCallbackHttpResponder;
import ape.web.client.WebClientBase;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ape.ErrorCodes;
import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.Json;
import ape.common.metrics.RequestResponseMonitor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.helpers.NOPLogger;

import java.net.InetAddress;
import java.net.URL;
import java.util.Locale;
import java.util.TreeMap;

/**
 * URL preview generator extracting OpenGraph metadata for link unfurling.
 * Fetches HTML content, parses title and og:* meta tags using Jsoup,
 * and returns structured JSON with title, description, image, and URL details.
 * Useful for generating rich link previews in chat or social applications.
 */
public class UrlSummaryGenerator {

  private static boolean shredUrl(String url, ObjectNode node) {
    try {
      URL parsed = new URL(url);
      node.put("host", parsed.getHost());
      node.put("path", parsed.getPath());
      return false;
    } catch (Exception ex) {
      return true;
    }
  }

  /** Validate that the URL uses https, has a proper hostname (not localhost or an IP address) */
  static boolean isValidUrl(String url) {
    try {
      URL parsed = new URL(url);
      if (!"https".equalsIgnoreCase(parsed.getProtocol())) {
        return false;
      }
      String host = parsed.getHost();
      if (host == null || host.isEmpty()) {
        return false;
      }
      // reject localhost
      if ("localhost".equalsIgnoreCase(host)) {
        return false;
      }
      // reject IP addresses (both IPv4 and IPv6)
      try {
        InetAddress addr = InetAddress.getByName(host);
        // if getByName succeeds and the string form matches the host, it's an IP literal
        if (addr.getHostAddress().equals(host) || host.startsWith("[")) {
          return false;
        }
      } catch (Exception ex) {
        // not an IP literal - this is good, means it's a hostname
      }
      // must contain at least one dot (a real domain, not a bare name)
      if (!host.contains(".")) {
        return false;
      }
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  public static void summarize(WebClientBase base, String url, Callback<ObjectNode> callback) {
    if (!isValidUrl(url)) {
      callback.failure(new ErrorCodeException(ErrorCodes.URL_SUMMARY_INVALID_URL));
      return;
    }
    SimpleHttpRequest request = new SimpleHttpRequest("GET", url, new TreeMap<>(), SimpleHttpRequestBody.EMPTY);
    base.executeShared(request, new StringCallbackHttpResponder(NOPLogger.NOP_LOGGER, RequestResponseMonitor.UNMONITORED, new Callback<String>() {
      @Override
      public void success(String html) {
        ObjectNode summary = Json.newJsonObject();
        summary.put("url", url);
        summary.put("title", url);
        summary.put("description", "");

        try {
          Document document = Jsoup.parse(html);
          Elements titles = document.getElementsByTag("title");
          if (titles.size() > 0) {
            summary.put("title", titles.get(0).text());
          }
          for (Element element : document.getElementsByTag("meta")) {
            String property = element.attr("property");
            if (property == null) {
              continue;
            }
            String content = element.attr("content");
            if (content == null) {
              continue;
            }
            property = property.toLowerCase(Locale.ENGLISH).trim();
            content = content.trim();
            if (property.startsWith("og:")) {
              summary.put(property.substring(3), content);
            }
          }
          if (shredUrl(summary.get("url").textValue(), summary)) {
            shredUrl(url, summary);
          }
        } catch (Exception failedToParseAndShred) {
          // do nothing
        }
        callback.success(summary);
      }

      @Override
      public void failure(ErrorCodeException ex) {
        callback.failure(ex);
      }
    }));
  }
}
