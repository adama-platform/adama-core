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

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;

import java.security.PrivateKey;
import java.util.Date;
import java.util.TreeMap;

/** a private key with a public id */
public class PrivateKeyWithId {
  public final int keyId;
  public final PrivateKey privateKey;

  public PrivateKeyWithId(int keyId, PrivateKey privateKey) {
    this.keyId = keyId;
    this.privateKey = privateKey;
  }

  private String sign(String agent, String authority, long expiry, String scopes) {
    TreeMap<String, Object> claims = new TreeMap<>();
    claims.put("kid", keyId);
    if (scopes != null) {
      claims.put("scp", scopes);
    }
    JwtBuilder builder = Jwts.builder().claims(claims).subject(agent);
    if (expiry > 0) {
      builder = builder.expiration(new Date(expiry));
    }
    return builder.issuer(authority).signWith(privateKey).compact();
  }

  public String signDocumentIdentity(String agent, String space, String key, long expiry) {
    return sign(agent, "doc/" + space + "/" + key, expiry, null);
  }

  public String signSocialUser(int userId, long expiry, String scopes) {
    return sign("" + userId, "user", expiry, scopes);
  }

  public String signDeveloper(int userId, long expiry) {
    return sign("" + userId, "adama", expiry, null);
  }
}
