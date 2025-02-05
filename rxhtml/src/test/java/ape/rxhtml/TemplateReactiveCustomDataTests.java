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

public class TemplateReactiveCustomDataTests extends BaseRxHtmlTest {
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
    gold.append("\n    // <customdata parameter:one=\"one\" parameter:two=\"{var}\" parameter:three=\"{var1}{var2}\" parameter:present=\"\">");
    gold.append("\n    var d=$.RX(['two','three']);");
    gold.append("\n    d.one='one';");
    gold.append("\n    $.Y2(a,d,'two','var',function(e) {");
    gold.append("\n      d.two=$.F(e,'var')");
    gold.append("\n      d.__();");
    gold.append("\n    });");
    gold.append("\n    $.Y2(a,d,'three','var1',function(e) {");
    gold.append("\n      d.three=$.F(e,'var1') + $.F(e,'var2')");
    gold.append("\n      d.__();");
    gold.append("\n    });");
    gold.append("\n    $.Y2(a,d,'three','var2',function(e) {");
    gold.append("\n      d.three=$.F(e,'var1') + $.F(e,'var2')");
    gold.append("\n      d.__();");
    gold.append("\n    });");
    gold.append("\n    d.present='';");
    gold.append("\n    $.CUDA(b,a,'',d,'/sign-in',function(e) {");
    gold.append("\n    });");
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
    gold.append("\n    // <customdata parameter:one=\"one\" parameter:two=\"{var}\" parameter:three=\"{var1}{var2}\" parameter:present=\"\">");
    gold.append("\n    var d=$.RX(['two','three']);");
    gold.append("\n    d.one='one';");
    gold.append("\n    $.Y2(a,d,'two','var',function(e) {");
    gold.append("\n      d.two=$.F(e,'var')");
    gold.append("\n      d.__();");
    gold.append("\n    });");
    gold.append("\n    $.Y2(a,d,'three','var1',function(e) {");
    gold.append("\n      d.three=$.F(e,'var1') + $.F(e,'var2')");
    gold.append("\n      d.__();");
    gold.append("\n    });");
    gold.append("\n    $.Y2(a,d,'three','var2',function(e) {");
    gold.append("\n      d.three=$.F(e,'var1') + $.F(e,'var2')");
    gold.append("\n      d.__();");
    gold.append("\n    });");
    gold.append("\n    d.present='';");
    gold.append("\n    $.CUDA(b,a,'',d,'/sign-in',function(e) {");
    gold.append("\n    });");
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
    source.append("\n        <customdata parameter:one=\"one\" parameter:two=\"{var}\"  parameter:three=\"{var1}{var2}\" parameter:present>");
    source.append("\n");
    source.append("\n        </customdata>");
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
