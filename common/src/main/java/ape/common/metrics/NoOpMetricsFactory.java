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
package ape.common.metrics;

public class NoOpMetricsFactory implements MetricsFactory {
  @Override
  public RequestResponseMonitor makeRequestResponseMonitor(String name) {
    return new RequestResponseMonitor() {
      @Override
      public RequestResponseMonitorInstance start() {
        return new RequestResponseMonitorInstance() {
          @Override
          public void success() {

          }

          @Override
          public void extra() {

          }

          @Override
          public void failure(int code) {

          }
        };
      }
    };
  }

  @Override
  public StreamMonitor makeStreamMonitor(String name) {
    return new StreamMonitor() {
      @Override
      public StreamMonitorInstance start() {
        return new StreamMonitorInstance() {
          @Override
          public void progress() {

          }

          @Override
          public void finish() {

          }

          @Override
          public void failure(int code) {

          }
        };
      }
    };
  }

  @Override
  public CallbackMonitor makeCallbackMonitor(String name) {
    return new CallbackMonitor() {
      @Override
      public CallbackMonitorInstance start() {
        return new CallbackMonitorInstance() {
          @Override
          public void success() {

          }

          @Override
          public void failure(int code) {

          }
        };
      }
    };
  }

  @Override
  public Runnable counter(String name) {
    return () -> {
    };
  }

  @Override
  public Inflight inflight(String name) {
    return new Inflight() {
      @Override
      public void up() {

      }

      @Override
      public void down() {

      }

      @Override
      public void set(int value) {
      }
    };
  }

  @Override
  public ItemActionMonitor makeItemActionMonitor(String name) {
    return new ItemActionMonitor() {
      @Override
      public ItemActionMonitorInstance start() {
        return new ItemActionMonitorInstance() {
          @Override
          public void executed() {

          }

          @Override
          public void rejected() {

          }

          @Override
          public void timeout() {

          }
        };
      }
    };
  }

  @Override
  public void page(String name, String title) {
  }

  @Override
  public void section(String title) {
  }
}
