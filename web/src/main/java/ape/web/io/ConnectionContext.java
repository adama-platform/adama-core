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
package ape.web.io;

import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * HTTP/WebSocket connection metadata extracted from headers.
 * Captures origin URL, client IP (stripped of port), user-agent string,
 * and cookie-based identities. Used for authentication context, logging,
 * and policy evaluation throughout request processing.
 */
public class ConnectionContext {
  public final String origin;
  public final String remoteIp;
  public final String userAgent;
  public final TreeMap<String, String> identities;

  public ConnectionContext(String origin, String remoteIp, String userAgent, TreeMap<String, String> identities) {
    this.origin = origin != null ? origin : "";
    this.remoteIp = remoteIpFix(remoteIp);
    this.userAgent = userAgent != null ? userAgent : "";
    this.identities = identities;
  }

  public String identityOf(String identityRaw) {
    if (identityRaw.startsWith("cookie:")) {
      if (identities != null) {
        return identities.get(identityRaw.substring(7));
      }
    }
    return identityRaw;
  }

  /** we don't care about the port and null values */
  public static String remoteIpFix(String remoteIp) {
    if (remoteIp == null) {
      return "";
    }
    return remoteIp.split(Pattern.quote(":"))[0];
  }
}
