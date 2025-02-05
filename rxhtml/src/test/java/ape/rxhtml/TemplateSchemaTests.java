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

public class TemplateSchemaTests extends BaseRxHtmlTest {
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
    gold.append("\n  // <template name=\"foo\">");
    gold.append("\n  $.TP('foo', function(a,b,c,d) {");
    gold.append("\n    var e=$.X();");
    gold.append("\n");
    gold.append("\n    // <button rx:click=\"set:y=3\">");
    gold.append("\n    var f=$.E('button');");
    gold.append("\n    var g=[];");
    gold.append("\n    g.push($.bS(f,$.pV(b),'y',3));");
    gold.append("\n    $.onB(f,'click',b,g);");
    gold.append("\n    f.append($.T('Set Y'));");
    gold.append("\n    a.append(f);");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <template name=\"foo_scope\">");
    gold.append("\n  $.TP('foo_scope', function(a,b,c,f) {");
    gold.append("\n    var h=$.X();");
    gold.append("\n");
    gold.append("\n    // <div rx:scope=\"fooscope\">");
    gold.append("\n    var i=$.E('div');");
    gold.append("\n    var j=$.pI(b,'fooscope');");
    gold.append("\n    c(i,j,'');");
    gold.append("\n    a.append(i);");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/should_be_canonical_test_for_schema_gen\">");
    gold.append("\n  $.PG(['fixed','should_be_canonical_test_for_schema_gen'], function(b,a) {");
    gold.append("\n    var c=$.X();");
    gold.append("\n");
    gold.append("\n    // <div rx:scope=\"child\" rx:expand-view-state=\"\">");
    gold.append("\n    var i=$.E('div');");
    gold.append("\n    var k=$.pIE(a,'child', true);");
    gold.append("\n");
    gold.append("\n    // <button rx:click=\"set:x=1\">");
    gold.append("\n    var l=$.E('button');");
    gold.append("\n    var m=[];");
    gold.append("\n    m.push($.bS(l,$.pV(k),'x',1));");
    gold.append("\n    $.onB(l,'click',k,m);");
    gold.append("\n    l.append($.T('Button In Child'));");
    gold.append("\n    i.append(l);");
    gold.append("\n");
    gold.append("\n    // <div rx:template=\"foo\">");
    gold.append("\n    var l=$.E('div');");
    gold.append("\n    $.UT(l,k,'foo', function(n,o,p) {");
    gold.append("\n      n.append($.T(' Fragment 1 '));");
    gold.append("\n    },{});");
    gold.append("\n    i.append(l);");
    gold.append("\n    b.append(i);");
    gold.append("\n");
    gold.append("\n    // <div rx:iterate=\"children\" rx:expand-view-state=\"\">");
    gold.append("\n    var i=$.E('div');");
    gold.append("\n    $.IT(i,a,'children',true,function(l) {");
    gold.append("\n");
    gold.append("\n      // <div>");
    gold.append("\n      var n=$.E('div');");
    gold.append("\n");
    gold.append("\n      // <button rx:click=\"set:z=x\">");
    gold.append("\n      var o=$.E('button');");
    gold.append("\n      var p=[];");
    gold.append("\n      p.push($.bS(o,$.pV(l),'z','x'));");
    gold.append("\n      $.onB(o,'click',l,p);");
    gold.append("\n      o.append($.T('Button In Children'));");
    gold.append("\n      n.append(o);");
    gold.append("\n");
    gold.append("\n      // <div rx:template=\"foo\">");
    gold.append("\n      var o=$.E('div');");
    gold.append("\n      $.UT(o,l,'foo', function(q,r,s) {");
    gold.append("\n        q.append($.T(' Fragment 2 '));");
    gold.append("\n      },{});");
    gold.append("\n      n.append(o);");
    gold.append("\n      return n;");
    gold.append("\n    },false);");
    gold.append("\n    b.append(i);");
    gold.append("\n    b.append($.L(a,'should_no_be_in_view'));");
    gold.append("\n    b.append($.L($.pV(a),'w'));");
    gold.append("\n");
    gold.append("\n    // <button rx:click=\"set:w=1\">");
    gold.append("\n    var i=$.E('button');");
    gold.append("\n    var l=[];");
    gold.append("\n    l.push($.bS(i,$.pV(a),'w',1));");
    gold.append("\n    $.onB(i,'click',a,l);");
    gold.append("\n    i.append($.T('Button Root'));");
    gold.append("\n    b.append(i);");
    gold.append("\n");
    gold.append("\n    // <div rx:template=\"foo\">");
    gold.append("\n    var i=$.E('div');");
    gold.append("\n    $.UT(i,a,'foo', function(n,o,q) {");
    gold.append("\n      n.append($.T(' Fragment 3 '));");
    gold.append("\n    },{});");
    gold.append("\n    b.append(i);");
    gold.append("\n");
    gold.append("\n    // <div rx:template=\"foo_scope\">");
    gold.append("\n    var i=$.E('div');");
    gold.append("\n    $.UT(i,a,'foo_scope', function(n,o,q) {");
    gold.append("\n");
    gold.append("\n      // <button rx:click=\"set:v=1\">");
    gold.append("\n      var r=$.E('button');");
    gold.append("\n      var s=[];");
    gold.append("\n      s.push($.bS(r,$.pV(o),'v',1));");
    gold.append("\n      $.onB(r,'click',o,s);");
    gold.append("\n      r.append($.T('Fragment Button'));");
    gold.append("\n      n.append(r);");
    gold.append("\n    },{});");
    gold.append("\n    b.append(i);");
    gold.append("\n  });");
    gold.append("\n})(RxHTML);");
    gold.append("\nStyle:");
    gold.append("\nShell:<!DOCTYPE html>");
    gold.append("\n<html>");
    gold.append("\n<head><script src=\"/libadama.js/GENMODE.js\"></script><script>");
    gold.append("\n");
    gold.append("\n(function($){");
    gold.append("\n");
    gold.append("\n  // <template name=\"foo\">");
    gold.append("\n  $.TP('foo', function(a,b,c,d) {");
    gold.append("\n    var e=$.X();");
    gold.append("\n");
    gold.append("\n    // <button rx:click=\"set:y=3\">");
    gold.append("\n    var f=$.E('button');");
    gold.append("\n    var g=[];");
    gold.append("\n    g.push($.bS(f,$.pV(b),'y',3));");
    gold.append("\n    $.onB(f,'click',b,g);");
    gold.append("\n    f.append($.T('Set Y'));");
    gold.append("\n    a.append(f);");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <template name=\"foo_scope\">");
    gold.append("\n  $.TP('foo_scope', function(a,b,c,f) {");
    gold.append("\n    var h=$.X();");
    gold.append("\n");
    gold.append("\n    // <div rx:scope=\"fooscope\">");
    gold.append("\n    var i=$.E('div');");
    gold.append("\n    var j=$.pI(b,'fooscope');");
    gold.append("\n    c(i,j,'');");
    gold.append("\n    a.append(i);");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/should_be_canonical_test_for_schema_gen\">");
    gold.append("\n  $.PG(['fixed','should_be_canonical_test_for_schema_gen'], function(b,a) {");
    gold.append("\n    var c=$.X();");
    gold.append("\n");
    gold.append("\n    // <div rx:scope=\"child\" rx:expand-view-state=\"\">");
    gold.append("\n    var i=$.E('div');");
    gold.append("\n    var k=$.pIE(a,'child', true);");
    gold.append("\n");
    gold.append("\n    // <button rx:click=\"set:x=1\">");
    gold.append("\n    var l=$.E('button');");
    gold.append("\n    var m=[];");
    gold.append("\n    m.push($.bS(l,$.pV(k),'x',1));");
    gold.append("\n    $.onB(l,'click',k,m);");
    gold.append("\n    l.append($.T('Button In Child'));");
    gold.append("\n    i.append(l);");
    gold.append("\n");
    gold.append("\n    // <div rx:template=\"foo\">");
    gold.append("\n    var l=$.E('div');");
    gold.append("\n    $.UT(l,k,'foo', function(n,o,p) {");
    gold.append("\n      n.append($.T(' Fragment 1 '));");
    gold.append("\n    },{});");
    gold.append("\n    i.append(l);");
    gold.append("\n    b.append(i);");
    gold.append("\n");
    gold.append("\n    // <div rx:iterate=\"children\" rx:expand-view-state=\"\">");
    gold.append("\n    var i=$.E('div');");
    gold.append("\n    $.IT(i,a,'children',true,function(l) {");
    gold.append("\n");
    gold.append("\n      // <div>");
    gold.append("\n      var n=$.E('div');");
    gold.append("\n");
    gold.append("\n      // <button rx:click=\"set:z=x\">");
    gold.append("\n      var o=$.E('button');");
    gold.append("\n      var p=[];");
    gold.append("\n      p.push($.bS(o,$.pV(l),'z','x'));");
    gold.append("\n      $.onB(o,'click',l,p);");
    gold.append("\n      o.append($.T('Button In Children'));");
    gold.append("\n      n.append(o);");
    gold.append("\n");
    gold.append("\n      // <div rx:template=\"foo\">");
    gold.append("\n      var o=$.E('div');");
    gold.append("\n      $.UT(o,l,'foo', function(q,r,s) {");
    gold.append("\n        q.append($.T(' Fragment 2 '));");
    gold.append("\n      },{});");
    gold.append("\n      n.append(o);");
    gold.append("\n      return n;");
    gold.append("\n    },false);");
    gold.append("\n    b.append(i);");
    gold.append("\n    b.append($.L(a,'should_no_be_in_view'));");
    gold.append("\n    b.append($.L($.pV(a),'w'));");
    gold.append("\n");
    gold.append("\n    // <button rx:click=\"set:w=1\">");
    gold.append("\n    var i=$.E('button');");
    gold.append("\n    var l=[];");
    gold.append("\n    l.push($.bS(i,$.pV(a),'w',1));");
    gold.append("\n    $.onB(i,'click',a,l);");
    gold.append("\n    i.append($.T('Button Root'));");
    gold.append("\n    b.append(i);");
    gold.append("\n");
    gold.append("\n    // <div rx:template=\"foo\">");
    gold.append("\n    var i=$.E('div');");
    gold.append("\n    $.UT(i,a,'foo', function(n,o,q) {");
    gold.append("\n      n.append($.T(' Fragment 3 '));");
    gold.append("\n    },{});");
    gold.append("\n    b.append(i);");
    gold.append("\n");
    gold.append("\n    // <div rx:template=\"foo_scope\">");
    gold.append("\n    var i=$.E('div');");
    gold.append("\n    $.UT(i,a,'foo_scope', function(n,o,q) {");
    gold.append("\n");
    gold.append("\n      // <button rx:click=\"set:v=1\">");
    gold.append("\n      var r=$.E('button');");
    gold.append("\n      var s=[];");
    gold.append("\n      s.push($.bS(r,$.pV(o),'v',1));");
    gold.append("\n      $.onB(r,'click',o,s);");
    gold.append("\n      r.append($.T('Fragment Button'));");
    gold.append("\n      n.append(r);");
    gold.append("\n    },{});");
    gold.append("\n    b.append(i);");
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
    source.append("\n    <page uri=\"/should_be_canonical_test_for_schema_gen\">");
    source.append("\n        <div rx:scope=\"child\" rx:expand-view-state>");
    source.append("\n            <button rx:click=\"set:x=1\">Button In Child</button>");
    source.append("\n            <div rx:template=\"foo\"> Fragment 1 </div>");
    source.append("\n        </div>");
    source.append("\n        <div rx:iterate=\"children\" rx:expand-view-state>");
    source.append("\n            <button rx:click=\"set:z=x\">Button In Children</button>");
    source.append("\n            <div rx:template=\"foo\"> Fragment 2 </div>");
    source.append("\n        </div>");
    source.append("\n        <lookup path=\"should_no_be_in_view\" />");
    source.append("\n        <lookup path=\"view:w\" />");
    source.append("\n        <button rx:click=\"set:w=1\">Button Root</button>");
    source.append("\n        <div rx:template=\"foo\"> Fragment 3 </div>");
    source.append("\n        <div rx:template=\"foo_scope\">");
    source.append("\n            <button rx:click=\"set:v=1\">Fragment Button</button>");
    source.append("\n        </div>");
    source.append("\n    </page>");
    source.append("\n    <template name=\"foo\">");
    source.append("\n        <button rx:click=\"set:y=3\">Set Y</button>");
    source.append("\n    </template>");
    source.append("\n    <template name=\"foo_scope\">");
    source.append("\n        <div rx:scope=\"fooscope\">");
    source.append("\n            <fragment />");
    source.append("\n        </div>");
    source.append("\n    </template>");
    source.append("\n</forest>");
    return source.toString();
  }
  @Override
  public String schema() {
    StringBuilder gold = new StringBuilder();
    gold.append("{");
    gold.append("\n  \"/should_be_canonical_test_for_schema_gen\" : {");
    gold.append("\n    \"child\" : {");
    gold.append("\n      \"x\" : \"int\",");
    gold.append("\n      \"y\" : \"int\"");
    gold.append("\n    },");
    gold.append("\n    \"children\" : {");
    gold.append("\n      \"#items\" : {");
    gold.append("\n        \"y\" : \"int\",");
    gold.append("\n        \"z\" : \"string\"");
    gold.append("\n      }");
    gold.append("\n    },");
    gold.append("\n    \"v\" : \"int\",");
    gold.append("\n    \"w\" : \"int\",");
    gold.append("\n    \"y\" : \"int\"");
    gold.append("\n  }");
    gold.append("\n}");
    return gold.toString();
  }
}
