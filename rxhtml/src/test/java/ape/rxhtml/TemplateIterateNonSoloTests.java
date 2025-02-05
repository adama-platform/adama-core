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

public class TemplateIterateNonSoloTests extends BaseRxHtmlTest {
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
    gold.append("\n    // <div rx:iterate=\"data:set\">");
    gold.append("\n    var d=$.E('div');");
    gold.append("\n    $.IT(d,$.pD(a),'set',false,function(e) {");
    gold.append("\n");
    gold.append("\n      // <div>");
    gold.append("\n      var f=$.E('div');");
    gold.append("\n      f.append($.L(e,'key'));");
    gold.append("\n      f.append($.L(e,'value'));");
    gold.append("\n      return f;");
    gold.append("\n    },false);");
    gold.append("\n    b.append(d);");
    gold.append("\n");
    gold.append("\n    // <table>");
    gold.append("\n    var d=$.E('table');");
    gold.append("\n    b.append(d);");
    gold.append("\n");
    gold.append("\n    // <div rx:iterate=\"data:set\" rx:expand-view-state=\"\">");
    gold.append("\n    var d=$.E('div');");
    gold.append("\n    $.IT(d,$.pD(a),'set',true,function(e) {");
    gold.append("\n");
    gold.append("\n      // <div>");
    gold.append("\n      var f=$.E('div');");
    gold.append("\n");
    gold.append("\n      // <span>");
    gold.append("\n      var g=$.E('span');");
    gold.append("\n      g.append($.L(e,'key'));");
    gold.append("\n      g.append($.T(' - '));");
    gold.append("\n      g.append($.L(e,'value'));");
    gold.append("\n      f.append(g);");
    gold.append("\n      return f;");
    gold.append("\n    },false);");
    gold.append("\n    b.append(d);");
    gold.append("\n");
    gold.append("\n    // <table>");
    gold.append("\n    var d=$.E('table');");
    gold.append("\n    b.append(d);");
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
    gold.append("\n    // <div rx:iterate=\"data:set\">");
    gold.append("\n    var d=$.E('div');");
    gold.append("\n    $.IT(d,$.pD(a),'set',false,function(e) {");
    gold.append("\n");
    gold.append("\n      // <div>");
    gold.append("\n      var f=$.E('div');");
    gold.append("\n      f.append($.L(e,'key'));");
    gold.append("\n      f.append($.L(e,'value'));");
    gold.append("\n      return f;");
    gold.append("\n    },false);");
    gold.append("\n    b.append(d);");
    gold.append("\n");
    gold.append("\n    // <table>");
    gold.append("\n    var d=$.E('table');");
    gold.append("\n    b.append(d);");
    gold.append("\n");
    gold.append("\n    // <div rx:iterate=\"data:set\" rx:expand-view-state=\"\">");
    gold.append("\n    var d=$.E('div');");
    gold.append("\n    $.IT(d,$.pD(a),'set',true,function(e) {");
    gold.append("\n");
    gold.append("\n      // <div>");
    gold.append("\n      var f=$.E('div');");
    gold.append("\n");
    gold.append("\n      // <span>");
    gold.append("\n      var g=$.E('span');");
    gold.append("\n      g.append($.L(e,'key'));");
    gold.append("\n      g.append($.T(' - '));");
    gold.append("\n      g.append($.L(e,'value'));");
    gold.append("\n      f.append(g);");
    gold.append("\n      return f;");
    gold.append("\n    },false);");
    gold.append("\n    b.append(d);");
    gold.append("\n");
    gold.append("\n    // <table>");
    gold.append("\n    var d=$.E('table');");
    gold.append("\n    b.append(d);");
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
    source.append("\n    <page uri=\"/\">");
    source.append("\n        <table>");
    source.append("\n            <div rx:iterate=\"data:set\">");
    source.append("\n                <lookup path=\"key\" />");
    source.append("\n                <lookup path=\"value\" />");
    source.append("\n            </div>");
    source.append("\n        </table>");
    source.append("\n        <table>");
    source.append("\n            <div rx:iterate=\"data:set\" rx:expand-view-state>");
    source.append("\n                [<span> <lookup path=\"key\" /> - <lookup path=\"value\" /> </span>]");
    source.append("\n            </div>");
    source.append("\n        </table>");
    source.append("\n    </page>");
    source.append("\n</forest>");
    return source.toString();
  }
  @Override
  public String schema() {
    StringBuilder gold = new StringBuilder();
    gold.append("{");
    gold.append("\n  \"/\" : {");
    gold.append("\n    \"set\" : {");
    gold.append("\n      \"#items\" : { }");
    gold.append("\n    }");
    gold.append("\n  }");
    gold.append("\n}");
    return gold.toString();
  }
}
