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
package ape.rxhtml;

public class TemplateMultipageWithMultiAuthTests extends BaseRxHtmlTest {
  @Override
  public boolean dev() {
    return false;
  }
  @Override
  public String issues() {
    StringBuilder issues = new StringBuilder();
    issues.append("");
    return issues.toString();
  }
  @Override
  public String gold() {
    StringBuilder gold = new StringBuilder();
    gold.append("JavaScript:(function($){");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/customeraction/$customer_id:text/signin\" default-redirect-source=\"\">");
    gold.append("\n  $.PG(['fixed','customeraction','text','customer_id','fixed','signin'], function(b,a) {");
    gold.append("\n    var c=$.X();");
    gold.append("\n    b.append($.T(' Sign in for customers '));");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/customer/$customer_id:text\" authenticate=\"\">");
    gold.append("\n  $.PG(['fixed','customer','text','customer_id'], function(b,a) {");
    gold.append("\n    var d=$.aRDp(a,function(vs) { return \"/customeraction/\" + $.F(vs,'customer_id') + \"/signin\";});");
    gold.append("\n    if($.ID('default',d).abort) {");
    gold.append("\n      return;");
    gold.append("\n    }");
    gold.append("\n    var d=$.X();");
    gold.append("\n    b.append($.T(' Simple Page for customers '));");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/adminaction/$admin_id:text/signin\" default-redirect-source=\"\">");
    gold.append("\n  $.PG(['fixed','adminaction','text','admin_id','fixed','signin'], function(b,a) {");
    gold.append("\n    var e=$.X();");
    gold.append("\n    b.append($.T(' Sign in for admins '));");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/admin/$admin_id:text\" authenticate=\"\">");
    gold.append("\n  $.PG(['fixed','admin','text','admin_id'], function(b,a) {");
    gold.append("\n    var f=$.aRDp(a,function(vs) { return \"/adminaction/\" + $.F(vs,'admin_id') + \"/signin\";});");
    gold.append("\n    if($.ID('default',f).abort) {");
    gold.append("\n      return;");
    gold.append("\n    }");
    gold.append("\n    var f=$.X();");
    gold.append("\n    b.append($.T(' Simple Page for admins '));");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/bounce\" authenticate=\"\">");
    gold.append("\n  $.PG(['fixed','bounce'], function(b,a) {");
    gold.append("\n    var g=$.aRDz('/logingeneric');");
    gold.append("\n    if($.ID('default',g).abort) {");
    gold.append("\n      return;");
    gold.append("\n    }");
    gold.append("\n    var g=$.X();");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/logingeneric\" default-redirect-source=\"\">");
    gold.append("\n  $.PG(['fixed','logingeneric'], function(b,a) {");
    gold.append("\n    var h=$.X();");
    gold.append("\n    b.append($.T(' Simple Page '));");
    gold.append("\n  });");
    gold.append("\n})(RxHTML);");
    gold.append("\nStyle:");
    gold.append("\nShell:<!DOCTYPE html>");
    gold.append("\n<html>");
    gold.append("\n<head><script src=\"/libadama.js/GENMODE.js\"></script><script>");
    gold.append("\n");
    gold.append("\n(function($){");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/customeraction/$customer_id:text/signin\" default-redirect-source=\"\">");
    gold.append("\n  $.PG(['fixed','customeraction','text','customer_id','fixed','signin'], function(b,a) {");
    gold.append("\n    var c=$.X();");
    gold.append("\n    b.append($.T(' Sign in for customers '));");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/customer/$customer_id:text\" authenticate=\"\">");
    gold.append("\n  $.PG(['fixed','customer','text','customer_id'], function(b,a) {");
    gold.append("\n    var d=$.aRDp(a,function(vs) { return \"/customeraction/\" + $.F(vs,'customer_id') + \"/signin\";});");
    gold.append("\n    if($.ID('default',d).abort) {");
    gold.append("\n      return;");
    gold.append("\n    }");
    gold.append("\n    var d=$.X();");
    gold.append("\n    b.append($.T(' Simple Page for customers '));");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/adminaction/$admin_id:text/signin\" default-redirect-source=\"\">");
    gold.append("\n  $.PG(['fixed','adminaction','text','admin_id','fixed','signin'], function(b,a) {");
    gold.append("\n    var e=$.X();");
    gold.append("\n    b.append($.T(' Sign in for admins '));");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/admin/$admin_id:text\" authenticate=\"\">");
    gold.append("\n  $.PG(['fixed','admin','text','admin_id'], function(b,a) {");
    gold.append("\n    var f=$.aRDp(a,function(vs) { return \"/adminaction/\" + $.F(vs,'admin_id') + \"/signin\";});");
    gold.append("\n    if($.ID('default',f).abort) {");
    gold.append("\n      return;");
    gold.append("\n    }");
    gold.append("\n    var f=$.X();");
    gold.append("\n    b.append($.T(' Simple Page for admins '));");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/bounce\" authenticate=\"\">");
    gold.append("\n  $.PG(['fixed','bounce'], function(b,a) {");
    gold.append("\n    var g=$.aRDz('/logingeneric');");
    gold.append("\n    if($.ID('default',g).abort) {");
    gold.append("\n      return;");
    gold.append("\n    }");
    gold.append("\n    var g=$.X();");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/logingeneric\" default-redirect-source=\"\">");
    gold.append("\n  $.PG(['fixed','logingeneric'], function(b,a) {");
    gold.append("\n    var h=$.X();");
    gold.append("\n    b.append($.T(' Simple Page '));");
    gold.append("\n  });");
    gold.append("\n})(RxHTML);");
    gold.append("\n");
    gold.append("\n");
    gold.append("\n</script><style>");
    gold.append("\n");
    gold.append("\n");
    gold.append("\n");
    gold.append("\n</style></head><body></body><script>");
    gold.append("\n  RxHTML.init();");
    gold.append("\n</script></html>");
    return gold.toString();
  }
  @Override
  public String source() {
    StringBuilder source = new StringBuilder();
    source.append("<forest>");
    source.append("\n    <page uri=\"/customeraction/$customer_id:text/signin\" default-redirect-source>");
    source.append("\n        Sign in for customers");
    source.append("\n    </page>");
    source.append("\n    <page uri=\"/customer/$customer_id:text\" authenticate>");
    source.append("\n        Simple Page for customers");
    source.append("\n    </page>");
    source.append("\n");
    source.append("\n    <page uri=\"/adminaction/$admin_id:text/signin\" default-redirect-source>");
    source.append("\n        Sign in for admins");
    source.append("\n    </page>");
    source.append("\n    <page uri=\"/admin/$admin_id:text\" authenticate>");
    source.append("\n        Simple Page for admins");
    source.append("\n    </page>");
    source.append("\n    <page uri=\"/bounce\" authenticate>");
    source.append("\n");
    source.append("\n    </page>");
    source.append("\n    <page uri=\"/logingeneric\" default-redirect-source>");
    source.append("\n        Simple Page");
    source.append("\n    </page>");
    source.append("\n</forest>");
    return source.toString();
  }
  @Override
  public String schema() {
    StringBuilder gold = new StringBuilder();
    gold.append("{");
    gold.append("\n  \"/customeraction/$text/signin\" : {");
    gold.append("\n    \"customer_id\" : \"text\"");
    gold.append("\n  },");
    gold.append("\n  \"/customer/$text\" : {");
    gold.append("\n    \"customer_id\" : \"text\"");
    gold.append("\n  },");
    gold.append("\n  \"/adminaction/$text/signin\" : {");
    gold.append("\n    \"admin_id\" : \"text\"");
    gold.append("\n  },");
    gold.append("\n  \"/admin/$text\" : {");
    gold.append("\n    \"admin_id\" : \"text\"");
    gold.append("\n  },");
    gold.append("\n  \"/bounce\" : { },");
    gold.append("\n  \"/logingeneric\" : { }");
    gold.append("\n}");
    return gold.toString();
  }
}
