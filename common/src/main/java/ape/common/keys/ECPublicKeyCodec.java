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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPoint;

/** codec to encode ECPublicKey and ECPoint */
public class ECPublicKeyCodec {

  private static void write(BigInteger i, OutputStream o) throws Exception {
    byte[] bytes = i.toByteArray();
    if (bytes.length == 31) {
      o.write((byte) 0x00);
      o.write(bytes);
    } else if (bytes.length == 32) {
      o.write(bytes);
    } else if (bytes.length == 33 && bytes[0] == 0) {
      o.write(bytes, 1, bytes.length - 1);
    } else {
      throw new Exception("failed to generate:" + bytes.length);
    }
  }

  public static byte[] encode(ECPoint ecp) throws Exception {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
    outputStream.write((byte) 0x04);
    write(ecp.getAffineX(), outputStream);
    write(ecp.getAffineY(), outputStream);
    return outputStream.toByteArray();
  }

  public static byte[] encode(ECPublicKey key) throws Exception {
    return encode(key.getW());
  }
}
