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
package ape.web.service;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import ape.web.io.ConnectionContext;

import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Factory for extracting ConnectionContext from HTTP request headers.
 * Parses origin (or derives from host), client IP (with X-Forwarded-For support),
 * user-agent, and identity cookies (id_* prefix with HttpOnly+Secure flags).
 */
public class ConnectionContextFactory {
  public static ConnectionContext of(final ChannelHandlerContext ctx, HttpHeaders headers) {
    return of(ctx, headers, false);
  }

  public static ConnectionContext of(final ChannelHandlerContext ctx, HttpHeaders headers, boolean useXForwardedFor) {
    String origin = headers.get("origin");
    if (origin == null) {
      origin = "https://" + headers.get("host");
    }
    String ip = ctx.channel().remoteAddress().toString().replaceAll(Pattern.quote("/"), "");
    if (useXForwardedFor) {
      String xForwardedFor = headers.get("x-forwarded-for");
      if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
        // Use the rightmost IP: this is the one added by the trusted load balancer/proxy.
        // Left-side entries can be spoofed by clients, but the rightmost is from our infrastructure.
        String[] parts = xForwardedFor.split(",");
        ip = parts[parts.length - 1].trim();
      }
    }
    String cookieHeader = headers.get(HttpHeaderNames.COOKIE);
    TreeMap<String, String> identites = new TreeMap<>();
    if (cookieHeader != null && cookieHeader.length() <= 8192) {
      for (Cookie cookie : ServerCookieDecoder.STRICT.decode(cookieHeader)) {
        if (cookie.name().startsWith("id_") && cookie.value().length() <= 1024) {
          identites.put(cookie.name().substring(3), cookie.value());
        }
      }
    }
    String userAgent = headers.get(HttpHeaderNames.USER_AGENT);
    return new ConnectionContext(origin, ip, userAgent, identites);
  }
}
