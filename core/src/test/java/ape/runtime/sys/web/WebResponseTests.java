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
package ape.runtime.sys.web;

import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.mocks.MockMessage;
import ape.runtime.natives.NtAsset;
import ape.runtime.sys.PredictiveInventory;
import org.junit.Assert;
import org.junit.Test;

public class WebResponseTests {
  @Test
  public void flow_xml() {
    WebResponse response = new WebResponse();
    response.xml("x");
    Assert.assertEquals("x", response.body);
    Assert.assertEquals("application/xml", response.contentType);
    PredictiveInventory inventory = new PredictiveInventory();
    response.account(inventory);
    Assert.assertEquals(1, inventory.sample().bandwidth);
  }

  @Test
  public void flow_json() {
    WebResponse response = new WebResponse();
    response.json(new MockMessage());
    Assert.assertEquals("{\"x\":42,\"y\":13}", response.body);
    Assert.assertEquals("application/json", response.contentType);
    PredictiveInventory inventory = new PredictiveInventory();
    response.account(inventory);
    Assert.assertEquals(15, inventory.sample().bandwidth);
  }

  @Test
  public void flow_html() {
    WebResponse response = new WebResponse();
    response.html("HTTTTMMMEl");
    Assert.assertEquals("HTTTTMMMEl", response.body);
    Assert.assertEquals("text/html; charset=utf-8", response.contentType);
    PredictiveInventory inventory = new PredictiveInventory();
    response.account(inventory);
    Assert.assertEquals(10, inventory.sample().bandwidth);
  }

  @Test
  public void flow_asset() {
    WebResponse response = new WebResponse();
    response.asset(new NtAsset("id", "name", "contentType", 42, "md5", "sha384"));
    response.asset_transform("transform").cache_ttl_seconds(100);
    Assert.assertEquals("id", response.asset.id);
    Assert.assertEquals("contentType", response.contentType);
    Assert.assertEquals("transform", response.asset_transform);
    Assert.assertEquals(100, response.cache_ttl_seconds);
    PredictiveInventory inventory = new PredictiveInventory();
    response.account(inventory);
    Assert.assertEquals(42, inventory.sample().bandwidth);
  }

  @Test
  public void flow_error() {
    WebResponse response = new WebResponse();
    response.error("message");
    Assert.assertEquals("message", response.body);
    Assert.assertEquals("text/error", response.contentType);
  }

  @Test
  public void flow_sign() {
    WebResponse response = new WebResponse();
    response.sign("agent");
    Assert.assertEquals("agent", response.body);
    Assert.assertEquals("text/agent", response.contentType);
  }

  @Test
  public void save_empty() {
    WebResponse response = new WebResponse();
    JsonStreamWriter writer = new JsonStreamWriter();
    response.writeAsObject(writer);
    Assert.assertEquals("{\"status\":200}", writer.toString());
  }

  @Test
  public void save_many() {
    WebResponse response = new WebResponse();
    response.cors = true;
    response.asset = NtAsset.NOTHING;
    response.asset_transform = "transform";
    response.body = "body";
    response.contentType = "type";
    response.cache_ttl_seconds = 42;
    JsonStreamWriter writer = new JsonStreamWriter();
    response.writeAsObject(writer);
    Assert.assertEquals("{\"content-type\":\"type\",\"body\":\"body\",\"asset\":{\"id\":\"\",\"size\":\"0\",\"name\":\"\",\"type\":\"\",\"md5\":\"\",\"sha384\":\"\",\"@gc\":\"@yes\"},\"asset-transform\":\"transform\",\"cors\":true,\"cache-ttl-seconds\":42,\"status\":200}", writer.toString());
  }

  @Test
  public void load_many() {
    JsonStreamReader reader = new JsonStreamReader("{\"content-type\":\"type\",\"body\":\"body\",\"asset\":{\"id\":\"\",\"size\":\"0\",\"name\":\"\",\"type\":\"\",\"md5\":\"\",\"sha384\":\"\",\"@gc\":\"@yes\"},\"asset-transform\":\"transform\",\"cors\":true,\"cache-ttl-seconds\":42,\"junk\":1}");
    WebResponse response = WebResponse.readFromObject(reader);
    Assert.assertEquals("transform", response.asset_transform);
    Assert.assertEquals("body", response.body);
    Assert.assertEquals("type", response.contentType);
    Assert.assertEquals(42, response.cache_ttl_seconds);
    Assert.assertEquals(NtAsset.NOTHING, response.asset);
    Assert.assertTrue(response.cors);
  }

  @Test
  public void skip() {
    JsonStreamReader reader = new JsonStreamReader("\"123\"");
    WebResponse response = WebResponse.readFromObject(reader);
    Assert.assertNull(response);
  }

}
