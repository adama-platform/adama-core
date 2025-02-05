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
package ape.translator.tree.definitions.client;

import ape.translator.env.Environment;
import ape.translator.parser.Formatter;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.definitions.Definition;
import ape.translator.tree.types.natives.*;
import ape.translator.tree.types.structures.FieldDefinition;
import ape.translator.tree.types.topo.TypeCheckerRoot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** organizes common properties of an HTTP client into one thing */
public class Group extends Definition {
  public static final Pattern URI_PARAMETER_EXTRACT = Pattern.compile("\\[%([a-zA-Z][a-zA-Z0-9]*)]");
  private final Group parent;
  private final Token begin;
  private Token end;
  private final ArrayList<Consumer<Consumer<Token>>> emissions;
  private final ArrayList<Consumer<Formatter>> formatting;
  private final ArrayList<Method> methods;
  private final ArrayList<Header> headers;
  private final ArrayList<Group> groups;
  private final ArrayList<Endpoint> endpoints;

  public Group(Group parent, Token begin) {
    this.parent = parent;
    this.begin = begin;
    ingest(begin);
    this.emissions = new ArrayList<>();
    this.formatting = new ArrayList<>();
    this.methods = new ArrayList<>();
    this.headers = new ArrayList<>();
    this.groups = new ArrayList<>();
    this.endpoints = new ArrayList<>();
  }

  public void index(TreeMap<String, Method> index) {
    for (Method m : methods) {
      index.put(m.name.text, m);
    }
    for (Group g : groups) {
      g.index(index);
    }
  }

  public void end(Token end) {
    this.end = end;
    ingest(end);
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    yielder.accept(begin);
    for (Consumer<Consumer<Token>> emission : emissions) {
      emission.accept(yielder);
    }
    if (end != null) {
      yielder.accept(end);
    }
  }

  public void add(Endpoint endpoint) {
    this.endpoints.add(endpoint);
    emissions.add((e) -> endpoint.emit(e));
    formatting.add((f) -> endpoint.format(f));
  }

  public void add(Method method) {
    methods.add(method);
    emissions.add((e) -> method.emit(e));
    formatting.add((f) -> method.format(f));
  }

  public void add(Header header) {
    headers.add(header);
    emissions.add((e) -> header.emit(e));
    formatting.add((f) -> header.format(f));
  }

  public void add(Group group) {
    groups.add(group);
    emissions.add((e) -> group.emit(e));
    formatting.add((f) -> group.format(f));
  }

  @Override
  public void format(Formatter formatter) {
    for (Consumer<Formatter> item : formatting) {
      item.accept(formatter);
    }
  }

  private void issueDuplicateErrors(TypeCheckerRoot checker, HashSet<String> methodsDefined) {
    for (Method m : methods) {
      if (methodsDefined.contains(m.name.text)) {
        checker.issueError(m, "The method %s has already been defined".formatted(m.name.text));
      }
      methodsDefined.add(m.name.text);
    }
    HashSet<String> headersDefineInGroup = new HashSet<>();
    for (Header h : headers) {
      if (headersDefineInGroup.contains(h.name.text)) {
        checker.issueError(h, "The header %s has already been defined within the same group".formatted(h.name.text));
      }
      headersDefineInGroup.add(h.name.text);
    }
    for (Group g : groups) {
      g.issueDuplicateErrors(checker, methodsDefined);
    }
  }

  private void checkEndpoints(TypeCheckerRoot checker) {
    HashSet<String> defined = new HashSet<>();
    for (Endpoint endpoint : endpoints) {
      if (defined.contains(endpoint.version())) {
        checker.issueError(endpoint, "Endpoint already defined for '%s'".formatted(endpoint.version()));
      }
      defined.add(endpoint.version());
      String value = endpoint.value.text;
      boolean http = value.startsWith("\"http://");
      boolean https = value.startsWith("\"https://");
      boolean localhost = value.startsWith("\"http://localhost") || value.startsWith("\"http://127.0.0.1");
      boolean valid = http || https;
      if (!valid) {
        checker.issueError(endpoint, "The endpoint must start with either 'http://' or 'https://'");
      }
      if (http && !(endpoint.version().equals("dev") || localhost)) {
        checker.issueError(endpoint, "Only endpoints for developers or localhost are allowed to start with http://");
      }
    }
    for (Group g : groups) {
      g.checkEndpoints(checker);
    }
  }

  public void typingForRootGroup(TypeCheckerRoot checker) {
    issueDuplicateErrors(checker, new HashSet<>());
    checkEndpoints(checker);
  }

  public void typecheck(Environment env) {
    typecheck(env, false);
  }

  public void typecheck(Environment env, boolean parentHasProdEndpoint) {
    for (Method m : methods) {
      if (m.type_query != null) {
        TyNativeMessage type = env.rules.FindMessageStructure(m.type_query.text, m, false);
        if (type != null) {
          Matcher matcher = Group.URI_PARAMETER_EXTRACT.matcher(m.uri_str.text);
          while (matcher.find()) {
            String lookup = matcher.group(1);
            FieldDefinition fd = type.storage.fields.get(lookup);
            if (fd == null) {
              env.document.createError(m, "The key '" + lookup + "' was not a field within the query message");
            } else {
              if (fd.type instanceof TyNativeString || fd.type instanceof TyNativeInteger  || fd.type instanceof TyNativeDouble || fd.type instanceof TyNativeLong || fd.type instanceof TyNativeBoolean) {
                // OK?
              } else {
                env.document.createError(m, "The key '" + lookup + "' will lookup a field that is netiher a string, int, double, long, or boolean");
              }
            }
          }
        }
      }
      if (m.type_body != null) {
        env.rules.FindMessageStructure(m.type_body.text, m, false);
      }
      if (m.return_type != null) {
        env.rules.FindMessageStructure(m.return_type.text, m, false);
      }
    }
    boolean hasProdEndpoint = parentHasProdEndpoint;
    for (Endpoint ep : endpoints) {
      if (ep.version().equals("prod")) {
        hasProdEndpoint = true;
      }
    }
    if (!hasProdEndpoint) {
      env.document.createError(this, "Group lacks a default/production endpoint");
    }
    for (Group g : groups) {
      g.typecheck(env, hasProdEndpoint);
    }
  }

  public void writeJavaScriptDefn(String baseVar, StringBuilderWithTabs sb, AtomicInteger uniqueIdGenerator, String version, String priorEndpoint, String priorHeaderGroup) {
    String endpointToUse = priorEndpoint;
    for (Endpoint endpoint : endpoints) {
      if (endpoint.version().equals(version) || endpointToUse == null && endpoint.version().equals("prod")) {
        endpointToUse = endpoint.value.text;
      }
    }
    String headersToUse = priorHeaderGroup;
    if (headers.size() > 0 || priorHeaderGroup == null) {
      String newHeaderGroup = "__headers_" + uniqueIdGenerator.incrementAndGet();
      sb.append("HeaderGroup ").append(newHeaderGroup).append(" = new HeaderGroup(").append(priorHeaderGroup).append(");");
      sb.writeNewline();
      for (Header h : headers) {
        if (h.secret) {
          sb.append(newHeaderGroup).append(".add(\"").append(h.name.text).append("\", __decryptor.decrypt(").append(h.value.text).append("));");
        } else {
          sb.append(newHeaderGroup).append(".add(\"").append(h.name.text).append("\", ").append(h.value.text).append(");");
        }
        sb.writeNewline();
      }
      headersToUse = newHeaderGroup;
    }
    for (Method m : methods) {
      sb.append(baseVar).append(".register(\"").append(m.name.text).append("\", \"").append(m.method.text).append("\", ");
      sb.append(endpointToUse).append(", ");
      sb.append(headersToUse).append(", ");
      sb.append("(o) -> GenericClient.URL(").append(m.uri_str.text).append(", o));");
      sb.writeNewline();
    }
  }
}
