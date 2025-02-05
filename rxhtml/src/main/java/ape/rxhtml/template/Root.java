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
package ape.rxhtml.template;

import ape.rxhtml.atl.ParseException;
import ape.rxhtml.atl.Parser;
import ape.rxhtml.atl.tree.Tree;
import ape.rxhtml.routing.Instructions;
import ape.rxhtml.template.config.Feedback;
import org.jsoup.nodes.Attribute;

import java.util.ArrayList;
import java.util.Map;

public class Root {
  public static void start(Environment env, String custom) {
    env.writer.append("(function($){").tabUp().newline();
    if (custom.length() > 0) {
      env.writer.append("/** BEGIN CUSTOM **/").newline();
      env.writer.append(custom).newline();
      env.writer.append("/** END CUSTOM **/").newline();
    }
  }

  public static void template(Environment env) {
    String parentVar = env.pool.ask();
    String stateVar = env.pool.ask();
    String fragmentFunc = env.pool.ask();
    String configVar = env.pool.ask();
    String name = env.element.attr("name");
    env.writeElementDebugIfTest();
    env.writer.tab().append("$.TP('").append(name).append("', function(").append(parentVar).append(",").append(stateVar).append(",").append(fragmentFunc).append(",").append(configVar).append(") {").newline().tabUp();
    String autoVar = env.pool.ask();
    env.writer.tab().append("var ").append(autoVar).append("=$.X();").newline();
    Feedback feedback = env.feedback;
    Base.children(env.stateVar(stateVar).parentVariable(parentVar).fragmentFunc(fragmentFunc).feedback("template " + name, (e, msg) -> feedback.warn(e, "template " + name + ":" + msg)).autoVar(autoVar).configVar(configVar));
    env.pool.give(parentVar);
    env.pool.give(stateVar);
    env.pool.give(fragmentFunc);
    env.writer.tabDown().tab().append("});").newline();
  }

  public static void page(Environment env, ArrayList<String> defaultRedirects) {
    String stateVar = env.pool.ask();
    String rootVar = env.pool.ask();
    Environment envToUse = env.parentVariable(rootVar).stateVar(stateVar).raiseOptimize();
    String uri = env.element.attr("uri");
    Instructions instructions = Instructions.parse(uri);
    env.writeElementDebugIfTest();
    env.writer.tab().append("$.PG(").append(instructions.javascript).append(", function(").append(rootVar).append(",").append(stateVar).append(") {").newline().tabUp();
    for (Attribute attr : env.element.attributes()) {
      if (attr.getKey().startsWith("init:")) {
        String key = attr.getKey().substring(5);
        String value = attr.getValue();
        env.writer.tab().append(stateVar).append(".view.init['").append(key).append("']=").append(Escapes.constantOf(value)).append(";").newline();
      }
    }
    if (env.element.hasAttr("authenticate")) {
      String identity = env.element.attr("authenticate");
      if (identity == null || identity.trim().equals("")) {
        identity = "default";
      }
      ArrayList<String> failAuthRedirects = new ArrayList<>(defaultRedirects);
      if (env.element.hasAttr("redirect")) {
        String redirectTo = env.element.attr("redirect");
        if (redirectTo != null && !redirectTo.trim().equals("")) {
          failAuthRedirects.add(redirectTo);
        }
      }
      String varForAuthTest = env.pool.ask();
      String zeroArgs = "$.aRDz('/');";
      String pullArgs = null;
      for (String redirect : failAuthRedirects) {
        // parse the redirect
        try {
          Tree tree = Parser.parse(redirect);
          Map<String, String> vars = tree.variables();
          if (tree.hasAuto()) {
            env.feedback.warn(env.element, "redirects can't use auto variable");
          }
          if (vars.size() == 0) {
            zeroArgs = "$.aRDz('" + redirect + "');";
          } else {
            boolean hasAll = true;
            ArrayList<String> varsToPullFromView = new ArrayList<>();
            for (String key : vars.keySet()) {
              varsToPullFromView.add("'" + key + "'");
              if (!instructions.depends.contains(key)) {
                hasAll = false;
                break;
              }
            }
            if (hasAll) {
              pullArgs = "$.aRDp(" + stateVar + ",function(vs) { return " + tree.js(env.contextOf("initial-view-state"), "vs") + ";});";
            }
          }
        } catch (ParseException pe) {
          env.feedback.warn(env.element, "redirect '" + redirect + "' has parser problems; " + pe.getMessage());
        }
      }
      env.writer.tab().append("var ").append(varForAuthTest).append("=");
      if (pullArgs != null) {
        env.writer.append(pullArgs).newline();
      } else {
        env.writer.append(zeroArgs).newline();
      }
      env.writer.tab().append("if($.ID('").append(identity).append("',").append(varForAuthTest).append(").abort) {").tabUp().newline();
      env.writer.tab().append("return;").tabDown().newline();
      env.writer.tab().append("}").newline();
      env.pool.give(varForAuthTest);
    }
    String autoVar = env.pool.ask();
    env.writer.tab().append("var ").append(autoVar).append("=$.X();").newline();
    Feedback prior = env.feedback;
    Base.children(envToUse.feedback("page:" + uri, (e, msg) -> prior.warn(e, uri + ":" + msg)).autoVar(autoVar));
    env.writer.tabDown().tab().append("});").newline();
    env.pool.give(rootVar);
    env.pool.give(stateVar);
  }

  public static String finish(Environment env) {
    env.writer.tabDown().tab().append("})(RxHTML);").newline();
    return env.writer.toString();
  }

}
