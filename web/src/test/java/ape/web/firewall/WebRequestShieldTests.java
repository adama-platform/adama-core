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

import org.junit.Assert;
import org.junit.Test;

public class WebRequestShieldTests {
  @Test
  public void coverage() {
    // Allowed paths
    Assert.assertFalse(WebRequestShield.block("/.well-known"));
    Assert.assertFalse(WebRequestShield.block("/.well-known/acme-challenge/xyz"));
    Assert.assertFalse(WebRequestShield.block("/my/name/is/ninja/"));
    Assert.assertFalse(WebRequestShield.block("/~s"));
    Assert.assertFalse(WebRequestShield.block("/~upload"));
    Assert.assertFalse(WebRequestShield.block("/~health_check_lb"));
    Assert.assertFalse(WebRequestShield.block("/libadama.js"));

    // Hidden files
    Assert.assertTrue(WebRequestShield.block("/.git/"));
    Assert.assertTrue(WebRequestShield.block("/.env"));
    Assert.assertTrue(WebRequestShield.block("/.aws/"));
    Assert.assertTrue(WebRequestShield.block("/.htaccess"));

    // WordPress
    Assert.assertTrue(WebRequestShield.block("/wp-admin"));
    Assert.assertTrue(WebRequestShield.block("/wp-admin/admin-ajax.php"));
    Assert.assertTrue(WebRequestShield.block("/wp-content/uploads/shell.php"));
    Assert.assertTrue(WebRequestShield.block("/wp-includes/js/jquery.js"));
    Assert.assertTrue(WebRequestShield.block("/wp-json/wp/v2/users"));
    Assert.assertTrue(WebRequestShield.block("/wp-login.php"));
    Assert.assertTrue(WebRequestShield.block("/wp-cron.php"));
    Assert.assertTrue(WebRequestShield.block("/xmlrpc.php"));

    // PHP / CGI / ASP probes
    Assert.assertTrue(WebRequestShield.block("/cgi-bin/test"));
    Assert.assertTrue(WebRequestShield.block("/cgi/test"));
    Assert.assertTrue(WebRequestShield.block("/index.php"));
    Assert.assertTrue(WebRequestShield.block("/admin/login.asp"));
    Assert.assertTrue(WebRequestShield.block("/default.aspx"));
    Assert.assertTrue(WebRequestShield.block("/page.jsp"));
    Assert.assertTrue(WebRequestShield.block("/script.cgi"));

    // Database admin tools
    Assert.assertTrue(WebRequestShield.block("/phpmyadmin"));
    Assert.assertTrue(WebRequestShield.block("/pma/index.php"));
    Assert.assertTrue(WebRequestShield.block("/adminer"));
    Assert.assertTrue(WebRequestShield.block("/myadmin"));
    Assert.assertTrue(WebRequestShield.block("/mysql"));
    Assert.assertTrue(WebRequestShield.block("/dbadmin"));
    Assert.assertTrue(WebRequestShield.block("/sql"));

    // Java / Spring actuators
    Assert.assertTrue(WebRequestShield.block("/actuator/"));
    Assert.assertTrue(WebRequestShield.block("/actuator/health"));
    Assert.assertTrue(WebRequestShield.block("/jolokia"));
    Assert.assertTrue(WebRequestShield.block("/jmx"));
    Assert.assertTrue(WebRequestShield.block("/heapdump"));
    Assert.assertTrue(WebRequestShield.block("/threaddump"));
    Assert.assertTrue(WebRequestShield.block("/trace"));

    // Infrastructure consoles
    Assert.assertTrue(WebRequestShield.block("/Portal/"));
    Assert.assertTrue(WebRequestShield.block("/portal/"));
    Assert.assertTrue(WebRequestShield.block("/server-status"));
    Assert.assertTrue(WebRequestShield.block("/server-info"));

    // Exchange / OWA
    Assert.assertTrue(WebRequestShield.block("/owa/"));
    Assert.assertTrue(WebRequestShield.block("/ecp/"));
    Assert.assertTrue(WebRequestShield.block("/autodiscover"));

    // VPN / remote access
    Assert.assertTrue(WebRequestShield.block("/vpn/"));
    Assert.assertTrue(WebRequestShield.block("/sslvpn"));
    Assert.assertTrue(WebRequestShield.block("/dana-na"));

    // API docs
    Assert.assertTrue(WebRequestShield.block("/swagger"));

    // Search engines
    Assert.assertTrue(WebRequestShield.block("/solr/"));
    Assert.assertTrue(WebRequestShield.block("/elasticsearch"));
    Assert.assertTrue(WebRequestShield.block("/_search"));
    Assert.assertTrue(WebRequestShield.block("/_cat"));

    // Framework paths
    Assert.assertTrue(WebRequestShield.block("/telescope/"));
    Assert.assertTrue(WebRequestShield.block("/vendor/"));
    Assert.assertTrue(WebRequestShield.block("/storage/"));
    Assert.assertTrue(WebRequestShield.block("/node_modules/"));
    Assert.assertTrue(WebRequestShield.block("/rails/"));

    // Scanner noise
    Assert.assertTrue(WebRequestShield.block("/CSS/"));
    Assert.assertTrue(WebRequestShield.block("/idx_config/"));

    // Common probe files
    Assert.assertTrue(WebRequestShield.block("/web.config"));
  }
}
