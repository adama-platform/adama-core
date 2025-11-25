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

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.rxhtml.routing.Target;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

/** construct the ServerSideTarget from an element */
public class ServerSideTargetBuilder {
  public static boolean hasNoAuth(String authority, String agent) {
    return authority == null || agent == null || "?".equals(authority) || "?".equals(agent);
  }

  private static void universalHandle(ServerPageShell shell, Element toMunge, RemoteInlineResolver resolver, String agent, String authority, String space, String uri, TreeMap<String, List<String>> query, TreeMap<String, List<String>> bodyIfPost, String redirectPathIfNoPrinciple, Callback<Target> callback) {
    if (hasNoAuth(authority, agent) && redirectPathIfNoPrinciple != null) {
      TreeMap<String, String> headers = new TreeMap<>();
      headers.put("location", redirectPathIfNoPrinciple);
      callback.success(new Target(302, headers, null, null));
      return;
    }
    TreeMap<String, String> extHeaders = new TreeMap();
    extHeaders.put("x-adama-agent", agent);
    extHeaders.put("x-adama-authority", authority);
    extHeaders.put("x-adama-space", space);
    extHeaders.put("x-adama-uri", uri);

    Elements toInlineRaw = toMunge.getElementsByTag("remote-inline");
    ArrayList<Element> toInlineDirect = new ArrayList<>();
    ArrayList<Element> toInlineDelay = new ArrayList<>();
    for (Element element : toInlineRaw) {
      (element.hasAttr("delay") ? toInlineDelay : toInlineDirect).add(element);
    }

    if (toInlineRaw.size() == 0) {
      String result = shell.wrap(null, null, toMunge);
      TreeMap<String, String> headers = new TreeMap<>();
      headers.put("content-type", "text/html; charset=utf-8");
      callback.success(new Target(200, headers, result.getBytes(StandardCharsets.UTF_8), null));
    } else {
      AtomicReference<String> newTitle = new AtomicReference<>(null);
      AtomicReference<String> newContentType = new AtomicReference<>("text/html; charset=utf-8");
      AtomicReference<String> newLocation = new AtomicReference<>(null);
      AtomicReference<String> newIdentity = new AtomicReference<>(null);
      AtomicReference<String> newBodyForAlternativeContentType = new AtomicReference<>(null);
      AtomicReference<String> extMeta = new AtomicReference<>("");

      BiConsumer<ArrayList<Element>, Runnable> act = (elements, done) -> {
        for (int k = 0; k < elements.size(); k++) {
          final Element toReplace = elements.get(k);
          final boolean isPostForced = toReplace.hasAttr("forced");
          final String pathLeveraged = toReplace.attr("path");
          final RemoteInlineRequest request = new RemoteInlineRequest(shell.endpoint, pathLeveraged, query, extHeaders);
          final Callback<RemoteInlineResponse> callbackForResponse = new Callback<>() {
            @Override
            public void success(RemoteInlineResponse value) {
              synchronized (toMunge) {
                if (value.contentType != null) {
                  newContentType.set(value.contentType);
                  newBodyForAlternativeContentType.set(value.body);
                }
                if (value.body != null) {
                  Element newBody = Jsoup.parse(value.body).body();
                  Elements elements = newBody.getElementsByTag("ext-meta");
                  for (Element element : elements) {
                    extMeta.set(extMeta.get() + element.html());
                  }
                  elements.remove();
                  elements = newBody.getElementsByTag("title");
                  for (Element element : elements) {
                    newTitle.set(element.text());
                  }
                  elements.remove();
                  shell.injectFormHandles(newBody, pathLeveraged);
                  shell.rewriteInline(newBody);
                  int index = toReplace.siblingIndex();
                  toReplace.parent().insertChildren(index, newBody.childNodes());
                  toReplace.remove();
                }
                if (value.title != null) {
                  newTitle.set(value.title);
                }
                if (value.identity != null) {
                  newIdentity.set(value.identity);
                }
                if (value.redirect != null) {
                  String redirectToUse = value.redirect;
                  if (!redirectToUse.startsWith("/")) {
                    redirectToUse = "/" + redirectToUse;
                  }
                  newLocation.set(shell.rewriteHref(redirectToUse));
                }
              }
              done.run();
            }

            @Override
            public void failure(ErrorCodeException ex) {
              synchronized (toMunge) {
                toReplace.html("Failure:" + ex.code);
              }
              done.run();
            }
          };
          boolean used = false;
          if (bodyIfPost != null) {
            if (isPostForced) {
              resolver.post(request, new RemoteInlinePostBody(bodyIfPost), callbackForResponse);
              used = true;
            } else if (bodyIfPost.containsKey("__path")) {
              List<String> pathSelector = bodyIfPost.get("__path");
              if (pathSelector.size() == 1 && pathLeveraged.equals(pathSelector.get(0))) {
                resolver.post(request, new RemoteInlinePostBody(bodyIfPost), callbackForResponse);
                used = true;
              }
            }
          }
          if (!used) {
            resolver.get(request, callbackForResponse);
          }
        }
      };

      Runnable complete = () -> {
        TreeMap<String, String> headers = new TreeMap<>();
        if (newIdentity.get() != null) {
          headers.put("identity", newIdentity.get());
        }
        String redirectLocation = newLocation.get();
        if (redirectLocation != null) {
          headers.put("location", redirectLocation);
          // TOOD: figure out if it makes sense to do 301 ever, probably not
          callback.success(new Target(302, headers, null, null));
        } else {
          headers.put("content-type", newContentType.get());
          if (newContentType.get().startsWith("text/html")) {
            String result = shell.wrap(newTitle.get(), extMeta.get(), toMunge);
            callback.success(new Target(200, headers, result.getBytes(StandardCharsets.UTF_8), null));
          } else {
            callback.success(new Target(200, headers, newBodyForAlternativeContentType.get().getBytes(StandardCharsets.UTF_8), null));
          }
        }
      };

      Runnable runDelay = () -> {
        if (toInlineDelay.size() > 0) {
          AsyncCountDown latchDelay = new AsyncCountDown(toInlineDelay.size(), complete);
          act.accept(toInlineDelay, latchDelay::down);
        } else {
          complete.run();
        }
      };

      if (toInlineDirect.size() > 0) {
        AsyncCountDown latchDirect = new AsyncCountDown(toInlineDirect.size(), runDelay);
        act.accept(toInlineDirect, latchDirect::down);
      } else {
        runDelay.run();
      }
    }
  }

  public static ServerSideTarget build(ServerPageShell shell, Element element, String redirectPathIfNoPrinciple) {
    return new ServerSideTarget() {
      @Override
      public void get(RemoteInlineResolver resolver, String agent, String authority, String space, String uri, TreeMap<String, List<String>> query, Callback<Target> callback) {
        universalHandle(shell, element.clone(), resolver, agent, authority, space, uri, query, null, redirectPathIfNoPrinciple, callback);
      }

      @Override
      public void post(RemoteInlineResolver resolver, String agent, String authority, String space, String uri, TreeMap<String, List<String>> query, TreeMap<String, List<String>> body, Callback<Target> callback) {
        universalHandle(shell, element.clone(), resolver, agent, authority, space, uri, query, body, redirectPathIfNoPrinciple, callback);
      }

      @Override
      public long memory() {
        return 0;
      }
    };
  }

  public static class AsyncCountDown {
    public final Runnable event;
    public int at;

    public AsyncCountDown(int n, Runnable event) {
      this.at = n;
      this.event = event;
    }

    public void down() {
      if (_down()) {
        event.run();
      }
    }

    private synchronized boolean _down() {
      at--;
      if (at == 0) {
        return true;
      } else {
        return false;
      }
    }
  }
}
