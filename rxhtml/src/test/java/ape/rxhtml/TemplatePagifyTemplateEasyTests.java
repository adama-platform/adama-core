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

public class TemplatePagifyTemplateEasyTests extends BaseRxHtmlTest {
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
    gold.append("\n  // <template name=\"foo\">");
    gold.append("\n  $.TP('foo', function(a,b,c,d) {");
    gold.append("\n    var e=$.X();");
    gold.append("\n    a.append($.T(' THIS IS A TEMPLATE '));");
    gold.append("\n    c(a,b,'');");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/a\" template:use=\"foo\">");
    gold.append("\n  $.PG(['fixed','a'], function(b,a) {");
    gold.append("\n    var c=$.X();");
    gold.append("\n");
    gold.append("\n    // <div rx:template=\"foo\">");
    gold.append("\n    var f=$.E('div');");
    gold.append("\n    $.UT(f,a,'foo', function(g,h,i) {");
    gold.append("\n      g.append($.T(' BODY '));");
    gold.append("\n    },{});");
    gold.append("\n    b.append(f);");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/b\" template:use=\"foo\" template:tag=\"yikes\">");
    gold.append("\n  $.PG(['fixed','b'], function(b,a) {");
    gold.append("\n    var f=$.X();");
    gold.append("\n");
    gold.append("\n    // <yikes rx:template=\"foo\">");
    gold.append("\n    var g=$.E('yikes');");
    gold.append("\n    $.UT(g,a,'foo', function(h,i,j) {");
    gold.append("\n      h.append($.T(' BODY '));");
    gold.append("\n    },{});");
    gold.append("\n    b.append(g);");
    gold.append("\n  });");
    gold.append("\n})(RxHTML);");
    gold.append("\nStyle:");
    gold.append("\nShell:<!DOCTYPE html>");
    gold.append("\n<html>");
    gold.append("\n<head><script src=\"/libadama.js/GENMODE.js\"></script><script>");
    gold.append("\n");
    gold.append("\n(function($){");
    gold.append("\n");
    gold.append("\n  // <template name=\"foo\">");
    gold.append("\n  $.TP('foo', function(a,b,c,d) {");
    gold.append("\n    var e=$.X();");
    gold.append("\n    a.append($.T(' THIS IS A TEMPLATE '));");
    gold.append("\n    c(a,b,'');");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/a\" template:use=\"foo\">");
    gold.append("\n  $.PG(['fixed','a'], function(b,a) {");
    gold.append("\n    var c=$.X();");
    gold.append("\n");
    gold.append("\n    // <div rx:template=\"foo\">");
    gold.append("\n    var f=$.E('div');");
    gold.append("\n    $.UT(f,a,'foo', function(g,h,i) {");
    gold.append("\n      g.append($.T(' BODY '));");
    gold.append("\n    },{});");
    gold.append("\n    b.append(f);");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/b\" template:use=\"foo\" template:tag=\"yikes\">");
    gold.append("\n  $.PG(['fixed','b'], function(b,a) {");
    gold.append("\n    var f=$.X();");
    gold.append("\n");
    gold.append("\n    // <yikes rx:template=\"foo\">");
    gold.append("\n    var g=$.E('yikes');");
    gold.append("\n    $.UT(g,a,'foo', function(h,i,j) {");
    gold.append("\n      h.append($.T(' BODY '));");
    gold.append("\n    },{});");
    gold.append("\n    b.append(g);");
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
    source.append("\n    <template name=\"foo\">");
    source.append("\n        THIS IS A TEMPLATE <fragment />");
    source.append("\n    </template>");
    source.append("\n    <page uri=\"/a\" template:use=\"foo\">");
    source.append("\n        BODY");
    source.append("\n    </page>");
    source.append("\n    <page uri=\"/b\" template:use=\"foo\" template:tag=\"yikes\">");
    source.append("\n        BODY");
    source.append("\n    </page>");
    source.append("\n</forest>");
    return source.toString();
  }
  @Override
  public String schema() {
    StringBuilder gold = new StringBuilder();
    gold.append("{");
    gold.append("\n  \"/a\" : { },");
    gold.append("\n  \"/b\" : { }");
    gold.append("\n}");
    return gold.toString();
  }
}
