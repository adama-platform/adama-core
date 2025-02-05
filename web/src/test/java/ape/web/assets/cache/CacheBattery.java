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
package ape.web.assets.cache;

import ape.runtime.natives.NtAsset;
import ape.web.assets.AssetStream;
import ape.web.assets.MockAssetStream;
import org.junit.Assert;

import java.nio.charset.StandardCharsets;

public class CacheBattery {
  public static final byte[] CHUNK_1 = "Hello:".getBytes(StandardCharsets.UTF_8);
  public static final byte[] CHUNK_2 = "World".getBytes(StandardCharsets.UTF_8);
  public static final NtAsset ASSET = new NtAsset("id", "name", "type", CHUNK_1.length + CHUNK_2.length, "md5", "sha");

  public static void driveSimple(CachedAsset ca) {
    int expectedSize = CHUNK_1.length + CHUNK_2.length;
    Assert.assertEquals(expectedSize, ca.measure());
    MockAssetStream first = new MockAssetStream();
    MockAssetStream second = new MockAssetStream();
    AssetStream pump = ca.attachWhileInExecutor(first);
    Assert.assertNotNull(pump);
    Assert.assertNull(ca.attachWhileInExecutor(second));
    first.assertHeaders(expectedSize, "type");
    second.assertHeaders(expectedSize, "type");
    pump.headers(1000, "nope", "md5");
    pump.body(CHUNK_1, 0, CHUNK_1.length, false);
    first.assertBody("Hello:");
    second.assertBody("Hello:");
    first.assertNotDone();
    second.assertNotDone();
    MockAssetStream third = new MockAssetStream();
    Assert.assertNull(ca.attachWhileInExecutor(third));
    third.assertHeaders(expectedSize, "type");
    third.assertBody("Hello:");
    third.assertNotDone();
    pump.body(CHUNK_2, 0, CHUNK_2.length, true);
    MockAssetStream fourth = new MockAssetStream();
    Assert.assertNull(ca.attachWhileInExecutor(fourth));
    fourth.assertHeaders(expectedSize, "type");
    first.assertBody("Hello:World");
    second.assertBody("Hello:World");
    third.assertBody("Hello:World");
    fourth.assertBody("Hello:World");
    first.assertDone();
    second.assertDone();
    third.assertDone();
    fourth.assertDone();
    first.assertNoFailure();
    second.assertNoFailure();
    third.assertNoFailure();
    fourth.assertNoFailure();
    ca.evict();
  }

  public static Runnable driveEvictionConcurrent(CachedAsset ca) {
    int expectedSize = CHUNK_1.length + CHUNK_2.length;
    Assert.assertEquals(expectedSize, ca.measure());
    MockAssetStream first = new MockAssetStream();
    MockAssetStream second = new MockAssetStream();
    AssetStream pump = ca.attachWhileInExecutor(first);
    Assert.assertNotNull(pump);
    Assert.assertNull(ca.attachWhileInExecutor(second));
    first.assertHeaders(expectedSize, "type");
    second.assertHeaders(expectedSize, "type");
    pump.headers(1000, "nope", "md5");
    pump.body(CHUNK_1, 0, CHUNK_1.length, false);
    first.assertBody("Hello:");
    second.assertBody("Hello:");
    first.assertNotDone();
    second.assertNotDone();
    MockAssetStream third = new MockAssetStream();
    Assert.assertNull(ca.attachWhileInExecutor(third));
    third.assertHeaders(expectedSize, "type");
    third.assertBody("Hello:");
    third.assertNotDone();
    ca.evict();
    pump.body(CHUNK_2, 0, CHUNK_2.length, true);

    first.assertBody("Hello:World");
    second.assertBody("Hello:World");
    third.assertBody("Hello:World");
    first.assertDone();
    second.assertDone();
    third.assertDone();
    first.assertNoFailure();
    second.assertNoFailure();
    third.assertNoFailure();

    return () -> {
      MockAssetStream fourth = new MockAssetStream();
      Assert.assertNull(ca.attachWhileInExecutor(fourth));
      fourth.assertFailure(920811);
    };
  }

  public static void driveFailure(CachedAsset ca) {
    int expectedSize = CHUNK_1.length + CHUNK_2.length;
    Assert.assertEquals(expectedSize, ca.measure());
    MockAssetStream first = new MockAssetStream();
    MockAssetStream second = new MockAssetStream();
    AssetStream pump = ca.attachWhileInExecutor(first);
    Assert.assertNotNull(pump);
    Assert.assertNull(ca.attachWhileInExecutor(second));
    first.assertHeaders(expectedSize, "type");
    second.assertHeaders(expectedSize, "type");
    pump.headers(1000, "nope", "md5");
    pump.body(CHUNK_1, 0, CHUNK_1.length, false);
    first.assertBody("Hello:");
    second.assertBody("Hello:");
    first.assertNotDone();
    second.assertNotDone();
    MockAssetStream third = new MockAssetStream();
    Assert.assertNull(ca.attachWhileInExecutor(third));
    third.assertHeaders(expectedSize, "type");
    third.assertBody("Hello:");
    third.assertNotDone();
    pump.failure(-13);
    MockAssetStream fourth = new MockAssetStream();
    Assert.assertNull(ca.attachWhileInExecutor(fourth));
    first.assertFailure(-13);
    second.assertFailure(-13);
    second.assertFailure(-13);
    fourth.assertFailure(-13);
  }
}
