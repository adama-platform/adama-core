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
package ape.runtime.natives;

import java.util.Objects;

/**
 * Immutable file/blob asset reference attached to a document.
 * Contains metadata about stored files: unique ID, original filename, MIME content
 * type, size in bytes, and integrity hashes (MD5, SHA-384). Assets are stored
 * externally and garbage-collected based on document references. The NOTHING
 * sentinel represents an empty/unset asset reference.
 */
public class NtAsset implements Comparable<NtAsset> {
  public static final NtAsset NOTHING = new NtAsset("", "", "", 0, "", "");
  public final String id;
  public final String name;
  public final String contentType;
  public final long size;
  public final String md5;
  public final String sha384;

  public NtAsset(String id, String name, String contentType, long size, String md5, String sha384) {
    this.id = id;
    this.name = name;
    this.contentType = contentType;
    this.size = size;
    this.md5 = md5;
    this.sha384 = sha384;
  }

  public String id() {
    return id;
  }

  public boolean valid() {
    return id.length() > 0;
  }

  public String name() {
    return name;
  }

  public String type() {
    return contentType;
  }

  public long size() {
    return size;
  }

  public String md5() {
    return md5;
  }

  public String sha384() {
    return sha384;
  }

  @Override
  public int compareTo(NtAsset o) {
    return id.compareTo(o.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, contentType, size, md5, sha384);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    NtAsset ntAsset = (NtAsset) o;
    return id.equals(ntAsset.id) && size == ntAsset.size && Objects.equals(name, ntAsset.name) && Objects.equals(contentType, ntAsset.contentType) && Objects.equals(md5, ntAsset.md5) && Objects.equals(sha384, ntAsset.sha384);
  }

  public long memory() {
    return (id.length() + name.length() + contentType.length() + md5.length() + sha384.length()) * 2L + 48;
  }
}
