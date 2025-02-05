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

/** represents who someone is */
public class NtPrincipal implements Comparable<NtPrincipal> {
  public static NtPrincipal NO_ONE = new NtPrincipal("?", "?");
  public final String agent;
  public final String authority;
  private final int cachedHash;

  public NtPrincipal(final String agent, final String authority) {
    this.agent = agent == null ? "?" : agent;
    this.authority = authority == null ? "?" : authority;
    this.cachedHash = agent.hashCode() * 31 + authority.hashCode();
  }

  @Override
  public int hashCode() {
    return cachedHash;
  }

  @Override
  public boolean equals(final Object o) {
    if (o instanceof NtPrincipal) {
      NtPrincipal other = (NtPrincipal) o;
      return other == this || (other.cachedHash == cachedHash && compareTo((NtPrincipal) o) == 0);
    }
    return false;
  }

  @Override
  public int compareTo(final NtPrincipal other) {
    var result = authority.compareTo(other.authority);
    if (result == 0) {
      result = agent.compareTo(other.agent);
    }
    return result;
  }

  @Override
  public String toString() {
    return "NtPrincipal<" + agent + "@" + authority + ">";
  }

  public long memory() {
    return (agent.length() + authority.length()) * 2L;
  }
}
