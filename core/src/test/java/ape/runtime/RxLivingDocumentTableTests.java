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
package ape.runtime;

import ape.runtime.contracts.Perspective;
import ape.runtime.json.JsonStreamReader;
import ape.runtime.natives.NtPrincipal;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class RxLivingDocumentTableTests {
  private static NtPrincipal A = new NtPrincipal("A", "TEST");
  @Test
  public void formula_add() throws Exception {
    try {
      RealDocumentSetup setup = new RealDocumentSetup(
          "@static { create { return true; } }" +
              "@connected { return true; }" +
              "record R { public int id; public int val; }" +
              "table<R> tbl;" +
              "message M {int val; }" +
              "public formula v1 = (iterate tbl where id == 1)[0];" +
              "public formula v2 = (iterate tbl where id == 2)[0];" +
              "public formula v3 = (iterate tbl where id == 3)[0];" +
              "channel add(M m) { tbl <- m; }",
          null);
      RealDocumentSetup.GotView gv = new RealDocumentSetup.GotView();
      ArrayList<String> list = new ArrayList<>();
      Perspective linked =
          new Perspective() {
            @Override
            public void data(String data) {
              list.add(data);
            }

            @Override
            public void disconnect() {}
          };

      setup.document.connect(ContextSupport.WRAP(A), new RealDocumentSetup.AssertInt(2));
      setup.document.createPrivateView(A, linked, new JsonStreamReader("{}"), gv);
      setup.document.send(ContextSupport.WRAP(A), null, null, "add", "{\"val\":1}", new RealDocumentSetup.AssertInt(5));
      setup.document.send(ContextSupport.WRAP(A), null, null, "add", "{\"val\":2}", new RealDocumentSetup.AssertInt(6));
      setup.document.send(ContextSupport.WRAP(A), null, null, "add", "{\"val\":3}", new RealDocumentSetup.AssertInt(7));
      Assert.assertEquals(4, list.size());
      Assert.assertEquals("{\"seq\":4}", list.get(0));
      Assert.assertEquals("{\"data\":{\"v1\":{\"id\":1,\"val\":1}},\"seq\":5}", list.get(1));
      Assert.assertEquals("{\"data\":{\"v2\":{\"id\":2,\"val\":2}},\"seq\":6}", list.get(2));
      Assert.assertEquals("{\"data\":{\"v3\":{\"id\":3,\"val\":3}},\"seq\":7}", list.get(3));
      System.out.println("COST:" + setup.document.document().__getCodeCost());
    } catch (RuntimeException re) {
      re.printStackTrace();
    }
  }

  @Test
  public void formula_raw() throws Exception {
    try {
      RealDocumentSetup setup = new RealDocumentSetup(
          "@static { create { return true; } }" +
              "@connected { return true; }" +
              "record R { public int id; public int val; }" +
              "table<R> tbl;" +
              "message M {int val; index val; }" +
              "public formula v1 = (iterate tbl where val == 1)[0];" +
              "public formula v2 = (iterate tbl where val == 2)[0];" +
              "public formula v3 = (iterate tbl where val == 3)[0];" +
              "channel add(M m) { tbl <- m; }",
          null);
      RealDocumentSetup.GotView gv = new RealDocumentSetup.GotView();
      ArrayList<String> list = new ArrayList<>();
      Perspective linked =
          new Perspective() {
            @Override
            public void data(String data) {
              list.add(data);
            }

            @Override
            public void disconnect() {}
          };

      setup.document.connect(ContextSupport.WRAP(A), new RealDocumentSetup.AssertInt(2));
      setup.document.createPrivateView(A, linked, new JsonStreamReader("{}"), gv);
      setup.document.send(ContextSupport.WRAP(A), null, null, "add", "{\"val\":1}", new RealDocumentSetup.AssertInt(5));
      setup.document.send(ContextSupport.WRAP(A), null, null, "add", "{\"val\":2}", new RealDocumentSetup.AssertInt(6));
      setup.document.send(ContextSupport.WRAP(A), null, null, "add", "{\"val\":3}", new RealDocumentSetup.AssertInt(7));
      Assert.assertEquals(4, list.size());
      Assert.assertEquals("{\"seq\":4}", list.get(0));
      Assert.assertEquals("{\"data\":{\"v1\":{\"id\":1,\"val\":1}},\"seq\":5}", list.get(1));
      Assert.assertEquals("{\"data\":{\"v2\":{\"id\":2,\"val\":2}},\"seq\":6}", list.get(2));
      Assert.assertEquals("{\"data\":{\"v3\":{\"id\":3,\"val\":3}},\"seq\":7}", list.get(3));
      System.out.println("COST:" + setup.document.document().__getCodeCost());

    } catch (RuntimeException re) {
      re.printStackTrace();
    }
  }

  @Test
  public void formula_idx() throws Exception {
    try {
      RealDocumentSetup setup = new RealDocumentSetup(
          "@static { create { return true; } }" +
              "@connected { return true; }" +
              "record R { public int id; public int val; index val; }" +
              "table<R> tbl;" +
              "message M {int val; index val; }" +
              "public formula v1 = (iterate tbl where val == 1)[0];" +
              "public formula v2 = (iterate tbl where val == 2)[0];" +
              "public formula v3 = (iterate tbl where val == 3)[0];" +
              "channel add(M m) { tbl <- m; }",
          null);
      RealDocumentSetup.GotView gv = new RealDocumentSetup.GotView();
      ArrayList<String> list = new ArrayList<>();
      Perspective linked =
          new Perspective() {
            @Override
            public void data(String data) {
              list.add(data);
            }

            @Override
            public void disconnect() {}
          };

      setup.document.connect(ContextSupport.WRAP(A), new RealDocumentSetup.AssertInt(2));
      setup.document.createPrivateView(A, linked, new JsonStreamReader("{}"), gv);
      setup.document.send(ContextSupport.WRAP(A), null, null, "add", "{\"val\":1}", new RealDocumentSetup.AssertInt(5));
      setup.document.send(ContextSupport.WRAP(A), null, null, "add", "{\"val\":2}", new RealDocumentSetup.AssertInt(6));
      setup.document.send(ContextSupport.WRAP(A), null, null, "add", "{\"val\":3}", new RealDocumentSetup.AssertInt(7));
      Assert.assertEquals(4, list.size());
      Assert.assertEquals("{\"seq\":4}", list.get(0));
      Assert.assertEquals("{\"data\":{\"v1\":{\"id\":1,\"val\":1}},\"seq\":5}", list.get(1));
      Assert.assertEquals("{\"data\":{\"v2\":{\"id\":2,\"val\":2}},\"seq\":6}", list.get(2));
      Assert.assertEquals("{\"data\":{\"v3\":{\"id\":3,\"val\":3}},\"seq\":7}", list.get(3));
      System.out.println("COST:" + setup.document.document().__getCodeCost());

    } catch (RuntimeException re) {
      re.printStackTrace();
    }
  }

  @Test
  public void bubble_add() throws Exception {
    try {
      RealDocumentSetup setup = new RealDocumentSetup(
          "@static { create { return true; } }" +
              "@connected { return true; }" +
              "record R { public int id; public int val; }" +
              "table<R> tbl;" +
              "message M {int val; }" +
              "bubble v1 = (iterate tbl where id == 1)[0];" +
              "bubble v2 = (iterate tbl where id == 2)[0];" +
              "bubble v3 = (iterate tbl where id == 3)[0];" +
              "channel add(M m) { tbl <- m; }",
          null);
      RealDocumentSetup.GotView gv = new RealDocumentSetup.GotView();
      ArrayList<String> list = new ArrayList<>();
      Perspective linked =
          new Perspective() {
            @Override
            public void data(String data) {
              list.add(data);
            }

            @Override
            public void disconnect() {}
          };

      setup.document.connect(ContextSupport.WRAP(A), new RealDocumentSetup.AssertInt(2));
      setup.document.createPrivateView(A, linked, new JsonStreamReader("{}"), gv);
      setup.document.send(ContextSupport.WRAP(A), null, null, "add", "{\"val\":1}", new RealDocumentSetup.AssertInt(5));
      setup.document.send(ContextSupport.WRAP(A), null, null, "add", "{\"val\":2}", new RealDocumentSetup.AssertInt(6));
      setup.document.send(ContextSupport.WRAP(A), null, null, "add", "{\"val\":3}", new RealDocumentSetup.AssertInt(7));
      Assert.assertEquals(4, list.size());
      Assert.assertEquals("{\"seq\":4}", list.get(0));
      Assert.assertEquals("{\"data\":{\"v1\":{\"id\":1,\"val\":1}},\"seq\":5}", list.get(1));
      Assert.assertEquals("{\"data\":{\"v2\":{\"id\":2,\"val\":2}},\"seq\":6}", list.get(2));
      Assert.assertEquals("{\"data\":{\"v3\":{\"id\":3,\"val\":3}},\"seq\":7}", list.get(3));
      System.out.println("COST:" + setup.document.document().__getCodeCost());
    } catch (RuntimeException re) {
      re.printStackTrace();
    }
  }

  @Test
  public void bubble_raw() throws Exception {
    try {
      RealDocumentSetup setup = new RealDocumentSetup(
          "@static { create { return true; } }" +
              "@connected { return true; }" +
              "record R { public int id; public int val; }" +
              "table<R> tbl;" +
              "message M {int val; index val; }" +
              "bubble v1 = (iterate tbl where val == 1)[0];" +
              "bubble v2 = (iterate tbl where val == 2)[0];" +
              "bubble v3 = (iterate tbl where val == 3)[0];" +
              "channel add(M m) { tbl <- m; }",
          null);
      RealDocumentSetup.GotView gv = new RealDocumentSetup.GotView();
      ArrayList<String> list = new ArrayList<>();
      Perspective linked =
          new Perspective() {
            @Override
            public void data(String data) {
              list.add(data);
            }

            @Override
            public void disconnect() {}
          };

      setup.document.connect(ContextSupport.WRAP(A), new RealDocumentSetup.AssertInt(2));
      setup.document.createPrivateView(A, linked, new JsonStreamReader("{}"), gv);
      setup.document.send(ContextSupport.WRAP(A), null, null, "add", "{\"val\":1}", new RealDocumentSetup.AssertInt(5));
      setup.document.send(ContextSupport.WRAP(A), null, null, "add", "{\"val\":2}", new RealDocumentSetup.AssertInt(6));
      setup.document.send(ContextSupport.WRAP(A), null, null, "add", "{\"val\":3}", new RealDocumentSetup.AssertInt(7));
      Assert.assertEquals(4, list.size());
      Assert.assertEquals("{\"seq\":4}", list.get(0));
      Assert.assertEquals("{\"data\":{\"v1\":{\"id\":1,\"val\":1}},\"seq\":5}", list.get(1));
      Assert.assertEquals("{\"data\":{\"v2\":{\"id\":2,\"val\":2}},\"seq\":6}", list.get(2));
      Assert.assertEquals("{\"data\":{\"v3\":{\"id\":3,\"val\":3}},\"seq\":7}", list.get(3));
      System.out.println("COST:" + setup.document.document().__getCodeCost());

    } catch (RuntimeException re) {
      re.printStackTrace();
    }
  }

  @Test
  public void bubble_idx() throws Exception {
    try {
      RealDocumentSetup setup = new RealDocumentSetup(
          "@static { create { return true; } }" +
              "@connected { return true; }" +
              "record R { public int id; public int val; index val; }" +
              "table<R> tbl;" +
              "message M {int val; index val; }" +
              "bubble v1 = (iterate tbl where val == 1)[0];" +
              "bubble v2 = (iterate tbl where val == 2)[0];" +
              "bubble v3 = (iterate tbl where val == 3)[0];" +
              "channel add(M m) { tbl <- m; }",
          null);
      RealDocumentSetup.GotView gv = new RealDocumentSetup.GotView();
      ArrayList<String> list = new ArrayList<>();
      Perspective linked =
          new Perspective() {
            @Override
            public void data(String data) {
              list.add(data);
            }

            @Override
            public void disconnect() {}
          };

      setup.document.connect(ContextSupport.WRAP(A), new RealDocumentSetup.AssertInt(2));
      setup.document.createPrivateView(A, linked, new JsonStreamReader("{}"), gv);
      setup.document.send(ContextSupport.WRAP(A), null, null, "add", "{\"val\":1}", new RealDocumentSetup.AssertInt(5));
      setup.document.send(ContextSupport.WRAP(A), null, null, "add", "{\"val\":2}", new RealDocumentSetup.AssertInt(6));
      setup.document.send(ContextSupport.WRAP(A), null, null, "add", "{\"val\":3}", new RealDocumentSetup.AssertInt(7));
      Assert.assertEquals(4, list.size());
      Assert.assertEquals("{\"seq\":4}", list.get(0));
      Assert.assertEquals("{\"data\":{\"v1\":{\"id\":1,\"val\":1}},\"seq\":5}", list.get(1));
      Assert.assertEquals("{\"data\":{\"v2\":{\"id\":2,\"val\":2}},\"seq\":6}", list.get(2));
      Assert.assertEquals("{\"data\":{\"v3\":{\"id\":3,\"val\":3}},\"seq\":7}", list.get(3));
      System.out.println("COST:" + setup.document.document().__getCodeCost());

    } catch (RuntimeException re) {
      re.printStackTrace();
    }
  }
}
