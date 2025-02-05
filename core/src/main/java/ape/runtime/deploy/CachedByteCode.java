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
package ape.runtime.deploy;

import ape.ErrorCodes;
import ape.common.ErrorCodeException;
import ape.common.ExceptionSupplier;
import ape.common.cache.Measurable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/** a cached artifact of compiled java byte code */
public class CachedByteCode implements Measurable {
  public final String spaceName;
  public final String className;
  public final String reflection;
  public final Map<String, byte[]> classBytes;
  private final long measure;

  public CachedByteCode(String spaceName, String className, String reflection, Map<String, byte[]> classBytes) {
    this.spaceName = spaceName;
    this.className = className;
    this.reflection = reflection;
    this.classBytes = classBytes;
    long _measure = 0;
    _measure += spaceName.length() + 32;
    _measure += className.length() + 32;
    _measure += reflection.length() + 32;
    for (Map.Entry<String, byte[]> entry : classBytes.entrySet()) {
      _measure += entry.getKey().length() + 32;
      _measure += entry.getValue().length;
    }
    this.measure = _measure;
  }

  /** pack up the byte code */
  public byte[] pack() throws ErrorCodeException {
    try {
      ByteArrayOutputStream memory = new ByteArrayOutputStream();
      DataOutputStream output = new DataOutputStream(memory);
      output.writeInt(0x42);
      {
        byte[] w = spaceName.getBytes(StandardCharsets.UTF_8);
        output.writeInt(w.length);
        output.write(w);
      }
      {
        byte[] w = className.getBytes(StandardCharsets.UTF_8);
        output.writeInt(w.length);
        output.write(w);
      }
      {
        byte[] w = reflection.getBytes(StandardCharsets.UTF_8);
        output.writeInt(w.length);
        output.write(w);
      }
      output.writeInt(classBytes.size());
      for (Map.Entry<String, byte[]> clazz : classBytes.entrySet()) {
        byte[] w = clazz.getKey().getBytes(StandardCharsets.UTF_8);
        output.writeInt(w.length);
        output.write(w);
        output.writeInt(clazz.getValue().length);
        output.write(clazz.getValue());
      }
      output.flush();
      return memory.toByteArray();
    } catch (Exception ex) {
      throw new ErrorCodeException(ErrorCodes.CACHED_BYTE_CODE_FAILED_PACK, ex);
    }
  }

  /** unpack byte code */
  public static CachedByteCode unpack(byte[] packed) throws ErrorCodeException {
    try {
      DataInputStream input = new DataInputStream(new ByteArrayInputStream(packed));
      if (input.readInt() != 0x42) {
        return null;
      }
      ExceptionSupplier<String> str = () -> {
        int w = input.readInt();
        byte[] b = new byte[w];
        input.readFully(b);
        return new String(b, StandardCharsets.UTF_8);
      };
      ExceptionSupplier<byte[]> bytes = () -> {
        int w = input.readInt();
        byte[] b = new byte[w];
        input.readFully(b);
        return b;
      };
      String spaceName = str.get();
      String className = str.get();
      String reflection = str.get();
      HashMap<String, byte[]> classBytes = new HashMap<>();
      int n = input.readInt();
      for (int k = 0; k < n; k ++) {
        String key = str.get();
        byte[] value = bytes.get();
        classBytes.put(key, value);
      }
      return new CachedByteCode(spaceName, className, reflection, classBytes);
    } catch (Exception ex) {
      throw new ErrorCodeException(ErrorCodes.CACHED_BYTE_CODE_FAILED_UNPACK, ex);
    }
  }

  @Override
  public long measure() {
    return measure;
  }
}
