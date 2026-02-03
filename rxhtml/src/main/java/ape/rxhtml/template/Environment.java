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

import ape.common.Escaping;
import ape.rxhtml.atl.Context;
import ape.rxhtml.codegen.VariablePool;
import ape.rxhtml.codegen.Writer;
import ape.rxhtml.template.config.Feedback;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Immutable context object passed through template processing.
 * Carries state including the current element, parent variable, state variable,
 * namespace, and configuration. Uses builder methods to create modified copies
 * for different scopes without mutation.
 */
public class Environment {
  public final String section;
  public final Environment parent;
  public final Feedback feedback;
  public final Writer writer;
  public final VariablePool pool;
  public final Element element;
  public final boolean elementAlone;
  public final String parentVariable;
  public final String stateVar;
  public final String caseVar;
  public final String xmlns;
  public final String fragmentFunc;
  private final Context classContext;
  private final Context attrContext;
  public final ArrayList<Task> tasks;
  public final boolean optimizeForPageLoad;
  public final String autoVar;
  public final String environment;
  public final String configVar;
  public final String siblingVar;

  private Environment(Environment parent, String section, Context classContext, Feedback feedback, Writer writer, VariablePool pool, Element element, boolean elementAlone, String parentVariable, String stateVar, String caseVar, String xmlns, String fragmentFunc, ArrayList<Task> tasks, boolean optimizeForPageLoad, String autoVar, String environment, String configVar, String siblingVar) {
    this.section = section;
    this.parent = parent;
    this.classContext = classContext;
    this.feedback = feedback;
    this.writer = writer;
    this.pool = pool;
    this.element = element;
    this.elementAlone = elementAlone;
    this.parentVariable = parentVariable;
    this.stateVar = stateVar;
    this.caseVar = caseVar;
    this.xmlns = xmlns;
    this.fragmentFunc = fragmentFunc;
    this.tasks = tasks;
    this.optimizeForPageLoad = optimizeForPageLoad;
    this.attrContext = Context.makeAutoVariable();
    this.autoVar = autoVar;
    this.environment = environment;
    this.configVar = configVar;
    this.siblingVar = siblingVar;
  }

  public static Environment fresh(Feedback feedback, String environment) {
    return new Environment(null, "fresh", Context.makeClassContext(), feedback, new Writer(), new VariablePool(), null, false, null, null, null, null, null, new ArrayList<>(), false, null, environment, null, null);
  }

  public HashMap<String, Integer> getCssFreq() {
    return classContext.freq;
  }

  public String val(String name, String defaultVal) {
    if (element.hasAttr(name)) {
      return element.attr(name);
    }
    return defaultVal;
  }

  public Element soloChildIfPossible() {
    Element result = null;
    for (int k = 0; k < element.childNodeSize(); k++) {
      Node node = element.childNode(k);
      if (node instanceof TextNode) {
        TextNode text = (TextNode) node;
        if (!text.text().trim().equalsIgnoreCase("")) {
          return null;
        }
      } else if (node instanceof Comment) {
        // ignore comments
      } else if (node instanceof Element) {
        if (result != null) {
          return null;
        }
        result = (Element) node;
      }
    }
    if (result != null && result.tagName() != null) {
      // some solo nodes are not actual nodes
      // TODO: use some reflection magic to simplify this
      switch (Base.normalizeTag(result.tagName())) {
        case "inlinetemplate":
        case "connection":
        case "connectionstatus":
        case "viewstateparams":
        case "viewwrite":
        case "title":
        case "exitgate":
        case "monitor":
          return null;
      }
    }
    return result;
  }

  public Environment element(Element element, boolean elementAlone, String siblingVar) {
    return new Environment(this, section, classContext, feedback, writer, pool, element, elementAlone, parentVariable, stateVar, caseVar, xmlns, fragmentFunc, tasks, optimizeForPageLoad, autoVar, environment, configVar, siblingVar);
  }

  public Environment parentVariable(String parentVariable) {
    return new Environment(this, section, classContext, feedback, writer, pool, element, elementAlone, parentVariable, stateVar, caseVar, xmlns, fragmentFunc, tasks, optimizeForPageLoad, autoVar, environment, configVar, siblingVar);
  }

  public Environment stateVar(String stateVar) {
    // we lower the optimize because we are creating a new state variable
    return new Environment(this, section, classContext, feedback, writer, pool, element, elementAlone, parentVariable, stateVar, caseVar, xmlns, fragmentFunc, tasks, false, autoVar, environment, configVar, siblingVar);
  }

  public Environment caseVar(String caseVar) {
    return new Environment(this, section, classContext, feedback, writer, pool, element, elementAlone, parentVariable, stateVar, caseVar, xmlns, fragmentFunc, tasks, optimizeForPageLoad, autoVar, environment, configVar, siblingVar);
  }

  public Environment xmlns(String xmlns) {
    return new Environment(this, section, classContext, feedback, writer, pool, element, elementAlone, parentVariable, stateVar, caseVar, xmlns, fragmentFunc, tasks, optimizeForPageLoad, autoVar, environment, configVar, siblingVar);
  }

  public Environment fragmentFunc(String fragmentFunc) {
    return new Environment(this, section, classContext, feedback, writer, pool, element, elementAlone, parentVariable, stateVar, caseVar, xmlns, fragmentFunc, tasks, optimizeForPageLoad, autoVar, environment, configVar, siblingVar);
  }

  public Environment autoVar(String autoVar) {
    return new Environment(this, section, classContext, feedback, writer, pool, element, elementAlone, parentVariable, stateVar, caseVar, xmlns, fragmentFunc, tasks, optimizeForPageLoad, autoVar, environment, configVar, siblingVar);
  }

  public Environment feedback(String section, Feedback feedback) {
    return new Environment(this, section, classContext, feedback, writer, pool, element, elementAlone, parentVariable, stateVar, caseVar, xmlns, fragmentFunc, tasks, optimizeForPageLoad, autoVar, environment, configVar, siblingVar);
  }

  public Environment configVar(String configVar) {
    return new Environment(this, section, classContext, feedback, writer, pool, element, elementAlone, parentVariable, stateVar, caseVar, xmlns, fragmentFunc, tasks, optimizeForPageLoad, autoVar, environment, configVar, siblingVar);
  }

  public Environment raiseOptimize() {
    if (!optimizeForPageLoad) {
      return new Environment(this, section, classContext, feedback, writer, pool, element, elementAlone, parentVariable, stateVar, caseVar, xmlns, fragmentFunc, tasks, true, autoVar, environment, configVar, siblingVar);
    }
    return this;
  }

  public Context contextOf(String attr) {
    if ("class".equals(attr)) {
      return classContext;
    }
    return Context.DEFAULT;
  }

  public Context attributeContext(String attr) {
    if ("class".equals(attr)) {
      return classContext;
    }
    return attrContext;
  }

  public void writeElementDebugIfTest() {
    if (this.environment != null && this.environment.equals("test")) {
      this.writer.newline().tab().append("// <").append(this.element.tagName());
      this.element.attributes().asList().forEach((attr) -> {
        this.writer.append(" ").append(attr.getKey()).append("=\"").append(new Escaping(attr.getValue()).switchQuotes().go()).append("\"");
      });
      this.writer.append(">").newline();
    }
  }
}
