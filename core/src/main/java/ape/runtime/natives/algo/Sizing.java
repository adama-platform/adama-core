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
package ape.runtime.natives.algo;

import ape.runtime.natives.NtAsset;
import ape.runtime.natives.NtDynamic;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.natives.NtTemplate;

/** size of a native simple type with value -1 */
public class Sizing {
  public static long memoryOf(String x) {
    return 80 + x.length() * 2;
  }
  public static long memoryOf(NtAsset x) {
    return 120 + (x.id.length() + x.md5.length() + x.sha384.length()) * 2;
  }
  public static long memoryOf(NtDynamic x) {
    return 80 + (x.json.length() * 4);
  }
  public static long memoryOf(NtPrincipal x) {
    return 120 + (x.agent.length() + x.authority.length()) * 2;
  }
  public static long memoryOf(NtTemplate x) {
    return 64 + x.template.memory();
  }
}
