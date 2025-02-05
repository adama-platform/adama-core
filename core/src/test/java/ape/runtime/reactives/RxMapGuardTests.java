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
package ape.runtime.reactives;

import ape.runtime.mocks.MockRxParent;
import org.junit.Assert;
import org.junit.Test;

public class RxMapGuardTests {
  @Test
  public void root_changed() {
    MockRxParent par = new MockRxParent();
    MockRxDependent dependent = new MockRxDependent(par);
    RxMapGuard<String> guard = new RxMapGuard<>(dependent);
    guard.reset();
    guard.readAll();
    Assert.assertFalse(dependent.getAndResetInvalid());
    guard.changed("foo");
    Assert.assertTrue(dependent.getAndResetInvalid());
  }

  @Test
  public void root_specific_miss() {
    MockRxParent par = new MockRxParent();
    MockRxDependent dependent = new MockRxDependent(par);
    RxMapGuard<String> guard = new RxMapGuard<>(dependent);
    guard.reset();
    guard.readKey("zoo");
    Assert.assertFalse(dependent.getAndResetInvalid());
    guard.changed("foo");
    Assert.assertFalse(dependent.getAndResetInvalid());
  }

  @Test
  public void root_specific_hit() {
    MockRxParent par = new MockRxParent();
    MockRxDependent dependent = new MockRxDependent(par);
    RxMapGuard<String> guard = new RxMapGuard<>(dependent);
    guard.reset();
    guard.readKey("foo");
    Assert.assertFalse(dependent.getAndResetInvalid());
    guard.changed("foo");
    Assert.assertTrue(dependent.getAndResetInvalid());
  }

  @Test
  public void child_change() {
    MockRxParent par = new MockRxParent();
    MockRxDependent dependent = new MockRxDependent(par);
    RxMapGuard<String> guard = new RxMapGuard<>(dependent);
    guard.resetView(100);
    guard.readAll();
    Assert.assertFalse(guard.isFired(100));
    guard.changed("foo");
    Assert.assertTrue(guard.isFired(100));
  }

  @Test
  public void child_specific_miss() {
    MockRxParent par = new MockRxParent();
    MockRxDependent dependent = new MockRxDependent(par);
    RxMapGuard<String> guard = new RxMapGuard<>(dependent);
    guard.resetView(100);
    guard.readKey("zoo");
    Assert.assertFalse(guard.isFired(100));
    guard.changed("foo");
    Assert.assertFalse(guard.isFired(100));
  }

  @Test
  public void child_specific_hit() {
    MockRxParent par = new MockRxParent();
    MockRxDependent dependent = new MockRxDependent(par);
    RxMapGuard<String> guard = new RxMapGuard<>(dependent);
    guard.resetView(100);
    guard.readKey("foo");
    Assert.assertFalse(guard.isFired(100));
    guard.changed("foo");
    Assert.assertTrue(guard.isFired(100));
  }
}
