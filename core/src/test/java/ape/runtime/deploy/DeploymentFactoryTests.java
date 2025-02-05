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
package ape.runtime.deploy;

import ape.ErrorCodes;
import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.keys.PrivateKeyBundle;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.remote.Deliverer;
import ape.translator.env.CompilerOptions;
import ape.translator.env.EnvironmentState;
import ape.translator.env.GlobalObjectPool;
import ape.translator.env.RuntimeEnvironment;
import ape.translator.env2.Scope;
import ape.translator.jvm.LivingDocumentFactory;
import ape.translator.parser.Parser;
import ape.translator.parser.exceptions.AdamaLangException;
import ape.translator.parser.token.TokenEngine;
import ape.translator.tree.Document;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeploymentFactoryTests {

  @Test
  public void cantParse() throws Exception {
    DeploymentPlan plan =
        new DeploymentPlan(
            "{\"versions\":{\"x\":\"@con\"},\"default\":\"x\",\"plan\":[{\"version\":\"x\",\"percent\":50,\"prefix\":\"k\",\"seed\":\"a2\"}]}",
            (t, errorCode) -> {});

    DeploymentFactoryBase base = new DeploymentFactoryBase(AsyncByteCodeCache.DIRECT, RuntimeEnvironment.Tooling);
    CountDownLatch latch = new CountDownLatch(1);
    base.deploy("space", plan, new TreeMap<>(), new Callback<Void>() {
      @Override
      public void success(Void value) {
        Assert.fail();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        Assert.assertEquals(198174, ex.code);
        latch.countDown();
      }
    });
    base.account(new HashMap<>());
    Assert.assertTrue(latch.await(10000, TimeUnit.MILLISECONDS));
  }

  @Test
  public void cantType() throws Exception {
    DeploymentPlan plan =
        new DeploymentPlan(
            "{\"versions\":{\"x\":\"public int x = true;\"},\"default\":\"x\",\"plan\":[{\"version\":\"x\",\"percent\":50,\"prefix\":\"k\",\"seed\":\"a2\"}]}",
            (t, errorCode) -> {});

    DeploymentFactoryBase base = new DeploymentFactoryBase(AsyncByteCodeCache.DIRECT, RuntimeEnvironment.Tooling);

    CountDownLatch latch = new CountDownLatch(1);
    base.deploy("space", plan, new TreeMap<>(), new Callback<Void>() {
      @Override
      public void success(Void value) {
        Assert.fail();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        Assert.assertEquals(132157, ex.code);
        latch.countDown();
      }
    });
    Assert.assertTrue(latch.await(10000, TimeUnit.MILLISECONDS));
  }

  @Test
  public void happy() throws Exception {
    DeploymentPlan plan =
        new DeploymentPlan(
            "{\"versions\":{\"x\":\"public int x = 123;\"},\"default\":\"x\",\"plan\":[{\"version\":\"x\",\"percent\":50,\"prefix\":\"k\",\"seed\":\"a2\"}]}",
            (t, errorCode) -> {});
    DeploymentFactoryBase base = new DeploymentFactoryBase(AsyncByteCodeCache.DIRECT, RuntimeEnvironment.Tooling);
    CountDownLatch latch = new CountDownLatch(1);
    base.deploy("space", plan, new TreeMap<>(), new Callback<Void>() {
      @Override
      public void success(Void value) {
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        Assert.fail();
      }
    });
    Assert.assertTrue(latch.await(10000, TimeUnit.MILLISECONDS));
    Assert.assertEquals("0w9NHaDbD2fTGSLlHuGyCQ==", base.hashOf("space"));
  }

  @Test
  public void rxhtml() throws Exception {
    DeploymentPlan plan =
        new DeploymentPlan(
            "{\"versions\":{\"x\":{\"main\":\"public int x = 123;\",\"rxhtml\":\"<forest><page uri=\\\"/\\\">Hello World</page></forest>\"}},\"default\":\"x\",\"plan\":[]}",
            (t, errorCode) -> {});
    DeploymentFactoryBase base = new DeploymentFactoryBase(AsyncByteCodeCache.DIRECT, RuntimeEnvironment.Tooling);
    CountDownLatch latch = new CountDownLatch(1);
    base.deploy("space", plan, new TreeMap<>(), new Callback<Void>() {
      @Override
      public void success(Void value) {
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        Assert.fail();
      }
    });
    Assert.assertTrue(latch.await(10000, TimeUnit.MILLISECONDS));
    Assert.assertEquals("W9ngBjBRMNDRTZY3N3OjKA==", base.hashOf("space"));
  }

  @Test
  public void happyDirect() throws Exception {
    DeploymentPlan plan =
        new DeploymentPlan(
            "{\"versions\":{\"x\":\"public int x = 123;\"},\"default\":\"x\",\"plan\":[{\"version\":\"x\",\"percent\":50,\"prefix\":\"k\",\"seed\":\"a2\"}]}",
            (t, errorCode) -> {});
    DeploymentFactory newFactory = forge("space", "Space_", new AtomicInteger(1000), null, plan, Deliverer.FAILURE, new TreeMap<>());
    Assert.assertEquals(1, newFactory.spacesAvailable().size());
    newFactory.account(new HashMap<>());
  }

  @Test
  public void happyInstrument() throws Exception {
    DeploymentPlan plan =
        new DeploymentPlan(
            "{\"instrument\":true,\"versions\":{\"x\":\"public int x = 123;\"},\"default\":\"x\",\"plan\":[{\"version\":\"x\",\"percent\":50,\"prefix\":\"k\",\"seed\":\"a2\"}]}",
            (t, errorCode) -> {});
    DeploymentFactory newFactory = forge("space", "Space_", new AtomicInteger(1000), null, plan, Deliverer.FAILURE, new TreeMap<>());
    Assert.assertEquals(1, newFactory.spacesAvailable().size());
  }

  public static DeploymentFactory forge(String name, String spacePrefix, AtomicInteger newClassId, DeploymentFactory prior, DeploymentPlan plan, Deliverer deliverer, TreeMap<Integer, PrivateKeyBundle> keys) throws ErrorCodeException {
    HashMap<String, LivingDocumentFactory> factories = new HashMap<>();
    long _memoryUsed = 0L;
    for (Map.Entry<String, DeployedVersion> entry : plan.versions.entrySet()) {
      LivingDocumentFactory factory = null;
      if (prior != null) {
        if (prior.plan.versions.containsKey(entry.getKey())) {
          if (prior.plan.versions.get(entry.getKey()).equals(entry.getValue())) {
            factory = prior.factories.get(entry.getKey());
          }
        }
      }
      if (factory == null) {
        factory = compile(name, spacePrefix.replaceAll(Pattern.quote("-"), Matcher.quoteReplacement("_")) + newClassId.getAndIncrement(), entry.getValue().main, entry.getValue().includes, deliverer, keys, plan.instrument);
      }
      _memoryUsed += factory.memoryUsage;
      factories.put(entry.getKey(), factory);
    }
    return new DeploymentFactory(name, plan, _memoryUsed, factories);
  }

  private static LivingDocumentFactory compile(String spaceName, String className, final String code, Map<String, String> includes, Deliverer deliverer, TreeMap<Integer, PrivateKeyBundle> keys, boolean instrument) throws ErrorCodeException {
    try {
      CompilerOptions.Builder builder = CompilerOptions.start();
      if (instrument) {
        builder = builder.instrument();
      }
      final var options = builder.make();
      final var globals = GlobalObjectPool.createPoolWithStdLib(RuntimeEnvironment.Tooling);
      final var state = new EnvironmentState(globals, options, RuntimeEnvironment.Tooling);
      final var document = new Document();
      document.setClassName(className);
      document.setIncludes(includes);
      final var tokenEngine = new TokenEngine("main", code.codePoints().iterator());
      final var parser = new Parser(tokenEngine, document.getSymbolIndex(), Scope.makeRootDocument());
      parser.document().accept(document);
      if (!document.check(state.scope())) {
        throw new ErrorCodeException(ErrorCodes.DEPLOYMENT_CANT_TYPE_LANGUAGE, document.errorsJson());
      }
      final var java = document.compileJava(state);
      JsonStreamWriter reflection = new JsonStreamWriter();
      document.writeTypeReflectionJson(reflection);
      return new LivingDocumentFactory(SyncCompiler.compile(spaceName, className, java, reflection.toString()), deliverer, keys);
    } catch (AdamaLangException ex) {
      throw new ErrorCodeException(ErrorCodes.DEPLOYMENT_CANT_PARSE_LANGUAGE, ex);
    }
  }
}
