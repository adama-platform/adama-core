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

public class RxTableGuardTests {
  @Test
  public void root_all_pkey() {
    MockRxParent par = new MockRxParent();
    MockRxDependent dependent = new MockRxDependent(par);
    RxTableGuard guard = new RxTableGuard(dependent);
    guard.reset();
    guard.readAll();
    Assert.assertFalse(dependent.getAndResetInvalid());
    guard.primary(42);
    Assert.assertTrue(dependent.getAndResetInvalid());
  }

  @Test
  public void root_all_idx() {
    MockRxParent par = new MockRxParent();
    MockRxDependent dependent = new MockRxDependent(par);
    RxTableGuard guard = new RxTableGuard(dependent);
    guard.reset();
    guard.readAll();
    Assert.assertFalse(dependent.getAndResetInvalid());
    guard.index(123, 42);
    Assert.assertTrue(dependent.getAndResetInvalid());
  }

  @Test
  public void root_pkey_pkey_same() {
    MockRxParent par = new MockRxParent();
    MockRxDependent dependent = new MockRxDependent(par);
    RxTableGuard guard = new RxTableGuard(dependent);
    guard.reset();
    guard.readPrimaryKey(42);
    Assert.assertFalse(dependent.getAndResetInvalid());
    guard.primary(42);
    Assert.assertTrue(dependent.getAndResetInvalid());
  }

  @Test
  public void root_pkey_pkey_diff() {
    MockRxParent par = new MockRxParent();
    MockRxDependent dependent = new MockRxDependent(par);
    RxTableGuard guard = new RxTableGuard(dependent);
    guard.reset();
    guard.readPrimaryKey(42);
    Assert.assertFalse(dependent.getAndResetInvalid());
    guard.primary(52);
    Assert.assertFalse(dependent.getAndResetInvalid());
  }

  @Test
  public void root_pkey_idx() {
    MockRxParent par = new MockRxParent();
    MockRxDependent dependent = new MockRxDependent(par);
    RxTableGuard guard = new RxTableGuard(dependent);
    guard.reset();
    guard.readPrimaryKey(42);
    Assert.assertFalse(dependent.getAndResetInvalid());
    guard.index(24, 23);
    Assert.assertFalse(dependent.getAndResetInvalid());
  }

  @Test
  public void root_idx_pkey() {
    MockRxParent par = new MockRxParent();
    MockRxDependent dependent = new MockRxDependent(par);
    RxTableGuard guard = new RxTableGuard(dependent);
    guard.reset();
    guard.readIndexValue(1, 5);
    Assert.assertFalse(dependent.getAndResetInvalid());
    guard.primary(542);
    Assert.assertFalse(dependent.getAndResetInvalid());
  }

  @Test
  public void root_idx_idx_same_same() {
    MockRxParent par = new MockRxParent();
    MockRxDependent dependent = new MockRxDependent(par);
    RxTableGuard guard = new RxTableGuard(dependent);
    guard.reset();
    guard.readIndexValue(1, 5);
    Assert.assertFalse(dependent.getAndResetInvalid());
    guard.index(1, 5);
    Assert.assertTrue(dependent.getAndResetInvalid());
  }

  @Test
  public void root_idx_idx_diff_same() {
    MockRxParent par = new MockRxParent();
    MockRxDependent dependent = new MockRxDependent(par);
    RxTableGuard guard = new RxTableGuard(dependent);
    guard.reset();
    guard.readIndexValue(1, 5);
    Assert.assertFalse(dependent.getAndResetInvalid());
    guard.index(0, 5);
    Assert.assertFalse(dependent.getAndResetInvalid());
  }

  @Test
  public void root_idx_idx_same_diff() {
    MockRxParent par = new MockRxParent();
    MockRxDependent dependent = new MockRxDependent(par);
    RxTableGuard guard = new RxTableGuard(dependent);
    guard.reset();
    guard.readIndexValue(1, 5);
    Assert.assertFalse(dependent.getAndResetInvalid());
    guard.index(1, 4);
    Assert.assertFalse(dependent.getAndResetInvalid());
  }

  @Test
  public void child_all_pkey() {
    MockRxParent par = new MockRxParent();
    MockRxDependent dependent = new MockRxDependent(par);
    RxTableGuard guard = new RxTableGuard(dependent);
    guard.resetView(100);
    guard.readAll();
    Assert.assertFalse(guard.isFired(100));
    guard.primary(24);
    Assert.assertTrue(guard.isFired(100));
  }

  @Test
  public void child_all_idx() {
    MockRxParent par = new MockRxParent();
    MockRxDependent dependent = new MockRxDependent(par);
    RxTableGuard guard = new RxTableGuard(dependent);
    guard.resetView(100);
    guard.readAll();
    Assert.assertFalse(guard.isFired(100));
    guard.index(24, 42);
    Assert.assertTrue(guard.isFired(100));
  }

  @Test
  public void child_pkey_pkey_same() {
    MockRxParent par = new MockRxParent();
    MockRxDependent dependent = new MockRxDependent(par);
    RxTableGuard guard = new RxTableGuard(dependent);
    guard.resetView(100);
    guard.readPrimaryKey(42);
    Assert.assertFalse(guard.isFired(100));
    guard.primary(42);
    Assert.assertTrue(guard.isFired(100));
  }

  @Test
  public void child_pkey_pkey_diff() {
    MockRxParent par = new MockRxParent();
    MockRxDependent dependent = new MockRxDependent(par);
    RxTableGuard guard = new RxTableGuard(dependent);
    guard.resetView(100);
    guard.readPrimaryKey(42);
    Assert.assertFalse(guard.isFired(100));
    guard.primary(234);
    Assert.assertFalse(guard.isFired(100));
  }

  @Test
  public void child_idx_pkey() {
    MockRxParent par = new MockRxParent();
    MockRxDependent dependent = new MockRxDependent(par);
    RxTableGuard guard = new RxTableGuard(dependent);
    guard.resetView(100);
    guard.readIndexValue(1, 5);
    Assert.assertFalse(guard.isFired(100));
    guard.primary(542);
    Assert.assertFalse(guard.isFired(100));
  }

  @Test
  public void child_idx_idx_same_same() {
    MockRxParent par = new MockRxParent();
    MockRxDependent dependent = new MockRxDependent(par);
    RxTableGuard guard = new RxTableGuard(dependent);
    guard.resetView(100);
    guard.readIndexValue(1, 5);
    Assert.assertFalse(guard.isFired(100));
    guard.index(1, 5);
    Assert.assertTrue(guard.isFired(100));
  }

  @Test
  public void child_idx_idx_diff_same() {
    MockRxParent par = new MockRxParent();
    MockRxDependent dependent = new MockRxDependent(par);
    RxTableGuard guard = new RxTableGuard(dependent);
    guard.resetView(100);
    guard.readIndexValue(1, 5);
    Assert.assertFalse(guard.isFired(100));
    guard.index(0, 5);
    Assert.assertFalse(guard.isFired(100));
  }

  @Test
  public void child_idx_idx_same_diff() {
    MockRxParent par = new MockRxParent();
    MockRxDependent dependent = new MockRxDependent(par);
    RxTableGuard guard = new RxTableGuard(dependent);
    guard.resetView(100);
    guard.readIndexValue(1, 5);
    Assert.assertFalse(guard.isFired(100));
    guard.index(1, 4);
    Assert.assertFalse(guard.isFired(100));
  }

  @Test
  public void proxy_live() {
    MockRxParent par = new MockRxParent();
    MockRxDependent dependent = new MockRxDependent(par);
    RxTableGuard guard = new RxTableGuard(dependent);
    Assert.assertTrue(guard.alive());
    par.alive = false;
    Assert.assertFalse(guard.alive());
  }
}
