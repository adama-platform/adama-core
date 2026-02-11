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

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.metrics.NoOpMetricsFactory;
import ape.runtime.contracts.PlanFetcher;
import ape.runtime.data.Key;
import ape.runtime.sys.PredictiveInventory;
import ape.translator.env.RuntimeEnvironment;
import ape.translator.jvm.LivingDocumentFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class OndemandDeploymentFactoryBaseTests {
  @Test
  public void happy() throws Exception {
    DeploymentPlan plan = new DeploymentPlan(
        "{\"versions\":{\"x\":\"\",\"y\":\"\",\"z\":\"\"},\"default\":\"z\",\"plan\":[{\"version\":\"x\",\"percent\":0,\"keys\":[\"1\",\"2\"],\"prefix\":\"k\",\"seed\":\"a2\"},{\"version\":\"y\",\"percent\":50,\"prefix\":\"\",\"seed\":\"a2\"}]}",
        (t, errorCode) -> {});

    DeploymentFactoryBase base = new DeploymentFactoryBase(AsyncByteCodeCache.DIRECT, RuntimeEnvironment.Tooling);
    Assert.assertFalse(base.contains("space"));
    MockDeploySync sync = new MockDeploySync();
    OndemandDeploymentFactoryBase ondemand = new OndemandDeploymentFactoryBase(new DeploymentMetrics(new NoOpMetricsFactory()), base, new PlanFetcher() {
      @Override
      public void find(String space, Callback<DeploymentBundle> callback) {
        Assert.assertEquals("space", space);
        callback.success(new DeploymentBundle(plan, new TreeMap<>()));
      }
    }, sync);
    CountDownLatch latch = new CountDownLatch(2);
    ondemand.fetch(new Key("space", "key"), new Callback<LivingDocumentFactory>() {
      @Override
      public void success(LivingDocumentFactory value) {
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        ex.printStackTrace();
      }
    });
    ondemand.fetch(new Key("space", "key"), new Callback<LivingDocumentFactory>() {
      @Override
      public void success(LivingDocumentFactory value) {
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        ex.printStackTrace();
      }
    });
    Assert.assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
    Assert.assertTrue(base.contains("space"));
    sync.assertContains("space");
    CountDownLatch happyDeploy = new CountDownLatch(1);
    ondemand.deploy("space", new Callback<Void>() {
      @Override
      public void success(Void value) {
        happyDeploy.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {

      }
    });
    Assert.assertTrue(happyDeploy.await(10000, TimeUnit.MILLISECONDS));
    Assert.assertTrue(ondemand.spacesAvailable().contains("space"));
    HashMap<String, PredictiveInventory.MeteringSample> account = new HashMap<>();
    ondemand.account(account);
    Assert.assertNotNull(account.get("space"));
    int expectedMemSize = 272103;
    Assert.assertEquals(expectedMemSize, account.get("space").memory);
    ondemand.account(account);
    Assert.assertEquals(expectedMemSize * 2 , account.get("space").memory);
    ondemand.undeploy("space");
    Assert.assertFalse(ondemand.spacesAvailable().contains("space"));
    account = new HashMap<>();
    ondemand.account(account);
    Assert.assertNull(account.get("space"));
  }

  @Test
  public void deploy_prior_fetch() throws Exception{
    DeploymentPlan plan = new DeploymentPlan(
        "{\"versions\":{\"x\":\"\",\"y\":\"\",\"z\":\"\"},\"default\":\"z\",\"plan\":[{\"version\":\"x\",\"percent\":0,\"keys\":[\"1\",\"2\"],\"prefix\":\"k\",\"seed\":\"a2\"},{\"version\":\"y\",\"percent\":50,\"prefix\":\"\",\"seed\":\"a2\"}]}",
        (t, errorCode) -> {});

    DeploymentFactoryBase base = new DeploymentFactoryBase(AsyncByteCodeCache.DIRECT, RuntimeEnvironment.Tooling);
    Assert.assertFalse(base.contains("space"));
    MockDeploySync sync = new MockDeploySync();
    OndemandDeploymentFactoryBase ondemand = new OndemandDeploymentFactoryBase(new DeploymentMetrics(new NoOpMetricsFactory()), base, new PlanFetcher() {
      @Override
      public void find(String space, Callback<DeploymentBundle> callback) {
        Assert.assertEquals("space", space);
        callback.success(new DeploymentBundle(plan, new TreeMap<>()));
      }
    }, sync);
    CountDownLatch happyDeploy = new CountDownLatch(1);
    ondemand.deploy("space", new Callback<Void>() {
      @Override
      public void success(Void value) {
        happyDeploy.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {

      }
    });
    Assert.assertTrue(happyDeploy.await(10000, TimeUnit.MILLISECONDS));
    CountDownLatch latch = new CountDownLatch(2);
    ondemand.fetch(new Key("space", "key"), new Callback<LivingDocumentFactory>() {
      @Override
      public void success(LivingDocumentFactory value) {
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        ex.printStackTrace();
      }
    });
    ondemand.fetch(new Key("space", "key"), new Callback<LivingDocumentFactory>() {
      @Override
      public void success(LivingDocumentFactory value) {
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        ex.printStackTrace();
      }
    });
    Assert.assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
    Assert.assertTrue(base.contains("space"));
    sync.assertContains("space");
  }

  @Test
  public void bad_deploy() throws Exception {
    DeploymentPlan plan = new DeploymentPlan(
        "{\"versions\":{\"x\":\"int x\",\"y\":\"\",\"z\":\"\"},\"default\":\"z\",\"plan\":[{\"version\":\"x\",\"percent\":0,\"keys\":[\"1\",\"2\"],\"prefix\":\"k\",\"seed\":\"a2\"},{\"version\":\"y\",\"percent\":50,\"prefix\":\"\",\"seed\":\"a2\"}]}",
        (t, errorCode) -> {});

    DeploymentFactoryBase base = new DeploymentFactoryBase(AsyncByteCodeCache.DIRECT, RuntimeEnvironment.Tooling);
    Assert.assertFalse(base.contains("space"));
    MockDeploySync sync = new MockDeploySync();
    OndemandDeploymentFactoryBase ondemand = new OndemandDeploymentFactoryBase(new DeploymentMetrics(new NoOpMetricsFactory()), base, new PlanFetcher() {
      @Override
      public void find(String space, Callback<DeploymentBundle> callback) {
        Assert.assertEquals("space", space);
        callback.success(new DeploymentBundle(plan, new TreeMap<>()));
      }
    }, sync);
    CountDownLatch sadDeploy = new CountDownLatch(1);
    ondemand.deploy("space", new Callback<Void>() {
      @Override
      public void success(Void value) {

      }

      @Override
      public void failure(ErrorCodeException ex) {
        sadDeploy.countDown();
      }
    });
    Assert.assertTrue(sadDeploy.await(10000, TimeUnit.MILLISECONDS));
  }

  @Test
  public void sad() throws Exception {
    DeploymentPlan plan = new DeploymentPlan(
        "{\"versions\":{\"x\":\"int x\",\"y\":\"\",\"z\":\"\"},\"default\":\"z\",\"plan\":[{\"version\":\"x\",\"percent\":0,\"keys\":[\"1\",\"2\"],\"prefix\":\"k\",\"seed\":\"a2\"},{\"version\":\"y\",\"percent\":50,\"prefix\":\"\",\"seed\":\"a2\"}]}",
        (t, errorCode) -> {});

    DeploymentFactoryBase base = new DeploymentFactoryBase(AsyncByteCodeCache.DIRECT, RuntimeEnvironment.Tooling);
    Assert.assertFalse(base.contains("space"));
    MockDeploySync sync = new MockDeploySync();
    OndemandDeploymentFactoryBase ondemand = new OndemandDeploymentFactoryBase(new DeploymentMetrics(new NoOpMetricsFactory()), base, new PlanFetcher() {
      @Override
      public void find(String space, Callback<DeploymentBundle> callback) {
        Assert.assertEquals("space", space);
        callback.success(new DeploymentBundle(plan, new TreeMap<>()));
      }
    }, sync);
    CountDownLatch latch = new CountDownLatch(1);
    ondemand.fetch(new Key("space", "key"), new Callback<LivingDocumentFactory>() {
      @Override
      public void success(LivingDocumentFactory value) {

      }

      @Override
      public void failure(ErrorCodeException ex) {
        ex.printStackTrace();
        latch.countDown();
      }
    });
    Assert.assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
    Assert.assertFalse(base.contains("space"));
    sync.assertDoesNotContains("space");
  }

  @Test
  public void cant_find() throws Exception {
    DeploymentFactoryBase base = new DeploymentFactoryBase(AsyncByteCodeCache.DIRECT, RuntimeEnvironment.Tooling);
    Assert.assertFalse(base.contains("space"));
    MockDeploySync sync = new MockDeploySync();
    OndemandDeploymentFactoryBase ondemand = new OndemandDeploymentFactoryBase(new DeploymentMetrics(new NoOpMetricsFactory()), base, new PlanFetcher() {
      @Override
      public void find(String space, Callback<DeploymentBundle> callback) {
        callback.failure(new ErrorCodeException(123));
      }
    }, sync);

    CountDownLatch latch = new CountDownLatch(1);
    ondemand.fetch(new Key("space", "key"), new Callback<LivingDocumentFactory>() {
      @Override
      public void success(LivingDocumentFactory value) {

      }

      @Override
      public void failure(ErrorCodeException ex) {
        ex.printStackTrace();
        latch.countDown();
      }
    });
    Assert.assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
    CountDownLatch sadDeploy = new CountDownLatch(1);
    ondemand.deploy("space", new Callback<Void>() {
      @Override
      public void success(Void value) {

      }

      @Override
      public void failure(ErrorCodeException ex) {
        sadDeploy.countDown();
      }
    });
    Assert.assertTrue(sadDeploy.await(10000, TimeUnit.MILLISECONDS));
  }
}
