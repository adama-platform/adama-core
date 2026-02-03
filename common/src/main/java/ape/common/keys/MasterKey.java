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

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256 encryption using a master key for protecting sensitive data at rest.
 * Uses CTR mode with random nonce for encryption. The master key is stored
 * as Base64 and used to encrypt other keys and secrets before database storage.
 */
public class MasterKey {
  public static String generateMasterKey() {
    SecureRandom secureRandom = new SecureRandom();
    byte[] key = new byte[256 / 8];
    secureRandom.nextBytes(key);
    return Base64.getEncoder().encodeToString(key);
  }

  public static String encrypt(String masterKey, String value) throws Exception {
    byte[] key = Base64.getDecoder().decode(masterKey);
    Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
    SecureRandom secureRandom = new SecureRandom();
    byte[] nonce = new byte[96 / 8];
    secureRandom.nextBytes(nonce);
    byte[] iv = new byte[128 / 8];
    System.arraycopy(nonce, 0, iv, 0, nonce.length);
    Key keySpec = new SecretKeySpec(key, "AES");
    IvParameterSpec ivSpec = new IvParameterSpec(iv);
    cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
    byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
    return Base64.getEncoder().encodeToString(nonce) + ";" + Base64.getEncoder().encodeToString(encrypted);
  }

  public static String decrypt(String masterKey, String value) throws Exception {
    byte[] key = Base64.getDecoder().decode(masterKey);
    Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
    String[] parts = value.split(";");
    byte[] nonce = Base64.getDecoder().decode(parts[0]);
    byte[] encrypted = Base64.getDecoder().decode(parts[1]);
    byte[] iv = new byte[128 / 8];
    System.arraycopy(nonce, 0, iv, 0, nonce.length);
    Key keySpec = new SecretKeySpec(key, "AES");
    IvParameterSpec ivSpec = new IvParameterSpec(iv);
    cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
    return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
  }
}
