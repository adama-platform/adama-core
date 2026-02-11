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

import java.security.SecureRandom;

public class VAPIDPublicPrivateKeyPairTests {
  @Test
  public void constructFromFactory() throws Exception {
    VAPIDFactory factory = new VAPIDFactory(new SecureRandom());
    VAPIDPublicPrivateKeyPair pair = factory.generateKeyPair();
    Assert.assertNotNull(pair.publicKey);
    Assert.assertNotNull(pair.privateKey);
    Assert.assertNotNull(pair.publicKeyBase64);
    Assert.assertNotNull(pair.privateKeyBase64);
  }

  @Test
  public void roundTripConstruction() throws Exception {
    VAPIDFactory factory = new VAPIDFactory(new SecureRandom());
    VAPIDPublicPrivateKeyPair original = factory.generateKeyPair();
    // Reconstruct from the base64 strings
    VAPIDPublicPrivateKeyPair reconstructed = new VAPIDPublicPrivateKeyPair(
        original.publicKeyBase64, original.privateKeyBase64);
    Assert.assertEquals(original.publicKeyBase64, reconstructed.publicKeyBase64);
    Assert.assertEquals(original.privateKeyBase64, reconstructed.privateKeyBase64);
    Assert.assertEquals(original.publicKey.getW(), reconstructed.publicKey.getW());
  }

  @Test
  public void invalidPublicKeyFails() {
    try {
      new VAPIDPublicPrivateKeyPair("AAAA", "AAAA");
      Assert.fail();
    } catch (Exception e) {
      // Expected
    }
  }

  @Test
  public void multipleKeyPairsAreDifferent() throws Exception {
    VAPIDFactory factory = new VAPIDFactory(new SecureRandom());
    VAPIDPublicPrivateKeyPair pair1 = factory.generateKeyPair();
    VAPIDPublicPrivateKeyPair pair2 = factory.generateKeyPair();
    Assert.assertNotEquals(pair1.publicKeyBase64, pair2.publicKeyBase64);
    Assert.assertNotEquals(pair1.privateKeyBase64, pair2.privateKeyBase64);
  }
}
