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

public class MasterKeyTests {
  @Test
  public void flow() throws Exception {
    String key = MasterKey.generateMasterKey();
    String result = MasterKey.encrypt(key, "this is secure");
    String output = MasterKey.decrypt(key, result);
    Assert.assertEquals("this is secure", output);
  }

  @Test
  public void emptyStringRoundTrip() throws Exception {
    String key = MasterKey.generateMasterKey();
    String encrypted = MasterKey.encrypt(key, "");
    String decrypted = MasterKey.decrypt(key, encrypted);
    Assert.assertEquals("", decrypted);
  }

  @Test
  public void longValueRoundTrip() throws Exception {
    String key = MasterKey.generateMasterKey();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 10000; i++) {
      sb.append("abcdefghij");
    }
    String longValue = sb.toString();
    String encrypted = MasterKey.encrypt(key, longValue);
    String decrypted = MasterKey.decrypt(key, encrypted);
    Assert.assertEquals(longValue, decrypted);
  }

  @Test
  public void unicodeRoundTrip() throws Exception {
    String key = MasterKey.generateMasterKey();
    String value = "\u00e9\u00e8\u00ea\u00eb \u4e16\u754c \ud83d\ude00";
    String encrypted = MasterKey.encrypt(key, value);
    String decrypted = MasterKey.decrypt(key, encrypted);
    Assert.assertEquals(value, decrypted);
  }

  @Test
  public void distinctEncryptions() throws Exception {
    String key = MasterKey.generateMasterKey();
    String enc1 = MasterKey.encrypt(key, "same data");
    String enc2 = MasterKey.encrypt(key, "same data");
    // Different nonces should produce different ciphertext
    Assert.assertNotEquals(enc1, enc2);
    Assert.assertEquals(MasterKey.decrypt(key, enc1), MasterKey.decrypt(key, enc2));
  }

  @Test
  public void decryptMalformedNoSemicolon() {
    try {
      String key = MasterKey.generateMasterKey();
      MasterKey.decrypt(key, "nosemicolon");
      Assert.fail();
    } catch (Exception e) {
      Assert.assertTrue(e.getMessage().contains("nonce;ciphertext"));
    }
  }

  @Test
  public void decryptMalformedTooManySemicolons() {
    try {
      String key = MasterKey.generateMasterKey();
      MasterKey.decrypt(key, "a;b;c");
      Assert.fail();
    } catch (Exception e) {
      Assert.assertTrue(e.getMessage().contains("nonce;ciphertext"));
    }
  }

  @Test
  public void encryptBadKeyLength() {
    try {
      String shortKey = java.util.Base64.getEncoder().encodeToString(new byte[16]);
      MasterKey.encrypt(shortKey, "data");
      Assert.fail();
    } catch (Exception e) {
      Assert.assertTrue(e.getMessage().contains("256 bits"));
    }
  }

  @Test
  public void decryptBadKeyLength() {
    try {
      String shortKey = java.util.Base64.getEncoder().encodeToString(new byte[16]);
      String goodKey = MasterKey.generateMasterKey();
      String encrypted = MasterKey.encrypt(goodKey, "data");
      MasterKey.decrypt(shortKey, encrypted);
      Assert.fail();
    } catch (Exception e) {
      Assert.assertTrue(e.getMessage().contains("256 bits"));
    }
  }

  @Test
  public void generatedKeyLength() {
    String key = MasterKey.generateMasterKey();
    byte[] decoded = java.util.Base64.getDecoder().decode(key);
    Assert.assertEquals(32, decoded.length);
  }

  @Test
  public void wrongKeyDecryption() {
    try {
      String key1 = MasterKey.generateMasterKey();
      String key2 = MasterKey.generateMasterKey();
      String encrypted = MasterKey.encrypt(key1, "secret");
      String decrypted = MasterKey.decrypt(key2, encrypted);
      // CTR mode won't throw, but will produce garbage
      Assert.assertNotEquals("secret", decrypted);
    } catch (Exception e) {
      // Also acceptable if it throws
    }
  }

  @Test
  public void nonceValidation() {
    try {
      String key = MasterKey.generateMasterKey();
      // Create a value with a bad nonce (too short - only 4 bytes)
      String badNonce = java.util.Base64.getEncoder().encodeToString(new byte[4]);
      String ciphertext = java.util.Base64.getEncoder().encodeToString(new byte[10]);
      MasterKey.decrypt(key, badNonce + ";" + ciphertext);
      Assert.fail("Should have thrown for bad nonce length");
    } catch (Exception e) {
      Assert.assertTrue(e.getMessage().contains("nonce"));
    }
  }

  @Test
  public void nonceValidationTooLong() {
    try {
      String key = MasterKey.generateMasterKey();
      String badNonce = java.util.Base64.getEncoder().encodeToString(new byte[20]);
      String ciphertext = java.util.Base64.getEncoder().encodeToString(new byte[10]);
      MasterKey.decrypt(key, badNonce + ";" + ciphertext);
      Assert.fail("Should have thrown for bad nonce length");
    } catch (Exception e) {
      Assert.assertTrue(e.getMessage().contains("nonce"));
    }
  }
}
