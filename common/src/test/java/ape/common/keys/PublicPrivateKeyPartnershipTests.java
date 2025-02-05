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
import java.util.Base64;

public class PublicPrivateKeyPartnershipTests {
  @Test
  public void flow() throws Exception {
    KeyPair a = PublicPrivateKeyPartnership.genKeyPair();
    KeyPair b = PublicPrivateKeyPartnership.genKeyPair();

    String x = Base64.getEncoder().encodeToString(PublicPrivateKeyPartnership.secretFrom(PublicPrivateKeyPartnership.keyPairFrom( //
        PublicPrivateKeyPartnership.publicKeyOf(a), //
        PublicPrivateKeyPartnership.privateKeyOf(b))));

    String y = Base64.getEncoder().encodeToString(PublicPrivateKeyPartnership.secretFrom(PublicPrivateKeyPartnership.keyPairFrom( //
        PublicPrivateKeyPartnership.publicKeyOf(b), //
        PublicPrivateKeyPartnership.privateKeyOf(a))));

    Assert.assertEquals(x, y);
  }

  @Test
  public void cipher() throws Exception {
    KeyPair x = PublicPrivateKeyPartnership.genKeyPair();
    byte[] secret = PublicPrivateKeyPartnership.secretFrom(x);
    String encrypted = PublicPrivateKeyPartnership.encrypt(secret, "This is a secret, yo");
    System.err.println(encrypted);
    System.err.println(PublicPrivateKeyPartnership.decrypt(secret, encrypted));
  }
}
