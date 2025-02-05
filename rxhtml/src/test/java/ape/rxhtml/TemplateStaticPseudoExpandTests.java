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

public class TemplateStaticPseudoExpandTests extends BaseRxHtmlTest {
  @Override
  public boolean dev() {
    return false;
  }
  @Override
  public String issues() {
    StringBuilder issues = new StringBuilder();
    issues.append("WARNING:template 'navbar' was not used");
    return issues.toString();
  }
  @Override
  public String gold() {
    StringBuilder gold = new StringBuilder();
    gold.append("JavaScript:(function($){");
    gold.append("\n");
    gold.append("\n  // <template name=\"navbar\">");
    gold.append("\n  $.TP('navbar', function(a,b,c,d) {");
    gold.append("\n    var e=$.X();");
    gold.append("\n");
    gold.append("\n    // <ul>");
    gold.append("\n    var f=$.E('ul');");
    gold.append("\n");
    gold.append("\n    // <a class=\"major\" href=\"/page/3\">");
    gold.append("\n    var g=$.E('a');");
    gold.append("\n    $.AC(g,\" major \");");
    gold.append("\n    $.HREF(g,b,\"/page/3\",false);");
    gold.append("\n    g.append($.T(' ['));");
    gold.append("\n    g.append($.T('Page 3'));");
    gold.append("\n    g.append($.T('] '));");
    gold.append("\n    f.append(g);");
    gold.append("\n");
    gold.append("\n    // <br>");
    gold.append("\n    var g=$.E('br');");
    gold.append("\n    f.append(g);");
    gold.append("\n");
    gold.append("\n    // <div>");
    gold.append("\n    var g=$.E('div');");
    gold.append("\n");
    gold.append("\n    // <ul>");
    gold.append("\n    var h=$.E('ul');");
    gold.append("\n    h.append($.T(' ++ '));");
    gold.append("\n");
    gold.append("\n    // <a class=\"\" href=\"/page/3/a\">");
    gold.append("\n    var i=$.E('a');");
    gold.append("\n    $.AC(i,\"\");");
    gold.append("\n    $.HREF(i,b,\"/page/3/a\",false);");
    gold.append("\n    i.append($.T(' ['));");
    gold.append("\n    i.append($.T('Part A'));");
    gold.append("\n    i.append($.T('] '));");
    gold.append("\n    h.append(i);");
    gold.append("\n");
    gold.append("\n    // <br>");
    gold.append("\n    var i=$.E('br');");
    gold.append("\n    h.append(i);");
    gold.append("\n    g.append(h);");
    gold.append("\n    f.append(g);");
    gold.append("\n");
    gold.append("\n    // <a class=\"\" href=\"/page/1\">");
    gold.append("\n    var g=$.E('a');");
    gold.append("\n    $.AC(g,\"\");");
    gold.append("\n    $.HREF(g,b,\"/page/1\",false);");
    gold.append("\n    g.append($.T(' ['));");
    gold.append("\n    g.append($.T('Page 1'));");
    gold.append("\n    g.append($.T('] '));");
    gold.append("\n    f.append(g);");
    gold.append("\n");
    gold.append("\n    // <br>");
    gold.append("\n    var g=$.E('br');");
    gold.append("\n    f.append(g);");
    gold.append("\n");
    gold.append("\n    // <a class=\"\" href=\"/page/2\">");
    gold.append("\n    var g=$.E('a');");
    gold.append("\n    $.AC(g,\"\");");
    gold.append("\n    $.HREF(g,b,\"/page/2\",false);");
    gold.append("\n    g.append($.T(' ['));");
    gold.append("\n    g.append($.T('Page 2'));");
    gold.append("\n    g.append($.T('] '));");
    gold.append("\n    f.append(g);");
    gold.append("\n");
    gold.append("\n    // <br>");
    gold.append("\n    var g=$.E('br');");
    gold.append("\n    f.append(g);");
    gold.append("\n    a.append(f);");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/page/3\" static[nav]:label=\"Page 3\" static[nav]:code=\"major\" static[nav]:var=\"current\">");
    gold.append("\n  $.PG(['fixed','page','fixed','3'], function(b,a) {");
    gold.append("\n    var c=$.X();");
    gold.append("\n  });");
    gold.append("\n})(RxHTML);");
    gold.append("\nStyle:");
    gold.append("\nShell:<!DOCTYPE html>");
    gold.append("\n<html>");
    gold.append("\n<head><script src=\"/libadama.js/GENMODE.js\"></script><script>");
    gold.append("\n");
    gold.append("\n(function($){");
    gold.append("\n");
    gold.append("\n  // <template name=\"navbar\">");
    gold.append("\n  $.TP('navbar', function(a,b,c,d) {");
    gold.append("\n    var e=$.X();");
    gold.append("\n");
    gold.append("\n    // <ul>");
    gold.append("\n    var f=$.E('ul');");
    gold.append("\n");
    gold.append("\n    // <a class=\"major\" href=\"/page/3\">");
    gold.append("\n    var g=$.E('a');");
    gold.append("\n    $.AC(g,\" major \");");
    gold.append("\n    $.HREF(g,b,\"/page/3\",false);");
    gold.append("\n    g.append($.T(' ['));");
    gold.append("\n    g.append($.T('Page 3'));");
    gold.append("\n    g.append($.T('] '));");
    gold.append("\n    f.append(g);");
    gold.append("\n");
    gold.append("\n    // <br>");
    gold.append("\n    var g=$.E('br');");
    gold.append("\n    f.append(g);");
    gold.append("\n");
    gold.append("\n    // <div>");
    gold.append("\n    var g=$.E('div');");
    gold.append("\n");
    gold.append("\n    // <ul>");
    gold.append("\n    var h=$.E('ul');");
    gold.append("\n    h.append($.T(' ++ '));");
    gold.append("\n");
    gold.append("\n    // <a class=\"\" href=\"/page/3/a\">");
    gold.append("\n    var i=$.E('a');");
    gold.append("\n    $.AC(i,\"\");");
    gold.append("\n    $.HREF(i,b,\"/page/3/a\",false);");
    gold.append("\n    i.append($.T(' ['));");
    gold.append("\n    i.append($.T('Part A'));");
    gold.append("\n    i.append($.T('] '));");
    gold.append("\n    h.append(i);");
    gold.append("\n");
    gold.append("\n    // <br>");
    gold.append("\n    var i=$.E('br');");
    gold.append("\n    h.append(i);");
    gold.append("\n    g.append(h);");
    gold.append("\n    f.append(g);");
    gold.append("\n");
    gold.append("\n    // <a class=\"\" href=\"/page/1\">");
    gold.append("\n    var g=$.E('a');");
    gold.append("\n    $.AC(g,\"\");");
    gold.append("\n    $.HREF(g,b,\"/page/1\",false);");
    gold.append("\n    g.append($.T(' ['));");
    gold.append("\n    g.append($.T('Page 1'));");
    gold.append("\n    g.append($.T('] '));");
    gold.append("\n    f.append(g);");
    gold.append("\n");
    gold.append("\n    // <br>");
    gold.append("\n    var g=$.E('br');");
    gold.append("\n    f.append(g);");
    gold.append("\n");
    gold.append("\n    // <a class=\"\" href=\"/page/2\">");
    gold.append("\n    var g=$.E('a');");
    gold.append("\n    $.AC(g,\"\");");
    gold.append("\n    $.HREF(g,b,\"/page/2\",false);");
    gold.append("\n    g.append($.T(' ['));");
    gold.append("\n    g.append($.T('Page 2'));");
    gold.append("\n    g.append($.T('] '));");
    gold.append("\n    f.append(g);");
    gold.append("\n");
    gold.append("\n    // <br>");
    gold.append("\n    var g=$.E('br');");
    gold.append("\n    f.append(g);");
    gold.append("\n    a.append(f);");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/page/3\" static[nav]:label=\"Page 3\" static[nav]:code=\"major\" static[nav]:var=\"current\">");
    gold.append("\n  $.PG(['fixed','page','fixed','3'], function(b,a) {");
    gold.append("\n    var c=$.X();");
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
    source.append("\n    <template name=\"navbar\">");
    source.append("\n        <static-expand source=\"nav\">");
    source.append("\n            <ul static:iterate=\"pages\">");
    source.append("\n                <a class=\"%%%code%%%\" href=\"%%%nav:uri%%%\"> [<static:lookup name=\"label\" />] </a><br />");
    source.append("\n                <div static:if=\"has_children\">");
    source.append("\n                    <ul static:iterate=\"children\">");
    source.append("\n                        ++ <a class=\"%%%code%%%\" href=\"%%%nav:uri%%%\"> [<static:lookup name=\"label\" />] </a><br />");
    source.append("\n                    </ul>");
    source.append("\n                </div>");
    source.append("\n            </ul>");
    source.append("\n        </static-expand>");
    source.append("\n    </template>");
    source.append("\n    <common-page uri:prefix=\"/page/\" static[nav]:var=\"current\" static[nav]:code=\"%%%GENERATE%%%\">");
    source.append("\n");
    source.append("\n    </common-page>");
    source.append("\n    <static-object name=\"foo\">");
    source.append("\n        {\"key\":\"value\"}");
    source.append("\n    </static-object>");
    source.append("\n    <pseudo-page uri=\"/page/1\" static[nav]:label=\"Page 1\">");
    source.append("\n");
    source.append("\n    </pseudo-page>");
    source.append("\n    <pseudo-page uri=\"/page/2\" static[nav]:label=\"Page 2\">");
    source.append("\n");
    source.append("\n    </pseudo-page>");
    source.append("\n    <page uri=\"/page/3\" static[nav]:label=\"Page 3\" static[nav]:code=\"major\">");
    source.append("\n");
    source.append("\n    </page>");
    source.append("\n    <pseudo-page uri=\"/page/3/a\" static[nav]:label=\"Part A\" static[nav]:parent=\"major\">");
    source.append("\n");
    source.append("\n    </pseudo-page>");
    source.append("\n</forest>");
    return source.toString();
  }
  @Override
  public String schema() {
    StringBuilder gold = new StringBuilder();
    gold.append("{");
    gold.append("\n  \"/page/3\" : { }");
    gold.append("\n}");
    return gold.toString();
  }
}
