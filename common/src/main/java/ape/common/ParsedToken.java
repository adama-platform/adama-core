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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ape.ErrorCodes;

import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Lightweight JWT parser for extracting claims without full verification.
 * Parses the token payload to extract issuer, subject, key ID, and proxy
 * fields. Used to determine which signing key to use for full validation.
 */
public class ParsedToken {
  public final String iss;
  public final String sub;
  public final int key_id;
  public final int proxy_user_id;
  public final String proxy_authority;
  public final String proxy_origin;
  public final String proxy_ip;
  public final String proxy_useragent;

  public ParsedToken(String token) throws ErrorCodeException {
    String[] parts = token.split(Pattern.quote("."));
    if (parts.length == 3) {
      try {
        String middle = new String(Base64.getDecoder().decode(parts[1]));
        JsonMapper mapper = new JsonMapper();
        JsonNode treeRaw = mapper.readTree(middle);
        if (treeRaw != null && treeRaw.isObject()) {
          ObjectNode tree = (ObjectNode) treeRaw;
          JsonNode _iss = tree.get("iss");
          JsonNode _sub = tree.get("sub");
          JsonNode _key_id = tree.get("kid");
          if (_key_id != null && _key_id.isIntegralNumber()) {
            this.key_id = _key_id.asInt();
          } else {
            this.key_id = -1;
          }
          if (tree.has("puid")) {
            this.proxy_user_id = tree.get("puid").asInt();
          } else {
            this.proxy_user_id = 0;
          }
          this.proxy_authority = Json.readString(tree, "pa");
          this.proxy_origin = Json.readString(tree, "po");
          this.proxy_ip = Json.readString(tree, "pip");
          this.proxy_useragent = Json.readString(tree, "pua");
          if (_iss != null && _iss.isTextual() && _sub != null && _sub.isTextual()) {
            this.iss = _iss.textValue();
            this.sub = _sub.textValue();
            return;
          }
        }
        throw new ErrorCodeException(ErrorCodes.AUTH_INVALID_TOKEN_JSON_COMPLETE);
      } catch (Exception ex) {
        throw new ErrorCodeException(ErrorCodes.AUTH_INVALID_TOKEN_JSON, ex);
      }
    } else {
      throw new ErrorCodeException(ErrorCodes.AUTH_INVALID_TOKEN_LAYOUT);
    }
  }
}
