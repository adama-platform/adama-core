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

public class TemplateMultipageTests extends BaseRxHtmlTest {
  @Override
  public boolean dev() {
    return false;
  }
  @Override
  public String issues() {
    StringBuilder issues = new StringBuilder();
    issues.append("WARNING:page has duplicate path of '/$text'");
    return issues.toString();
  }
  @Override
  public String gold() {
    StringBuilder gold = new StringBuilder();
    gold.append("JavaScript:(function($){");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/\" authenticate=\"\">");
    gold.append("\n  $.PG(['fixed',''], function(b,a) {");
    gold.append("\n    var c=$.aRDz('/login');");
    gold.append("\n    if($.ID('default',c).abort) {");
    gold.append("\n      return;");
    gold.append("\n    }");
    gold.append("\n    var c=$.X();");
    gold.append("\n    b.append($.T(' Simple Page '));");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/$user:text\" authenticate=\"\">");
    gold.append("\n  $.PG(['text','user'], function(b,a) {");
    gold.append("\n    var d=$.aRDz('/login');");
    gold.append("\n    if($.ID('default',d).abort) {");
    gold.append("\n      return;");
    gold.append("\n    }");
    gold.append("\n    var d=$.X();");
    gold.append("\n    b.append($.T(' Text type user profile '));");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/$usery:string\" authenticate=\"\">");
    gold.append("\n  $.PG(['text','usery'], function(b,a) {");
    gold.append("\n    var e=$.aRDz('/login');");
    gold.append("\n    if($.ID('default',e).abort) {");
    gold.append("\n      return;");
    gold.append("\n    }");
    gold.append("\n    var e=$.X();");
    gold.append("\n    b.append($.T(' Text type user profile '));");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/$user/profile\" authenticate=\"\">");
    gold.append("\n  $.PG(['text','user','fixed','profile'], function(b,a) {");
    gold.append("\n    var f=$.aRDz('/login');");
    gold.append("\n    if($.ID('default',f).abort) {");
    gold.append("\n      return;");
    gold.append("\n    }");
    gold.append("\n    var f=$.X();");
    gold.append("\n    b.append($.T(' No type '));");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/$user/settings/$key\" authenticate=\"\">");
    gold.append("\n  $.PG(['text','user','fixed','settings','text','key'], function(b,a) {");
    gold.append("\n    var g=$.aRDz('/login');");
    gold.append("\n    if($.ID('default',g).abort) {");
    gold.append("\n      return;");
    gold.append("\n    }");
    gold.append("\n    var g=$.X();");
    gold.append("\n    b.append($.T(' Keys for a user '));");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/$user/sets1/$key:number\" authenticate=\"\">");
    gold.append("\n  $.PG(['text','user','fixed','sets1','number','key'], function(b,a) {");
    gold.append("\n    var h=$.aRDz('/login');");
    gold.append("\n    if($.ID('default',h).abort) {");
    gold.append("\n      return;");
    gold.append("\n    }");
    gold.append("\n    var h=$.X();");
    gold.append("\n    b.append($.T(' Keys for a user '));");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/$user/sets2/$key:int\" authenticate=\"\">");
    gold.append("\n  $.PG(['text','user','fixed','sets2','number','key'], function(b,a) {");
    gold.append("\n    var i=$.aRDz('/login');");
    gold.append("\n    if($.ID('default',i).abort) {");
    gold.append("\n      return;");
    gold.append("\n    }");
    gold.append("\n    var i=$.X();");
    gold.append("\n    b.append($.T(' Keys for a user '));");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/$user/sets3/$key:double\" authenticate=\"\">");
    gold.append("\n  $.PG(['text','user','fixed','sets3','number','key'], function(b,a) {");
    gold.append("\n    var j=$.aRDz('/login');");
    gold.append("\n    if($.ID('default',j).abort) {");
    gold.append("\n      return;");
    gold.append("\n    }");
    gold.append("\n    var j=$.X();");
    gold.append("\n    b.append($.T(' Keys for a user '));");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/yolo\" authenticate=\"\" redirect=\"/bounce\">");
    gold.append("\n  $.PG(['fixed','yolo'], function(b,a) {");
    gold.append("\n    var k=$.aRDz('/bounce');");
    gold.append("\n    if($.ID('default',k).abort) {");
    gold.append("\n      return;");
    gold.append("\n    }");
    gold.append("\n    var k=$.X();");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/bounce\" authenticate=\"\">");
    gold.append("\n  $.PG(['fixed','bounce'], function(b,a) {");
    gold.append("\n    var l=$.aRDz('/login');");
    gold.append("\n    if($.ID('default',l).abort) {");
    gold.append("\n      return;");
    gold.append("\n    }");
    gold.append("\n    var l=$.X();");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/login\" default-redirect-source=\"\">");
    gold.append("\n  $.PG(['fixed','login'], function(b,a) {");
    gold.append("\n    var m=$.X();");
    gold.append("\n    b.append($.T(' Simple Page '));");
    gold.append("\n  });");
    gold.append("\n})(RxHTML);");
    gold.append("\nStyle:");
    gold.append("\nShell:<!DOCTYPE html>");
    gold.append("\n<html>");
    gold.append("\n<head><script src=\"/libadama.js/GENMODE.js\"></script><script>");
    gold.append("\n");
    gold.append("\n(function($){");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/\" authenticate=\"\">");
    gold.append("\n  $.PG(['fixed',''], function(b,a) {");
    gold.append("\n    var c=$.aRDz('/login');");
    gold.append("\n    if($.ID('default',c).abort) {");
    gold.append("\n      return;");
    gold.append("\n    }");
    gold.append("\n    var c=$.X();");
    gold.append("\n    b.append($.T(' Simple Page '));");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/$user:text\" authenticate=\"\">");
    gold.append("\n  $.PG(['text','user'], function(b,a) {");
    gold.append("\n    var d=$.aRDz('/login');");
    gold.append("\n    if($.ID('default',d).abort) {");
    gold.append("\n      return;");
    gold.append("\n    }");
    gold.append("\n    var d=$.X();");
    gold.append("\n    b.append($.T(' Text type user profile '));");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/$usery:string\" authenticate=\"\">");
    gold.append("\n  $.PG(['text','usery'], function(b,a) {");
    gold.append("\n    var e=$.aRDz('/login');");
    gold.append("\n    if($.ID('default',e).abort) {");
    gold.append("\n      return;");
    gold.append("\n    }");
    gold.append("\n    var e=$.X();");
    gold.append("\n    b.append($.T(' Text type user profile '));");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/$user/profile\" authenticate=\"\">");
    gold.append("\n  $.PG(['text','user','fixed','profile'], function(b,a) {");
    gold.append("\n    var f=$.aRDz('/login');");
    gold.append("\n    if($.ID('default',f).abort) {");
    gold.append("\n      return;");
    gold.append("\n    }");
    gold.append("\n    var f=$.X();");
    gold.append("\n    b.append($.T(' No type '));");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/$user/settings/$key\" authenticate=\"\">");
    gold.append("\n  $.PG(['text','user','fixed','settings','text','key'], function(b,a) {");
    gold.append("\n    var g=$.aRDz('/login');");
    gold.append("\n    if($.ID('default',g).abort) {");
    gold.append("\n      return;");
    gold.append("\n    }");
    gold.append("\n    var g=$.X();");
    gold.append("\n    b.append($.T(' Keys for a user '));");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/$user/sets1/$key:number\" authenticate=\"\">");
    gold.append("\n  $.PG(['text','user','fixed','sets1','number','key'], function(b,a) {");
    gold.append("\n    var h=$.aRDz('/login');");
    gold.append("\n    if($.ID('default',h).abort) {");
    gold.append("\n      return;");
    gold.append("\n    }");
    gold.append("\n    var h=$.X();");
    gold.append("\n    b.append($.T(' Keys for a user '));");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/$user/sets2/$key:int\" authenticate=\"\">");
    gold.append("\n  $.PG(['text','user','fixed','sets2','number','key'], function(b,a) {");
    gold.append("\n    var i=$.aRDz('/login');");
    gold.append("\n    if($.ID('default',i).abort) {");
    gold.append("\n      return;");
    gold.append("\n    }");
    gold.append("\n    var i=$.X();");
    gold.append("\n    b.append($.T(' Keys for a user '));");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/$user/sets3/$key:double\" authenticate=\"\">");
    gold.append("\n  $.PG(['text','user','fixed','sets3','number','key'], function(b,a) {");
    gold.append("\n    var j=$.aRDz('/login');");
    gold.append("\n    if($.ID('default',j).abort) {");
    gold.append("\n      return;");
    gold.append("\n    }");
    gold.append("\n    var j=$.X();");
    gold.append("\n    b.append($.T(' Keys for a user '));");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/yolo\" authenticate=\"\" redirect=\"/bounce\">");
    gold.append("\n  $.PG(['fixed','yolo'], function(b,a) {");
    gold.append("\n    var k=$.aRDz('/bounce');");
    gold.append("\n    if($.ID('default',k).abort) {");
    gold.append("\n      return;");
    gold.append("\n    }");
    gold.append("\n    var k=$.X();");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/bounce\" authenticate=\"\">");
    gold.append("\n  $.PG(['fixed','bounce'], function(b,a) {");
    gold.append("\n    var l=$.aRDz('/login');");
    gold.append("\n    if($.ID('default',l).abort) {");
    gold.append("\n      return;");
    gold.append("\n    }");
    gold.append("\n    var l=$.X();");
    gold.append("\n  });");
    gold.append("\n");
    gold.append("\n  // <page uri=\"/login\" default-redirect-source=\"\">");
    gold.append("\n  $.PG(['fixed','login'], function(b,a) {");
    gold.append("\n    var m=$.X();");
    gold.append("\n    b.append($.T(' Simple Page '));");
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
    source.append("\n    <page uri=\"/\" authenticate>");
    source.append("\n        Simple Page");
    source.append("\n    </page>");
    source.append("\n    <page uri=\"/$user:text\" authenticate>");
    source.append("\n        Text type user profile");
    source.append("\n    </page>");
    source.append("\n    <page uri=\"/$usery:string\" authenticate>");
    source.append("\n        Text type user profile");
    source.append("\n    </page>");
    source.append("\n    <page uri=\"/$user/profile\" authenticate>");
    source.append("\n        No type");
    source.append("\n    </page>");
    source.append("\n    <page uri=\"/$user/settings/$key\" authenticate>");
    source.append("\n        Keys for a user");
    source.append("\n    </page>");
    source.append("\n    <page uri=\"/$user/sets1/$key:number\" authenticate>");
    source.append("\n        Keys for a user");
    source.append("\n    </page>");
    source.append("\n    <page uri=\"/$user/sets2/$key:int\" authenticate>");
    source.append("\n        Keys for a user");
    source.append("\n    </page>");
    source.append("\n    <page uri=\"/$user/sets3/$key:double\" authenticate>");
    source.append("\n        Keys for a user");
    source.append("\n    </page>");
    source.append("\n    <page uri=\"/yolo\" authenticate redirect=\"/bounce\">");
    source.append("\n");
    source.append("\n    </page>");
    source.append("\n    <page uri=\"/bounce\" authenticate>");
    source.append("\n");
    source.append("\n    </page>");
    source.append("\n    <page uri=\"/login\" default-redirect-source>");
    source.append("\n        Simple Page");
    source.append("\n    </page>");
    source.append("\n</forest>");
    return source.toString();
  }
  @Override
  public String schema() {
    StringBuilder gold = new StringBuilder();
    gold.append("{");
    gold.append("\n  \"/\" : { },");
    gold.append("\n  \"/$text\" : {");
    gold.append("\n    \"usery\" : \"text\"");
    gold.append("\n  },");
    gold.append("\n  \"/$text/profile\" : {");
    gold.append("\n    \"user\" : \"text\"");
    gold.append("\n  },");
    gold.append("\n  \"/$text/settings/$text\" : {");
    gold.append("\n    \"key\" : \"text\",");
    gold.append("\n    \"user\" : \"text\"");
    gold.append("\n  },");
    gold.append("\n  \"/$text/sets1/$number\" : {");
    gold.append("\n    \"key\" : \"number\",");
    gold.append("\n    \"user\" : \"text\"");
    gold.append("\n  },");
    gold.append("\n  \"/$text/sets2/$number\" : {");
    gold.append("\n    \"key\" : \"number\",");
    gold.append("\n    \"user\" : \"text\"");
    gold.append("\n  },");
    gold.append("\n  \"/$text/sets3/$number\" : {");
    gold.append("\n    \"key\" : \"number\",");
    gold.append("\n    \"user\" : \"text\"");
    gold.append("\n  },");
    gold.append("\n  \"/yolo\" : { },");
    gold.append("\n  \"/bounce\" : { },");
    gold.append("\n  \"/login\" : { }");
    gold.append("\n}");
    return gold.toString();
  }
}
