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
package ape.runtime.json;

import ape.runtime.contracts.Perspective;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.sys.StreamHandle;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class PrivateViewTests {
  @Test
  public void killing() {
    ArrayList<String> list = new ArrayList<>();
    PrivateView pv =
        new PrivateView(3,
            NtPrincipal.NO_ONE,
            new Perspective() {
              @Override
              public void data(String data) {
                list.add(data);
              }

              @Override
              public void disconnect() {}
            }) {

          @Override
          public long memory() {
            return 0;
          }

          @Override
          public void ingest(JsonStreamReader reader) {}

          @Override
          public void dumpViewer(JsonStreamWriter writer) {}

          @Override
          public void update(JsonStreamWriter writer) {}
        };
    Assert.assertTrue(pv.isAlive());
    pv.kill();
    Assert.assertFalse(pv.isAlive());
    pv.deliver("{}");
    Assert.assertEquals("{}", list.get(0));
    Assert.assertTrue(pv.hasRead());
  }

  @Test
  public void usurp() {
    ArrayList<String> list = new ArrayList<>();
    AtomicBoolean gotViewUpdate = new AtomicBoolean(false);
    PrivateView pv1 =
        new PrivateView(2,
            NtPrincipal.NO_ONE,
            new Perspective() {
              @Override
              public void data(String data) {
                list.add(data);
              }

              @Override
              public void disconnect() {}
            }) {

          @Override
          public long memory() {
            return 0;
          }

          @Override
          public void ingest(JsonStreamReader reader) { }

          @Override
          public void dumpViewer(JsonStreamWriter writer) {}

          @Override
          public void update(JsonStreamWriter writer) {}
        };
    StreamHandle handle = new StreamHandle(pv1);
    pv1.link(handle);

    PrivateView pv2 =
        new PrivateView(1, NtPrincipal.NO_ONE, pv1.perspective) {

          @Override
          public long memory() {
            return 0;
          }

          @Override
          public void ingest(JsonStreamReader reader) { gotViewUpdate.set(true); }

          @Override
          public void dumpViewer(JsonStreamWriter writer) {}

          @Override
          public void update(JsonStreamWriter writer) {}
        };
    AtomicInteger iv = new AtomicInteger(0);
    pv2.setRefresh(() -> iv.addAndGet(2));
    pv1.setRefresh(() -> iv.addAndGet(5));
    handle.triggerRefresh();
    pv1.usurp(pv2);
    handle.triggerRefresh();
    Assert.assertEquals(7, iv.get());
    Assert.assertFalse(pv1.isAlive());
    Assert.assertFalse(gotViewUpdate.get());
    handle.ingestViewUpdate(new JsonStreamReader("{}"));;
    Assert.assertTrue(gotViewUpdate.get());
    handle.kill();
    Assert.assertFalse(pv1.isAlive());
  }

  @Test
  public void futures() {
    ArrayList<String> list = new ArrayList<>();
    PrivateView pv =
        new PrivateView(4,
            NtPrincipal.NO_ONE,
            new Perspective() {
              @Override
              public void data(String data) {
                list.add(data);
              }

              @Override
              public void disconnect() {}
            }) {

          @Override
          public long memory() {
            return 0;
          }

          @Override
          public void ingest(JsonStreamReader reader) {}

          @Override
          public void dumpViewer(JsonStreamWriter writer) {}

          @Override
          public void update(JsonStreamWriter writer) {}
        };
    Assert.assertFalse(pv.futures("\"outstanding\":[],\"blockers\":[]"));
    Assert.assertTrue(pv.futures("XYZ"));
    Assert.assertFalse(pv.futures("XYZ"));
  }
}
