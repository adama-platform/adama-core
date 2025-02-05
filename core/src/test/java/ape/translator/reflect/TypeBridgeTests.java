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
package ape.translator.reflect;

import ape.runtime.natives.NtPrincipal;
import ape.runtime.natives.NtList;
import ape.runtime.natives.NtMaybe;
import org.junit.Assert;
import org.junit.Test;

public class TypeBridgeTests {
  @Test
  public void basics() {
    Assert.assertEquals("int", TypeBridge.getAdamaType(Integer.class, null).getAdamaType());
    Assert.assertEquals("int", TypeBridge.getAdamaType(int.class, null).getAdamaType());
    Assert.assertEquals("bool", TypeBridge.getAdamaType(Boolean.class, null).getAdamaType());
    Assert.assertEquals("bool", TypeBridge.getAdamaType(boolean.class, null).getAdamaType());
    Assert.assertEquals("double", TypeBridge.getAdamaType(Double.class, null).getAdamaType());
    Assert.assertEquals("double", TypeBridge.getAdamaType(double.class, null).getAdamaType());
    Assert.assertEquals("string", TypeBridge.getAdamaType(String.class, null).getAdamaType());
    Assert.assertEquals("long", TypeBridge.getAdamaType(Long.class, null).getAdamaType());
    Assert.assertEquals("long", TypeBridge.getAdamaType(long.class, null).getAdamaType());
    Assert.assertEquals("principal", TypeBridge.getAdamaType(NtPrincipal.class, null).getAdamaType());
  }

  @Test
  public void ntlistNoAnnotation() {
    var worked = false;
    try {
      TypeBridge.getAdamaType(NtList.class, null);
      worked = true;
    } catch (final RuntimeException re) {
    }
    Assert.assertFalse(worked);
  }

  @Test
  public void ntMaybeNoAnnotation() {
    var worked = false;
    try {
      TypeBridge.getAdamaType(NtMaybe.class, null);
      worked = true;
    } catch (final RuntimeException re) {
    }
    Assert.assertFalse(worked);
  }

  @Test
  public void sanityTestVoid() {
    Assert.assertEquals(null, TypeBridge.getAdamaType(Void.class, null));
    Assert.assertEquals(null, TypeBridge.getAdamaType(void.class, null));
  }

  @Test
  public void unknownType() {
    var worked = false;
    try {
      TypeBridge.getAdamaType(TypeBridgeTests.class, null);
      worked = true;
    } catch (final RuntimeException re) {
    }
    Assert.assertFalse(worked);
  }
}
