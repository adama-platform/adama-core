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
package ape.common.keys;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/** simple rsa keys */
public class RSAPemKey {
  // ssh-keygen -t rsa -b 4096 -m PEM -f rsa.key.4096
  // openssl pkcs8 -topk8 -inform PEM -in rsa.key.4096 -out private_key.rsa.pem -nocrypt
  public static RSAPrivateKey privateFrom(String pem) throws Exception {
    String simple = pem
        .replaceAll("[\r\n]", "")
        .replaceAll("-----(BEGIN|END) PRIVATE KEY-----", "");
    byte[] encoded = Base64.getDecoder().decode(simple.getBytes(StandardCharsets.UTF_8));
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
    return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
  }

  // openssl rsa -in rsa.key.4096 -pubout -outform PEM -out public_key.rsa.pem
  public static RSAPublicKey publicFrom(String pem) throws Exception {
    String simple = pem
        .replaceAll("[\r\n]", "")
        .replaceAll("-----(BEGIN|END) PUBLIC KEY-----", "");
    byte[] encoded = Base64.getDecoder().decode(simple.getBytes(StandardCharsets.UTF_8));
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
    return (RSAPublicKey) keyFactory.generatePublic(keySpec);
  }
}
