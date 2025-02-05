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
package ape.runtime.remote.replication;

import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import org.junit.Assert;
import org.junit.Test;

public class TombStoneTests {
  @Test
  public void flowingCoverageOfGlory() {
    final String json;
    {
      TombStone ts = new TombStone("service", "method", "key");
      JsonStreamWriter writer = new JsonStreamWriter();
      ts.dump(writer);
      json = writer.toString();
    }
    Assert.assertEquals("{\"s\":\"service\",\"m\":\"method\",\"k\":\"key\"}", json);
    TombStone t = TombStone.read(new JsonStreamReader(json));
    Assert.assertEquals("service", t.service);
    Assert.assertEquals("method", t.method);
    Assert.assertEquals("key", t.key);
    Assert.assertNull(TombStone.read(new JsonStreamReader("{}")));
  }

  @Test
  public void skipNonObject() {
    Assert.assertNull(TombStone.read(new JsonStreamReader("null")));
    Assert.assertNull(TombStone.read(new JsonStreamReader("123")));
    Assert.assertNull(TombStone.read(new JsonStreamReader("true")));
    Assert.assertNull(TombStone.read(new JsonStreamReader("[123,42]")));
  }

  @Test
  public void skipJunk() {
    TombStone t = TombStone.read(new JsonStreamReader("{\"x\":123,\"s\":\"service\",\"nope\":true,\"m\":\"method\",\"yo\":[{}],\"k\":\"key\"}"));
    Assert.assertEquals("service", t.service);
    Assert.assertEquals("method", t.method);
    Assert.assertEquals("key", t.key);
  }

  @Test
  public void generated() {
    TombStone t = TombStone.read(new JsonStreamReader("{\"x\":123,\"s\":\"service\",\"nope\":true,\"m\":\"method\",\"yo\":[{}],\"k\":\"key\"}"));
    TombStone t2 = TombStone.read(new JsonStreamReader("{\"x\":8,\"s\":\"service\",\"nope\":false,\"m\":\"method\",\"yo\":[{}],\"k\":\"key\"}"));
    Assert.assertEquals(761482098, t.hashCode());
    Assert.assertEquals(t, t);
    Assert.assertEquals(t, t2);
    Assert.assertNotEquals(t, null);
    Assert.assertNotEquals(t, "nope");
  }
}
