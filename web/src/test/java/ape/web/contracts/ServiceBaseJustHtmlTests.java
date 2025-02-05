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
package ape.web.contracts;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.web.io.ConnectionContext;
import ape.web.io.JsonResponder;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ServiceBaseJustHtmlTests {
  @Test
  public void coverage() throws Exception {
    ServiceBase base = ServiceBase.JUST_HTTP(new HttpHandler() {

      @Override
      public void handle(ConnectionContext context, Method method, String identity, String uri, TreeMap<String, String> headers, String parametersJson, String body, Callback<HttpResult> callback) {
        switch (method) {
          case PUT:
            handlePost(uri, headers, parametersJson, body, callback);
            return;
          case OPTIONS:
            handleOptions(uri, headers, parametersJson, callback);
            return;
          case DELETE:
            handleDelete(uri, headers, parametersJson, callback);
            return;
          case GET:
          default:
            handleGet(uri, headers, parametersJson, callback);
        }
      }

      public void handleOptions(String uri, TreeMap<String, String> headers, String parametersJson, Callback<HttpResult> callback) {
        callback.success(new HttpResult(200, "","".getBytes(StandardCharsets.UTF_8), uri.equalsIgnoreCase("/opt=yes")));
      }

      public void handleGet(String uri, TreeMap<String, String> headers, String parametersJson, Callback<HttpResult> callback) {
        callback.success(new HttpResult(200, "yay", "yay".getBytes(StandardCharsets.UTF_8), true));
      }

      public void handleDelete(String uri, TreeMap<String, String> headers, String parametersJson, Callback<HttpResult> callback) {
        callback.success(new HttpResult(200, "yay", "yay".getBytes(StandardCharsets.UTF_8), true));
      }

      public void handlePost(String uri, TreeMap<String, String> headers, String parametersJson, String body, Callback<HttpResult> callback) {
        callback.success(new HttpResult(200, "post", "post".getBytes(StandardCharsets.UTF_8), true));
      }

      @Override
      public void handleDeepHealth(Callback<String> callback) {
        callback.success("COVERAGE");
      }
    });
    base.establish(null).execute(null, new JsonResponder() {
      @Override
      public void stream(String json) {

      }

      @Override
      public void finish(String json) {

      }

      @Override
      public void error(ErrorCodeException ex) {

      }
    });
    base.establish(null).keepalive();
    base.establish(null).kill();
    base.assets();
    CountDownLatch latch = new CountDownLatch(4);
    ConnectionContext context = new ConnectionContext("origin", "ip", "ua", new TreeMap<>());
    base.http().handle(context, HttpHandler.Method.OPTIONS, null, "/opt=yes", new TreeMap<>(), "{}", null, new Callback<HttpHandler.HttpResult>() {
      @Override
      public void success(HttpHandler.HttpResult value) {
        Assert.assertTrue(value.cors);
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {

      }
    });
    base.http().handle(context, HttpHandler.Method.GET, null, "x", new TreeMap<>(), "{}", null, new Callback<HttpHandler.HttpResult>() {
      @Override
      public void success(HttpHandler.HttpResult value) {
        Assert.assertEquals("yay", new String(value.body, StandardCharsets.UTF_8));
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {

      }
    });
    base.http().handle(context, HttpHandler.Method.DELETE, null, "x", new TreeMap<>(), "{}", null, new Callback<HttpHandler.HttpResult>() {
      @Override
      public void success(HttpHandler.HttpResult value) {
        Assert.assertEquals("yay", new String(value.body, StandardCharsets.UTF_8));
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {

      }
    });
    base.http().handle(context, HttpHandler.Method.PUT,null, "x", new TreeMap<>(), "{}", null, new Callback<HttpHandler.HttpResult>() {
      @Override
      public void success(HttpHandler.HttpResult value) {
        Assert.assertEquals("post", new String(value.body, StandardCharsets.UTF_8));
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
      }
    });
    Assert.assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));

  }
}
