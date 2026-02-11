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

import org.junit.Assert;
import org.junit.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECPoint;

public class ECPublicKeyCodecTests {
  @Test
  public void encodeECPublicKey() throws Exception {
    KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
    generator.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom());
    KeyPair keyPair = generator.generateKeyPair();
    ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();
    byte[] encoded = ECPublicKeyCodec.encode(publicKey);
    // Uncompressed EC point: 0x04 + 32 bytes X + 32 bytes Y = 65 bytes
    Assert.assertEquals(65, encoded.length);
    Assert.assertEquals(0x04, encoded[0]);
  }

  @Test
  public void encodeECPoint() throws Exception {
    KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
    generator.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom());
    KeyPair keyPair = generator.generateKeyPair();
    ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();
    ECPoint point = publicKey.getW();
    byte[] encoded = ECPublicKeyCodec.encode(point);
    Assert.assertEquals(65, encoded.length);
    Assert.assertEquals(0x04, encoded[0]);
  }

  @Test
  public void encodeMultipleKeysProduceDifferentResults() throws Exception {
    KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
    generator.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom());
    byte[] encoded1 = ECPublicKeyCodec.encode((ECPublicKey) generator.generateKeyPair().getPublic());
    byte[] encoded2 = ECPublicKeyCodec.encode((ECPublicKey) generator.generateKeyPair().getPublic());
    // Two different keys should produce different encodings
    Assert.assertFalse(java.util.Arrays.equals(encoded1, encoded2));
  }

  @Test
  public void roundTripThroughVAPIDFactory() throws Exception {
    VAPIDFactory factory = new VAPIDFactory(new SecureRandom());
    VAPIDPublicPrivateKeyPair pair = factory.generateKeyPair();
    // The public key was encoded via ECPublicKeyCodec.encode then base64
    // VAPIDPublicPrivateKeyPair constructor decodes it back
    Assert.assertNotNull(pair.publicKey);
    Assert.assertNotNull(pair.privateKey);
    // Re-encode the decoded public key and verify it matches
    byte[] reEncoded = ECPublicKeyCodec.encode(pair.publicKey);
    String reEncodedB64 = java.util.Base64.getEncoder().encodeToString(reEncoded);
    Assert.assertEquals(pair.publicKeyBase64, reEncodedB64);
  }
}
