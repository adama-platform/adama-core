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
import ape.common.Json;
import ape.web.contracts.MCPSession.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class NoOpMCPHandlerTest {

  @Test
  public void testServerInfo() {
    MCPSession session = MCPSession.NOOP;
    ServerInfo info = session.getServerInfo();
    Assert.assertEquals("noop", info.name);
    Assert.assertEquals("1.0.0", info.version);
  }

  @Test
  public void testListToolsReturnsEmpty() throws Exception {
    MCPSession session = MCPSession.NOOP;

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<ToolDefinition[]> result = new AtomicReference<>();

    session.listTools(new Callback<ToolDefinition[]>() {
      @Override
      public void success(ToolDefinition[] value) {
        result.set(value);
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        latch.countDown();
      }
    });

    Assert.assertTrue(latch.await(1, TimeUnit.SECONDS));
    Assert.assertNotNull(result.get());
    Assert.assertEquals(0, result.get().length);
  }

  @Test
  public void testCallToolReturnsError() throws Exception {
    MCPSession session = MCPSession.NOOP;

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<ErrorCodeException> error = new AtomicReference<>();

    session.callTool("any-tool", Json.newJsonObject(), new Callback<ToolResult>() {
      @Override
      public void success(ToolResult value) {
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        error.set(ex);
        latch.countDown();
      }
    });

    Assert.assertTrue(latch.await(1, TimeUnit.SECONDS));
    Assert.assertNotNull(error.get());
    Assert.assertEquals(MCPSession.ERROR_METHOD_NOT_FOUND, error.get().code);
    Assert.assertTrue(error.get().getMessage().contains("any-tool"));
  }

  @Test
  public void testListResourcesReturnsEmpty() throws Exception {
    MCPSession session = MCPSession.NOOP;

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<ResourceDefinition[]> result = new AtomicReference<>();

    session.listResources(new Callback<ResourceDefinition[]>() {
      @Override
      public void success(ResourceDefinition[] value) {
        result.set(value);
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        latch.countDown();
      }
    });

    Assert.assertTrue(latch.await(1, TimeUnit.SECONDS));
    Assert.assertNotNull(result.get());
    Assert.assertEquals(0, result.get().length);
  }

  @Test
  public void testReadResourceReturnsError() throws Exception {
    MCPSession session = MCPSession.NOOP;

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<ErrorCodeException> error = new AtomicReference<>();

    session.readResource("file:///any/path", new Callback<ResourceContent[]>() {
      @Override
      public void success(ResourceContent[] value) {
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        error.set(ex);
        latch.countDown();
      }
    });

    Assert.assertTrue(latch.await(1, TimeUnit.SECONDS));
    Assert.assertNotNull(error.get());
    Assert.assertEquals(MCPSession.ERROR_RESOURCE_NOT_FOUND, error.get().code);
    Assert.assertTrue(error.get().getMessage().contains("file:///any/path"));
  }

  @Test
  public void testListPromptsReturnsEmpty() throws Exception {
    MCPSession session = MCPSession.NOOP;

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<PromptDefinition[]> result = new AtomicReference<>();

    session.listPrompts(new Callback<PromptDefinition[]>() {
      @Override
      public void success(PromptDefinition[] value) {
        result.set(value);
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        latch.countDown();
      }
    });

    Assert.assertTrue(latch.await(1, TimeUnit.SECONDS));
    Assert.assertNotNull(result.get());
    Assert.assertEquals(0, result.get().length);
  }

  @Test
  public void testGetPromptReturnsError() throws Exception {
    MCPSession session = MCPSession.NOOP;

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<ErrorCodeException> error = new AtomicReference<>();

    session.getPrompt("any-prompt", Json.newJsonObject(), new Callback<PromptMessages>() {
      @Override
      public void success(PromptMessages value) {
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        error.set(ex);
        latch.countDown();
      }
    });

    Assert.assertTrue(latch.await(1, TimeUnit.SECONDS));
    Assert.assertNotNull(error.get());
    Assert.assertEquals(MCPSession.ERROR_METHOD_NOT_FOUND, error.get().code);
    Assert.assertTrue(error.get().getMessage().contains("any-prompt"));
  }

  @Test
  public void testCloseDoesNotThrow() {
    MCPSession session = MCPSession.NOOP;
    // Should not throw
    session.close();
    session.close(); // Double close should be safe too
  }

  @Test
  public void testErrorCodeConstants() {
    Assert.assertEquals(-32601, MCPSession.ERROR_METHOD_NOT_FOUND);
    Assert.assertEquals(-32002, MCPSession.ERROR_RESOURCE_NOT_FOUND);
    Assert.assertEquals(-32602, MCPSession.ERROR_INVALID_PARAMS);
  }
}
