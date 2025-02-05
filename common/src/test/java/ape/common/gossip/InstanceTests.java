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
package ape.common.gossip;

import org.junit.Assert;
import org.junit.Test;

public class InstanceTests extends CommonTest {

  @Test
  public void flow() {
    Instance instance = new Instance(newBuilder().setCounter(224).setId("id").setIp("ip").setRole("proxy").setPort(4242).setMonitoringPort(10002).build(), 0, false);
    Assert.assertEquals(224, instance.counter());
    instance.bump(12);
    Assert.assertEquals(225, instance.counter());
    Assert.assertEquals(12, instance.witnessed());
    instance.absorb(0, 254242);
    Assert.assertEquals(225, instance.counter());
    Assert.assertEquals(12, instance.witnessed());
    instance.absorb(500, 254242);
    Assert.assertEquals(500, instance.counter());
    Assert.assertEquals(254242, instance.witnessed());
    Assert.assertFalse(instance.canDelete(0));
    Assert.assertEquals("id", instance.toEndpoint().id);
    Assert.assertEquals("ip", instance.toEndpoint().ip);
    Assert.assertEquals(500, instance.toEndpoint().counter);
    Assert.assertEquals("proxy", instance.toEndpoint().role);
    Assert.assertEquals(4242, instance.toEndpoint().port);
    Assert.assertEquals("ip:4242", instance.target());
    Assert.assertEquals("proxy", instance.role());
    Assert.assertEquals(10002, instance.toEndpoint().monitoringPort);
    Assert.assertEquals(0, Instance.humanizeCompare(instance, instance));
    Instance other = new Instance(newBuilder().setCounter(224).setId("id").setIp("ip2").setRole("proxy").setPort(4242).setMonitoringPort(10002).build(), 0, false);
    Assert.assertEquals(-1, Instance.humanizeCompare(instance, other));
  }

  @Test
  public void deletion() {
    Instance instance = new Instance(newBuilder().setCounter(224).setId("id").setIp("ip").setRole("proxy").setPort(4242).build(), 0, false);
    instance.bump(100);
    Assert.assertFalse(instance.canDelete(100));
    Assert.assertFalse(instance.tooOldMustDelete(100));
    Assert.assertFalse(instance.canDelete(7599));
    Assert.assertFalse(instance.tooOldMustDelete(7599));
    Assert.assertFalse(instance.canDelete(7600));
    Assert.assertFalse(instance.tooOldMustDelete(7600));
    Assert.assertTrue(instance.canDelete(7601));
    Assert.assertFalse(instance.tooOldMustDelete(7601));
    Assert.assertTrue(instance.canDelete(10099));
    Assert.assertFalse(instance.tooOldMustDelete(10099));
    Assert.assertTrue(instance.canDelete(10100));
    Assert.assertFalse(instance.tooOldMustDelete(10100));
    Assert.assertTrue(instance.canDelete(10101));
    Assert.assertTrue(instance.tooOldMustDelete(10101));
  }

  @Test
  public void equals() {
    Instance instance1 = new Instance(newBuilder().setCounter(224).setId("id").setIp("ip").setRole("proxy").setPort(4242).build(), 0, false);
    Instance instance2 = new Instance(newBuilder().setCounter(224).setId("id").setIp("ip").setRole("proxy").setPort(4242).build(), 0, false);
    Instance instance3 = new Instance(newBuilder().setCounter(224).setId("id2").setIp("ip").setRole("proxy").setPort(4242).build(), 0, false);
    Assert.assertEquals(instance1, instance1);
    Assert.assertEquals(instance1, instance2);
    Assert.assertNotEquals(instance3, instance1);
    Assert.assertNotEquals(instance3, instance2);
    Assert.assertNotEquals(instance3, "x");
    Assert.assertNotEquals(instance3, 123);
  }

  @Test
  public void compare() {
    Instance instance1 = new Instance(newBuilder().setCounter(224).setId("1").setIp("ip").setRole("proxy").setPort(4242).build(), 0, false);
    Instance instance2 = new Instance(newBuilder().setCounter(224).setId("2").setIp("ip").setRole("proxy").setPort(4242).build(), 0, false);
    Assert.assertEquals(-1, instance1.compareTo(instance2));
    Assert.assertEquals(1, instance2.compareTo(instance1));
    Assert.assertEquals(0, instance1.compareTo(instance1));
    Assert.assertEquals(0, instance2.compareTo(instance2));
  }

  @Test
  public void hashing() {
    Instance instance1 = new Instance(newBuilder().setCounter(224).setId("id").setIp("ip").setRole("proxy").setPort(4242).build(), 0, false);
    Instance instance2 = new Instance(newBuilder().setCounter(224).setId("id").setIp("ip").setRole("proxy").setPort(4242).build(), 0, false);
    Assert.assertEquals(211180553, instance1.hashCode());
    Assert.assertEquals(211180553, instance2.hashCode());
  }
}
