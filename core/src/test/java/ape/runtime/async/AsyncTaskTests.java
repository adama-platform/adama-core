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

import ape.runtime.exceptions.AbortMessageException;
import ape.runtime.exceptions.RetryProgressException;
import ape.runtime.natives.NtPrincipal;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class AsyncTaskTests {
  @Test
  public void abort_flow() {
    final var at = new AsyncTask(0, 1, NtPrincipal.NO_ONE, 123, "ch", 0, "origin", "ip", "message", new IdHistoryLog());
    at.setAction(
        () -> {
          throw new AbortMessageException();
        });
    try {
      at.execute();
      Assert.fail();
    } catch (final RetryProgressException rpe) {
    }
  }

  @Test
  public void ideal_flow() throws Exception {
    final var at = new AsyncTask(0, 1, NtPrincipal.NO_ONE, null, "ch", 0, "origin", "ip","message", new IdHistoryLog());
    final var ref = new AtomicInteger(0);
    at.setAction(
        () -> {
          ref.incrementAndGet();
        });
    at.execute();
  }

  @Test
  public void usedSanity() {
    final var at = new AsyncTask(0, 1, NtPrincipal.NO_ONE, null, "ch", 0, "origin", "ip","message", new IdHistoryLog());
    Assert.assertFalse(at.isUsed());
    at.markUsed();
    Assert.assertTrue(at.isUsed());
    at.resetUsed();
    Assert.assertFalse(at.isUsed());
  }
}
