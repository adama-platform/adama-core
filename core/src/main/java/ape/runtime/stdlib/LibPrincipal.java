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
package ape.runtime.stdlib;

import ape.runtime.natives.NtPrincipal;
import ape.translator.reflect.Extension;

/** Helpful functions around NtPrincipal */
public class LibPrincipal {
  @Extension
  public static boolean isAdamaDeveloper(final NtPrincipal principal) {
    return "adama".equals(principal.authority);
  }

  @Extension
  public static boolean isAnonymous(final NtPrincipal principal) {
    return "anonymous".equals(principal.authority);
  }

  @Extension
  public static boolean isOverlord(final NtPrincipal principal) {
    return "overlord".equals(principal.authority);
  }

  @Extension
  public static boolean isAdamaHost(final NtPrincipal principal) {
    return "region".equals(principal.authority);
  }

  @Extension
  public static boolean fromAuthority(final NtPrincipal principal, String authority) {
    return authority.equals(principal.authority);
  }

  @Extension
  public static String agent(final NtPrincipal principal) {
    return principal.agent;
  }

  @Extension
  public static String authority(final NtPrincipal principal) {
    return principal.authority;
  }
}
