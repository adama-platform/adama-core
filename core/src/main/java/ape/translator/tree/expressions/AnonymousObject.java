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
import ape.translator.tree.types.natives.TyNativeMaybe;
import ape.translator.tree.types.natives.TyNativeMessage;
import ape.translator.tree.types.structures.FieldDefinition;
import ape.translator.tree.types.structures.StorageSpecialization;
import ape.translator.tree.types.structures.StructureStorage;
import ape.translator.tree.types.traits.SupportsTwoPhaseTyping;
import ape.translator.tree.types.traits.details.DetailInventDefaultValueExpression;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

/**
 * an anonymous is an instance of an object that has no defined type.
 *
 * <p>For instance {x:1, y:0} is anonymous in that no type is defined, and yet if
 *
 * <p>message Point { int x; int y; } existed then the type Point should be used.
 */
public class AnonymousObject extends Expression implements SupportsTwoPhaseTyping {
  public final TreeMap<String, Expression> fields;
  public final Token openBraceToken;
  public final ArrayList<RawField> rawFields;
  public Token closeBraceToken;

  public AnonymousObject(final Token openBraceToken) {
    this.openBraceToken = openBraceToken;
    fields = new TreeMap<>();
    rawFields = new ArrayList<>();
    ingest(openBraceToken);
  }

  /** add a field to this object */
  public void add(final Token commaToken, final Token fieldToken, final Token colonToken, final Expression expression) {
    rawFields.add(new RawField(commaToken, fieldToken, colonToken, expression));
    fields.put(fieldToken.text, expression);
    ingest(commaToken);
    ingest(expression);
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    yielder.accept(openBraceToken);
    for (final RawField rf : rawFields) {
      if (rf.commaToken != null) {
        yielder.accept(rf.commaToken);
      }
      yielder.accept(rf.fieldToken);
      if (rf.colonToken != null) {
        yielder.accept(rf.colonToken);
        rf.expression.emit(yielder);
      }
    }
    yielder.accept(closeBraceToken);
  }

  @Override
  public void format(Formatter formatter) {
    boolean multiline = rawFields.size() > 1;
    if (multiline) {
      formatter.endLine(openBraceToken);
      formatter.tabUp();
      formatter.tabUp();
    }
    int n = rawFields.size();
    for (int k = 0; k < n; k++) {
      RawField rf = rawFields.get(k);
      if (multiline) {
        if (rf.commaToken != null) {
          formatter.endLine(rf.commaToken);
        }
        formatter.startLine(rf.fieldToken);
      }
      if (rf.colonToken != null) {
        rf.expression.format(formatter);
      }
      if (multiline && k == n - 1) {
        Formatter.FirstAndLastToken fal = new Formatter.FirstAndLastToken();
        rf.expression.emit(fal);
        if (fal.last != null) {
          formatter.endLine(fal.last);
        }
      }
    }
    if (multiline) {
      formatter.tabDown();
      formatter.startLine(closeBraceToken);
      formatter.tabDown();
    }
  }

  @Override
  protected TyType typingInternal(final Environment environment, final TyType suggestion) {
    environment.mustBeComputeContext(this);
    if (suggestion != null) {
      var theType = environment.rules.GetMaxType(suggestion, estimateType(environment), false);
      theType = environment.rules.EnsureRegisteredAndDedupe(theType, false);
      if (theType != null) {
        upgradeType(environment, theType);
        return theType.makeCopyWithNewPosition(this, theType.behavior);
      }
    }
    var typeToUse = estimateType(environment);
    typeToUse = environment.rules.EnsureRegisteredAndDedupe(typeToUse, false);
    upgradeType(environment, typeToUse);
    return typeToUse;
  }

  @Override
  public TyType estimateType(final Environment environment) {
    Token name = Token.WRAP("_AnonObjConvert_" + environment.autoVariable());
    environment.mustBeComputeContext(this);
    final var storage = new StructureStorage(name, StorageSpecialization.Message, true, false, null);
    for (final Map.Entry<String, Expression> entry : fields.entrySet()) {
      final var type = entry.getValue() instanceof SupportsTwoPhaseTyping ? ((SupportsTwoPhaseTyping) entry.getValue()).estimateType(environment) : entry.getValue().typing(environment, null);
      final var fd = FieldDefinition.invent(type, entry.getKey());
      storage.add(fd);
    }
    return new TyNativeMessage(TypeBehavior.ReadOnlyNativeValue, null, name, storage).withPosition(this);
  }

  @Override
  public void upgradeType(final Environment environment, final TyType newType) {
    cachedType = newType.withPosition(this);
    if (environment.rules.IsNativeMessage(newType, false)) {
      final var other = (TyNativeMessage) newType;
      for (final Map.Entry<String, FieldDefinition> otherField : other.storage().fields.entrySet()) {
        var myField = fields.get(otherField.getKey());
        final var otherFieldType = otherField.getValue().type;
        if (myField == null) {
          if (otherFieldType != null && otherFieldType instanceof DetailInventDefaultValueExpression) {
            final var newValue = ((DetailInventDefaultValueExpression) otherFieldType).inventDefaultValueExpression(this);
            if (newValue != null) {
              newValue.typing(environment, null);
              fields.put(otherField.getKey(), newValue);
            }
          }
        } else {
          if (otherFieldType instanceof TyNativeMaybe && !(myField.typing(environment, null) instanceof TyNativeMaybe)) {
            if (myField instanceof SupportsTwoPhaseTyping) {
              ((SupportsTwoPhaseTyping) myField).upgradeType(environment, ((TyNativeMaybe) otherFieldType).tokenElementType.item);
            }
            myField = new MaybeLift(null, null, null, myField, null);
            myField.typing(environment, otherFieldType);
            fields.put(otherField.getKey(), myField);
          } else {
            if (myField instanceof SupportsTwoPhaseTyping) {
              ((SupportsTwoPhaseTyping) myField).upgradeType(environment, otherFieldType);
            }
          }
        }
      }
    }
  }

  @Override
  public void writeJava(final StringBuilder sb, final Environment environment) {
    final var me = (TyNativeMessage) cachedType;
    if (me != null) {
      sb.append("new RTx" + me.name + "(");
      var first = true;
      for (final Expression expression : fields.values()) {
        if (first) {
          first = false;
        } else {
          sb.append(", ");
        }
        expression.writeJava(sb, environment);
      }
      sb.append(")");
    }
  }

  public void end(final Token closeBraceToken) {
    this.closeBraceToken = closeBraceToken;
    ingest(closeBraceToken);
  }

  public class RawField {
    public final Token colonToken;
    public final Token commaToken;
    public final Expression expression;
    public final Token fieldToken;

    public RawField(final Token commaToken, final Token fieldToken, final Token colonToken, final Expression expression) {
      this.commaToken = commaToken;
      this.fieldToken = fieldToken;
      this.colonToken = colonToken;
      this.expression = expression;
    }
  }

  @Override
  public void free(FreeEnvironment environment) {
    for (var entry : fields.entrySet()) {
      entry.getValue().free(environment);
    }
  }
}
