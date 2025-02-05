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
package ape.runtime.security;

import io.jsonwebtoken.Jwts;
import ape.common.ErrorCodeException;
import ape.common.Json;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.Base64;

public class KeystoreTests {
  @Test
  public void flow() throws Exception {
    Keystore ks = Keystore.parse("{}");
    Keystore.validate(Json.newJsonObject());
    String privateKey = ks.generate("a001");
    Keystore.parsePrivateKey(Json.parseJsonObject(privateKey));
    Keystore ks2 = Keystore.parse(ks.persist());
    PrivateKey signingKey = Keystore.parsePrivateKey(Json.parseJsonObject(privateKey));
    String token1 = Jwts.builder().subject("agent").issuer("a001").signWith(signingKey).compact();
    ks.validate("a001", token1);
    try {
      ks.validate("a002", token1);
      Assert.fail();
    } catch (ErrorCodeException ex) {
      Assert.assertEquals(916531, ex.code);
    }
    try {
      Keystore.parse("");
    } catch (ErrorCodeException ex) {
      Assert.assertEquals(998459, ex.code);
    }
    try {
      Keystore.parse("{\"x\":[]}");
    } catch (ErrorCodeException ex) {
      Assert.assertEquals(949307, ex.code);
    }
  }

  @Test
  public void bad_public_key() throws Exception {
    try {
      Keystore.parsePublicKey(Json.parseJsonObject("{}"));
      Assert.fail();
    } catch (ErrorCodeException ece) {
      Assert.assertEquals(967735, ece.code);
    }
    try {
      Keystore.parsePublicKey(Json.parseJsonObject("{\"algo\":\"x\"}"));
      Assert.fail();
    } catch (ErrorCodeException ece) {
      Assert.assertEquals(901179, ece.code);
    }
    try {
      Keystore.parsePublicKey(Json.parseJsonObject("{\"algo\":\"x\",\"bytes64\":\"123\"}"));
      Assert.fail();
    } catch (ErrorCodeException ece) {
      Assert.assertEquals(907319, ece.code);
    }
    try {
      Keystore.parsePublicKey(Json.parseJsonObject("{\"algo\":\"x\",\"bytes64\":\""+ Base64.getEncoder().encode("XYZ".getBytes(StandardCharsets.UTF_8)) +"\"}"));
      Assert.fail();
    } catch (ErrorCodeException ece) {
      Assert.assertEquals(987191, ece.code);
    }
    try {
      Keystore.parsePublicKey(Json.parseJsonObject("{\"algo\":\"ES256\",\"bytes64\":\""+ Base64.getEncoder().encode("XYZ".getBytes(StandardCharsets.UTF_8)) +"\"}"));
      Assert.fail();
    } catch (ErrorCodeException ece) {
      Assert.assertEquals(987191, ece.code);
    }
  }

  @Test
  public void bad_private_key() throws Exception {
    try {
      Keystore.parsePrivateKey(Json.parseJsonObject("{}"));
      Assert.fail();
    } catch (ErrorCodeException ece) {
      Assert.assertEquals(967735, ece.code);
    }
    try {
      Keystore.parsePrivateKey(Json.parseJsonObject("{\"algo\":\"x\"}"));
      Assert.fail();
    } catch (ErrorCodeException ece) {
      Assert.assertEquals(901179, ece.code);
    }
    try {
      Keystore.parsePrivateKey(Json.parseJsonObject("{\"algo\":\"x\",\"bytes64\":\"123\"}"));
      Assert.fail();
    } catch (ErrorCodeException ece) {
      Assert.assertEquals(907319, ece.code);
    }
    try {
      Keystore.parsePrivateKey(Json.parseJsonObject("{\"algo\":\"x\",\"bytes64\":\""+ Base64.getEncoder().encode("XYZ".getBytes(StandardCharsets.UTF_8)) +"\"}"));
      Assert.fail();
    } catch (ErrorCodeException ece) {
      Assert.assertEquals(987191, ece.code);
    }
    try {
      Keystore.parsePrivateKey(Json.parseJsonObject("{\"algo\":\"ES256\",\"bytes64\":\""+ Base64.getEncoder().encode("XYZ".getBytes(StandardCharsets.UTF_8)) +"\"}"));
      Assert.fail();
    } catch (ErrorCodeException ece) {
      Assert.assertEquals(987191, ece.code);
    }
  }
}
