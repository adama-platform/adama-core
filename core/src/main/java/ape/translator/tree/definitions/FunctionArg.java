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

import ape.translator.env.Environment;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.DocumentPosition;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.TyNativeMessage;
import ape.translator.tree.types.natives.TyNativeTable;

import java.util.function.Consumer;

/** argument pair for the tuple (type, name) */
public class FunctionArg {
  public final Token commaToken;
  public final Token modifierToken;
  public TyType type;
  public final Token argNameToken;
  public String argName;

  public FunctionArg(final Token commaToken, final Token modifierToken, final TyType type, final Token argNameToken) {
    this.modifierToken = modifierToken;
    this.commaToken = commaToken;
    this.type = type;
    this.argNameToken = argNameToken;
    argName = argNameToken.text;
  }

  public void emit(final Consumer<Token> yielder) {
    if (commaToken != null) {
      yielder.accept(commaToken);
    }
    if (modifierToken != null) {
      yielder.accept(modifierToken);
    }
    type.emit(yielder);
    yielder.accept(argNameToken);
  }

  public void typing(final Environment environment) {
    type = environment.rules.Resolve(type, false);
    if (type != null) {
      type.typing(environment);
    }
  }

  public boolean evalReadonly(boolean previous, DocumentPosition pos, Environment environment) {
    if (modifierToken != null) {
      if (modifierToken.text.equals("readonly")) {
        return true;
      } else if (modifierToken.text.equals("mutable")) {
        validateMutableType(pos, environment);
        return false;
      }
    }
    if (type.behavior == TypeBehavior.ReadOnlyWithGet) {
      return true;
    }
    return previous;
  }

  public void validateMutableType(DocumentPosition pos, Environment env) {
    if (type != null) {
      TyType resolved = env.rules.Resolve(type, false);
      if (resolved instanceof TyNativeTable) {
        // tis-valid
        return;
      }
      if (resolved instanceof TyNativeMessage) {
        return;
      }
      env.document.createError(pos, "Type " + type.getAdamaType() + " is not a mutable type.");
    }
  }
}
