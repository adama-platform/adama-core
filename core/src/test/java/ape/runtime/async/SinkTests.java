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

import ape.runtime.natives.NtPrincipal;
import org.junit.Assert;
import org.junit.Test;

public class SinkTests {
  @Test
  public void flow_in_and_out() {
    final var sink = new Sink<String>("channel");
    sink.enqueue(new AsyncTask(0, 1, NtPrincipal.NO_ONE, null, "channel", 0, "origin", "ip","message", new IdHistoryLog()), "Cake");
    final var sf = sink.dequeue(NtPrincipal.NO_ONE);
    Assert.assertTrue(sf.exists());
    Assert.assertEquals("Cake", sf.await());
    final var sf2 = sink.dequeue(NtPrincipal.NO_ONE);
    Assert.assertFalse(sf2.exists());
  }

  @Test
  public void dequeue_if_works_as_expected() {
    final var sink = new Sink<String>("channel");
    sink.enqueue(new AsyncTask(0, 2, NtPrincipal.NO_ONE, null, "channel", 1000, "origin", "ip","message", new IdHistoryLog()), "A");
    Assert.assertFalse(sink.dequeueIf(NtPrincipal.NO_ONE, 500).exists());
    Assert.assertEquals("A", sink.dequeueIf(NtPrincipal.NO_ONE, 3000).await());
  }

  @Test
  public void flow_in_clear_out() {
    final var sink = new Sink<String>("channel");
    sink.enqueue(new AsyncTask(0, 3, NtPrincipal.NO_ONE, null, "channel", 0, "origin", "ip","message", new IdHistoryLog()), "Cake");
    sink.clear();
    final var sf2 = sink.dequeue(NtPrincipal.NO_ONE);
    Assert.assertFalse(sf2.exists());
  }

  @Test
  public void maybe_out_no_data() {
    final var sink = new Sink<String>("channel");
    final var sf = sink.dequeueMaybe(NtPrincipal.NO_ONE);
    Assert.assertFalse(sf.exists());
  }

  @Test
  public void maybe_out_with_data() {
    final var sink = new Sink<String>("channel");
    sink.enqueue(new AsyncTask(0, 4, NtPrincipal.NO_ONE, null, "channel", 0, "origin", "ip","message", new IdHistoryLog()), "Cake");
    final var sf = sink.dequeueMaybe(NtPrincipal.NO_ONE);
    Assert.assertTrue(sf.exists());
    Assert.assertEquals("Cake", sf.await().get());
    final var sf2 = sink.dequeue(NtPrincipal.NO_ONE);
    Assert.assertFalse(sf2.exists());
  }
}
