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

/**
 * Encapsulates a private key for decryption operations.
 * Can be loaded from disk (encrypted with master key) or from network
 * (plaintext). Uses X25519 elliptic curve key exchange for deriving
 * shared secrets used in AES decryption.
 */
public class PrivateKeyBundle {
  private final String privateKey;

  private PrivateKeyBundle(String privateKey) {
    this.privateKey = privateKey;
  }

  // TODO: I'd like to consider optimizing the decrypt process OR locking it down more
  /*
byte[] rawPublicKey = Base64.getDecoder().decode(publicKey);
byte[] rawPrivateKey = Base64.getDecoder().decode(privateKey);
KeyFactory kf = KeyFactory.getInstance("XDH");
NamedParameterSpec spec = new NamedParameterSpec("X25519");
PublicKey pubKey = kf.generatePublic(new XECPublicKeySpec(spec, new BigInteger(rawPublicKey)));
PrivateKey prvKey = kf.generatePrivate(new XECPrivateKeySpec(spec, rawPrivateKey));
return new KeyPair(pubKey, prvKey);
*/

  public static PrivateKeyBundle fromDisk(String encryptedPrivateKey, String masterKey) throws Exception {
    return new PrivateKeyBundle(MasterKey.decrypt(masterKey, encryptedPrivateKey));
  }

  public static PrivateKeyBundle fromNetwork(String privateKey) {
    return new PrivateKeyBundle(privateKey);
  }

  public String getPrivateKey() {
    return privateKey;
  }

  public String decrypt(String publicKey, String cipher) throws Exception {
    byte[] secret = PublicPrivateKeyPartnership.secretFrom(PublicPrivateKeyPartnership.keyPairFrom(publicKey, privateKey));
    return PublicPrivateKeyPartnership.decrypt(secret, cipher);
  }
}
