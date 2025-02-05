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
package ape.runtime.remote.client;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.Json;
import ape.runtime.contracts.Caller;
import ape.runtime.data.Key;
import ape.runtime.natives.NtDynamic;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.natives.NtResult;
import ape.runtime.natives.NtToDynamic;
import ape.runtime.remote.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** a generic web client that maps into the generic HTTP client */
public class GenericClient {
  private static final NtPrincipal SYSTEM = new NtPrincipal("client", "system");

  /** a call site */
  private class CallSite {
    public final String httpMethod;
    public final String endpoint;
    public final HeaderGroup headers;
    public final Function<NtToDynamic, String> url;

    public CallSite(String httpMethod, String endpoint, HeaderGroup headers, Function<NtToDynamic, String> url) {
      this.httpMethod = httpMethod;
      this.endpoint = endpoint;
      this.headers = headers;
      this.url = url;
    }
  }
  private final TreeMap<String, CallSite> sites;
  private final GenericClientBase base;

  public GenericClient(GenericClientBase base) {
    this.sites = new TreeMap<>();
    this.base = base;
  }

  public void register(String name, String httpMethod, String endpoint, HeaderGroup headers, Function<NtToDynamic, String> url) {
    sites.put(name, new CallSite(httpMethod, endpoint, headers, url));
  }

  private void route(int id, Caller caller, RemoteResult result) {
    caller.__getDeliverer().deliver(SYSTEM, new Key(caller.__getSpace(), caller.__getKey()), id, result, false, Callback.DONT_CARE_INTEGER);
  }

  private Callback<NtToDynamic> deliverFor(Caller caller, int id) {
    return new Callback<NtToDynamic>() {
      @Override
      public void success(NtToDynamic value) {
        RemoteResult result = new RemoteResult(value.to_dynamic().json, null, null);
        route(id, caller, result);
      }

      @Override
      public void failure(ErrorCodeException ex) {
        RemoteResult result = new RemoteResult(null, "failed", ex.code);
        route(id, caller, result);
      }
    };
  }

  public <T> NtResult<T> invoke(Caller caller, String name, RxCache cache, NtPrincipal who, NtToDynamic query, NtToDynamic bodyIfAvailable, Function<String, T> parser) {
    CallSite callsite = sites.get(name);
    final String url = callsite.endpoint + callsite.url.apply(query);

    ObjectNode dedupe = Json.newJsonObject();
    dedupe.put("method", callsite.httpMethod);
    dedupe.put("url", url);
    if (bodyIfAvailable != null) {
      dedupe.set("body", Json.parse(bodyIfAvailable.to_dynamic().json));
    }
    return cache.answer("http", callsite.httpMethod, who, new NtDynamic(dedupe.toString()), parser, (id, defunct) -> {
      base.execute(callsite.httpMethod.toUpperCase(), callsite.headers.headers, url, bodyIfAvailable, deliverFor(caller, id));
      return null;
    });
  }

  public <T> NtResult<T> invoke(Caller caller, String name, RxCache cache, NtPrincipal who, NtToDynamic query, Function<String, T> parser) {
    return invoke(caller, name, cache, who, query, null, parser);
  }

  public static String URL(String pattern, NtToDynamic query) {
    String url = pattern;
    String json = query.to_dynamic().json;
    ArrayList<String> queryFragments = new ArrayList<>();
    ObjectNode parameters = Json.parseJsonObject(json);
    Iterator<Map.Entry<String, JsonNode>> it = parameters.fields();
    while (it.hasNext()) {
      Map.Entry<String, JsonNode> entry = it.next();
      if (url.contains("[%" + entry.getKey() + "]")) {
        url = url.replaceAll(Pattern.quote("[%" + entry.getKey() + "]"), Matcher.quoteReplacement(entry.getValue().toString()));
      } else {
        queryFragments.add(entry.getKey() + "=" + URLEncoder.encode(entry.getValue().toString()));
      }
    }
    if (queryFragments.size() > 0) {
      url += "?" + String.join("&", queryFragments);
    }
    return url;
  }
}
