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
import ape.common.ErrorCodeException;
import ape.runtime.contracts.DeleteTask;
import ape.runtime.data.ComputeMethod;
import ape.runtime.data.DocumentSnapshot;
import ape.runtime.data.Key;
import ape.runtime.data.LocalDocumentChange;
import ape.runtime.json.PrivateView;
import org.junit.Assert;
import org.junit.Test;

public class DumbDataServiceTests {
  @Test
  public void coverage() {
    DumbDataService dds = new DumbDataService((t) -> {});
    dds.get(
        new Key("0", "0"),
        new Callback<LocalDocumentChange>() {
          @Override
          public void success(LocalDocumentChange value) {
            Assert.fail();
          }

          @Override
          public void failure(ErrorCodeException ex) {}
        });
    Key key = new Key("?", "1");
    dds.delete(
        key,
        DeleteTask.TRIVIAL,
        new Callback<Void>() {
          @Override
          public void success(Void value) {}

          @Override
          public void failure(ErrorCodeException ex) {
            Assert.fail();
          }
        });
    dds.compute(
        key,
        ComputeMethod.Rewind,
        1,
        new Callback<LocalDocumentChange>() {
          @Override
          public void success(LocalDocumentChange value) {
            Assert.assertEquals("{\"x\":1000}", value.patch);
          }

          @Override
          public void failure(ErrorCodeException ex) {
            Assert.fail();
          }
        });
  }

  @Test
  public void acquire() {
    DumbDataService.DumbDurableLivingDocumentAcquire acquire =
        new DumbDataService.DumbDurableLivingDocumentAcquire();
    try {
      acquire.get();
      Assert.fail();
    } catch (NullPointerException npe) {
    }
    try {
      acquire.failure(new ErrorCodeException(0, new Exception()));
      Assert.fail();
    } catch (RuntimeException re) {
    }
  }

  @Test
  public void nocompact() {
    try {
      new DumbDataService((up) -> {}).snapshot(null, new DocumentSnapshot(1, "{}",-1, 1234L), null);
      Assert.fail();
    } catch (UnsupportedOperationException re) {
    }
  }

  @Test
  public void printer_coverage() {
    StringBuilder sb = new StringBuilder();
    Callback<Integer> cb1 = DumbDataService.makePrinterInt("X", sb);
    Callback<PrivateView> cb2 = DumbDataService.makePrinterPrivateView("Y", sb);
    cb1.success(123);
    cb2.success(null);
    cb1.failure(new ErrorCodeException(123));
    cb2.failure(new ErrorCodeException(456));
    Assert.assertEquals("X|SUCCESS:123\n" + "Y: CREATED PRIVATE VIEW\n" + "X|FAILURE:123\n" + "Y: FAILED PRIVATE VIEW DUE TO:456\n", sb.toString());
  }
}
