/*
* Adama Platform and Language
* Copyright (C) 2021 - 2025 by Adama Platform Engineering, LLC
* 
* This program is free software for non-commercial purposes: 
* you can redistribute it and/or modify it under the terms of the 
* GNU Affero General Public License as published by the Free Software Foundation,
* either version 3 of the License, or (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
* 
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package ape.web.client;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.metrics.NoOpMetricsFactory;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.helpers.NOPLogger;

import java.util.TreeMap;

public class StringCallbackHttpResponderTests {
  @Test
  public void flow_400() {
    StringCallbackHttpResponder responder = new StringCallbackHttpResponder(NOPLogger.NOP_LOGGER, new NoOpMetricsFactory().makeRequestResponseMonitor("x").start(), new Callback<String>() {
      @Override
      public void success(String value) {
        Assert.fail();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        Assert.assertEquals(979143, ex.code);
      }
    });
    responder.start(new SimpleHttpResponseHeader(400, new TreeMap<>()));
    responder.bodyStart(32);
    responder.bodyFragment(new byte[32], 0, 32);
    responder.bodyEnd();
  }

  @Test
  public void flow_200() {
    StringCallbackHttpResponder responder = new StringCallbackHttpResponder(NOPLogger.NOP_LOGGER, new NoOpMetricsFactory().makeRequestResponseMonitor("x").start(), new Callback<String>() {
      @Override
      public void success(String value) {
        Assert.assertEquals("Hello World", value);
      }

      @Override
      public void failure(ErrorCodeException ex) {
        Assert.fail();
      }
    });
    responder.start(new SimpleHttpResponseHeader(200, new TreeMap<>()));
    byte[] hello = "Hello World".getBytes();
    responder.bodyStart(hello.length);
    responder.bodyFragment(hello, 0, hello.length);
    responder.bodyEnd();
  }

  @Test
  public void flow_200_fragments() {
    StringCallbackHttpResponder responder = new StringCallbackHttpResponder(NOPLogger.NOP_LOGGER, new NoOpMetricsFactory().makeRequestResponseMonitor("x").start(), new Callback<String>() {
      @Override
      public void success(String value) {
        Assert.assertEquals("Hello World", value);
      }

      @Override
      public void failure(ErrorCodeException ex) {
        Assert.fail();
      }
    });
    responder.start(new SimpleHttpResponseHeader(200, new TreeMap<>()));
    byte[] hello = "Hello World".getBytes();
    responder.bodyStart(hello.length);
    responder.bodyFragment(hello, 0, 5);
    responder.bodyFragment(hello, 5, hello.length - 5);
    responder.bodyEnd();
  }

  @Test
  public void failure() {
    StringCallbackHttpResponder responder = new StringCallbackHttpResponder(NOPLogger.NOP_LOGGER, new NoOpMetricsFactory().makeRequestResponseMonitor("x").start(), new Callback<String>() {
      @Override
      public void success(String value) {
        Assert.fail();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        Assert.assertEquals(123, ex.code);
      }
    });
    responder.failure(new ErrorCodeException(123));
  }
}
