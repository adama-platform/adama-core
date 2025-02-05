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
package ape.web.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import ape.common.ConfigObject;
import ape.common.Json;
import org.junit.Assert;
import org.junit.Test;

public class WebConfigTests {
  public static WebConfig mockConfig(Scenario scenario) throws Exception {
    ObjectNode configNode = Json.newJsonObject();
    configNode.put("http-port", scenario.port);
    configNode.put("websocket-heart-beat-ms", 250);
    return new WebConfig(new ConfigObject(configNode));
  }

  @Test
  public void defaults() throws Exception {
    WebConfig webConfig = new WebConfig(new ConfigObject(Json.newJsonObject()));
    Assert.assertEquals("/~health_check_lb", webConfig.healthCheckPath);
    Assert.assertEquals(4 * 1024 * 1024, webConfig.maxWebSocketFrameSize);
    Assert.assertEquals(2500, webConfig.timeoutWebsocketHandshake);
    Assert.assertEquals(12582912, webConfig.maxContentLengthSize);
    Assert.assertEquals(1000, webConfig.heartbeatTimeMilliseconds);
    Assert.assertEquals(8080, webConfig.port);
  }

  @Test
  public void override() throws Exception {
    ObjectNode node = Json.newJsonObject();
    node.put("http-port", 9000);
    node.put("http-max-content-length-size", 5000);
    node.put("websocket-max-frame-size", 7000);
    node.put("websocket-handshake-timeout-ms", 123);
    node.put("http-health-check-path", "HEALTH");
    node.put("websocket-heart-beat-ms", 666);
    WebConfig webConfig = new WebConfig(new ConfigObject(node));
    Assert.assertEquals(666, webConfig.heartbeatTimeMilliseconds);
    Assert.assertEquals("HEALTH", webConfig.healthCheckPath);
    Assert.assertEquals(7000, webConfig.maxWebSocketFrameSize);
    Assert.assertEquals(123, webConfig.timeoutWebsocketHandshake);
    Assert.assertEquals(5000, webConfig.maxContentLengthSize);
    Assert.assertEquals(9000, webConfig.port);
  }

  public static enum Scenario {
    Mock1(15000),
    Mock2(15001),
    Mock3(15002),
    Dev(15003),
    Prod(15004),
    DevScope(15005),
    ProdScope(15006),
    ClientTest1(15100),
    ClientTest2(15101),
    ClientTest3(15102),
    ClientTest4(15103),
    ClientTest5(15104),
    Pool(16000),
    HttpExecute1(16001);

    public final int port;

    private Scenario(int port) {
      this.port = port;
    }
  }
}
