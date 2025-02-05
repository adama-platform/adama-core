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

import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.*;
import java.util.Base64;

/** Vapid keys; https://datatracker.ietf.org/doc/html/draft-ietf-webpush-vapid-01 */
public class VAPIDPublicPrivateKeyPair {
  public final String publicKeyBase64;
  public final String privateKeyBase64;
  public final ECPrivateKey privateKey;
  public final ECPublicKey publicKey;

  public VAPIDPublicPrivateKeyPair(String publicKeyBase64, String privateKeyBase64) throws Exception {
    this.publicKeyBase64 = publicKeyBase64;
    this.privateKeyBase64 = privateKeyBase64;
    AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
    parameters.init(new ECGenParameterSpec("secp256r1"));
    ECParameterSpec ecParameters = parameters.getParameterSpec(ECParameterSpec.class);
    ECPrivateKeySpec privateSpec = new ECPrivateKeySpec(new BigInteger(Base64.getDecoder().decode(privateKeyBase64)), ecParameters);
    byte[] publicRaw = Base64.getDecoder().decode(publicKeyBase64);
    BigInteger x = new BigInteger(1, publicRaw, 1, 32);
    BigInteger y = new BigInteger(1, publicRaw, 33, 32);
    ECPoint w = new ECPoint(x, y);
    ECPublicKeySpec publicSpec = new ECPublicKeySpec(w, ecParameters);
    KeyFactory kf = KeyFactory.getInstance("EC");
    privateKey = (ECPrivateKey) kf.generatePrivate(privateSpec);
    publicKey = (ECPublicKey) kf.generatePublic(publicSpec);
  }
}
