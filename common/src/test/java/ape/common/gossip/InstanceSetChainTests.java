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

public class InstanceSetChainTests extends CommonTest {
  @Test
  public void flow_empty() {
    MockTime time = new MockTime();
    InstanceSetChain chain = new InstanceSetChain(time);
    Assert.assertNull(chain.pick("xyz"));
    Assert.assertNull(chain.find("myhash"));
    InstanceSet set = chain.find(chain.current().hash());
    Assert.assertNotNull(set);
    chain.scan();
    chain.gc();
    Assert.assertEquals(0, chain.missing(set).length);
    Assert.assertEquals(0, chain.all().length);
    Assert.assertEquals(0, chain.recent().length);
    Assert.assertEquals(0, chain.deletes().length);
  }

  @Test
  public void timeproxy() {
    MockTime time = new MockTime();
    InstanceSetChain chain = new InstanceSetChain(time);
    time.currentTime = 10000;
    Assert.assertEquals(10000, chain.now());
    time.currentTime = 20000;
    Assert.assertEquals(20000, chain.now());
  }

  @Test
  public void scan() {
    MockTime time = new MockTime();
    InstanceSetChain chain = new InstanceSetChain(time);
    Assert.assertEquals("1B2M2Y8AsgTpgAmY7PhCfg==", chain.current().hash());
    time.currentTime = 5000;
    chain.ingest(ENDPOINTS(A(), B()), new String[]{}, false);
    chain.current().ingest(counters(1000, 1000), time.nowMilliseconds());
    Assert.assertEquals("ltgPF0P/6UcAj3+6Mnd/sA==", chain.current().hash());
    time.currentTime = 15000;
    Assert.assertEquals(5000, chain.scan());
    Assert.assertEquals("ltgPF0P/6UcAj3+6Mnd/sA==", chain.current().hash());
    time.currentTime = 15000;
    Assert.assertEquals(5000, chain.scan());
    Assert.assertEquals("ltgPF0P/6UcAj3+6Mnd/sA==", chain.current().hash());
    time.currentTime = 15001;
    Assert.assertEquals(15001, chain.scan());
    Assert.assertEquals("1B2M2Y8AsgTpgAmY7PhCfg==", chain.current().hash());
  }

  @Test
  public void scan_local_dont_change() {
    MockTime time = new MockTime();
    InstanceSetChain chain = new InstanceSetChain(time);
    Assert.assertEquals("1B2M2Y8AsgTpgAmY7PhCfg==", chain.current().hash());
    time.currentTime = 5000;
    chain.ingest(ENDPOINTS(A(), B()), new String[]{}, true);
    chain.current().ingest(counters(1000, 1000), time.nowMilliseconds());
    Assert.assertEquals("ltgPF0P/6UcAj3+6Mnd/sA==", chain.current().hash());
    time.currentTime = 15000;
    Assert.assertEquals(5000, chain.scan());
    Assert.assertEquals("ltgPF0P/6UcAj3+6Mnd/sA==", chain.current().hash());
    time.currentTime = 15000;
    Assert.assertEquals(5000, chain.scan());
    Assert.assertEquals("ltgPF0P/6UcAj3+6Mnd/sA==", chain.current().hash());
    time.currentTime = 15001;
    Assert.assertEquals(5000, chain.scan());
    Assert.assertEquals("ltgPF0P/6UcAj3+6Mnd/sA==", chain.current().hash());
  }

  @Test
  public void ingest() {
    MockTime time = new MockTime();
    InstanceSetChain chain = new InstanceSetChain(time);
    Assert.assertEquals("1B2M2Y8AsgTpgAmY7PhCfg==", chain.current().hash());
    chain.ingest(ENDPOINTS(A(), B()), new String[]{}, false);
    time.currentTime = 5000;
    Assert.assertEquals("ltgPF0P/6UcAj3+6Mnd/sA==", chain.current().hash());
    chain.current().ingest(counters(1000, 1000), time.nowMilliseconds());
    Assert.assertEquals("ltgPF0P/6UcAj3+6Mnd/sA==", chain.current().hash());
    time.currentTime = 10000;
    chain.ingest(ENDPOINTS(), new String[]{"id-a"}, false);
    Assert.assertEquals("ltgPF0P/6UcAj3+6Mnd/sA==", chain.current().hash());
    Assert.assertEquals(0, chain.deletes().length);
    time.currentTime = 15000;
    chain.ingest(ENDPOINTS(), new String[]{"id-a"}, false);
    Assert.assertEquals("PlBLj9Ty9gKbLiKc59dLig==", chain.current().hash());
    Assert.assertEquals(1, chain.deletes().length);
    Assert.assertEquals("id-a", chain.deletes()[0]);
    chain.ingest(ENDPOINTS(A(), B()), new String[]{}, false);
    Assert.assertEquals(0, chain.deletes().length);
    time.currentTime = 30001;
    chain.ingest(ENDPOINTS(), new String[]{"id-a"}, false);
    Assert.assertEquals(1, chain.deletes().length);
    chain.gc();
    Assert.assertEquals(1, chain.deletes().length);
    time.currentTime = 90001;
    chain.gc();
    Assert.assertEquals(1, chain.deletes().length);
    time.currentTime = 90002;
    chain.gc();
    Assert.assertEquals(0, chain.deletes().length);
  }
}
