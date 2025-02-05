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
package ape.web.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import ape.common.Validators;

/** simple parser for a /SPACE/KEY/URI... request */
public class SpaceKeyRequest {
  public final String space;
  public final String key;
  public final String uri;

  public SpaceKeyRequest(String space, String key, String uri) {
    this.space = space;
    this.key = key;
    this.uri = uri;
  }

  public static SpaceKeyRequest parse(String uri) {
    int firstSlash = uri.indexOf('/');
    if (firstSlash >= 0) {
      int secondSlash = uri.indexOf('/', firstSlash + 1);
      if (secondSlash >= 0) {
        String space = uri.substring(firstSlash + 1, secondSlash);
        if (Validators.simple(space, 127)) {
          int third = uri.indexOf('/', secondSlash + 1);
          String key = third >= 0 ? uri.substring(secondSlash + 1, third) : uri.substring(secondSlash + 1);
          if (Validators.simple(key, 511)) {
            return new SpaceKeyRequest(space, key, third >= 0 ? uri.substring(third) : "/");
          }
        }
      }
    }
    return null;
  }

  public void logInto(ObjectNode logItem) {
    logItem.put("space", space);
    logItem.put("uri", uri);
    logItem.put("key", key);
  }

  public String cacheKey(String parameters) {
    return space + "/" + key + "/" + uri + "?" + parameters;
  }
}
