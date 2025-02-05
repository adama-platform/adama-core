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
package ape.translator.tree.types.structures;

import ape.translator.env.ComputeContext;
import ape.translator.env.Environment;
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.watcher.WatchSet;
import ape.translator.tree.definitions.DefineAssoc;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.checking.ruleset.RuleSetMaybe;
import ape.translator.tree.types.natives.TyNativeInteger;
import ape.translator.tree.types.reactive.TyReactiveTable;
import ape.translator.tree.watcher.WatchSetWatcher;

import java.util.function.Consumer;

/** Register a differential join operation on a table */
public class JoinAssoc extends StructureComponent {
  private final Token joinToken;
  private final Token assoc;
  private final Token via;
  public final Token tableName;
  private final Token brackOpen;
  public final Token itemVar;
  private final Token brackClose;
  private final Token fromLabel;
  public final Expression fromExpr;
  private final Token toLabel;
  public final Expression toExpr;
  private final Token semicolon;
  public final WatchSet watching;
  public DefineAssoc foundAssoc;
  public String edgeRecordName;
  private TyType elementType;
  public boolean fromMaybe;
  public boolean toMaybe;

  public JoinAssoc(Token joinToken, Token assoc, Token via, Token tableName, Token brackOpen, Token itemVar, Token brackClose, Token fromLabel, Expression fromExpr, Token toLabel, Expression toExpr, Token semicolon) {
    this.joinToken = joinToken;
    this.assoc = assoc;
    this.via = via;
    this.tableName = tableName;
    this.brackOpen = brackOpen;
    this.itemVar = itemVar;
    this.brackClose = brackClose;
    this.fromLabel = fromLabel;
    this.fromExpr = fromExpr;
    this.toLabel = toLabel;
    this.toExpr = toExpr;
    this.semicolon = semicolon;
    this.watching = new WatchSet();
    this.fromMaybe = false;
    this.toMaybe = false;
    ingest(joinToken);
    ingest(semicolon);
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    yielder.accept(joinToken);
    yielder.accept(assoc);
    yielder.accept(via);
    yielder.accept(tableName);
    yielder.accept(brackOpen);
    yielder.accept(itemVar);
    yielder.accept(brackClose);
    yielder.accept(fromLabel);
    fromExpr.emit(yielder);
    yielder.accept(toLabel);
    toExpr.emit(yielder);
    yielder.accept(semicolon);
  }

  @Override
  public void format(Formatter formatter) {
    formatter.startLine(joinToken);
    fromExpr.format(formatter);
    toExpr.format(formatter);
    formatter.endLine(semicolon);
  }

  public Environment nextItemEnv(Environment env) {
    Environment itemEnv = env.scopeAsReadOnlyBoundary().scopeWithComputeContext(ComputeContext.Computation);
    itemEnv.define(itemVar.text, elementType, true, this);
    return itemEnv;
  }

  public void typing(final Environment environment, StructureStorage owningStructureStorage) {
    Environment next = environment.watch(new WatchSetWatcher(environment, watching));
    foundAssoc = environment.document.assocs.get(assoc.text);
    String edgeType = null;
    if (foundAssoc == null) {
      environment.document.createError(this, "The assoc '" + assoc.text + "' was not found in the document.");
    } else {
      if (foundAssoc.edgeType != null) {
        edgeType = foundAssoc.edgeType.text;
      }
    }
    FieldDefinition fd = owningStructureStorage.fields.get(tableName.text);
    if (fd == null) {
      environment.document.createError(this, "The table '" + tableName.text + "' was not found within the record.");
    } else {
      if (fd.type instanceof TyReactiveTable) {
        TyReactiveTable rxTable = (TyReactiveTable) fd.type;
        elementType = environment.document.types.get(rxTable.recordName);
        if (elementType != null) {
          edgeRecordName = rxTable.recordName;
          if (edgeType != null && !edgeType.equals(edgeRecordName)) {
            environment.document.createError(this, "The assoc '" + assoc.text + "' requires an edge type of '" + edgeType + "' while being given a table with '" + edgeRecordName + "'.");
          }
          Environment itemEnv = nextItemEnv(next);
          TyType suggestion = new TyNativeInteger(TypeBehavior.ReadOnlyNativeValue, null, null).withPosition(this);
          TyType fromType = environment.rules.Resolve(fromExpr.typing(itemEnv, suggestion), false);
          TyType toType = environment.rules.Resolve(toExpr.typing(itemEnv, suggestion), false);
          RuleSetMaybe.IsMaybeIntegerOrJustInteger(itemEnv, fromType, false);
          RuleSetMaybe.IsMaybeIntegerOrJustInteger(itemEnv, toType, false);
          this.fromMaybe = RuleSetMaybe.IsMaybe(itemEnv, fromType, true);
          this.toMaybe = RuleSetMaybe.IsMaybe(itemEnv, toType, true);
        } else {
          environment.document.createError(this, "'" + tableName.text + "' was not yet registered ");
        }
      } else {
        environment.document.createError(this, "'" + tableName.text + "' was not a table");
      }
    }
  }
}
