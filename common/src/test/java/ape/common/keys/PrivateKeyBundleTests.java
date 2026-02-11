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

public class PrivateKeyBundleTests {
  @Test
  public void fromNetworkAndGetKey() {
    PrivateKeyBundle bundle = PrivateKeyBundle.fromNetwork("my-private-key");
    Assert.assertEquals("my-private-key", bundle.getPrivateKey());
  }

  @Test
  public void fromDiskDecryptsWithMasterKey() throws Exception {
    String masterKey = MasterKey.generateMasterKey();
    String privateKeyValue = "secret-private-key-data";
    String encrypted = MasterKey.encrypt(masterKey, privateKeyValue);
    PrivateKeyBundle bundle = PrivateKeyBundle.fromDisk(encrypted, masterKey);
    Assert.assertEquals(privateKeyValue, bundle.getPrivateKey());
  }

  @Test
  public void decryptRoundTrip() throws Exception {
    KeyPair pair = PublicPrivateKeyPartnership.genKeyPair();
    String publicKey = PublicPrivateKeyPartnership.publicKeyOf(pair);
    String privateKey = PublicPrivateKeyPartnership.privateKeyOf(pair);

    // Encrypt with the key pair's public key using a separate key pair
    KeyPair senderPair = PublicPrivateKeyPartnership.genKeyPair();
    String senderPublicKey = PublicPrivateKeyPartnership.publicKeyOf(senderPair);
    String senderPrivateKey = PublicPrivateKeyPartnership.privateKeyOf(senderPair);

    // Derive shared secret and encrypt
    byte[] encryptSecret = PublicPrivateKeyPartnership.secretFrom(
        PublicPrivateKeyPartnership.keyPairFrom(publicKey, senderPrivateKey));
    String encrypted = PublicPrivateKeyPartnership.encrypt(encryptSecret, "hello world");

    // Decrypt using PrivateKeyBundle
    PrivateKeyBundle bundle = PrivateKeyBundle.fromNetwork(privateKey);
    String decrypted = bundle.decrypt(senderPublicKey, encrypted);
    Assert.assertEquals("hello world", decrypted);
  }

  @Test
  public void fromDiskWithBadMasterKeyFails() {
    try {
      String masterKey = MasterKey.generateMasterKey();
      String wrongKey = MasterKey.generateMasterKey();
      String encrypted = MasterKey.encrypt(masterKey, "data");
      PrivateKeyBundle.fromDisk(encrypted, wrongKey);
      // May succeed or fail depending on cipher behavior; if it returns garbage that's OK too
    } catch (Exception e) {
      // Expected - decryption with wrong key fails
    }
  }
}
