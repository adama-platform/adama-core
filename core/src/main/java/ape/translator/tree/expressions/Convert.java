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
package ape.translator.tree.expressions;

import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.checking.ruleset.RuleSetEnums;
import ape.translator.tree.types.natives.TyNativeArray;
import ape.translator.tree.types.structures.FieldDefinition;
import ape.translator.tree.types.traits.details.DetailContainsAnEmbeddedType;

import java.util.Map;
import java.util.function.Consumer;

public class Convert extends Expression {
  public final Token closeParen;
  public final Token closeType;
  public final Token convertToken;
  public final Expression expression;
  public final String newTypeName;
  public final Token newTypeToken;
  public final Token openParen;
  public final Token openType;
  private ConversionStyle style;

  public Convert(final Token convertToken, final Token openType, final Token newTypeToken, final Token closeType, final Token openParen, final Expression expression, final Token closeParen) {
    this.convertToken = convertToken;
    this.openType = openType;
    this.newTypeToken = newTypeToken;
    this.closeType = closeType;
    this.openParen = openParen;
    this.expression = expression;
    this.closeParen = closeParen;
    newTypeName = newTypeToken.text;
    style = ConversionStyle.None;
    ingest(openType);
    ingest(expression);
    ingest(closeParen);
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    yielder.accept(convertToken);
    yielder.accept(openType);
    yielder.accept(newTypeToken);
    yielder.accept(closeType);
    yielder.accept(openParen);
    expression.emit(yielder);
    yielder.accept(closeParen);
  }

  @Override
  public void format(Formatter formatter) {
    expression.format(formatter);
  }

  @Override
  protected TyType typingInternal(final Environment environment, final TyType suggestion) {
    TyType exprTypeRaw = expression.typing(environment, null);
    if (environment.rules.IsInteger(exprTypeRaw, true)) {
      style = ConversionStyle.Enum;
      TyType enumType = environment.document.types.get(newTypeName);
      RuleSetEnums.IsEnum(environment, enumType, false);
      return enumType;
    }

    final var preCopyType = environment.rules.FindMessageStructure(newTypeName, this, false);
    final var exprType = environment.rules.ResolvePtr(exprTypeRaw, false);
    if (preCopyType == null) {
      return null;
    }
    final var idealType = preCopyType.makeCopyWithNewPosition(this, TypeBehavior.ReadOnlyNativeValue);
    if (environment.rules.IsNativeArrayOfStructure(exprType, true)) {
      // X{]
      style = ConversionStyle.Multiple;
      if (environment.rules.CanStructureAProjectIntoStructureB(((DetailContainsAnEmbeddedType) exprType).getEmbeddedType(environment), idealType, false)) {
        return new TyNativeArray(TypeBehavior.ReadOnlyNativeValue, idealType.makeCopyWithNewPosition(this, TypeBehavior.ReadOnlyNativeValue), null).withPosition(this);
      }
    } else if (environment.rules.IsNativeListOfStructure(exprType, true)) {
      // list<X>
      style = ConversionStyle.Multiple;
      if (environment.rules.CanStructureAProjectIntoStructureB(((DetailContainsAnEmbeddedType) exprType).getEmbeddedType(environment), idealType, false)) {
        return new TyNativeArray(TypeBehavior.ReadOnlyNativeValue, idealType.makeCopyWithNewPosition(this, TypeBehavior.ReadOnlyNativeValue), null).withPosition(this);
      }
    } else if (environment.rules.IsStructure(exprType, true)) {
      // X
      style = ConversionStyle.Single;
      if (environment.rules.CanStructureAProjectIntoStructureB(exprType, idealType, false)) {
        return idealType;
      }
    } else if (environment.rules.IsMaybe(exprType, true)) {
      final var subExpr = environment.rules.ResolvePtr(environment.rules.ExtractEmbeddedType(exprType, false), false);
      // maybe<X>
      if (subExpr != null && environment.rules.IsStructure(subExpr, false)) {
        style = ConversionStyle.Maybe;
        if (environment.rules.CanStructureAProjectIntoStructureB(subExpr, idealType, false)) {
          return idealType;
        }
      } else {
        environment.rules.SignalConversionIssue(exprType, false);
      }
    } else {
      environment.rules.SignalConversionIssue(exprType, false);
    }
    return null;
  }

  @Override
  public void writeJava(final StringBuilder sb, final Environment environment) {
    if (style == ConversionStyle.Multiple) {
      final var elementType = environment.rules.ExtractEmbeddedType(expression.cachedType, true);
      sb.append("Utility.convertMultiple(");
      expression.writeJava(sb, environment);
      sb.append(", (__n) -> new RTx").append(newTypeName).append("[__n], (__obj) -> ");
      writeNewMessage(sb, elementType, environment);
      sb.append(")");
    } else if (style == ConversionStyle.Single) {
      sb.append("Utility.convertSingle(");
      expression.writeJava(sb, environment);
      sb.append(", (__obj) -> ");
      writeNewMessage(sb, expression.cachedType, environment);
      sb.append(")");
    } else if (style == ConversionStyle.Maybe) {
      final var elementType = environment.rules.ExtractEmbeddedType(expression.cachedType, true);
      sb.append("Utility.convertMaybe(");
      expression.writeJava(sb, environment);
      sb.append(", (__obj) -> ");
      writeNewMessage(sb, elementType, environment);
      sb.append(")");
    } else if (style == ConversionStyle.Enum) {
      sb.append("__EnumFix_").append(newTypeName).append("(");
      expression.writeJava(sb, environment);
      sb.append(")");
    }
  }

  private void writeNewMessage(final StringBuilder sb, final TyType elementType, final Environment environment) {
    final var idealType = environment.rules.FindMessageStructure(newTypeName, this, false);
    sb.append("new RTx").append(newTypeName).append("(");
    var first = true;
    final var scoped = environment.scope();
    scoped.define("__obj", elementType, false, this);
    for (final Map.Entry<String, FieldDefinition> entry : idealType.storage().fields.entrySet()) {
      if (first) {
        first = false;
      } else {
        sb.append(", ");
      }
      final var fieldLookup = new FieldLookup(new Lookup(Token.WRAP("__obj")), null, Token.WRAP(entry.getKey()));
      fieldLookup.typing(scoped, null);
      fieldLookup.writeJava(sb, scoped);
    }
    sb.append(")");
  }

  @Override
  public void free(FreeEnvironment environment) {
    expression.free(environment);
  }
}
