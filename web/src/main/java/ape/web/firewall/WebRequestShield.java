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
 * Applied as first-pass filter in WebHandler before any business logic executes.
 *
 * WebRequestShield's job is minimizing logging noise as the entire web server is in-memory
 * and doesn't reflect any existing security surface. These blocked paths are technologies
 * (WordPress, PHP, database admin tools, Spring actuators, etc.) that are simply not part
 * of the Adama platform, so any requests to them are scanner noise.
 */
public class WebRequestShield {
  public static boolean block(String uri) {
    // Allow .well-known (ACME, security.txt, etc.) but block all other hidden paths
    if (uri.startsWith("/.well-known")) return false;
    if (uri.startsWith("/.")) return true;

    // WordPress
    if (uri.startsWith("/wp-admin")) return true;
    if (uri.startsWith("/wp-content")) return true;
    if (uri.startsWith("/wp-includes")) return true;
    if (uri.startsWith("/wp-json")) return true;
    if (uri.startsWith("/wp-login")) return true;
    if (uri.startsWith("/wp-cron")) return true;
    if (uri.startsWith("/xmlrpc.php")) return true;

    // PHP / CGI / ASP probes
    if (uri.startsWith("/cgi-bin/")) return true;
    if (uri.startsWith("/cgi/")) return true;
    if (uri.endsWith(".php")) return true;
    if (uri.endsWith(".asp")) return true;
    if (uri.endsWith(".aspx")) return true;
    if (uri.endsWith(".jsp")) return true;
    if (uri.endsWith(".cgi")) return true;

    // Database admin tools
    if (uri.startsWith("/phpmyadmin")) return true;
    if (uri.startsWith("/pma")) return true;
    if (uri.startsWith("/adminer")) return true;
    if (uri.startsWith("/myadmin")) return true;
    if (uri.startsWith("/mysql")) return true;
    if (uri.startsWith("/dbadmin")) return true;
    if (uri.startsWith("/sql")) return true;

    // Java / Spring / DevOps actuators
    if (uri.startsWith("/actuator")) return true;
    if (uri.startsWith("/jolokia")) return true;
    if (uri.startsWith("/jmx")) return true;
    if (uri.startsWith("/heapdump")) return true;
    if (uri.startsWith("/threaddump")) return true;
    if (uri.startsWith("/trace")) return true;

    // Infrastructure and management consoles
    if (uri.startsWith("/Portal/")) return true;
    if (uri.startsWith("/portal/")) return true;
    if (uri.startsWith("/server-status")) return true;
    if (uri.startsWith("/server-info")) return true;
    if (uri.startsWith("/status.")) return true;

    // Exchange / OWA / ECP
    if (uri.startsWith("/owa/")) return true;
    if (uri.startsWith("/ecp/")) return true;
    if (uri.startsWith("/autodiscover")) return true;

    // VPN / remote access
    if (uri.startsWith("/vpn/")) return true;
    if (uri.startsWith("/sslvpn")) return true;
    if (uri.startsWith("/dana-na")) return true;

    // API docs / Swagger
    if (uri.startsWith("/swagger")) return true;
    if (uri.startsWith("/v2/api-docs")) return true;
    if (uri.startsWith("/v3/api-docs")) return true;

    // Search engines / data tools
    if (uri.startsWith("/solr/")) return true;
    if (uri.startsWith("/elasticsearch")) return true;
    if (uri.startsWith("/_search")) return true;
    if (uri.startsWith("/_cat")) return true;
    if (uri.startsWith("/_cluster")) return true;
    if (uri.startsWith("/_nodes")) return true;

    // Laravel / Node / Ruby frameworks
    if (uri.startsWith("/telescope/")) return true;
    if (uri.startsWith("/vendor/")) return true;
    if (uri.startsWith("/storage/")) return true;
    if (uri.startsWith("/node_modules/")) return true;
    if (uri.startsWith("/rails/")) return true;

    // Common scanner noise paths
    if (uri.startsWith("/CSS/")) return true;
    if (uri.startsWith("/idx_config/")) return true;

    // Common vulnerable files probed by scanners
    if (uri.startsWith("/web.config")) return true;
    return false;
  }
}
