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
package ape.web.firewall;

/**
 * Static request filter blocking common attack vectors and scanner probes.
 * Rejects requests targeting hidden files (except .well-known), WordPress admin,
 * CGI-bin, actuator endpoints, and other commonly-targeted paths. Applied as
 * first-pass filter in WebHandler before any business logic executes.
 */
public class WebRequestShield {
  public static boolean block(String x) {
    if (x.startsWith("/.well-known")) return false;
    if (x.startsWith("/.")) return true;
    if (x.startsWith("/CSS/")) return true;
    if (x.startsWith("/Portal/")) return true;
    if (x.startsWith("/actuator/")) return true;
    if (x.startsWith("/api/")) return true;
    if (x.startsWith("/cgi-bin/")) return true;
    if (x.startsWith("/docs/")) return true;
    if (x.startsWith("/ecp/")) return true;
    if (x.startsWith("/owa/")) return true;
    if (x.startsWith("/scripts/")) return true;
    if (x.startsWith("/vendor/")) return true;
    if (x.startsWith("/remote/")) return true;
    if (x.startsWith("/portal/")) return true;
    if (x.startsWith("/d/")) return true;
    if (x.startsWith("/s/")) return true;
    if (x.startsWith("/telescope/")) return true;
    if (x.startsWith("/idx_config/")) return true;
    if (x.startsWith("/console/")) return true;
    if (x.startsWith("/mgmt/")) return true;
    if (x.startsWith("/wp-admin/")) return true;
    return false;
  }
}
