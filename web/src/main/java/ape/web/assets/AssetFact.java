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
package ape.web.assets;

import ape.common.Hashing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;

/**
 * Computed metadata for uploaded assets including size and cryptographic hashes.
 * Calculates MD5 (for HTTP Content-MD5 header) and SHA-384 (for integrity verification)
 * from file or byte array content. Used during asset upload to populate NtAsset metadata.
 */
public class AssetFact {
  public final long size;
  public final String md5;
  public final String sha384;

  public AssetFact(long size, String md5, String sha384) {
    this.size = size;
    this.md5 = md5;
    this.sha384 = sha384;
  }

  public static AssetFact of(AssetUploadBody body) throws IOException  {
    MessageDigest md5 = Hashing.md5();
    MessageDigest sha384 = Hashing.sha384();
    long size = 0;
    File file = body.getFileIfExists();
    if (file != null && file.exists()) {
      try(FileInputStream input = new FileInputStream(file)) {
        byte[] chunk = new byte[8196];
        int sz;
        while ((sz = input.read(chunk)) >= 0) {
          size += sz;
          md5.update(chunk, 0, sz);
          sha384.update(chunk, 0, sz);
       }
      }
    } else {
      byte[] bytes = body.getBytes();
      size = bytes.length;
      md5.update(bytes);
      sha384.update(bytes);
    }
    return new AssetFact(size, Hashing.finishAndEncode(md5), Hashing.finishAndEncode(sha384));
  }
}
