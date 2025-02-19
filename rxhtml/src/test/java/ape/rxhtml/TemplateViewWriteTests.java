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

public class TemplateViewWriteTests extends BaseRxHtmlTest {
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
    gold.append("\n    // <connection name=\"myname\" use-domain=\"\" redirect=\"/sign-in\">");
    gold.append("\n    var d=$.RX([]);");
    gold.append("\n    d.name='myname';");
    gold.append("\n    d.identity=true;");
    gold.append("\n    d.redirect='/sign-in';");
    gold.append("\n    $.DCONNECT(a,d);");
    gold.append("\n    d.__();");
    gold.append("\n");
    gold.append("\n    // <connection name=\"myname\" use-domain=\"\" redirect=\"/sign-in\">");
    gold.append("\n    var e=$.RX([]);");
    gold.append("\n    e.name='myname';");
    gold.append("\n    $.P(b,a,e,function(b,f) {");
    gold.append("\n");
    gold.append("\n      // <input type=\"text\" rx:sync=\"value_a\">");
    gold.append("\n      var g=$.E('input');");
    gold.append("\n      $.SA(g,'type',\"text\");");
    gold.append("\n      b.append(g);");
    gold.append("\n      $.SY(g,$.pV(f),'value_a',100.0);");
    gold.append("\n");
    gold.append("\n      // <input type=\"text\" rx:sync=\"value_b\">");
    gold.append("\n      var g=$.E('input');");
    gold.append("\n      $.SA(g,'type',\"text\");");
    gold.append("\n      b.append(g);");
    gold.append("\n      $.SY(g,$.pV(f),'value_b',100.0);");
    gold.append("\n");
    gold.append("\n      // <input type=\"text\" disabled=\"{view:disable}\">");
    gold.append("\n      var g=$.E('input');");
    gold.append("\n      $.SA(g,'type',\"text\");");
    gold.append("\n      {");
    gold.append("\n        var h={};");
    gold.append("\n        h.__dom=g;");
    gold.append("\n        var i=(function() {");
    gold.append("\n          $.FV(this.__dom,'disabled',$.B($.F(this,'disable')));");
    gold.append("\n        }).bind(h);");
    gold.append("\n        $.Y($.pV(f),h,'disable',i);");
    gold.append("\n        i();");
    gold.append("\n      }");
    gold.append("\n      b.append(g);");
    gold.append("\n");
    gold.append("\n      // <input type=\"text\" disabled=\"true\">");
    gold.append("\n      var g=$.E('input');");
    gold.append("\n      $.SA(g,'type',\"text\");");
    gold.append("\n      $.FV(g,'disabled',true);");
    gold.append("\n      b.append(g);");
    gold.append("\n");
    gold.append("\n      // <input type=\"text\" disabled=\"false\">");
    gold.append("\n      var g=$.E('input');");
    gold.append("\n      $.SA(g,'type',\"text\");");
    gold.append("\n      $.FV(g,'disabled',false);");
    gold.append("\n      b.append(g);");
    gold.append("\n");
    gold.append("\n      // <input type=\"text\" disabled=\"{view:disable}\" required=\"{view:required}\">");
    gold.append("\n      var g=$.E('input');");
    gold.append("\n      $.SA(g,'type',\"text\");");
    gold.append("\n      {");
    gold.append("\n        var h={};");
    gold.append("\n        h.__dom=g;");
    gold.append("\n        var i=(function() {");
    gold.append("\n          $.FV(this.__dom,'disabled',$.B($.F(this,'disable')));");
    gold.append("\n        }).bind(h);");
    gold.append("\n        $.Y($.pV(f),h,'disable',i);");
    gold.append("\n        i();");
    gold.append("\n      }");
    gold.append("\n      {");
    gold.append("\n        var h={};");
    gold.append("\n        h.__dom=g;");
    gold.append("\n        var i=(function() {");
    gold.append("\n          $.FV(this.__dom,'required',$.B($.F(this,'required')));");
    gold.append("\n        }).bind(h);");
    gold.append("\n        $.Y($.pV(f),h,'required',i);");
    gold.append("\n        i();");
    gold.append("\n      }");
    gold.append("\n      b.append(g);");
    gold.append("\n");
    gold.append("\n      // <view-write path=\"sum\" value=\"{view:value_a}_{view:value_b}\">");
    gold.append("\n      var g=$.RX(['value']);");
    gold.append("\n      $.Y2($.pV(f),g,'value','value_a',function(h) {");
    gold.append("\n        g.value=$.F(h,'value_a') + \"_\" + $.F(h,'value_b')");
    gold.append("\n        g.__();");
    gold.append("\n      });");
    gold.append("\n      $.Y2($.pV(f),g,'value','value_b',function(h) {");
    gold.append("\n        g.value=$.F(h,'value_a') + \"_\" + $.F(h,'value_b')");
    gold.append("\n        g.__();");
    gold.append("\n      });");
    gold.append("\n      $.VW($.pV(f),'sum',g);");
    gold.append("\n      b.append($.L($.pV(f),'sum'));");
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
    gold.append("\n    // <connection name=\"myname\" use-domain=\"\" redirect=\"/sign-in\">");
    gold.append("\n    var d=$.RX([]);");
    gold.append("\n    d.name='myname';");
    gold.append("\n    d.identity=true;");
    gold.append("\n    d.redirect='/sign-in';");
    gold.append("\n    $.DCONNECT(a,d);");
    gold.append("\n    d.__();");
    gold.append("\n");
    gold.append("\n    // <connection name=\"myname\" use-domain=\"\" redirect=\"/sign-in\">");
    gold.append("\n    var e=$.RX([]);");
    gold.append("\n    e.name='myname';");
    gold.append("\n    $.P(b,a,e,function(b,f) {");
    gold.append("\n");
    gold.append("\n      // <input type=\"text\" rx:sync=\"value_a\">");
    gold.append("\n      var g=$.E('input');");
    gold.append("\n      $.SA(g,'type',\"text\");");
    gold.append("\n      b.append(g);");
    gold.append("\n      $.SY(g,$.pV(f),'value_a',100.0);");
    gold.append("\n");
    gold.append("\n      // <input type=\"text\" rx:sync=\"value_b\">");
    gold.append("\n      var g=$.E('input');");
    gold.append("\n      $.SA(g,'type',\"text\");");
    gold.append("\n      b.append(g);");
    gold.append("\n      $.SY(g,$.pV(f),'value_b',100.0);");
    gold.append("\n");
    gold.append("\n      // <input type=\"text\" disabled=\"{view:disable}\">");
    gold.append("\n      var g=$.E('input');");
    gold.append("\n      $.SA(g,'type',\"text\");");
    gold.append("\n      {");
    gold.append("\n        var h={};");
    gold.append("\n        h.__dom=g;");
    gold.append("\n        var i=(function() {");
    gold.append("\n          $.FV(this.__dom,'disabled',$.B($.F(this,'disable')));");
    gold.append("\n        }).bind(h);");
    gold.append("\n        $.Y($.pV(f),h,'disable',i);");
    gold.append("\n        i();");
    gold.append("\n      }");
    gold.append("\n      b.append(g);");
    gold.append("\n");
    gold.append("\n      // <input type=\"text\" disabled=\"true\">");
    gold.append("\n      var g=$.E('input');");
    gold.append("\n      $.SA(g,'type',\"text\");");
    gold.append("\n      $.FV(g,'disabled',true);");
    gold.append("\n      b.append(g);");
    gold.append("\n");
    gold.append("\n      // <input type=\"text\" disabled=\"false\">");
    gold.append("\n      var g=$.E('input');");
    gold.append("\n      $.SA(g,'type',\"text\");");
    gold.append("\n      $.FV(g,'disabled',false);");
    gold.append("\n      b.append(g);");
    gold.append("\n");
    gold.append("\n      // <input type=\"text\" disabled=\"{view:disable}\" required=\"{view:required}\">");
    gold.append("\n      var g=$.E('input');");
    gold.append("\n      $.SA(g,'type',\"text\");");
    gold.append("\n      {");
    gold.append("\n        var h={};");
    gold.append("\n        h.__dom=g;");
    gold.append("\n        var i=(function() {");
    gold.append("\n          $.FV(this.__dom,'disabled',$.B($.F(this,'disable')));");
    gold.append("\n        }).bind(h);");
    gold.append("\n        $.Y($.pV(f),h,'disable',i);");
    gold.append("\n        i();");
    gold.append("\n      }");
    gold.append("\n      {");
    gold.append("\n        var h={};");
    gold.append("\n        h.__dom=g;");
    gold.append("\n        var i=(function() {");
    gold.append("\n          $.FV(this.__dom,'required',$.B($.F(this,'required')));");
    gold.append("\n        }).bind(h);");
    gold.append("\n        $.Y($.pV(f),h,'required',i);");
    gold.append("\n        i();");
    gold.append("\n      }");
    gold.append("\n      b.append(g);");
    gold.append("\n");
    gold.append("\n      // <view-write path=\"sum\" value=\"{view:value_a}_{view:value_b}\">");
    gold.append("\n      var g=$.RX(['value']);");
    gold.append("\n      $.Y2($.pV(f),g,'value','value_a',function(h) {");
    gold.append("\n        g.value=$.F(h,'value_a') + \"_\" + $.F(h,'value_b')");
    gold.append("\n        g.__();");
    gold.append("\n      });");
    gold.append("\n      $.Y2($.pV(f),g,'value','value_b',function(h) {");
    gold.append("\n        g.value=$.F(h,'value_a') + \"_\" + $.F(h,'value_b')");
    gold.append("\n        g.__();");
    gold.append("\n      });");
    gold.append("\n      $.VW($.pV(f),'sum',g);");
    gold.append("\n      b.append($.L($.pV(f),'sum'));");
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
    source.append("\n    <page uri=\"/\">");
    source.append("\n        <connection name=\"myname\" use-domain>");
    source.append("\n            <input type=\"text\" rx:sync=\"value_a\" />");
    source.append("\n            <input type=\"text\" rx:sync=\"value_b\" />");
    source.append("\n            <input type=\"text\" disabled=\"{view:disable}\" />");
    source.append("\n            <input type=\"text\" disabled=\"true\" />");
    source.append("\n            <input type=\"text\" disabled=\"false\" />");
    source.append("\n            <input type=\"text\" disabled=\"{view:disable}\" required=\"{view:required}\"/>");
    source.append("\n            <view-write path=\"sum\" value=\"{view:value_a}_{view:value_b}\" />");
    source.append("\n            <lookup path=\"view:sum\" />");
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
    gold.append("\n    \"disable\" : \"value\",");
    gold.append("\n    \"required\" : \"value\",");
    gold.append("\n    \"sum\" : \"lookup\",");
    gold.append("\n    \"value_a\" : \"value\",");
    gold.append("\n    \"value_b\" : \"value\"");
    gold.append("\n  }");
    gold.append("\n}");
    return gold.toString();
  }
}
