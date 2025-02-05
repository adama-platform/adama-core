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

public class TemplateChooseSimpleTests extends BaseRxHtmlTest {
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
    gold.append("\n    // <connection space=\"space\" key=\"key\" name=\"default\" redirect=\"/sign-in\">");
    gold.append("\n    var d=$.RX([]);");
    gold.append("\n    d.name='default';");
    gold.append("\n    d.space='space';");
    gold.append("\n    d.key='key';");
    gold.append("\n    d.identity=true;");
    gold.append("\n    d.redirect='/sign-in';");
    gold.append("\n    $.CONNECT(a,d);");
    gold.append("\n    d.__();");
    gold.append("\n");
    gold.append("\n    // <connection space=\"space\" key=\"key\" name=\"default\" redirect=\"/sign-in\">");
    gold.append("\n    var e=$.RX([]);");
    gold.append("\n    e.name='default';");
    gold.append("\n    $.P(b,a,e,function(b,f) {");
    gold.append("\n");
    gold.append("\n      // <div rx:if=\"choose:channel\">");
    gold.append("\n      var g=$.E('div');");
    gold.append("\n      $.DE(g,f,f,'channel','id','id',true,false,function(i,h) {");
    gold.append("\n");
    gold.append("\n        // <button rx:click=\"choose:channel\">");
    gold.append("\n        var j=$.E('button');");
    gold.append("\n        $.exCH(j,'click',h,'id','channel','id');");
    gold.append("\n        j.append($.T('Add Choice'));");
    gold.append("\n        i.append(j);");
    gold.append("\n      },function(i,h) {");
    gold.append("\n      },false);");
    gold.append("\n      b.append(g);");
    gold.append("\n");
    gold.append("\n      // <div rx:if=\"finalize:channel\">");
    gold.append("\n      var g=$.E('div');");
    gold.append("\n      $.FIN(g,f,'channel',true,false,function(i,h) {");
    gold.append("\n");
    gold.append("\n        // <button rx:click=\"finalize:channel\">");
    gold.append("\n        var j=$.E('button');");
    gold.append("\n        $.exFIN(j,'click',h,'channel');");
    gold.append("\n        j.append($.T('Send choices'));");
    gold.append("\n        i.append(j);");
    gold.append("\n        i.append($.T(' Time to decide! '));");
    gold.append("\n      },function(i,h) {");
    gold.append("\n      },false);");
    gold.append("\n      b.append(g);");
    gold.append("\n    },function(b,f) {");
    gold.append("\n    },false);");
    gold.append("\n    e.__();");
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
    gold.append("\n    // <connection space=\"space\" key=\"key\" name=\"default\" redirect=\"/sign-in\">");
    gold.append("\n    var d=$.RX([]);");
    gold.append("\n    d.name='default';");
    gold.append("\n    d.space='space';");
    gold.append("\n    d.key='key';");
    gold.append("\n    d.identity=true;");
    gold.append("\n    d.redirect='/sign-in';");
    gold.append("\n    $.CONNECT(a,d);");
    gold.append("\n    d.__();");
    gold.append("\n");
    gold.append("\n    // <connection space=\"space\" key=\"key\" name=\"default\" redirect=\"/sign-in\">");
    gold.append("\n    var e=$.RX([]);");
    gold.append("\n    e.name='default';");
    gold.append("\n    $.P(b,a,e,function(b,f) {");
    gold.append("\n");
    gold.append("\n      // <div rx:if=\"choose:channel\">");
    gold.append("\n      var g=$.E('div');");
    gold.append("\n      $.DE(g,f,f,'channel','id','id',true,false,function(i,h) {");
    gold.append("\n");
    gold.append("\n        // <button rx:click=\"choose:channel\">");
    gold.append("\n        var j=$.E('button');");
    gold.append("\n        $.exCH(j,'click',h,'id','channel','id');");
    gold.append("\n        j.append($.T('Add Choice'));");
    gold.append("\n        i.append(j);");
    gold.append("\n      },function(i,h) {");
    gold.append("\n      },false);");
    gold.append("\n      b.append(g);");
    gold.append("\n");
    gold.append("\n      // <div rx:if=\"finalize:channel\">");
    gold.append("\n      var g=$.E('div');");
    gold.append("\n      $.FIN(g,f,'channel',true,false,function(i,h) {");
    gold.append("\n");
    gold.append("\n        // <button rx:click=\"finalize:channel\">");
    gold.append("\n        var j=$.E('button');");
    gold.append("\n        $.exFIN(j,'click',h,'channel');");
    gold.append("\n        j.append($.T('Send choices'));");
    gold.append("\n        i.append(j);");
    gold.append("\n        i.append($.T(' Time to decide! '));");
    gold.append("\n      },function(i,h) {");
    gold.append("\n      },false);");
    gold.append("\n      b.append(g);");
    gold.append("\n    },function(b,f) {");
    gold.append("\n    },false);");
    gold.append("\n    e.__();");
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
    source.append("\n        <connection space=\"space\" key=\"key\">");
    source.append("\n            <div rx:if=\"choose:channel\">");
    source.append("\n                <button rx:click=\"choose:channel\">Add Choice</button>");
    source.append("\n            </div>");
    source.append("\n            <div rx:if=\"finalize:channel\">");
    source.append("\n                <button rx:click=\"finalize:channel\">Send choices</button>");
    source.append("\n                Time to decide!");
    source.append("\n            </div>");
    source.append("\n        </connection>");
    source.append("\n    </page>");
    source.append("\n</forest>");
    return source.toString();
  }
  @Override
  public String schema() {
    StringBuilder gold = new StringBuilder();
    gold.append("{");
    gold.append("\n  \"/\" : {");
    gold.append("\n    \"choose:channel\" : \"bool\",");
    gold.append("\n    \"finalize:channel\" : \"bool\"");
    gold.append("\n  }");
    gold.append("\n}");
    return gold.toString();
  }
}
