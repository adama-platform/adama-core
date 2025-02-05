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

import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.topo.TypeCheckerRoot;

import java.util.TreeSet;
import java.util.function.Consumer;

public class DefineAssoc extends Definition {
  private final Token assoc;
  public final Token name;
  public final Token open;
  public final Token fromTypeName;
  public final Token comma;
  public final Token toTypeName;
  public final Token secondCommaOptional;
  public final Token edgeType;
  public final Token close;
  private final Token semicolon;
  public short id;

  public DefineAssoc(Token assoc, Token open, Token fromTypeName, Token comma, Token toTypeName, Token secondCommaOptional, Token edgeType,  Token close, Token name, Token semicolon) {
    this.assoc = assoc;
    this.open = open;
    this.fromTypeName = fromTypeName;
    this.comma = comma;
    this.toTypeName = toTypeName;
    this.secondCommaOptional = secondCommaOptional;
    this.edgeType = edgeType;
    this.close = close;
    this.name = name;
    this.semicolon = semicolon;
    this.id = 0;
    ingest(assoc);
    ingest(semicolon);
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    yielder.accept(assoc);
    yielder.accept(open);
    yielder.accept(fromTypeName);
    yielder.accept(comma);
    yielder.accept(toTypeName);
    if (secondCommaOptional != null) {
      yielder.accept(secondCommaOptional);
      yielder.accept(edgeType);
    }
    yielder.accept(close);
    yielder.accept(name);
    yielder.accept(semicolon);
  }

  @Override
  public void format(Formatter formatter) {
    formatter.startLine(assoc);
    formatter.endLine(semicolon);
  }

  public void typing(TypeCheckerRoot checker) {
    TreeSet<String> depends = new TreeSet<>();
    depends.add(fromTypeName.text);
    depends.add(toTypeName.text);
    checker.register(depends, (env) -> {
      TyType fromT = env.document.types.get(fromTypeName.text);
      if (fromT == null) {
        checker.issueError(DefineAssoc.this, "The type '" + fromTypeName.text + "' was not found");
      }
      TyType toT = env.document.types.get(toTypeName.text);
      if (toT == null) {
        checker.issueError(DefineAssoc.this, "The type '" + toTypeName.text + "' was not found");
      }
      env.rules.IsRxStructure(fromT, false);
      env.rules.IsRxStructure(toT, false);
      if (secondCommaOptional != null) {
        TyType edType = env.document.types.get(edgeType.text);
        if (edType == null) {
          checker.issueError(DefineAssoc.this, "The type '" + edgeType.text + "' was not found");
        }
        env.rules.IsRxStructure(edType, false);
      }
    });
  }
}
