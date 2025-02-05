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

public class TemplateTemplateInlineTests extends BaseRxHtmlTest {
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
    gold.append("\n  // <template name=\"temp\">");
    gold.append("\n  $.TP('temp', function(a,b,c,d) {");
    gold.append("\n    var e=$.X();");
    gold.append("\n    a.append($.T(' This is a template with a fragment: '));");
    gold.append("\n    c(a,b,'foo');");
    gold.append("\n");
    gold.append("\n    // <hr>");
    gold.append("\n    var f=$.E('hr');");
    gold.append("\n    a.append(f);");
    gold.append("\n    c(a,b,'boo');");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/\">");
    gold.append("\n  $.PG(['fixed',''], function(b,a) {");
    gold.append("\n    var c=$.X();");
    gold.append("\n    b.append($.T(' This is a page which is going to use the template. '));");
    gold.append("\n    $.UT(b,a,'temp', function(f,g,h) {");
    gold.append("\n");
    gold.append("\n      // <div rx:case=\"foo\">");
    gold.append("\n      if (h == 'foo') {");
    gold.append("\n        var i=$.E('div');");
    gold.append("\n        i.append($.T(' Foo '));");
    gold.append("\n        f.append(i);");
    gold.append("\n      }");
    gold.append("\n");
    gold.append("\n      // <div rx:case=\"boo\">");
    gold.append("\n      if (h == 'boo') {");
    gold.append("\n        var i=$.E('div');");
    gold.append("\n        i.append($.T(' Boo '));");
    gold.append("\n        f.append(i);");
    gold.append("\n      }");
    gold.append("\n    },{\"foo\":123});");
    gold.append("\n  });");
    gold.append("\n})(RxHTML);");
    gold.append("\nStyle:");
    gold.append("\nShell:<!DOCTYPE html>");
    gold.append("\n<html>");
    gold.append("\n<head><script src=\"/libadama.js/GENMODE.js\"></script><script>");
    gold.append("\n");
    gold.append("\n(function($){");
    gold.append("\n");
    gold.append("\n  // <template name=\"temp\">");
    gold.append("\n  $.TP('temp', function(a,b,c,d) {");
    gold.append("\n    var e=$.X();");
    gold.append("\n    a.append($.T(' This is a template with a fragment: '));");
    gold.append("\n    c(a,b,'foo');");
    gold.append("\n");
    gold.append("\n    // <hr>");
    gold.append("\n    var f=$.E('hr');");
    gold.append("\n    a.append(f);");
    gold.append("\n    c(a,b,'boo');");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/\">");
    gold.append("\n  $.PG(['fixed',''], function(b,a) {");
    gold.append("\n    var c=$.X();");
    gold.append("\n    b.append($.T(' This is a page which is going to use the template. '));");
    gold.append("\n    $.UT(b,a,'temp', function(f,g,h) {");
    gold.append("\n");
    gold.append("\n      // <div rx:case=\"foo\">");
    gold.append("\n      if (h == 'foo') {");
    gold.append("\n        var i=$.E('div');");
    gold.append("\n        i.append($.T(' Foo '));");
    gold.append("\n        f.append(i);");
    gold.append("\n      }");
    gold.append("\n");
    gold.append("\n      // <div rx:case=\"boo\">");
    gold.append("\n      if (h == 'boo') {");
    gold.append("\n        var i=$.E('div');");
    gold.append("\n        i.append($.T(' Boo '));");
    gold.append("\n        f.append(i);");
    gold.append("\n      }");
    gold.append("\n    },{\"foo\":123});");
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
    source.append("\n    <template name=\"temp\">");
    source.append("\n        This is a template with a fragment: <fragment case=\"foo\" />");
    source.append("\n        <hr />");
    source.append("\n        <fragment case=\"boo\" />");
    source.append("\n    </template>");
    source.append("\n    <page uri=\"/\">");
    source.append("\n        This is a page which is going to use the template.");
    source.append("\n        <inline-template name=\"temp\" config:foo=\"123\">");
    source.append("\n            <div rx:case=\"foo\"> Foo </div>");
    source.append("\n            <div rx:case=\"boo\"> Boo </div>");
    source.append("\n        </inline-template>");
    source.append("\n    </page>");
    source.append("\n</forest>");
    return source.toString();
  }
  @Override
  public String schema() {
    StringBuilder gold = new StringBuilder();
    gold.append("{");
    gold.append("\n  \"/\" : { }");
    gold.append("\n}");
    return gold.toString();
  }
}
