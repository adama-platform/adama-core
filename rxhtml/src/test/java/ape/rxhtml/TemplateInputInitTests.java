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

public class TemplateInputInitTests extends BaseRxHtmlTest {
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
    gold.append("\n    // <input name=\"email\" value=\"{value}\">");
    gold.append("\n    var d=$.E('input');");
    gold.append("\n    $.SA(d,'name',\"email\");");
    gold.append("\n    {");
    gold.append("\n      var e={};");
    gold.append("\n      e.__dom=d;");
    gold.append("\n      var f=(function() {");
    gold.append("\n        $.SV(this.__dom,$.F(this,'value'));");
    gold.append("\n      }).bind(e);");
    gold.append("\n      $.Y(a,e,'value',f);");
    gold.append("\n      f();");
    gold.append("\n    }");
    gold.append("\n    b.append(d);");
    gold.append("\n");
    gold.append("\n    // <textarea name=\"email\" value=\"{value}\">");
    gold.append("\n    var d=$.E('textarea');");
    gold.append("\n    $.SA(d,'name',\"email\");");
    gold.append("\n    {");
    gold.append("\n      var e={};");
    gold.append("\n      e.__dom=d;");
    gold.append("\n      var f=(function() {");
    gold.append("\n        $.SV(this.__dom,$.F(this,'value'));");
    gold.append("\n      }).bind(e);");
    gold.append("\n      $.Y(a,e,'value',f);");
    gold.append("\n      f();");
    gold.append("\n    }");
    gold.append("\n    b.append(d);");
    gold.append("\n");
    gold.append("\n    // <select name=\"email\" value=\"{value}\">");
    gold.append("\n    var d=$.E('select');");
    gold.append("\n    $.SA(d,'name',\"email\");");
    gold.append("\n    {");
    gold.append("\n      var e={};");
    gold.append("\n      e.__dom=d;");
    gold.append("\n      var f=(function() {");
    gold.append("\n        $.SV(this.__dom,$.F(this,'value'));");
    gold.append("\n      }).bind(e);");
    gold.append("\n      $.Y(a,e,'value',f);");
    gold.append("\n      f();");
    gold.append("\n    }");
    gold.append("\n");
    gold.append("\n    // <option value=\"foo\">");
    gold.append("\n    var e=$.E('option');");
    gold.append("\n    $.SV(e,\"foo\");");
    gold.append("\n    e.append($.T('FOOO'));");
    gold.append("\n    d.append(e);");
    gold.append("\n    b.append(d);");
    gold.append("\n");
    gold.append("\n    // <input type=\"checkbox\" checked=\"\" rx:debounce=\"nope\">");
    gold.append("\n    var d=$.E('input');");
    gold.append("\n    $.SA(d,'type',\"checkbox\");");
    gold.append("\n    $.FV(d,'checked',true);");
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
    gold.append("\n    // <input name=\"email\" value=\"{value}\">");
    gold.append("\n    var d=$.E('input');");
    gold.append("\n    $.SA(d,'name',\"email\");");
    gold.append("\n    {");
    gold.append("\n      var e={};");
    gold.append("\n      e.__dom=d;");
    gold.append("\n      var f=(function() {");
    gold.append("\n        $.SV(this.__dom,$.F(this,'value'));");
    gold.append("\n      }).bind(e);");
    gold.append("\n      $.Y(a,e,'value',f);");
    gold.append("\n      f();");
    gold.append("\n    }");
    gold.append("\n    b.append(d);");
    gold.append("\n");
    gold.append("\n    // <textarea name=\"email\" value=\"{value}\">");
    gold.append("\n    var d=$.E('textarea');");
    gold.append("\n    $.SA(d,'name',\"email\");");
    gold.append("\n    {");
    gold.append("\n      var e={};");
    gold.append("\n      e.__dom=d;");
    gold.append("\n      var f=(function() {");
    gold.append("\n        $.SV(this.__dom,$.F(this,'value'));");
    gold.append("\n      }).bind(e);");
    gold.append("\n      $.Y(a,e,'value',f);");
    gold.append("\n      f();");
    gold.append("\n    }");
    gold.append("\n    b.append(d);");
    gold.append("\n");
    gold.append("\n    // <select name=\"email\" value=\"{value}\">");
    gold.append("\n    var d=$.E('select');");
    gold.append("\n    $.SA(d,'name',\"email\");");
    gold.append("\n    {");
    gold.append("\n      var e={};");
    gold.append("\n      e.__dom=d;");
    gold.append("\n      var f=(function() {");
    gold.append("\n        $.SV(this.__dom,$.F(this,'value'));");
    gold.append("\n      }).bind(e);");
    gold.append("\n      $.Y(a,e,'value',f);");
    gold.append("\n      f();");
    gold.append("\n    }");
    gold.append("\n");
    gold.append("\n    // <option value=\"foo\">");
    gold.append("\n    var e=$.E('option');");
    gold.append("\n    $.SV(e,\"foo\");");
    gold.append("\n    e.append($.T('FOOO'));");
    gold.append("\n    d.append(e);");
    gold.append("\n    b.append(d);");
    gold.append("\n");
    gold.append("\n    // <input type=\"checkbox\" checked=\"\" rx:debounce=\"nope\">");
    gold.append("\n    var d=$.E('input');");
    gold.append("\n    $.SA(d,'type',\"checkbox\");");
    gold.append("\n    $.FV(d,'checked',true);");
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
    source.append("\n        <input name=\"email\" value=\"{value}\"/>");
    source.append("\n        <textarea name=\"email\" value=\"{value}\"></textarea>");
    source.append("\n        <select name=\"email\" value=\"{value}\">");
    source.append("\n            <option value=\"foo\">FOOO</option>");
    source.append("\n        </select>");
    source.append("\n        <input type=\"checkbox\" checked rx:debounce=\"nope\" />");
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
