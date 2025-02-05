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

public class TemplateReactiveConnectionTests extends BaseRxHtmlTest {
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
    gold.append("\n  // <page uri=\"/\">");
    gold.append("\n  $.PG(['fixed',''], function(b,a) {");
    gold.append("\n    var c=$.X();");
    gold.append("\n");
    gold.append("\n    // <connection name=\"myname\" space=\"space{suffix}\" key=\"{view:/key}\" redirect=\"/sign-in\">");
    gold.append("\n    var d=$.RX(['space','key']);");
    gold.append("\n    d.name='myname';");
    gold.append("\n    $.Y2(a,d,'space','suffix',function(e) {");
    gold.append("\n      d.space=\"space\" + $.F(e,'suffix')");
    gold.append("\n      d.__();");
    gold.append("\n    });");
    gold.append("\n    $.Y2($.pR($.pV(a)),d,'key','key',function(e) {");
    gold.append("\n      d.key=$.F(e,'key')");
    gold.append("\n      d.__();");
    gold.append("\n    });");
    gold.append("\n    d.identity=true;");
    gold.append("\n    d.redirect='/sign-in';");
    gold.append("\n    $.CONNECT(a,d);");
    gold.append("\n");
    gold.append("\n    // <connection name=\"myname\" space=\"space{suffix}\" key=\"{view:/key}\" redirect=\"/sign-in\">");
    gold.append("\n    var e=$.RX([]);");
    gold.append("\n    e.name='myname';");
    gold.append("\n    var g=$.E('div');");
    gold.append("\n    b.append(g);");
    gold.append("\n    $.P(g,a,e,function(g,f) {");
    gold.append("\n    },function(g,f) {");
    gold.append("\n    },false);");
    gold.append("\n    e.__();");
    gold.append("\n");
    gold.append("\n    // <pick name=\"myname\">");
    gold.append("\n    var f=$.RX([]);");
    gold.append("\n    f.name='myname';");
    gold.append("\n    var i=$.E('div');");
    gold.append("\n    b.append(i);");
    gold.append("\n    $.P(i,a,f,function(i,h) {");
    gold.append("\n    },function(i,h) {");
    gold.append("\n    },false);");
    gold.append("\n    f.__();");
    gold.append("\n  });");
    gold.append("\n})(RxHTML);");
    gold.append("\nStyle:");
    gold.append("\nShell:<!DOCTYPE html>");
    gold.append("\n<html>");
    gold.append("\n<head><script src=\"/libadama.js/GENMODE.js\"></script><script>");
    gold.append("\n");
    gold.append("\n(function($){");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/\">");
    gold.append("\n  $.PG(['fixed',''], function(b,a) {");
    gold.append("\n    var c=$.X();");
    gold.append("\n");
    gold.append("\n    // <connection name=\"myname\" space=\"space{suffix}\" key=\"{view:/key}\" redirect=\"/sign-in\">");
    gold.append("\n    var d=$.RX(['space','key']);");
    gold.append("\n    d.name='myname';");
    gold.append("\n    $.Y2(a,d,'space','suffix',function(e) {");
    gold.append("\n      d.space=\"space\" + $.F(e,'suffix')");
    gold.append("\n      d.__();");
    gold.append("\n    });");
    gold.append("\n    $.Y2($.pR($.pV(a)),d,'key','key',function(e) {");
    gold.append("\n      d.key=$.F(e,'key')");
    gold.append("\n      d.__();");
    gold.append("\n    });");
    gold.append("\n    d.identity=true;");
    gold.append("\n    d.redirect='/sign-in';");
    gold.append("\n    $.CONNECT(a,d);");
    gold.append("\n");
    gold.append("\n    // <connection name=\"myname\" space=\"space{suffix}\" key=\"{view:/key}\" redirect=\"/sign-in\">");
    gold.append("\n    var e=$.RX([]);");
    gold.append("\n    e.name='myname';");
    gold.append("\n    var g=$.E('div');");
    gold.append("\n    b.append(g);");
    gold.append("\n    $.P(g,a,e,function(g,f) {");
    gold.append("\n    },function(g,f) {");
    gold.append("\n    },false);");
    gold.append("\n    e.__();");
    gold.append("\n");
    gold.append("\n    // <pick name=\"myname\">");
    gold.append("\n    var f=$.RX([]);");
    gold.append("\n    f.name='myname';");
    gold.append("\n    var i=$.E('div');");
    gold.append("\n    b.append(i);");
    gold.append("\n    $.P(i,a,f,function(i,h) {");
    gold.append("\n    },function(i,h) {");
    gold.append("\n    },false);");
    gold.append("\n    f.__();");
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
    source.append("\n<page uri=\"/\">");
    source.append("\n        <connection name=\"myname\" space=\"space{suffix}\" key=\"{view:/key}\">");
    source.append("\n");
    source.append("\n        </connection>");
    source.append("\n        <pick name=\"myname\">");
    source.append("\n");
    source.append("\n        </pick>");
    source.append("\n    </page>");
    source.append("\n</forest>");
    return source.toString();
  }
  @Override
  public String schema() {
    StringBuilder gold = new StringBuilder();
    gold.append("{");
    gold.append("\n  \"/\" : {");
    gold.append("\n    \"key\" : \"value\"");
    gold.append("\n  }");
    gold.append("\n}");
    return gold.toString();
  }
}
