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
package ape.common;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * TLS identity bundle for a machine containing IP address, trust store,
 * certificate, and private key. Loads from JSON containing PEM-encoded
 * credentials, enabling secure inter-node communication in the cluster.
 */
public class MachineIdentity {
  public final String ip;
  private final String trust;
  private final String cert;
  private final String key;

  public MachineIdentity(String json) throws Exception {
    ObjectNode tree = Json.parseJsonObject(json);
    this.ip = Json.readString(tree, "ip");
    this.key = Json.readString(tree, "key");
    this.cert = Json.readString(tree, "cert");
    this.trust = Json.readString(tree, "trust");
    if (ip == null) {
      throw new Exception("ip was not found in json object");
    }
    if (key == null) {
      throw new Exception("key was not found in json object");
    }
    if (cert == null) {
      throw new Exception("cert was not found in json object");
    }
    if (trust == null) {
      throw new Exception("trust was not found in json object");
    }
  }

  public static String convertToJson(String ip, File trust, File cert, File key) throws Exception {
    ObjectNode tree = Json.newJsonObject();
    tree.put("ip", ip);
    tree.put("trust", Files.readString(trust.toPath()));
    tree.put("cert", Files.readString(cert.toPath()));
    tree.put("key", Files.readString(key.toPath()));
    return tree.toString();
  }

  public static MachineIdentity fromFile(String file) throws Exception {
    return new MachineIdentity(Files.readString(new File(file).toPath()));
  }

  public ByteArrayInputStream getTrust() {
    return new ByteArrayInputStream(trust.getBytes(StandardCharsets.UTF_8));
  }

  public ByteArrayInputStream getKey() {
    return new ByteArrayInputStream(key.getBytes(StandardCharsets.UTF_8));
  }

  public ByteArrayInputStream getCert() {
    return new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8));
  }
}
