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

public class TemplateIfEqualsTests extends BaseRxHtmlTest {
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
    gold.append("\n    // <div rx:if=\"left=right\">");
    gold.append("\n    var d=$.E('div');");
    gold.append("\n    $.IFeq(d,a,a,'left',a,'right',true,false,function(f,e) {");
    gold.append("\n      f.append($.T(' Yaz '));");
    gold.append("\n    },function(f,e) {");
    gold.append("\n    },false);");
    gold.append("\n    b.append(d);");
    gold.append("\n");
    gold.append("\n    // <div rx:if=\"view:a=view:b\">");
    gold.append("\n    var d=$.E('div');");
    gold.append("\n    $.IFeq(d,a,$.pV(a),'a',$.pV(a),'b',true,false,function(f,e) {");
    gold.append("\n      f.append($.T(' Yaz '));");
    gold.append("\n    },function(f,e) {");
    gold.append("\n    },false);");
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
    gold.append("\n    // <div rx:if=\"left=right\">");
    gold.append("\n    var d=$.E('div');");
    gold.append("\n    $.IFeq(d,a,a,'left',a,'right',true,false,function(f,e) {");
    gold.append("\n      f.append($.T(' Yaz '));");
    gold.append("\n    },function(f,e) {");
    gold.append("\n    },false);");
    gold.append("\n    b.append(d);");
    gold.append("\n");
    gold.append("\n    // <div rx:if=\"view:a=view:b\">");
    gold.append("\n    var d=$.E('div');");
    gold.append("\n    $.IFeq(d,a,$.pV(a),'a',$.pV(a),'b',true,false,function(f,e) {");
    gold.append("\n      f.append($.T(' Yaz '));");
    gold.append("\n    },function(f,e) {");
    gold.append("\n    },false);");
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
    source.append("\n        <div rx:if=\"left=right\">");
    source.append("\n            Yaz");
    source.append("\n        </div>");
    source.append("\n        <div rx:if=\"view:a=view:b\">");
    source.append("\n            Yaz");
    source.append("\n        </div>");
    source.append("\n    </page>");
    source.append("\n</forest>");
    return source.toString();
  }
  @Override
  public String schema() {
    StringBuilder gold = new StringBuilder();
    gold.append("{");
    gold.append("\n  \"/\" : {");
    gold.append("\n    \"a\" : \"cmpval\",");
    gold.append("\n    \"b\" : \"cmpval\",");
    gold.append("\n    \"left\" : \"cmpval\",");
    gold.append("\n    \"right\" : \"cmpval\"");
    gold.append("\n  }");
    gold.append("\n}");
    return gold.toString();
  }
}
