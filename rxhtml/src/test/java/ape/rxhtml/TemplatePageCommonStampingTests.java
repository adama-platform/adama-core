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

public class TemplatePageCommonStampingTests extends BaseRxHtmlTest {
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
    gold.append("\n  // <template name=\"t\">");
    gold.append("\n  $.TP('t', function(a,b,c,d) {");
    gold.append("\n    var e=$.X();");
    gold.append("\n    a.append($.T(' XYZ '));");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/foo\" init:show=\"keep-me\" template:tag=\"dog\" init:foo=\"foo\" template:use=\"t\">");
    gold.append("\n  $.PG(['fixed','foo'], function(b,a) {");
    gold.append("\n    a.view.init['show']='keep-me';");
    gold.append("\n    a.view.init['foo']='foo';");
    gold.append("\n    var c=$.X();");
    gold.append("\n");
    gold.append("\n    // <dog rx:template=\"t\">");
    gold.append("\n    var f=$.E('dog');");
    gold.append("\n    $.UT(f,a,'t', function(g,h,i) {");
    gold.append("\n      g.append($.T(' Yo '));");
    gold.append("\n    },{});");
    gold.append("\n    b.append(f);");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/foody\" template:tag=\"dog\" init:foo=\"foo\" init:show=\"showing\" template:use=\"t\">");
    gold.append("\n  $.PG(['fixed','foody'], function(b,a) {");
    gold.append("\n    a.view.init['foo']='foo';");
    gold.append("\n    a.view.init['show']='showing';");
    gold.append("\n    var f=$.X();");
    gold.append("\n");
    gold.append("\n    // <dog rx:template=\"t\">");
    gold.append("\n    var g=$.E('dog');");
    gold.append("\n    $.UT(g,a,'t', function(h,i,j) {");
    gold.append("\n      h.append($.T(' Food? '));");
    gold.append("\n    },{});");
    gold.append("\n    b.append(g);");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/g\">");
    gold.append("\n  $.PG(['fixed','g'], function(b,a) {");
    gold.append("\n    var g=$.X();");
    gold.append("\n    b.append($.T(' Yo '));");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/auth/me/yo\" authenticate=\"some_id\" bundle=\"giggy\">");
    gold.append("\n  $.PG(['fixed','auth','fixed','me','fixed','yo'], function(b,a) {");
    gold.append("\n    var h=$.aRDz('/');");
    gold.append("\n    if($.ID('some_id',h).abort) {");
    gold.append("\n      return;");
    gold.append("\n    }");
    gold.append("\n    var h=$.X();");
    gold.append("\n    b.append($.T(' Yo '));");
    gold.append("\n  });");
    gold.append("\n})(RxHTML);");
    gold.append("\nStyle:");
    gold.append("\nShell:<!DOCTYPE html>");
    gold.append("\n<html>");
    gold.append("\n<head><script src=\"/libadama.js/GENMODE.js\"></script><script>");
    gold.append("\n");
    gold.append("\n(function($){");
    gold.append("\n");
    gold.append("\n  // <template name=\"t\">");
    gold.append("\n  $.TP('t', function(a,b,c,d) {");
    gold.append("\n    var e=$.X();");
    gold.append("\n    a.append($.T(' XYZ '));");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/foo\" init:show=\"keep-me\" template:tag=\"dog\" init:foo=\"foo\" template:use=\"t\">");
    gold.append("\n  $.PG(['fixed','foo'], function(b,a) {");
    gold.append("\n    a.view.init['show']='keep-me';");
    gold.append("\n    a.view.init['foo']='foo';");
    gold.append("\n    var c=$.X();");
    gold.append("\n");
    gold.append("\n    // <dog rx:template=\"t\">");
    gold.append("\n    var f=$.E('dog');");
    gold.append("\n    $.UT(f,a,'t', function(g,h,i) {");
    gold.append("\n      g.append($.T(' Yo '));");
    gold.append("\n    },{});");
    gold.append("\n    b.append(f);");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/foody\" template:tag=\"dog\" init:foo=\"foo\" init:show=\"showing\" template:use=\"t\">");
    gold.append("\n  $.PG(['fixed','foody'], function(b,a) {");
    gold.append("\n    a.view.init['foo']='foo';");
    gold.append("\n    a.view.init['show']='showing';");
    gold.append("\n    var f=$.X();");
    gold.append("\n");
    gold.append("\n    // <dog rx:template=\"t\">");
    gold.append("\n    var g=$.E('dog');");
    gold.append("\n    $.UT(g,a,'t', function(h,i,j) {");
    gold.append("\n      h.append($.T(' Food? '));");
    gold.append("\n    },{});");
    gold.append("\n    b.append(g);");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/g\">");
    gold.append("\n  $.PG(['fixed','g'], function(b,a) {");
    gold.append("\n    var g=$.X();");
    gold.append("\n    b.append($.T(' Yo '));");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/auth/me/yo\" authenticate=\"some_id\" bundle=\"giggy\">");
    gold.append("\n  $.PG(['fixed','auth','fixed','me','fixed','yo'], function(b,a) {");
    gold.append("\n    var h=$.aRDz('/');");
    gold.append("\n    if($.ID('some_id',h).abort) {");
    gold.append("\n      return;");
    gold.append("\n    }");
    gold.append("\n    var h=$.X();");
    gold.append("\n    b.append($.T(' Yo '));");
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
    source.append("\n    <template name=\"t\">");
    source.append("\n        XYZ");
    source.append("\n    </template>");
    source.append("\n    <common-page uri:prefix=\"/f\" init:foo=\"foo\" init:show=\"showing\" template:use=\"t\" template:tag=\"dog\" />");
    source.append("\n    <page uri=\"/foo\" init:show=\"keep-me\">");
    source.append("\n        Yo");
    source.append("\n    </page>");
    source.append("\n    <page uri=\"/foody\">");
    source.append("\n        Food?");
    source.append("\n    </page>");
    source.append("\n    <page uri=\"/g\">");
    source.append("\n        Yo");
    source.append("\n    </page>");
    source.append("\n    <common-page uri:prefix=\"/auth\" authenticate=\"some_id\" bundle=\"giggy\" />");
    source.append("\n    <page uri=\"/auth/me/yo\">");
    source.append("\n        Yo");
    source.append("\n    </page>");
    source.append("\n</forest>");
    return source.toString();
  }
  @Override
  public String schema() {
    StringBuilder gold = new StringBuilder();
    gold.append("{");
    gold.append("\n  \"/foo\" : { },");
    gold.append("\n  \"/foody\" : { },");
    gold.append("\n  \"/g\" : { },");
    gold.append("\n  \"/auth/me/yo\" : { }");
    gold.append("\n}");
    return gold.toString();
  }
}
