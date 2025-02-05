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

public class TemplateStaticMuchTests extends BaseRxHtmlTest {
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
    gold.append("\n  // <template name=\"silly\">");
    gold.append("\n  $.TP('silly', function(a,b,c,d) {");
    gold.append("\n    var e=$.X();");
    gold.append("\n");
    gold.append("\n    // <div static:content=\"\">");
    gold.append("\n    var f=$.E('div');");
    gold.append("\n    $.SIH(f,\"<span>An Element</span> <span>An Element</span> <span>An Element</span> <span>An Element</span>\");");
    gold.append("\n    a.append(f);");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/\">");
    gold.append("\n  $.PG(['fixed',''], function(b,a) {");
    gold.append("\n    var c=$.X();");
    gold.append("\n");
    gold.append("\n    // <div static:content=\"\">");
    gold.append("\n    var f=$.E('div');");
    gold.append("\n    $.SIH(f,\"<span>An Element</span>\\n<svg xmlns=\\\"http://www.w3.org/2000/svg\\\" fill=\\\"none\\\" viewBox=\\\"0 0 24 24\\\" stroke-width=\\\"1.5\\\" stroke=\\\"currentColor\\\" class=\\\"w-6 h-6\\\">\\n <path stroke-linecap=\\\"round\\\" stroke-linejoin=\\\"round\\\" d=\\\"M4.26 10.147a60.438 60.438 0 0 0-.491 6.347A48.62 48.62 0 0 1 12 20.904a48.62 48.62 0 0 1 8.232-4.41 60.46 60.46 0 0 0-.491-6.347m-15.482 0a50.636 50.636 0 0 0-2.658-.813A59.906 59.906 0 0 1 12 3.493a59.903 59.903 0 0 1 10.399 5.84c-.896.248-1.783.52-2.658.814m-15.482 0A50.717 50.717 0 0 1 12 13.489a50.702 50.702 0 0 1 7.74-3.342M6.75 15a.75.75 0 1 0 0-1.5.75.75 0 0 0 0 1.5Zm0 0v-3.675A55.378 55.378 0 0 1 12 8.443m-7.007 11.55A5.981 5.981 0 0 0 6.75 15.75v-1.5\\\" />\\n</svg><span>An Element</span> <span>An Element</span> <span>An Element</span>\");");
    gold.append("\n    b.append(f);");
    gold.append("\n");
    gold.append("\n    // <div rx:template=\"silly\">");
    gold.append("\n    var f=$.E('div');");
    gold.append("\n    $.UT(f,a,'silly', function(g,h,i) {");
    gold.append("\n    },{});");
    gold.append("\n    b.append(f);");
    gold.append("\n  });");
    gold.append("\n})(RxHTML);");
    gold.append("\nStyle:");
    gold.append("\nShell:<!DOCTYPE html>");
    gold.append("\n<html>");
    gold.append("\n<head><script src=\"/libadama.js/GENMODE.js\"></script><script>");
    gold.append("\n");
    gold.append("\n(function($){");
    gold.append("\n");
    gold.append("\n  // <template name=\"silly\">");
    gold.append("\n  $.TP('silly', function(a,b,c,d) {");
    gold.append("\n    var e=$.X();");
    gold.append("\n");
    gold.append("\n    // <div static:content=\"\">");
    gold.append("\n    var f=$.E('div');");
    gold.append("\n    $.SIH(f,\"<span>An Element</span> <span>An Element</span> <span>An Element</span> <span>An Element</span>\");");
    gold.append("\n    a.append(f);");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/\">");
    gold.append("\n  $.PG(['fixed',''], function(b,a) {");
    gold.append("\n    var c=$.X();");
    gold.append("\n");
    gold.append("\n    // <div static:content=\"\">");
    gold.append("\n    var f=$.E('div');");
    gold.append("\n    $.SIH(f,\"<span>An Element</span>\\n<svg xmlns=\\\"http://www.w3.org/2000/svg\\\" fill=\\\"none\\\" viewBox=\\\"0 0 24 24\\\" stroke-width=\\\"1.5\\\" stroke=\\\"currentColor\\\" class=\\\"w-6 h-6\\\">\\n <path stroke-linecap=\\\"round\\\" stroke-linejoin=\\\"round\\\" d=\\\"M4.26 10.147a60.438 60.438 0 0 0-.491 6.347A48.62 48.62 0 0 1 12 20.904a48.62 48.62 0 0 1 8.232-4.41 60.46 60.46 0 0 0-.491-6.347m-15.482 0a50.636 50.636 0 0 0-2.658-.813A59.906 59.906 0 0 1 12 3.493a59.903 59.903 0 0 1 10.399 5.84c-.896.248-1.783.52-2.658.814m-15.482 0A50.717 50.717 0 0 1 12 13.489a50.702 50.702 0 0 1 7.74-3.342M6.75 15a.75.75 0 1 0 0-1.5.75.75 0 0 0 0 1.5Zm0 0v-3.675A55.378 55.378 0 0 1 12 8.443m-7.007 11.55A5.981 5.981 0 0 0 6.75 15.75v-1.5\\\" />\\n</svg><span>An Element</span> <span>An Element</span> <span>An Element</span>\");");
    gold.append("\n    b.append(f);");
    gold.append("\n");
    gold.append("\n    // <div rx:template=\"silly\">");
    gold.append("\n    var f=$.E('div');");
    gold.append("\n    $.UT(f,a,'silly', function(g,h,i) {");
    gold.append("\n    },{});");
    gold.append("\n    b.append(f);");
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
    source.append("\n        <div static:content>");
    source.append("\n            <span>An Element</span>");
    source.append("\n            <svg xmlns=\"http://www.w3.org/2000/svg\" fill=\"none\" viewBox=\"0 0 24 24\" stroke-width=\"1.5\" stroke=\"currentColor\" class=\"w-6 h-6\">");
    source.append("\n                <path stroke-linecap=\"round\" stroke-linejoin=\"round\" d=\"M4.26 10.147a60.438 60.438 0 0 0-.491 6.347A48.62 48.62 0 0 1 12 20.904a48.62 48.62 0 0 1 8.232-4.41 60.46 60.46 0 0 0-.491-6.347m-15.482 0a50.636 50.636 0 0 0-2.658-.813A59.906 59.906 0 0 1 12 3.493a59.903 59.903 0 0 1 10.399 5.84c-.896.248-1.783.52-2.658.814m-15.482 0A50.717 50.717 0 0 1 12 13.489a50.702 50.702 0 0 1 7.74-3.342M6.75 15a.75.75 0 1 0 0-1.5.75.75 0 0 0 0 1.5Zm0 0v-3.675A55.378 55.378 0 0 1 12 8.443m-7.007 11.55A5.981 5.981 0 0 0 6.75 15.75v-1.5\" />");
    source.append("\n            </svg>");
    source.append("\n            <span>An Element</span>");
    source.append("\n            <span>An Element</span>");
    source.append("\n            <span>An Element</span>");
    source.append("\n        </div>");
    source.append("\n        <div rx:template=\"silly\"></div>");
    source.append("\n    </page>");
    source.append("\n");
    source.append("\n    <template name=\"silly\">");
    source.append("\n        <div static:content>");
    source.append("\n            <span>An Element</span>");
    source.append("\n            <span>An Element</span>");
    source.append("\n            <span>An Element</span>");
    source.append("\n            <span>An Element</span>");
    source.append("\n        </div>");
    source.append("\n    </template>");
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
