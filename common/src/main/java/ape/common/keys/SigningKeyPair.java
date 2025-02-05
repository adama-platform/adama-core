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

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jsonwebtoken.Jwts;
import ape.common.Json;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/** a signing pair for authentication (specifically for documents) */
public class SigningKeyPair {
  public final String algo;
  public final PrivateKey privateKey;
  public final PublicKey publicKey;

  public SigningKeyPair(String masterKey, String encryptedPayload) throws Exception {
    String payload = MasterKey.decrypt(masterKey, encryptedPayload);
    ObjectNode node = Json.parseJsonObject(payload);
    this.algo = node.get("algo").textValue();
    byte[] privateKey = Base64.getDecoder().decode(node.get("private").textValue());
    byte[] publicKey = Base64.getDecoder().decode(node.get("public").textValue());
    this.privateKey = KeyFactory.getInstance("EC").generatePrivate(new PKCS8EncodedKeySpec(privateKey));
    this.publicKey = KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(publicKey));
  }

  public static String generate(String masterKey) throws Exception {
    KeyPair pair = Jwts.SIG.ES256.keyPair().build();
    ObjectNode localKeyFile = Json.newJsonObject();
    localKeyFile.put("algo", "ES256");
    localKeyFile.put("private", new String(Base64.getEncoder().encode(pair.getPrivate().getEncoded())));
    localKeyFile.put("public", new String(Base64.getEncoder().encode(pair.getPublic().getEncoded())));
    return MasterKey.encrypt(masterKey, localKeyFile.toString());
  }

  public String signDocument(String space, String key, String agent) {
    return Jwts.builder().subject(agent).issuer("doc/" + space + "/" + key).signWith(privateKey).compact();
  }

  public void validateTokenThrows(String token) {
    Jwts.parser().verifyWith(publicKey).build().parse(token);
  }
}
