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
package ape.runtime.sys.domains;

import ape.common.cache.Measurable;

import java.sql.Date;

/** a domain mapped to a space */
public class Domain implements Measurable {
  public final String domain;
  public final int owner;
  public final String space;
  public final String key;
  public final String forwardTo;
  public final boolean routeKey;
  public final String certificate;
  public final Date updated;
  public final long timestamp;
  public boolean configured;
  private final long _measure;

  public Domain(String domain, int owner, String space, String key, String forwardTo, boolean routeKey, String certificate, Date updated, long timestamp, boolean configured) {
    this.domain = domain;
    this.owner = owner;
    this.space = space;
    this.routeKey = routeKey;
    this.key = key;
    this.forwardTo = forwardTo;
    this.certificate = certificate;
    this.updated = updated;
    this.timestamp = timestamp;
    this.configured = configured;
    long m = 0;
    if (domain != null) {
      m += domain.length();
    }
    if (space != null) {
      m += space.length();
    }
    if (key != null) {
      m += key.length();
    }
    if (certificate != null) {
      m += certificate.length();
    }
    _measure = m;
  }

  @Override
  public long measure() {
    return _measure;
  }
}
