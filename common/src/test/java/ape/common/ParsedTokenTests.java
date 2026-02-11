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
package ape.common;

import ape.ErrorCodes;
import org.junit.Assert;
import org.junit.Test;

import java.util.Base64;

public class ParsedTokenTests {

  private static String makeToken(String header, String payload, String signature) {
    return Base64.getEncoder().encodeToString(header.getBytes()) + "."
        + Base64.getEncoder().encodeToString(payload.getBytes()) + "."
        + Base64.getEncoder().encodeToString(signature.getBytes());
  }

  @Test
  public void validToken() throws Exception {
    String token = makeToken("{\"alg\":\"ES256\"}", "{\"iss\":\"adama\",\"sub\":\"user123\"}", "sig");
    ParsedToken parsed = new ParsedToken(token);
    Assert.assertEquals("adama", parsed.iss);
    Assert.assertEquals("user123", parsed.sub);
    Assert.assertEquals(-1, parsed.key_id);
    Assert.assertEquals(0, parsed.proxy_user_id);
    Assert.assertNull(parsed.proxy_authority);
    Assert.assertNull(parsed.proxy_origin);
    Assert.assertNull(parsed.proxy_ip);
    Assert.assertNull(parsed.proxy_useragent);
  }

  @Test
  public void validTokenWithKeyId() throws Exception {
    String token = makeToken("{\"alg\":\"ES256\"}", "{\"iss\":\"adama\",\"sub\":\"user\",\"kid\":42}", "sig");
    ParsedToken parsed = new ParsedToken(token);
    Assert.assertEquals("adama", parsed.iss);
    Assert.assertEquals("user", parsed.sub);
    Assert.assertEquals(42, parsed.key_id);
  }

  @Test
  public void validTokenWithProxyFields() throws Exception {
    String token = makeToken("{\"alg\":\"ES256\"}",
        "{\"iss\":\"adama\",\"sub\":\"user\",\"puid\":99,\"pa\":\"auth\",\"po\":\"origin\",\"pip\":\"1.2.3.4\",\"pua\":\"Mozilla\"}", "sig");
    ParsedToken parsed = new ParsedToken(token);
    Assert.assertEquals(99, parsed.proxy_user_id);
    Assert.assertEquals("auth", parsed.proxy_authority);
    Assert.assertEquals("origin", parsed.proxy_origin);
    Assert.assertEquals("1.2.3.4", parsed.proxy_ip);
    Assert.assertEquals("Mozilla", parsed.proxy_useragent);
  }

  @Test
  public void invalidLayout_noDots() {
    try {
      new ParsedToken("nodots");
      Assert.fail();
    } catch (ErrorCodeException e) {
      Assert.assertEquals(ErrorCodes.AUTH_INVALID_TOKEN_LAYOUT, e.code);
    }
  }

  @Test
  public void invalidLayout_onePart() {
    try {
      new ParsedToken("one.two");
      Assert.fail();
    } catch (ErrorCodeException e) {
      Assert.assertEquals(ErrorCodes.AUTH_INVALID_TOKEN_LAYOUT, e.code);
    }
  }

  @Test
  public void invalidLayout_tooManyParts() {
    try {
      new ParsedToken("a.b.c.d");
      Assert.fail();
    } catch (ErrorCodeException e) {
      Assert.assertEquals(ErrorCodes.AUTH_INVALID_TOKEN_LAYOUT, e.code);
    }
  }

  @Test
  public void invalidBase64Payload() {
    try {
      new ParsedToken(Base64.getEncoder().encodeToString("h".getBytes()) + ".!!!invalid!!!." + Base64.getEncoder().encodeToString("s".getBytes()));
      Assert.fail();
    } catch (ErrorCodeException e) {
      Assert.assertEquals(ErrorCodes.AUTH_INVALID_TOKEN_JSON, e.code);
    }
  }

  @Test
  public void payloadNotObject() {
    try {
      String token = Base64.getEncoder().encodeToString("h".getBytes()) + "."
          + Base64.getEncoder().encodeToString("[1,2,3]".getBytes()) + "."
          + Base64.getEncoder().encodeToString("s".getBytes());
      new ParsedToken(token);
      Assert.fail();
    } catch (ErrorCodeException e) {
      Assert.assertEquals(ErrorCodes.AUTH_INVALID_TOKEN_JSON, e.code);
    }
  }

  @Test
  public void missingIss() {
    try {
      String token = makeToken("{}", "{\"sub\":\"user\"}", "sig");
      new ParsedToken(token);
      Assert.fail();
    } catch (ErrorCodeException e) {
      Assert.assertEquals(ErrorCodes.AUTH_INVALID_TOKEN_JSON, e.code);
    }
  }

  @Test
  public void missingSub() {
    try {
      String token = makeToken("{}", "{\"iss\":\"adama\"}", "sig");
      new ParsedToken(token);
      Assert.fail();
    } catch (ErrorCodeException e) {
      Assert.assertEquals(ErrorCodes.AUTH_INVALID_TOKEN_JSON, e.code);
    }
  }

  @Test
  public void nonTextualIss() {
    try {
      String token = makeToken("{}", "{\"iss\":123,\"sub\":\"user\"}", "sig");
      new ParsedToken(token);
      Assert.fail();
    } catch (ErrorCodeException e) {
      Assert.assertEquals(ErrorCodes.AUTH_INVALID_TOKEN_JSON, e.code);
    }
  }

  @Test
  public void nonTextualSub() {
    try {
      String token = makeToken("{}", "{\"iss\":\"adama\",\"sub\":456}", "sig");
      new ParsedToken(token);
      Assert.fail();
    } catch (ErrorCodeException e) {
      Assert.assertEquals(ErrorCodes.AUTH_INVALID_TOKEN_JSON, e.code);
    }
  }

  @Test
  public void nonIntKeyIdDefaultsToMinusOne() throws Exception {
    String token = makeToken("{}", "{\"iss\":\"adama\",\"sub\":\"user\",\"kid\":\"notanint\"}", "sig");
    ParsedToken parsed = new ParsedToken(token);
    Assert.assertEquals(-1, parsed.key_id);
  }
}
