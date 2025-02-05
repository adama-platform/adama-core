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

import ape.runtime.contracts.LivingDocumentFactoryFactory;
import ape.runtime.sys.DocumentThreadBase;
import ape.runtime.sys.PredictiveInventory;
import ape.runtime.sys.readonly.ReadOnlyReplicaThreadBase;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/** a state machine for computing a service meter across all threads */
public class MeteringStateMachine {
  private final DocumentThreadBase[] bases;
  private final ReadOnlyReplicaThreadBase[] readonlyBases;
  private final Consumer<HashMap<String, PredictiveInventory.MeteringSample>> onFinalSampling;
  private final HashMap<String, PredictiveInventory.MeteringSample> accum;
  private int at;

  private MeteringStateMachine(DocumentThreadBase[] bases, ReadOnlyReplicaThreadBase[] readonlyBases, Consumer<HashMap<String, PredictiveInventory.MeteringSample>> onFinalSampling) {
    this.bases = bases;
    this.readonlyBases = readonlyBases;
    this.at = 0;
    this.onFinalSampling = onFinalSampling;
    this.accum = new HashMap<>();
  }

  public static void estimate(DocumentThreadBase[] bases, ReadOnlyReplicaThreadBase[] readonlyBases, LivingDocumentFactoryFactory factory, Consumer<HashMap<String, PredictiveInventory.MeteringSample>> onFinalSampling) {
    new MeteringStateMachine(bases, readonlyBases, onFinalSampling).seed(factory.spacesAvailable()).next();
  }

  private void next() {
    if (at < bases.length) {
      bases[at].sampleMetering((b) -> {
        for (Map.Entry<String, PredictiveInventory.MeteringSample> entry : b.entrySet()) {
          PredictiveInventory.MeteringSample prior = accum.get(entry.getKey());
          if (prior != null) {
            accum.put(entry.getKey(), PredictiveInventory.MeteringSample.add(prior, entry.getValue()));
          } else {
            accum.put(entry.getKey(), entry.getValue());
          }
        }
        at++;
        next();
      });
    } else {
      int adjustedAt = at - bases.length;
      if (adjustedAt < readonlyBases.length) {
        readonlyBases[adjustedAt].sampleMetering((b) -> {
          for (Map.Entry<String, PredictiveInventory.MeteringSample> entry : b.entrySet()) {
            PredictiveInventory.MeteringSample prior = accum.get(entry.getKey());
            if (prior != null) {
              accum.put(entry.getKey(), PredictiveInventory.MeteringSample.add(prior, entry.getValue()));
            } else {
              accum.put(entry.getKey(), entry.getValue());
            }
          }
          at++;
          next();
        });
      } else {
        onFinalSampling.accept(accum);
      }
    }
  }

  private MeteringStateMachine seed(Collection<String> spaces) {
    for (String space : spaces) {
      accum.put(space, new PredictiveInventory.MeteringSample(0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
    }
    return this;
  }
}
