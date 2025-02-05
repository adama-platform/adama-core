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
package ape.runtime.data;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.SimpleExecutor;
import ape.common.metrics.NoOpMetricsFactory;
import ape.runtime.mocks.MockAdamaStream;
import org.junit.Assert;
import org.junit.Test;

public class DelayAdamaStreamTests {
  @Test
  public void just_delay() {
    DelayAdamaStream delay = new DelayAdamaStream(SimpleExecutor.NOW, new NoOpMetricsFactory().makeItemActionMonitor("xyz"));
    delay.attach("id", "name", "type", 1000, "md5", "sha", Callback.DONT_CARE_INTEGER);
    delay.canAttach(new Callback<Boolean>() {
      @Override
      public void success(Boolean value) {

      }

      @Override
      public void failure(ErrorCodeException ex) {

      }
    });
    delay.update("UPDATE!", Callback.DONT_CARE_VOID);
    delay.send("channel", "marker", "message", Callback.DONT_CARE_INTEGER);
    delay.send("failure", "marker", "message", Callback.DONT_CARE_INTEGER);
    delay.password("p", Callback.DONT_CARE_INTEGER);
    delay.close();
    MockAdamaStream stream = new MockAdamaStream();
    delay.ready(stream);
    delay.unready();
    Assert.assertEquals("ATTACH:id/name/type/1000/md5/sha\n" + "CANATTACH\n" + "UPDATE:UPDATE!\n" + "SEND:channel/marker/message\n" + "SEND:failure/marker/message\n" + "PASSWORD!\n" + "CLOSE\n", stream.toString());
  }

  @Test
  public void errors() {
    DelayAdamaStream delay = new DelayAdamaStream(SimpleExecutor.NOW, new NoOpMetricsFactory().makeItemActionMonitor("xyz"));
    for (int k = 0; k < 1000; k++) {
      delay.send("channel", "marker", "message", Callback.DONT_CARE_INTEGER);
    }
    delay.send("channel", "marker", "message", new Callback<Integer>() {
      @Override
      public void success(Integer value) {
        Assert.fail();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        Assert.assertEquals(115917, ex.code);
      }
    });
  }
}
