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

/** for URIs of the for /key/uri ; this will parse out the key */
public class KeyPrefixUri {
  public final String key;
  public final String uri;

  public KeyPrefixUri(String key, String uri) {
    this.key = key;
    this.uri = uri;
  }

  public static KeyPrefixUri fromCompleteUri(String uri) {
    int offset = uri.charAt(0) == '/' ? 1 : 0;
    int slashIndex = uri.indexOf('/', offset);
    if (slashIndex > offset) {
      return new KeyPrefixUri(uri.substring(offset, slashIndex), uri.substring(slashIndex));
    } else {
      return new KeyPrefixUri(uri.substring(offset), "/");
    }
  }
}
