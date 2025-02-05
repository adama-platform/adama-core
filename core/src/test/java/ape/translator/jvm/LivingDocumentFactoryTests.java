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
package ape.translator.jvm;

import ape.common.ErrorCodeException;
import ape.runtime.ContextSupport;
import ape.runtime.deploy.SyncCompiler;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.remote.Deliverer;
import org.junit.Assert;
import org.junit.Test;

import java.util.TreeMap;

public class LivingDocumentFactoryTests {
  @Test
  public void almostOK() throws Exception {
    final var compiler =
        new LivingDocumentFactory(SyncCompiler.compile(
            "Space",
            "Foo",
            "import java.util.HashMap; \nimport ape.runtime.contracts.DocumentMonitor;import ape.runtime.remote.client.*; import ape.runtime.remote.*;import ape.runtime.natives.*;import ape.runtime.sys.*;\n public class Foo { public Foo(DocumentMonitor dm) {} public static boolean __onCanCreate(CoreRequestContext who) { return false; } public static boolean __onCanInvent(CoreRequestContext who) { return false; } public static boolean __onCanSendWhileDisconnected(CoreRequestContext who) { return false; } public static HashMap<String, Object> __config() { return new HashMap<>(); } public static HashMap<String, HashMap<String, Object>> __services() { return new HashMap<>(); } public static void __create_generic_clients(ServiceRegistry s, HeaderDecryptor h) {} } ",
            "{}"), Deliverer.FAILURE, new TreeMap<>());
    var success = false;
    try {
      compiler.create(null);
      success = true;
    } catch (final ErrorCodeException nsme) {
      Assert.assertEquals(115747, nsme.code);
    }
    Assert.assertFalse(success);
  }

  @Test
  public void badCode() throws Exception {
    var failed = true;
    try {
      new LivingDocumentFactory(SyncCompiler.compile(
          "Space",
          "Foo",
          "import ape.runtime.reactives.RxObject;\n class Foo { public Foo(}",
          "{}"), Deliverer.FAILURE, new TreeMap<>());
      failed = false;
    } catch (final ErrorCodeException nsme) {
      Assert.assertEquals(180258, nsme.code);
    }
    Assert.assertTrue(failed);
  }

  @Test
  public void castFailure() throws Exception {
    final var compiler =
        new LivingDocumentFactory(SyncCompiler.compile(
            "Space",
            "Foo",
            "import java.util.HashMap; \nimport ape.runtime.contracts.DocumentMonitor;import ape.runtime.natives.*; import ape.runtime.remote.client.*; import ape.runtime.remote.*; import ape.runtime.sys.*;\n public class Foo { public Foo(DocumentMonitor dm) {} public static boolean __onCanCreate(CoreRequestContext who) { return false; }  public static boolean __onCanInvent(CoreRequestContext who) { return false; } public static boolean __onCanSendWhileDisconnected(CoreRequestContext who) { return false; } public static HashMap<String, Object> __config() { return new HashMap<>(); } public static HashMap<String, HashMap<String, Object>> __services() { return new HashMap<>(); } public static void __create_generic_clients(ServiceRegistry s, HeaderDecryptor h) {} }",
            "{}"), Deliverer.FAILURE, new TreeMap<>());
    var success = false;
    try {
      compiler.create(null);
      success = true;
    } catch (final ErrorCodeException nsme) {
      Assert.assertEquals(115747, nsme.code);
    }
    Assert.assertFalse(success);
  }

  @Test
  public void noConstructor() throws Exception {
    try {
      new LivingDocumentFactory(SyncCompiler.compile(
          "Space",
          "Foo",
          "import java.util.HashMap;" +
              "import ape.runtime.natives.*;" +
              "import ape.runtime.sys.*;" +
              "class Foo {" +
              " public static boolean __onCanCreate(CoreRequestContext who) { return false; }" +
              "public static boolean __onCanInvent(CoreRequestContext who) { return false; }" +
              "public static boolean __onCanSendWhileDisconnected(CoreRequestContext who) { return false; } " +
              "public static HashMap<String, HashMap<String, Object>> __services() { return new HashMap<>(); }" +
              "public static HashMap<String, Object> __config() { return new HashMap<>(); }" +
              "}",
          "{}"), Deliverer.FAILURE, new TreeMap<>());
      Assert.fail();
    } catch (final ErrorCodeException nsme) {
      Assert.assertEquals(198174, nsme.code);
    }
  }

  @Test
  public void invalidPolicies() throws Exception {
    LivingDocumentFactory factory = new LivingDocumentFactory(SyncCompiler.compile(
        "Space",
        "Foo",
        "import ape.runtime.contracts.DocumentMonitor; import ape.runtime.sys.*; import ape.runtime.remote.client.*; import ape.runtime.remote.*;" +
            "import java.util.HashMap; import ape.runtime.natives.*; public class Foo {" +
            "public Foo(final DocumentMonitor __monitor) { }" +
            "public static boolean __onCanCreate(CoreRequestContext who) { throw new NullPointerException(); }" +
            "public static boolean __onCanInvent(CoreRequestContext who) { throw new NullPointerException(); }" +
            "public static boolean __onCanSendWhileDisconnected(CoreRequestContext who) { throw new NullPointerException(); }" +
            "public static HashMap<String, HashMap<String, Object>> __services() { return new HashMap<>(); }" +
            "public static HashMap<String, Object> __config() { return new HashMap<>(); }" +
            "public static void __create_generic_clients(ServiceRegistry s, HeaderDecryptor h) {}" +

            "}",
        "{}"), Deliverer.FAILURE, new TreeMap<>());

    Assert.assertEquals(1000, factory.maximum_history);
    try {
      factory.canCreate(ContextSupport.WRAP(NtPrincipal.NO_ONE));
      Assert.fail();
    } catch (ErrorCodeException ex) {
      Assert.assertEquals(180858, ex.code);
    }
    try {
      factory.canInvent(ContextSupport.WRAP(NtPrincipal.NO_ONE));
      Assert.fail();
    } catch (ErrorCodeException ex) {
      Assert.assertEquals(146558, ex.code);
    }
    try {
      factory.canSendWhileDisconnected(ContextSupport.WRAP(NtPrincipal.NO_ONE));
      Assert.fail();
    } catch (ErrorCodeException ex) {
      Assert.assertEquals(148095, ex.code);
    }
  }

  @Test
  public void configWorks() throws Exception {
    LivingDocumentFactory factory = new LivingDocumentFactory(SyncCompiler.compile(
        "Space",
        "Foo",
        "import ape.runtime.contracts.DocumentMonitor; import ape.runtime.sys.*; import ape.runtime.remote.client.*; import ape.runtime.remote.*;" +
            "import java.util.HashMap; import ape.runtime.natives.*; public class Foo {" +
            "public Foo(final DocumentMonitor __monitor) { }" +
            "public static boolean __onCanCreate(CoreRequestContext who) { throw new NullPointerException(); }" +
            "public static boolean __onCanInvent(CoreRequestContext who) { throw new NullPointerException(); }" +
            "public static boolean __onCanSendWhileDisconnected(CoreRequestContext who) { throw new NullPointerException(); }" +
            "public static HashMap<String, Object> __config() { HashMap<String, Object> map = new HashMap<>(); map.put(\"maximum_history\", 150); return map; }" +
            "public static HashMap<String, HashMap<String, Object>> __services() { return new HashMap<>(); }" +
            "public static void __create_generic_clients(ServiceRegistry s, HeaderDecryptor h) {}" +
            "}",
        "{}"), Deliverer.FAILURE, new TreeMap<>());
    Assert.assertEquals(150, factory.maximum_history);
  }

  @Test
  public void servicesWork() throws Exception {
    LivingDocumentFactory factory = new LivingDocumentFactory(SyncCompiler.compile(
        "Space",
        "Foo",
        "import ape.runtime.contracts.DocumentMonitor; import ape.runtime.sys.*; import ape.runtime.remote.client.*; import ape.runtime.remote.*;" +
            "import java.util.HashMap; import ape.runtime.natives.*; public class Foo {" +
            "public Foo(final DocumentMonitor __monitor) { }" +
            "public static boolean __onCanCreate(CoreRequestContext who) { throw new NullPointerException(); }" +
            "public static boolean __onCanInvent(CoreRequestContext who) { throw new NullPointerException(); }" +
            "public static boolean __onCanSendWhileDisconnected(CoreRequestContext who) { throw new NullPointerException(); }" +
            "public static HashMap<String, Object> __config() { HashMap<String, Object> map = new HashMap<>(); map.put(\"maximum_history\", 150); return map; }" +
            "public static HashMap<String, HashMap<String, Object>> __services() { HashMap<String, HashMap<String, Object>> map = new HashMap<>(); map.put(\"test\", new HashMap<>()); return map; }" +
            "public static void __create_generic_clients(ServiceRegistry s, HeaderDecryptor h) {}" +
            "}",
        "{}"), Deliverer.FAILURE, new TreeMap<>());
    Assert.assertTrue(factory.registry.contains("test"));
  }

  @Test
  public void missingPolicy1() throws Exception {
    try {
      new LivingDocumentFactory(SyncCompiler.compile(
          "Space",
          "Foo",
          "import ape.runtime.contracts.DocumentMonitor; class Foo { public Foo(DocumentMonitor dm) {} }",
          "{}"), Deliverer.FAILURE, new TreeMap<>());
      Assert.fail();
    } catch (final ErrorCodeException nsme) {
      Assert.assertEquals(198174, nsme.code);
    }
  }

  @Test
  public void missingPolicy2() throws Exception {
    try {
      new LivingDocumentFactory(SyncCompiler.compile(
          "Space",
          "Foo",
          "import ape.runtime.natives.*; import ape.runtime.contracts.DocumentMonitor; class Foo { public Foo(DocumentMonitor dm) {} public static boolean __onCanCreate(NtPrincipal who) { throw new NullPointerException(); } }",
          "{}"), Deliverer.FAILURE, new TreeMap<>());
      Assert.fail();
    } catch (final ErrorCodeException nsme) {
      Assert.assertEquals(198174, nsme.code);
    }
  }

  @Test
  public void missingPolicy3() throws Exception {
    try {
      new LivingDocumentFactory(SyncCompiler.compile(
          "Space",
          "Foo",
          "import ape.runtime.natives.*; import ape.runtime.sys.*; import ape.runtime.contracts.DocumentMonitor; class Foo { public Foo(DocumentMonitor dm) {} public static boolean __onCanCreate(CoreRequestContext who) { throw new NullPointerException(); } public static boolean __onCanSendWhileDisconnected(NtPrincipal who) { throw new NullPointerException(); } }",
          "{}"), Deliverer.FAILURE, new TreeMap<>());
      Assert.fail();
    } catch (final ErrorCodeException nsme) {
      Assert.assertEquals(198174, nsme.code);
    }
  }
}
