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
package ape.runtime.sys.metering;

import ape.common.SimpleExecutor;
import ape.runtime.mocks.MockTime;
import ape.runtime.sys.PredictiveInventory;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DiskMeteringBatchMakerTests {

  private class MockMeteringBatchReady implements MeteringBatchReady {
    private final ArrayList<String> ready = new ArrayList<>();

    @Override
    public void init(DiskMeteringBatchMaker me) {

    }

    @Override
    public synchronized void ready(String batchId) {
      ready.add(batchId);
    }
  }

  private Runnable makeAdvance1000(MockTime time, DiskMeteringBatchMaker maker, String space) {
    return  () -> {
      maker.write(
          new MeterReading(
              time.nowMilliseconds(),
              100,
              space,
              "hash",
              new PredictiveInventory.MeteringSample(1000, 5234242, 4, 1000, 128, 42, 100, 200, 13, 1717)));
      time.time += 1000;
    };
  }

  @Test
  public void happy() throws Exception {
    boolean failedDelete = false;
    MockTime time = new MockTime(150000);
    File root = new File(File.createTempFile("ADAMATEST_", "123").getParentFile(), "temp_" + System.currentTimeMillis());
    try {
      root.mkdirs();
      Assert.assertTrue(root.isDirectory());
      MockMeteringBatchReady mock = new MockMeteringBatchReady();
      DiskMeteringBatchMaker maker = new DiskMeteringBatchMaker(time, SimpleExecutor.NOW, root, 3600000L, mock);
      Runnable advance1000 = makeAdvance1000(time, maker, "space");
      for (int k = 0; k < 3599; k++) {
        advance1000.run();
      }
      Assert.assertNull(maker.getNextAvailableBatchId());
      for (int k = 0; k < 200; k++) {
        advance1000.run();
      }
      String batchId = maker.getNextAvailableBatchId();
      Assert.assertNotNull(batchId);
      Assert.assertEquals(
          "{\"time\":\"3751000\",\"spaces\":{\"space\":{\"cpu\":\"18853739684\",\"messages\":\"3602000\",\"count_p95\":\"4\",\"memory_p95\":\"1000\",\"connections_p95\":\"128\",\"bandwidth\":\"151284\",\"first_party_service_calls\":\"360200\",\"third_party_service_calls\":\"720400\",\"cpu_ms\":\"46826\",\"backup_bytes_hours\":\"6184634\"}}}",
          maker.getBatch(batchId));
      maker.deleteBatch(batchId);
      CountDownLatch latchFlush = new CountDownLatch(1);
      maker.flush(latchFlush);
      Assert.assertTrue(latchFlush.await(1000, TimeUnit.MILLISECONDS));
      String batchId2 = maker.getNextAvailableBatchId();
      maker.deleteBatch(batchId2);

    } finally {
      for (File child : root.listFiles()) {
        if (child.getName().startsWith("SUMMARY")) {
          failedDelete = true;
        }
        child.delete();
      }
      root.delete();
    }
    Assert.assertFalse(failedDelete);
  }

  @Test
  public void recoverFromClose() throws Exception {
    boolean failedDelete = false;
    MockTime time = new MockTime(150000);
    File root = new File(File.createTempFile("ADAMATEST_", "123").getParentFile(), "temp_" + System.currentTimeMillis());
    try {
      root.mkdirs();
      Assert.assertTrue(root.isDirectory());
      MockMeteringBatchReady mock = new MockMeteringBatchReady();
      {
        DiskMeteringBatchMaker maker = new DiskMeteringBatchMaker(time, SimpleExecutor.NOW, root, 3600000L, mock);
        Runnable advance1000 = makeAdvance1000(time, maker, "space");
        for (int k = 0; k < 3599; k++) {
          advance1000.run();
        }
        maker.close();
      }
      DiskMeteringBatchMaker maker = new DiskMeteringBatchMaker(time, SimpleExecutor.NOW, root, 3600000L, mock);
      Assert.assertNull(maker.getNextAvailableBatchId());
      Runnable advance1000 = makeAdvance1000(time, maker, "space");
      for (int k = 0; k < 200; k++) {
        advance1000.run();
      }
      String batchId = maker.getNextAvailableBatchId();
      Assert.assertNotNull(batchId);
      Assert.assertEquals(
          "{\"time\":\"3751000\",\"spaces\":{\"space\":{\"cpu\":\"18853739684\",\"messages\":\"3602000\",\"count_p95\":\"4\",\"memory_p95\":\"1000\",\"connections_p95\":\"128\",\"bandwidth\":\"151284\",\"first_party_service_calls\":\"360200\",\"third_party_service_calls\":\"720400\",\"cpu_ms\":\"46826\",\"backup_bytes_hours\":\"6184634\"}}}",
          maker.getBatch(batchId));
      maker.deleteBatch(batchId);
    } finally {
      for (File child : root.listFiles()) {
        if (child.getName().startsWith("SUMMARY")) {
          failedDelete = true;
        }
        child.delete();
      }
      root.delete();
    }
    Assert.assertFalse(failedDelete);
  }


  @Test
  public void recoverFromCorruptionButExperienceDataLoss() throws Exception {
    boolean failedDelete = false;
    MockTime time = new MockTime(150000);
    File root = new File(File.createTempFile("ADAMATEST_", "123").getParentFile(), "temp_" + System.currentTimeMillis());
    try {
      root.mkdirs();
      Assert.assertTrue(root.isDirectory());
      MockMeteringBatchReady mock = new MockMeteringBatchReady();
      {
        DiskMeteringBatchMaker maker = new DiskMeteringBatchMaker(time, SimpleExecutor.NOW, root, 3600000L, mock);
        Runnable advance1000 = makeAdvance1000(time, maker, "space");
        for (int k = 0; k < 3599; k++) {
          advance1000.run();
        }
        maker.close();
      }
      {
        ArrayList<String> input = new ArrayList<>();
        try(FileReader reader = new FileReader(new File(root, "CURRENT"))) {
          try(BufferedReader buffered = new BufferedReader(reader)) {
            String ln;
            while ((ln = buffered.readLine()) != null) {
              input.add(ln);
            }
          }
        }
        int track = 0;
        try (OutputStream out = new FileOutputStream(new File(root, "CURRENT"))) {
          try (PrintWriter writer = new PrintWriter(out)) {
            for (String in : input) {
              track++;
              if (track > 3000) {
                // just don't write it
              } else if (track > 2500 && track % 3 == 0 || track > 2990) {
                writer.println(in.substring(0, in.length() / 2));
              } else {
                writer.println(in);
              }
            }
            writer.flush();
          }
        }
      }

      DiskMeteringBatchMaker maker = new DiskMeteringBatchMaker(time, SimpleExecutor.NOW, root, 3600000L, mock);
      Assert.assertNull(maker.getNextAvailableBatchId());
      Runnable advance1000 = makeAdvance1000(time, maker, "space");
      for (int k = 0; k < 200; k++) {
        advance1000.run();
      }
      String batchId = maker.getNextAvailableBatchId();
      Assert.assertNotNull(batchId);
      Assert.assertEquals(
          "{\"time\":\"3751000\",\"spaces\":{\"space\":{\"cpu\":\"14812904860\",\"messages\":\"2830000\",\"count_p95\":\"4\",\"memory_p95\":\"1000\",\"connections_p95\":\"128\",\"bandwidth\":\"118860\",\"first_party_service_calls\":\"283000\",\"third_party_service_calls\":\"566000\",\"cpu_ms\":\"36790\",\"backup_bytes_hours\":\"4859110\"}}}",
          maker.getBatch(batchId));
      maker.deleteBatch(batchId);
    } finally {
      for (File child : root.listFiles()) {
        if (child.getName().startsWith("SUMMARY")) {
          failedDelete = true;
        }
        child.delete();
      }
      root.delete();
    }
    Assert.assertFalse(failedDelete);
  }

}
