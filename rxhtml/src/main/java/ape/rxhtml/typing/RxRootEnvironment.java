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
package ape.rxhtml.typing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ape.common.Json;
import ape.rxhtml.Loader;
import ape.rxhtml.ProductionMode;
import ape.rxhtml.atl.ParseException;
import ape.rxhtml.atl.Parser;
import ape.rxhtml.atl.tree.Tree;
import ape.rxhtml.template.Base;
import ape.rxhtml.template.config.Feedback;
import org.jsoup.nodes.*;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/** The base environment for checking types */
public class RxRootEnvironment {
  public final Document document;
  public final Feedback feedback;
  public final HashMap<String, ObjectNode> reflections;
  public final HashSet<DedupeTemplateCheck> dedupeTemplates;
  public final String defaultBackend;
  public final HashMap<String, Element> templates;
  public final HashMap<String, HashSet<String>> templateUsageByPage;

  public RxRootEnvironment(String forest, File root, Feedback feedback) {
    this.document = Loader.parseForest(forest, feedback, ProductionMode.Web);
    this.feedback = feedback;
    this.reflections = new HashMap<>();
    this.dedupeTemplates = new HashSet<>();
    this.templateUsageByPage = new HashMap<>();
    String _defaultBackend = null;
    BiFunction<Element, String, Boolean> load = (element, name) -> {
      if (!reflections.containsKey(name)) {
        try {
          File fileToLoad = new File(root, name);
          if (!fileToLoad.exists()) {
            fileToLoad = new File(root, name + ".json");
          }
          String json = Files.readString(fileToLoad.toPath());
          reflections.put(name, Json.parseJsonObject(json));
          return true;
        } catch (Exception ex) {
          feedback.warn(element, "The input file for reflection '" + name + "' failed with " + ex.getMessage());
        }
      }
      return false;
    };
    for (Element element : this.document.getElementsByTag("shell")) {
      if (element.hasAttr("default-backend")) {
        String candidate = element.attr("default-backend");
        if (load.apply(element, candidate)) {
          _defaultBackend = candidate;
        }
      }
    }
    this.defaultBackend = _defaultBackend;
    for (Element element : this.document.getElementsByTag("connection")) {
      if (element.hasAttr("backend")) {
        load.apply(element, element.attr("backend"));
      }
    }
    for (Element element : this.document.getElementsByTag("domain-get")) {
      if (element.hasAttr("backend")) {
        load.apply(element, element.attr("backend"));
      }
    }
    this.templates = new HashMap<>();
    for (Element element : this.document.getElementsByTag("template")) {
      String name = element.attr("name");
      if (name != null) {
        if (templates.containsKey(name)) {
          feedback.warn(element, "template '" + name + "' already defined");
        }
        templates.put(name, element);
      }
    }
  }

  private void children(Element parent, PageEnvironment env) {
    for (Element child : parent.children()) {
      check(child, env);
    }
  }

  private void checkCondition(String condition, PageEnvironment env, Consumer<String> reportError) {
    Consumer<String> acceptChannel = (channel) -> {
      if (env.scope == null) {
        reportError.accept(condition + " has no data scope");
      } else {
        if (!env.scope.hasChannel(channel)) {
          reportError.accept(condition + " failed to find channel '" + channel + "'");
        }
      }
    };
    if (condition.startsWith("decide:") || condition.startsWith("choose:") || condition.startsWith("chosen:")) {
      acceptChannel.accept(condition.substring(7));
      return;
    }
    if (condition.startsWith("finalize:")) {
      acceptChannel.accept(condition.substring(9));
      return;
    }
    if (condition.startsWith("eval:")) {
      // TODO:
      return;
    }
    Consumer<String> checkBranch = (branch) -> {
      if (branch != null) {
        DataSelector selector = env.scope.select(env.privacy, branch, reportError);
        if (selector != null) {
          selector.validateAttribute(reportError);
        }
      }
    };

    int kEq = condition.indexOf('=');
    if (kEq > 0) {
      checkBranch.accept(condition.substring(0, kEq).trim());
      checkBranch.accept(condition.substring(kEq + 1).trim());
    } else {
      DataSelector selector = env.scope.select(env.privacy, condition, reportError);
      if (selector != null) {
        selector.validateBoolean(reportError);
      }
    }
  }

  private void _checkExpression(Element element, PageEnvironment env, String... attributes) {
    if (env.scope == null) {
      return;
    }
    Consumer<String> reportError = (err) -> feedback.warn(element, err);
    for (String attr : attributes) {
      if (element.attributes().hasDeclaredValueForKey(attr)) {
        String valueToCheck = element.attr(attr);
        try {
          Tree tree = Parser.parse(valueToCheck);
          for (String query : tree.queries()) {
            DataSelector selector = env.scope.select(env.privacy, query, reportError);
            if (selector != null) {
              selector.validateAttribute(reportError);
            }
          }
        } catch (ParseException pe) {
          reportError.accept("failed to parse ATL: " + valueToCheck);
        }
      }
    }
  }


  private void _checkPath(Element element, PageEnvironment env, String... attributes) {
    if (env.scope != null) {
      Consumer<String> reportError = (err) -> feedback.warn(element, err);
      for (String attr : attributes) {
        if (element.attributes().hasDeclaredValueForKey(attr)) {
          String pathToCheck = element.attr(attr);
          DataSelector selector = env.scope.select(env.privacy, pathToCheck, reportError);
          if (selector != null) {
            selector.validateAttribute(reportError);
          }
        }
      }
    }
  }

  private void base(Element element, PageEnvironment env) {
    PageEnvironment next = env;
    boolean expand = element.hasAttr("rx:expand-view-state");
    Consumer<String> reportError = (err) -> feedback.warn(element, err);

    BiFunction<PageEnvironment, String, String> findScopable = (e, key) -> {
      if (e.scope == null) {
        return null;
      }
      if (element.hasAttr(key)) {
        String value = element.attr(key).trim();
        if ("".equals(value)) {
          feedback.warn(element, "attribute '" + key + "' is present but empty");
          return null;
        }
        return value;
      }
      return null;
    };
    String toScopeInto = findScopable.apply(next, "rx:scope");
    if (toScopeInto != null) {
      DataSelector selector = next.scope.select(next.privacy, toScopeInto, reportError);
      if (selector != null) {
        DataScope scopedInto = selector.scopeInto(reportError);
        if (scopedInto != null) {
          next = next.withDataScope(scopedInto);
        }
      }
    }
    String toIterateInto = findScopable.apply(next, "rx:iterate");
    if (toIterateInto != null) {
      DataSelector selector = next.scope.select(next.privacy, toIterateInto, reportError);
      if (selector != null) {
        DataScope scopedIterate = selector.iterateInto(reportError);
        if (scopedIterate != null) {
          next = next.withDataScope(scopedIterate);
        }
      }
    }
    String toRepeat = findScopable.apply(next, "rx:repeat");
    if (toRepeat != null) {
      DataSelector selector = next.scope.select(next.privacy, toRepeat, reportError);
      if (selector != null) {
        selector.validateIntegral(reportError);
      }
    }
    String toMonitor = findScopable.apply(next, "rx:monitor");
    if (toMonitor != null) {
      DataSelector selector = next.scope.select(next.privacy, toMonitor, reportError);
      if (selector != null) {
        selector.validateIntegral(reportError);
      }
    }
    String toIfCheck = findScopable.apply(next, "rx:if");
    if (toIfCheck != null) {
      checkCondition(toIfCheck, next, reportError);
    }
    String toIfNotCheck = findScopable.apply(next, "rx:ifnot");
    if (toIfNotCheck != null) {
      checkCondition(toIfNotCheck, env, reportError);
    }
    String toSwitch = findScopable.apply(next, "rx:switch");
    if (toSwitch != null) {
      DataSelector selector = next.scope.select(next.privacy, toSwitch, reportError);
      if (selector != null) {
        selector.validateSwitchable(reportError);
      }
    }
    if (element.hasAttr("rx:template")) {
      String templateToUse = element.attr("rx:template");
      Element template = next.findTemplate(templateToUse);
      if (template != null) {
        children(template, next.withFragmentProvider(element));
        return;
      } else {
        reportError.accept("template '" + templateToUse + "' not found");
      }
    }

    for (Attribute attr : element.attributes()) {
      if (attr.hasDeclaredValue()) {
        _checkExpression(element, env, attr.getKey());
      }
    }
    children(element, next);
  }

  public void inlinetemplate(Element inlinetemplate, PageEnvironment env) {
    String templateToUse = inlinetemplate.attr("name");
    Element template = env.findTemplate(templateToUse);
    if (template != null) {
      children(template, env.withFragmentProvider(inlinetemplate));
      return;
    } else {
      feedback.warn(inlinetemplate, "Template '" + templateToUse + "' was not found");
    }
    children(inlinetemplate, env);
  }

  public void fragment(Element fragment, PageEnvironment env) {
    children(env.getFragmentProvider(), env);
  }

  public void lookup(Element lookup, PageEnvironment env) {
    _checkPath(lookup, env, "path");
  }

  public void monitor(Element monitor, PageEnvironment env) {
    _checkPath(monitor, env, "path");
  }

  public void title(Element title, PageEnvironment env) {
    _checkExpression(title, env, "title");
  }

  public void viewwrite(Element viewwrite, PageEnvironment env) {
    _checkExpression(viewwrite, env, "value");
  }

  public void todotask(Element fragment, PageEnvironment env) {
    // ignore
  }

  public void viewstateparams(Element vsp, PageEnvironment env) {
    ArrayList<String> sync = new ArrayList<>();
    for (Attribute attr : vsp.attributes()) {
      if (attr.hasDeclaredValue() && attr.getKey().startsWith("sync:")) {
        sync.add(attr.getValue());
      }
    }
    _checkExpression(vsp, env, sync.toArray(new String[sync.size()]));
  }

  public void form(Element form, PageEnvironment env) {
    if (form.hasAttr("rx:action")) {
      String action = form.attr("rx:action");
      if (action.startsWith("send:") || action.startsWith("send-once:")) {
        String channel = action.startsWith("send:") ? action.substring(5) : action.substring(10);
        if (env.scope == null) {
          feedback.warn(form, "no data channel for a form to send channel '" + channel + "' on");
        } else {
          if(!env.scope.hasChannel(channel)) {
            feedback.warn(form, "channel '" + channel + "' was not found");
          }
        }
      }
      // TODO: all the strange things
    }
    children(form, env);
  }

  public void exitgate(Element exitgate, PageEnvironment env) {
    // TODO: both guard and set are related to the view:
  }

  public void domainget(Element domainGet, PageEnvironment env) {
    children(domainGet, env.withDataScope(null));
  }

  public void documentget(Element domainGet, PageEnvironment env) {
    children(domainGet, env.withDataScope(null));
  }

  public void localstoragepoll(Element domainGet, PageEnvironment env) {
    children(domainGet, env.withDataScope(null));
  }

  public void connection(Element connection, PageEnvironment env) {
    String backend = defaultBackend;
    if (connection.hasAttr("backend")) {
      backend = connection.attr("backend");
    }
    String name = "default";
    if (connection.hasAttr("name")) {
      name = connection.attr("name");
    }
    _checkExpression(connection, env, "identity", "redirect", "name");
    if (!(connection.hasAttr("use-domain") || connection.hasAttr("billing"))) {
      _checkExpression(connection, env, "space", "key");
    }
    ObjectNode reflect = reflections.get(backend);
    if (reflect == null) {
      if (backend != null) {
        feedback.warn(connection, "The backend '" + backend + "' was not available");
      }
      children(connection, env);
    } else {
      DataScope child = DataScope.root(reflect);
      env.registerConnection(name, child);
      children(connection, env.withDataScope(child));
    }
  }

  public void pick(Element pick, PageEnvironment env) {
    String name = "default";
    if (pick.hasAttr("name")) {
      name = pick.attr("name");
    }
    PageEnvironment next = env.maybePickConnection(name);
    if (next != null) {
      children(pick, next);
    } else {
      // TODO: WARN
      children(pick, env);
    }
  }

  private void check(Element element, PageEnvironment env) {
    String tag = Base.normalizeTag(element.tagName());
    try {
      Method method = RxRootEnvironment.class.getMethod(tag, Element.class, PageEnvironment.class);
      method.invoke(this, element, env);
    } catch (NoSuchMethodException nme) {
      base(element, env);
    } catch (Exception ex) {
      feedback.warn(element, "[" + element.tagName() + "] problem:" + ex.getMessage());
    }
  }

  public void check() {
    HashSet<String> templatesUnused = new HashSet<>(templates.keySet());
    for (Element element : document.getElementsByTag("page")) {
      String privacy = "";
      if (element.hasAttr("privacy")) {
        privacy = element.attr("privacy");
      }
      String uri = element.attr("uri");
      HashSet<String> templatesUsed = new HashSet<>();
      templateUsageByPage.put(uri, templatesUsed);
      check(element, PageEnvironment.newPage(privacy, templates, templatesUnused, templatesUsed));
    }
    for (String unused : templatesUnused) {
      feedback.warn(templates.get(unused), "template '" + unused + "' was not used");
    }
    for (Map.Entry<String, ObjectNode> entry : reflections.entrySet()) {
      ObjectNode types = (ObjectNode) entry.getValue().get("types");
      Iterator<Map.Entry<String, JsonNode>> typeIt = types.fields();
      while (typeIt.hasNext()) {
        ObjectNode type = (ObjectNode) typeIt.next().getValue();
        if ("reactive_record".equals(type.get("nature").textValue())) {
          String recordName = type.get("name").textValue();
          Iterator<Map.Entry<String, JsonNode>> fieldIt = type.get("fields").fields();
          while (fieldIt.hasNext()) {
            Map.Entry<String, JsonNode> fieldEntry = fieldIt.next();
            String fieldName = fieldEntry.getKey();
            ObjectNode field = (ObjectNode) fieldEntry.getValue();
            boolean isPrivate = "private".equals(field.get("privacy").textValue());
            if (!field.has("used") && !isPrivate) {
              feedback.warn(document, recordName + "::" + fieldName + " is unused");
            }
          }
        }
      }
    }
  }
}
