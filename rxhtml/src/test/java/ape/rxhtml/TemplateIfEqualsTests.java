/*
* Adama Platform and Language
* Copyright (C) 2021 - 2025 by Adama Platform Engineering, LLC
* 
* This program is free software for non-commercial purposes: 
* you can redistribute it and/or modify it under the terms of the 
* GNU Affero General Public License as published by the Free Software Foundation,
* either version 3 of the License, or (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
* 
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
