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
package ape.runtime.async;

import ape.runtime.json.JsonStreamWriter;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.reactives.RxInt32;
import ape.runtime.reactives.RxInt64;
import org.junit.Assert;
import org.junit.Test;

public class OutstandingFutureTrackerTests {

  public OutstandingFutureTracker makeFutures() {
    final var src = new RxInt32(null, 0);
    final var futures = new OutstandingFutureTracker(src, new TimeoutTracker(new RxInt64(null, 0)));
    return futures;
  }

  @Test
  public void caching() {
    final var futures = makeFutures();
    final var futureA = futures.make("chan", NtPrincipal.NO_ONE);
    futures.restore();
    final var futureB = futures.make("chan", NtPrincipal.NO_ONE);
    Assert.assertTrue(futureA == futureB);
    futures.commit();
    final var futureC = futures.make("chan", NtPrincipal.NO_ONE);
    Assert.assertTrue(futureA != futureC);
  }

  @Test
  public void multi_user_flow() {
    final var futures = makeFutures();
    final var A = new NtPrincipal("a", "local");
    final var B = new NtPrincipal("b", "local");
    final var futureA1 = futures.make("chan", A);
    final var futureB1 = futures.make("chan", B);
    final var futureA2 = futures.make("chan", A);
    final var futureB2 = futures.make("chan", B);
    final var futureA3 = futures.make("chan", A);
    final var futureB3 = futures.make("chan", B);
    futureA1.json = "{\"a\":1}";
    futureA2.json = "{\"a\":2}";
    futureA3.json = "{\"a\":3}";
    futureB1.json = "{\"b\":1}";
    futureB2.json = "{\"b\":2}";
    futureB3.json = "{\"b\":3}";
    final var viewA = new JsonStreamWriter();
    final var viewB = new JsonStreamWriter();
    futures.dump(viewA, A);
    futures.dump(viewB, B);
    Assert.assertEquals(
        "\"outstanding\":[{\"a\":1},{\"a\":2},{\"a\":3}],\"blockers\":[{\"agent\":\"b\",\"authority\":\"local\"},{\"agent\":\"a\",\"authority\":\"local\"}]",
        viewA.toString());
    Assert.assertEquals(
        "\"outstanding\":[{\"b\":1},{\"b\":2},{\"b\":3}],\"blockers\":[{\"agent\":\"b\",\"authority\":\"local\"},{\"agent\":\"a\",\"authority\":\"local\"}]",
        viewB.toString());
  }
}
