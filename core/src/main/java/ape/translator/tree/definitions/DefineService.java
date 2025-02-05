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
package ape.translator.tree.definitions;

import ape.translator.env.ComputeContext;
import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.types.topo.TypeCheckerRoot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.function.Consumer;

/** Define a service */
public class DefineService extends Definition {
  public static class ServiceAspect extends Definition {
    public final Token name;
    public final Token equals;
    public final Expression expression;
    public final Token semicolon;

    public ServiceAspect(Token name, Token equals, Expression expression, Token semicolon) {
      this.name = name;
      this.equals = equals;
      this.expression = expression;
      this.semicolon = semicolon;
      ingest(name);
      ingest(semicolon);
    }

    @Override
    public void emit(Consumer<Token> yielder) {
      yielder.accept(name);
      yielder.accept(equals);
      expression.emit(yielder);
      yielder.accept(semicolon);
    }

    @Override
    public void format(Formatter formatter) {
      formatter.startLine(name);
      expression.format(formatter);
      formatter.endLine(semicolon);
    }

    public void typing(Environment environment) {
      expression.typing(environment.scopeWithComputeContext(ComputeContext.Computation), null);
    }
  }

  public static class ServiceMethod extends Definition {
    public final Token methodToken;
    public final Token secured;
    public final Token pairOpen;
    public final Token inputTypeName;
    public final Token comma;
    public final Token outputTypeName;
    public final Token outputArrayExt;
    public final Token pairClose;
    public final Token name;
    public final Token semicolon;

    public ServiceMethod(Token methodToken, Token secured, Token pairOpen, Token inputTypeName, Token comma, Token outputTypeName, Token outputArrayExt, Token pairClose, Token name, Token semicolon) {
      this.methodToken = methodToken;
      this.secured = secured;
      this.pairOpen = pairOpen;
      this.inputTypeName = inputTypeName;
      this.comma = comma;
      this.outputTypeName = outputTypeName;
      this.outputArrayExt = outputArrayExt;
      this.pairClose = pairClose;
      this.name = name;
      this.semicolon = semicolon;
      ingest(methodToken);
      ingest(semicolon);
    }

    @Override
    public void format(Formatter formatter) {
      formatter.startLine(methodToken);
      formatter.endLine(semicolon);
    }

    public boolean requiresSecureCaller() {
      return secured != null;
    }

    @Override
    public void emit(Consumer<Token> yielder) {
      yielder.accept(methodToken);
      if (secured != null) {
        yielder.accept(secured);
      }
      yielder.accept(pairOpen);
      yielder.accept(inputTypeName);
      yielder.accept(comma);
      yielder.accept(outputTypeName);
      if (outputArrayExt != null) {
        yielder.accept(outputArrayExt);
      }
      yielder.accept(pairClose);
      yielder.accept(name);
      yielder.accept(semicolon);
    }

    private void typing(String name, Environment environment) {
      if ("dynamic".equals(name)) {
        return;
      }
      environment.rules.FindMessageStructure(name, this, false);
    }

    public void typing(Environment environment) {
      typing(inputTypeName.text, environment);
      typing(outputTypeName.text, environment);
      if ("dynamic".equals(outputTypeName.text) && outputArrayExt != null) {
       environment.document.createError(this, "service method returns dynamic, and can't be an array");
      }
    }
  }

  public static class ServiceReplication extends Definition {
    public final Token methodToken;
    public final Token pairOpen;
    public final Token inputTypeName;
    public final Token pairClose;
    public final Token name;
    public final Token semicolon;

    public ServiceReplication(Token methodToken, Token pairOpen, Token inputTypeName, Token pairClose, Token name, Token semicolon) {
      this.methodToken = methodToken;
      this.pairOpen = pairOpen;
      this.inputTypeName = inputTypeName;
      this.pairClose = pairClose;
      this.name = name;
      this.semicolon = semicolon;
      ingest(methodToken);
      ingest(semicolon);
    }

    @Override
    public void emit(Consumer<Token> yielder) {
      yielder.accept(methodToken);
      yielder.accept(pairOpen);
      yielder.accept(inputTypeName);
      yielder.accept(pairClose);
      yielder.accept(name);
      yielder.accept(semicolon);
    }

    @Override
    public void format(Formatter formatter) {
      formatter.startLine(methodToken);
      formatter.endLine(semicolon);
    }

    public void typing(Environment environment) {
      if ("dynamic".equals(inputTypeName.text)) {
        return;
      }
      environment.rules.FindMessageStructure(inputTypeName.text, this, false);
    }
  }

  public final Token serviceToken;
  public final Token name;
  public final Token open;
  public final ArrayList<ServiceAspect> aspects;
  public final TreeMap<String, ServiceAspect> aspectsMap;
  public final ArrayList<ServiceMethod> methods;
  public final TreeMap<String, ServiceMethod> methodsMap;
  public final ArrayList<Consumer<Consumer<Token>>> emission;
  public final ArrayList<Consumer<Formatter>> formatting;
  public final ArrayList<ServiceReplication> replications;
  public final TreeMap<String, ServiceReplication> replicationsMap;
  public final Token close;

  public DefineService(Token serviceToken, Token name, Token open, ArrayList<ServiceAspect> aspects, ArrayList<ServiceMethod> methods, ArrayList<ServiceReplication> replications, Token close, ArrayList<Consumer<Consumer<Token>>> emission, ArrayList<Consumer<Formatter>> formatting) {
    this.serviceToken = serviceToken;
    this.name = name;
    this.open = open;
    this.aspects = aspects;
    this.methods = methods;
    this.replications = replications;
    this.emission = emission;
    this.close = close;
    ingest(serviceToken);
    ingest(close);
    aspectsMap = new TreeMap<>();
    methodsMap = new TreeMap<>();
    replicationsMap = new TreeMap<>();
    for (ServiceMethod sm : methods) {
      methodsMap.put(sm.name.text, sm);
    }
    for (ServiceAspect sa : aspects) {
      aspectsMap.put(sa.name.text, sa);
    }
    for (ServiceReplication sr : replications) {
      replicationsMap.put(sr.name.text, sr);
    }
    this.formatting = formatting;
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    yielder.accept(serviceToken);
    yielder.accept(name);
    yielder.accept(open);
    for (Consumer<Consumer<Token>> e : emission) {
      e.accept(yielder);
    }
    yielder.accept(close);
  }

  @Override
  public void format(Formatter formatter) {
    for(Consumer<Formatter> c : formatting) {
      c.accept(formatter);
    }
  }

  public void typing(TypeCheckerRoot checker) {
    FreeEnvironment fe = FreeEnvironment.root();
    checker.define(name.cloneWithNewText("service:" + name.text), fe.free, (environment) -> {
      HashSet<String> alreadyDefinedAspects = new HashSet<>();
      for (ServiceAspect aspect : aspects) {
        if (alreadyDefinedAspects.contains(aspect.name.text)) {
          environment.document.createError(this, String.format("'%s' was already defined as an aspect within the service.", aspect.name.text));
        }
        alreadyDefinedAspects.add(aspect.name.text);
        aspect.typing(environment);
      }
      HashSet<String> alreadyDefinedMethods = new HashSet<>();
      for (ServiceMethod method : methods) {
        if (alreadyDefinedMethods.contains(method.name.text)) {
          environment.document.createError(this, String.format("'%s' was already defined as a method within the service.", method.name.text));
        }
        alreadyDefinedMethods.add(method.name.text);
        method.typing(environment);
      }
      HashSet<String> alreadyDefinedReplications = new HashSet<>();
      for (ServiceReplication replication : replications) {
        if (alreadyDefinedReplications.contains(replication.name.text)) {
          environment.document.createError(this, String.format("'%s' was already defined as a replication within the service.", replication.name.text));
        }
        alreadyDefinedReplications.add(replication.name.text);
        replication.typing(environment);
      }
    });
  }
}
