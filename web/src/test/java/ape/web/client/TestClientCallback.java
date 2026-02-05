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
package ape.web.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ape.common.Json;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TestClientCallback {
  private final CountDownLatch closeLatch;
  private final CountDownLatch pingLatch;
  private final CountDownLatch firstLatch;
  private final CountDownLatch failureLatch;
  private final CountDownLatch failedToConnectLatch;
  private final CountDownLatch disconnectLatch;
  private String data;
  private Throwable exception;
  public ArrayList<String> writes;
  private HashMap<Integer, Mailbox> mailboxes;
  public final HashMap<String, String> headers;

  public TestClientCallback() {
    this.closeLatch = new CountDownLatch(1);
    this.firstLatch = new CountDownLatch(1);
    this.failureLatch = new CountDownLatch(1);
    this.failedToConnectLatch = new CountDownLatch(1);
    this.pingLatch = new CountDownLatch(1);
    this.disconnectLatch = new CountDownLatch(1);
    this.data = "";
    this.writes = new ArrayList<>();
    this.mailboxes = new HashMap<>();
    this.headers = new HashMap<>();
  }

  public void awaitClosed() throws Exception {
    Assert.assertTrue(closeLatch.await(5000, TimeUnit.MILLISECONDS));
  }

  public void awaitPing() throws Exception {
    Assert.assertTrue(pingLatch.await(5000, TimeUnit.MILLISECONDS));
  }

  public void awaitFailure() throws Exception {
    Assert.assertTrue(failureLatch.await(5000, TimeUnit.MILLISECONDS));
  }

  public void awaitFailedToConnect() throws Exception {
    Assert.assertTrue(failedToConnectLatch.await(5000, TimeUnit.MILLISECONDS));
  }

  public void awaitFirst() throws Exception {
    Assert.assertTrue(firstLatch.await(25000, TimeUnit.MILLISECONDS));
  }

  public void assertData(String data) {
    Assert.assertEquals(data, this.data);
  }

  public void assertData(int write, String data) {
    Assert.assertEquals(data, this.writes.get(write));
  }

  public void assertDataPrefix(int write, String data) {
    Assert.assertTrue(this.writes.get(write).startsWith(data));
  }

  public void assertDataPrefix(String prefix) {
    Assert.assertTrue(this.data.startsWith(prefix));
  }

  public void closed() {
    closeLatch.countDown();
  }

  public void failed(Throwable exception) {
    this.exception = exception;
    failureLatch.countDown();
  }

  public void failedToConnect() {
    failedToConnectLatch.countDown();
  }

  public void awaitDisconnect() throws Exception {
    Assert.assertTrue(disconnectLatch.await(5000, TimeUnit.MILLISECONDS));
  }

  public boolean keepPings = false;

  public void successfulResponse(String data) {
    if (!data.contains("ping") || keepPings) {
      this.data += data;
    }
    writes.add(data);
    firstLatch.countDown();
    try {
      ObjectNode node = Json.parseJsonObject(data);

      if (node.has("ping")) {
        pingLatch.countDown();
      }

      if (node.has("status")) {
        if ("disconnected".equals(node.get("status").textValue())) {
          disconnectLatch.countDown();
        }
      }
      if (node.has("id")) {
        JsonNode idNode = node.get("id");
        if (idNode != null && idNode.isInt()) {
          getOrCreate(idNode.asInt()).deliver(data);
        }
      }
      if (node.has("failure")) {
        JsonNode idNode = node.get("failure");
        if (idNode != null && idNode.isInt()) {
          getOrCreate(idNode.asInt()).deliver(data);
        }
      }
      if (node.has("deliver")) {
        JsonNode idNode = node.get("deliver");
        if (idNode != null && idNode.isInt()) {
          getOrCreate(idNode.asInt()).deliver(data);
        }
      }
    } catch (Exception ex) {

    }
  }

  public synchronized Mailbox getOrCreate(int id) {
    Mailbox mailbox = mailboxes.get(id);
    if (mailbox == null) {
      mailbox = new Mailbox();
      mailboxes.put(id, mailbox);
    }
    return mailbox;
  }

  public class Mailbox {
    private final ArrayList<String> writes;
    private final HashSet<CountDownLatch> arrivals;
    private final CountDownLatch firstLatch;

    public Mailbox() {
      this.arrivals = new HashSet<>();
      writes = new ArrayList<>();
      this.firstLatch = new CountDownLatch(1);
    }

    public synchronized void deliver(String data) {
      firstLatch.countDown();
      writes.add(data);
      for (CountDownLatch latch : arrivals) {
        latch.countDown();
      }
    }

    public synchronized CountDownLatch latch(int c) {
      CountDownLatch latch = new CountDownLatch(c);
      arrivals.add(latch);
      return latch;
    }

    public void assertData(int at, String data) {
      Assert.assertEquals(data, writes.get(at));
    }

    public void awaitFirst() throws Exception {
      Assert.assertTrue(firstLatch.await(5000, TimeUnit.MILLISECONDS));
    }
  }
}
