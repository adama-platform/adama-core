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
package ape.runtime.sys.mocks;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.runtime.sys.AuthResponse;
import org.junit.Assert;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SimpleAuthCallback implements Callback<AuthResponse> {
  public AuthResponse value;
  private boolean success;
  private int count;
  private int reason;
  private CountDownLatch latch;

  public SimpleAuthCallback() {
    this.value = null;
    this.success = false;
    this.count = 0;
    this.reason = -1;
    this.latch = new CountDownLatch(1);
  }

  @Override
  public synchronized void success(AuthResponse value) {
    this.value = value;
    this.success = true;
    this.count++;
    latch.countDown();
  }

  @Override
  public synchronized void failure(ErrorCodeException ex) {
    this.reason = ex.code;
    this.success = false;
    this.count++;
    latch.countDown();
  }

  public void assertSuccess(String expected) throws Exception {
    Assert.assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
    synchronized (this) {
      Assert.assertEquals(1, count);
      Assert.assertTrue(success);
      Assert.assertEquals(expected, this.value.agent + "/" + this.value.hash);
    }
  }

  public void assertFailure(int code) throws Exception {
    Assert.assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
    synchronized (this) {
      Assert.assertEquals(1, count);
      Assert.assertFalse(success);
      Assert.assertEquals(code, this.reason);
    }
  }
}