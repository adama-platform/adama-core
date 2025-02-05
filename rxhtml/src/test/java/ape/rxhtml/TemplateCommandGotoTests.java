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

public class TemplateCommandGotoTests extends BaseRxHtmlTest {
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
    gold.append("\n    // <button rx:click=\"goto:/{view:here}\">");
    gold.append("\n    var d=$.E('button');");
    gold.append("\n    var e={};");
    gold.append("\n    $.YS($.pV(a),e,'here');");
    gold.append("\n    $.onGO(d,'click',a,function(){ return \"/\" + $.F(e,'here');});");
    gold.append("\n    d.append($.T('goto View'));");
    gold.append("\n    b.append(d);");
    gold.append("\n");
    gold.append("\n    // <button rx:click=\"goto:/path-to-data/{data:there}\">");
    gold.append("\n    var d=$.E('button');");
    gold.append("\n    var f={};");
    gold.append("\n    $.YS($.pD(a),f,'there');");
    gold.append("\n    $.onGO(d,'click',a,function(){ return \"/path-to-data/\" + $.F(f,'there');});");
    gold.append("\n    d.append($.T('goto Data'));");
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
    gold.append("\n    // <button rx:click=\"goto:/{view:here}\">");
    gold.append("\n    var d=$.E('button');");
    gold.append("\n    var e={};");
    gold.append("\n    $.YS($.pV(a),e,'here');");
    gold.append("\n    $.onGO(d,'click',a,function(){ return \"/\" + $.F(e,'here');});");
    gold.append("\n    d.append($.T('goto View'));");
    gold.append("\n    b.append(d);");
    gold.append("\n");
    gold.append("\n    // <button rx:click=\"goto:/path-to-data/{data:there}\">");
    gold.append("\n    var d=$.E('button');");
    gold.append("\n    var f={};");
    gold.append("\n    $.YS($.pD(a),f,'there');");
    gold.append("\n    $.onGO(d,'click',a,function(){ return \"/path-to-data/\" + $.F(f,'there');});");
    gold.append("\n    d.append($.T('goto Data'));");
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
    source.append("\n        <button rx:click=\"goto:/{view:here}\">goto View</button>");
    source.append("\n        <button rx:click=\"goto:/path-to-data/{data:there}\">goto Data</button>");
    source.append("\n    </page>");
    source.append("\n</forest>");
    return source.toString();
  }
  @Override
  public String schema() {
    StringBuilder gold = new StringBuilder();
    gold.append("{");
    gold.append("\n  \"/\" : {");
    gold.append("\n    \"here\" : \"value\"");
    gold.append("\n  }");
    gold.append("\n}");
    return gold.toString();
  }
}
