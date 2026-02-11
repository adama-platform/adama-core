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

/**
 * Standard library for principal (identity) operations in Adama documents.
 * Provides authority checks, agent/authority extraction, and identity type tests
 * for NtPrincipal values. Used to implement access control logic in Adama documents.
 */
public class LibPrincipal {
  /** Return true if the principal is an Adama platform developer (authority == "adama"). */
  @Extension
  public static boolean isAdamaDeveloper(final NtPrincipal principal) {
    return "adama".equals(principal.authority);
  }

  /** Return true if the principal is anonymous (authority == "anonymous"). */
  @Extension
  public static boolean isAnonymous(final NtPrincipal principal) {
    return "anonymous".equals(principal.authority);
  }

  /** Return true if the principal is the cluster overlord (authority == "overlord"). */
  @Extension
  public static boolean isOverlord(final NtPrincipal principal) {
    return "overlord".equals(principal.authority);
  }

  /** Return true if the principal is an Adama host node (authority == "region"). */
  @Extension
  public static boolean isAdamaHost(final NtPrincipal principal) {
    return "region".equals(principal.authority);
  }

  /** Return true if the principal's authority matches the given string. */
  @Extension
  public static boolean fromAuthority(final NtPrincipal principal, String authority) {
    return authority.equals(principal.authority);
  }

  /** Return the agent (user identifier) portion of the principal. */
  @Extension
  public static String agent(final NtPrincipal principal) {
    return principal.agent;
  }

  /** Return the authority (identity provider) portion of the principal. */
  @Extension
  public static String authority(final NtPrincipal principal) {
    return principal.authority;
  }
}
