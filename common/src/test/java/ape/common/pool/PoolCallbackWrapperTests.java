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
package ape.common.pool;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.Living;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class PoolCallbackWrapperTests {
  public class LivingString implements Living {
    public final String value;

    public LivingString(String value) {
      this.value = value;
    }

    @Override
    public boolean alive() {
      return true;
    }
  }
  @Test
  public void coverage() {
    ArrayList<String> events = new ArrayList<>();
    PoolItem<LivingString> item = new PoolItem<LivingString>() {
      @Override
      public LivingString item() {
        return new LivingString("xyz");
      }

      @Override
      public void signalFailure() {
        events.add("ITEM:FAILURE");
      }

      @Override
      public void returnToPool() {
        events.add("ITEM:SUCCESS->RETURN");
      }
    };
    Callback<String> cb = new Callback<String>() {
      @Override
      public void success(String value) {
        events.add("CB:SUCCESS==" + value);
      }

      @Override
      public void failure(ErrorCodeException ex) {
        events.add("CB:FAILURE:" + ex.code);
      }
    };
    PoolCallbackWrapper<String, LivingString> w = new PoolCallbackWrapper<>(cb, item);
    w.success("xyz");
    Assert.assertEquals(2, events.size());
    w.failure(new ErrorCodeException(123));
    Assert.assertEquals(4, events.size());
    Assert.assertEquals("CB:SUCCESS==xyz", events.get(0));
    Assert.assertEquals("ITEM:SUCCESS->RETURN", events.get(1));
    Assert.assertEquals("CB:FAILURE:123", events.get(2));
    Assert.assertEquals("ITEM:FAILURE", events.get(3));
  }
}
