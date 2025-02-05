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
package ape.support.testgen;

import ape.common.Callback;
import ape.common.SimpleExecutor;
import ape.common.TimeSource;
import ape.common.metrics.NoOpMetricsFactory;
import ape.runtime.contracts.DocumentMonitor;
import ape.runtime.data.Key;
import ape.runtime.contracts.Perspective;
import ape.runtime.json.JsonStreamReader;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.remote.MetricsReporter;
import ape.runtime.remote.ServiceRegistry;
import ape.runtime.sys.*;
import ape.runtime.sys.*;
import ape.runtime.sys.cron.NoOpWakeService;
import ape.translator.jvm.LivingDocumentFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class PhaseRun {
  public static void go(final LivingDocumentFactory factory, final DocumentMonitor monitor, final AtomicBoolean passedTests, final StringBuilder outputFile) throws Exception {
    ServiceRegistry.add("sample", SampleService.class, (s, stringObjectHashMap, keys) -> new SampleService());
    final var testTime = new AtomicLong(0);
    final var time = (TimeSource) () -> testTime.get();
    outputFile.append("--JAVA RUNNING-------------------------------------").append("\n");
    AtomicBoolean sawALoad = new AtomicBoolean(false);
    DumbDataService dds = new DumbDataService((patch) -> {
      if (patch.request.contains("\"command\":\"load\"")) {
        sawALoad.set(true);
      }
      outputFile.append(patch.request + "-->" + patch.redo + " need:" + patch.requiresFutureInvalidation + " in:" + patch.whenToInvalidateMilliseconds + "\n");
      testTime.addAndGet(Math.max(patch.whenToInvalidateMilliseconds / 2, 25));
    });
    DumbDataService.DumbDurableLivingDocumentAcquire acquire = new DumbDataService.DumbDurableLivingDocumentAcquire();
    Key key = new Key("0", "0");
    DocumentThreadBase base = new DocumentThreadBase(0, new ServiceShield(), new MetricsReporter() {
      @Override
      public void emitMetrics(Key key, String metricsPayload) {
      }
    }, dds, (key1, seq, reason, document, callback) -> {

    }, new NoOpWakeService(), new CoreMetrics(new NoOpMetricsFactory()), SimpleExecutor.NOW, time);
    DurableLivingDocument.fresh(key, factory, new CoreRequestContext(NtPrincipal.NO_ONE, "origin", "ip", key.key), "{}", "0", monitor, base, acquire);
    DurableLivingDocument doc = acquire.get();
    doc.invalidate(Callback.DONT_CARE_INTEGER);
    outputFile.append("CPU:").append(doc.getCodeCost()).append("\n");
    outputFile.append("MEMORY:").append(doc.getMemoryBytes()).append("\n");
    doc.createPrivateView(NtPrincipal.NO_ONE, wrap(str -> {
      outputFile.append("+ NO_ONE DELTA:").append(str).append("\n");
    }), new JsonStreamReader("{}"), DumbDataService.makePrinterPrivateView("NO_ONE", outputFile));
    doc.connect(new CoreRequestContext(NtPrincipal.NO_ONE, "phase", "ip", "key"), DumbDataService.makePrinterInt("NO_ONE", outputFile));
    final var rando = new NtPrincipal("rando", "random-place");
    doc.createPrivateView(rando, wrap(str -> {
      outputFile.append("+ RANDO DELTA:").append(str).append("\n");
    }), new JsonStreamReader("{}"), DumbDataService.makePrinterPrivateView("RANDO", outputFile));
    doc.connect(new CoreRequestContext(rando, "phase", "ip", "key"), DumbDataService.makePrinterInt("RANDO", outputFile));
    doc.invalidate(DumbDataService.makePrinterInt("RANDO", outputFile));
    outputFile.append("MEMORY:" + doc.getMemoryBytes() + "\n");
    outputFile.append("--JAVA RESULTS-------------------------------------").append("\n");
    outputFile.append(doc.json()).append("\n");
    outputFile.append("--DUMP RESULTS-------------------------------------").append("\n");
    outputFile.append(doc.document().__metrics()).append("\n");
    outputFile.append("--METRIC RESULTS-----------------------------------").append("\n");
    final var json = doc.json();
    dds.setData(json);
    outputFile.append(json).append("\n");
    DumbDataService.DumbDurableLivingDocumentAcquire acquire2 = new DumbDataService.DumbDurableLivingDocumentAcquire();
    DurableLivingDocument.load(key, factory, monitor, base, acquire2);

    DurableLivingDocument doc2 = acquire2.get();
    outputFile.append(doc2.json()).append("\n");
    if (sawALoad.get()) {
      outputFile.append("SKIPPING JSON COMPARE AS A LOAD WAS DETECTED\n");
      return;
    }
    mustBeTrue(doc2.json().equals(json), "JSON don't match load, dump cycle");
  }

  public static Perspective wrap(Consumer<String> consumer) {
    return new Perspective() {
      @Override
      public void data(String data) {
        consumer.accept(data);
      }

      @Override
      public void disconnect() {
      }
    };
  }

  public static void mustBeTrue(boolean v, String ex) {
    if (!v) {
      throw new RuntimeException(ex);
    }
  }
}
