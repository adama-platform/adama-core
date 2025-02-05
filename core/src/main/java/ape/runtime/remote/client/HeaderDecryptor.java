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
package ape.runtime.remote.client;

import ape.ErrorCodes;
import ape.common.ErrorCodeException;
import ape.common.ExceptionLogger;
import ape.common.keys.PrivateKeyBundle;

import java.util.TreeMap;
import java.util.regex.Pattern;

/** wrapper around keys to decrypt headers within generic clients */
public class HeaderDecryptor {
  private static final ExceptionLogger EXLOGGER = ExceptionLogger.FOR(HeaderDecryptor.class);

  private final TreeMap<Integer, PrivateKeyBundle> keys;

  public HeaderDecryptor(TreeMap<Integer, PrivateKeyBundle> keys) {
    this.keys = keys;
  }

  public String decrypt(String encryptedString) throws ErrorCodeException {
    String[] parts = encryptedString.split(Pattern.quote(";"));
    if (parts.length != 3) {
      throw new ErrorCodeException(ErrorCodes.HEADER_DECRYPTOR_INVALID_SECRET_FORMAT);
    }
    PrivateKeyBundle bundle;
    try {
      bundle = keys.get(Integer.parseInt(parts[0]));
      if (bundle == null) {
        throw new ErrorCodeException(ErrorCodes.HEADER_DECRYPTOR_BUNDLE_NOT_FOUND);
      }
    } catch (NumberFormatException nfe) {
      throw new ErrorCodeException(ErrorCodes.HEADER_DECRYPTOR_INVALID_BUNDLE_ID, nfe);
    }
    try {
      return bundle.decrypt(parts[1], parts[2]);
    } catch (Exception ex) {
      throw ErrorCodeException.detectOrWrap(ErrorCodes.HEADER_DECRYPTOR_FAILED_DECRYPTION, ex, EXLOGGER);
    }
  }
}
