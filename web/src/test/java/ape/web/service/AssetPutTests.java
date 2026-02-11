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
package ape.web.service;

import ape.common.Hashing;
import ape.common.metrics.NoOpMetricsFactory;
import ape.web.client.TestClientCallback;
import ape.web.client.TestClientRequestBuilder;
import ape.web.service.mocks.MockDomainFinder;
import ape.web.service.mocks.MockServiceBase;
import ape.web.service.mocks.NullCertificateFinder;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class AssetPutTests {

  private static String computeMd5Base64(byte[] data) {
    MessageDigest md5 = Hashing.md5();
    md5.update(data);
    return Hashing.finishAndEncode(md5);
  }

  @Test
  public void putAssetSuccessful() throws Exception {
    EventLoopGroup group = new NioEventLoopGroup();
    WebConfig webConfig = WebConfigTests.mockConfig(WebConfigTests.Scenario.PutAssetTest);
    MockServiceBase base = new MockServiceBase();
    final var runnable = new ServiceRunnable(webConfig, new WebMetrics(new NoOpMetricsFactory()), base, new NullCertificateFinder(), new MockDomainFinder(), () -> {});
    final var thread = new Thread(runnable);
    thread.start();
    runnable.waitForReady(1000);
    try {
      // successful upload with Bearer auth and all required params
      {
        byte[] payload = "hello world".getBytes(StandardCharsets.UTF_8);
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder.start(group)
            .server("localhost", webConfig.port)
            .header("Authorization", "Bearer test-token-123")
            .header("Content-Type", "text/plain")
            .put("/~put?space=myspace&key=attach-ok&filename=test.txt", new String(payload, StandardCharsets.UTF_8))
            .execute(callback);
        callback.awaitFirst();
        callback.assertData("");
      }

      // successful upload with Content-MD5 that matches
      {
        byte[] payload = "verify me".getBytes(StandardCharsets.UTF_8);
        String md5 = computeMd5Base64(payload);
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder.start(group)
            .server("localhost", webConfig.port)
            .header("Authorization", "Bearer test-token-456")
            .header("Content-MD5", md5)
            .header("Content-Type", "application/octet-stream")
            .put("/~put?space=myspace&key=attach-ok&filename=verified.bin", new String(payload, StandardCharsets.UTF_8))
            .execute(callback);
        callback.awaitFirst();
        callback.assertData("");
      }

      // missing Bearer token -> 401
      {
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder.start(group)
            .server("localhost", webConfig.port)
            .put("/~put?space=myspace&key=mykey&filename=test.txt", "data")
            .execute(callback);
        callback.awaitFirst();
        callback.assertData("missing bearer token");
      }

      // missing space param -> 400
      {
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder.start(group)
            .server("localhost", webConfig.port)
            .header("Authorization", "Bearer token")
            .put("/~put?key=mykey&filename=test.txt", "data")
            .execute(callback);
        callback.awaitFirst();
        callback.assertData("missing 'space' query parameter");
      }

      // missing key param -> 400
      {
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder.start(group)
            .server("localhost", webConfig.port)
            .header("Authorization", "Bearer token")
            .put("/~put?space=myspace&filename=test.txt", "data")
            .execute(callback);
        callback.awaitFirst();
        callback.assertData("missing 'key' query parameter");
      }

      // missing filename param -> 400
      {
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder.start(group)
            .server("localhost", webConfig.port)
            .header("Authorization", "Bearer token")
            .put("/~put?space=myspace&key=mykey", "data")
            .execute(callback);
        callback.awaitFirst();
        callback.assertData("missing 'filename' query parameter");
      }

      // Content-MD5 mismatch -> 400
      {
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder.start(group)
            .server("localhost", webConfig.port)
            .header("Authorization", "Bearer token")
            .header("Content-MD5", "dGhpcyBpcyBub3QgdGhlIG1kNQ==")
            .put("/~put?space=myspace&key=mykey&filename=test.txt", "actual data here")
            .execute(callback);
        callback.awaitFirst();
        callback.assertData("content-md5 mismatch");
      }

      // upload failure (key=failure triggers mock error) -> 500
      {
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder.start(group)
            .server("localhost", webConfig.port)
            .header("Authorization", "Bearer token")
            .put("/~put?space=myspace&key=failure&filename=test.txt", "data")
            .execute(callback);
        callback.awaitFirst();
        callback.assertData("<html><head><title>Bad Request; Internal Error Uploading</title></head><body>Sorry, the upload failed.</body></html>");
      }

      // attach failure (key != attach-ok triggers mock error) -> 500
      {
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder.start(group)
            .server("localhost", webConfig.port)
            .header("Authorization", "Bearer token")
            .put("/~put?space=myspace&key=attach-fails&filename=test.txt", "data")
            .execute(callback);
        callback.awaitFirst();
        callback.assertData("<html><head><title>Bad Request; Internal Error Uploading</title></head><body>Sorry, the upload failed.</body></html>");
      }

      // default content-type when omitted
      {
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder.start(group)
            .server("localhost", webConfig.port)
            .header("Authorization", "Bearer token")
            .put("/~put?space=myspace&key=attach-ok&filename=data.bin", "binary data")
            .execute(callback);
        callback.awaitFirst();
        callback.assertData("");
      }

      // with channel parameter
      {
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder.start(group)
            .server("localhost", webConfig.port)
            .header("Authorization", "Bearer token")
            .header("Content-Type", "image/jpeg")
            .put("/~put?space=myspace&key=attach-ok&filename=photo.jpg&channel=uploads", "image bytes")
            .execute(callback);
        callback.awaitFirst();
        callback.assertData("");
      }

      // with channel and message parameters
      {
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder.start(group)
            .server("localhost", webConfig.port)
            .header("Authorization", "Bearer token")
            .header("Content-Type", "image/jpeg")
            .put("/~put?space=myspace&key=attach-ok&filename=photo.jpg&channel=uploads&message.label=profile&message_tag=avatar", "image bytes")
            .execute(callback);
        callback.awaitFirst();
        callback.assertData("");
      }

      // OPTIONS preflight for /~put
      {
        TestClientCallback callback = new TestClientCallback();
        TestClientRequestBuilder.start(group)
            .server("localhost", webConfig.port)
            .options("/~put")
            .execute(callback);
        callback.awaitFirst();
        callback.assertData("");
      }

    } finally {
      runnable.shutdown();
      thread.join();
      group.shutdownGracefully();
    }
  }
}
