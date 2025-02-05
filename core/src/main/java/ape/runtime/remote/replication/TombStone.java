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
package ape.runtime.remote.replication;

import ape.common.Hashing;
import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Objects;

/** represents a future delete */
public class TombStone {
  private static final byte[] BOUNDARY = ";".getBytes(StandardCharsets.UTF_8);
  public final String service;
  public final String method;
  public final String key;
  public final String md5;

  public TombStone(String service, String method, String key) {
    this.service = service;
    this.method = method;
    this.key = key;
    MessageDigest digest = Hashing.md5();
    digest.update(service.getBytes(StandardCharsets.UTF_8));
    digest.update(BOUNDARY);
    digest.update(method.getBytes(StandardCharsets.UTF_8));
    digest.update(BOUNDARY);
    digest.update(key.getBytes(StandardCharsets.UTF_8));
    this.md5 = Hashing.finishAndEncode(digest);
  }

  public void dump(JsonStreamWriter writer) {
    writer.beginObject();
    writer.writeObjectFieldIntro("s");
    writer.writeString(service);
    writer.writeObjectFieldIntro("m");
    writer.writeString(method);
    writer.writeObjectFieldIntro("k");
    writer.writeString(key);
    writer.endObject();
  }

  public static TombStone read(JsonStreamReader reader) {
    if (reader.startObject()) {
      String service = null;
      String method = null;
      String key = null;
      while (reader.notEndOfObject()) {
        String field = reader.fieldName();
        switch (field) {
          case "s":
          case "service":
            service = reader.readString();
            break;
          case "m":
          case "method":
            method = reader.readString();
            break;
          case "k":
          case "key":
            key = reader.readString();
            break;
          default:
            reader.skipValue();
        }
      }
      if (service != null && method != null && key != null) {
        return new TombStone(service, method, key);
      }
    } else {
      reader.skipValue();
    }
    return null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TombStone tombStone = (TombStone) o;
    return Objects.equals(service, tombStone.service) && Objects.equals(method, tombStone.method) && Objects.equals(key, tombStone.key);
  }

  @Override
  public int hashCode() {
    return Objects.hash(service, method, key);
  }
}
