/*
* Adama Platform and Language
* Copyright (C) 2021 - 2025 by Adama Platform Engineering, LLC
* 
* This program is free software for non-commercial purposes: 
* you can redistribute it and/or modify it under the terms of the 
* GNU Affero General Public License as published by the Free Software Foundation,
* either version 3 of the License, or (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
* 
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package ape.translator.tree.statements.control;

import ape.translator.env.ComputeContext;
import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.statements.ControlFlow;
import ape.translator.tree.statements.Statement;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.checking.properties.StorageTweak;
import ape.translator.tree.types.natives.TyNativeMessage;
import ape.translator.tree.types.structures.FieldDefinition;

import java.util.ArrayList;
import java.util.function.Consumer;

/** return from the current function (and maybe with a value) */
public class Return extends Statement {
  public final Expression expression;
  public final Token returnToken;
  public final Token semicolonToken;
  private ArrayList<String> webFields;
  private TyNativeMessage webReturnType;

  private ArrayList<String> authorizationFields;
  private TyNativeMessage authorizationReturnType;

  public Return(final Token returnToken, final Expression expression, final Token semicolonToken) {
    this.returnToken = returnToken;
    this.expression = expression;
    this.semicolonToken = semicolonToken;
    webFields = null;
    webReturnType = null;
    authorizationFields = null;
    authorizationReturnType = null;
    ingest(returnToken);
    ingest(semicolonToken);
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    yielder.accept(returnToken);
    if (expression != null) {
      expression.emit(yielder);
    }
    yielder.accept(semicolonToken);
  }

  @Override
  public void format(Formatter formatter) {
    if (expression != null) {
      expression.format(formatter);
    }
  }

  private static boolean consider(String field, TyNativeMessage message, Consumer<TyType> check, ArrayList<String> fields) {
    FieldDefinition fd = message.storage.fields.get(field);
    if (fd != null) {
      check.accept(fd.type);
      fields.add(field);
      return true;
    }
    return false;
  }

  @Override
  public ControlFlow typing(final Environment environment) {
    final var expectedReturnType = environment.getMostRecentReturnType();
    if (environment.state.isAuthorize() && expectedReturnType == null) {
      final var givenReturnType = environment.rules.Resolve(expression.typing(environment.scopeWithComputeContext(ComputeContext.Computation), null), true);
      if (givenReturnType instanceof TyNativeMessage) {
        authorizationFields = new ArrayList<>();
        authorizationReturnType = (TyNativeMessage) givenReturnType;
        boolean hasHash = false;
        boolean hasAgent = false;
        boolean hasChannel = false;
        boolean hasSuccess = false;

        if (consider("hash", authorizationReturnType, (ty) -> environment.rules.IsString(ty, false), authorizationFields)) {
          hasHash = true;
        }
        if (consider("agent", authorizationReturnType, (ty) -> environment.rules.IsString(ty, false), authorizationFields)) {
          hasAgent = true;
        }
        if (consider("channel", authorizationReturnType, (ty) -> environment.rules.IsString(ty, false), authorizationFields)) {
          hasChannel = true;
        }
        if (consider("success", authorizationReturnType, (ty) -> environment.rules. IsNativeMessage(ty, false), authorizationFields)) {
          hasSuccess = true;
        }
        if (!hasHash) {
          environment.document.createError(this, String.format("The return statement within a @authorization expects a hash"));
        }
        if (!hasAgent) {
          environment.document.createError(this, String.format("The return statement within a @authorization expects an agent"));
        }
        boolean hasOne = hasSuccess || hasChannel;
        boolean hasBoth = hasSuccess && hasChannel;
        if (hasOne && !hasBoth) {
          environment.document.createError(this, String.format("The return statement within a @authorization expects both a channel and a success field"));
        }
      } else {
        environment.document.createError(this, String.format("The return statement within a @authorization expects a message type"));
      }
    } else if (environment.state.isWeb() && expectedReturnType == null) {
      final var givenReturnType = environment.rules.Resolve(expression.typing(environment.scopeWithComputeContext(ComputeContext.Computation), null), true);
      if (givenReturnType instanceof TyNativeMessage) {
        String method = environment.state.getWebMethod();
        webFields = new ArrayList<>();
        webReturnType = (TyNativeMessage) givenReturnType;
        int body = 0;
        if (consider("html", webReturnType, (ty) -> environment.rules.IsString(ty, false), webFields)) {
          body++;
        }
        if (consider("sign", webReturnType, (ty) -> environment.rules.IsString(ty, false), webFields)) {
          body++;
        }
        if (consider("xml", webReturnType, (ty) -> environment.rules.IsString(ty, false), webFields)) {
          body++;
        }
        if (consider("js", webReturnType, (ty) -> environment.rules.IsString(ty, false), webFields)) {
          body++;
        }
        if (consider("css", webReturnType, (ty) -> environment.rules.IsString(ty, false), webFields)) {
          body++;
        }
        if (consider("csv", webReturnType, (ty) -> environment.rules.IsString(ty, false), webFields)) {
          body++;
        }
        if (consider("error", webReturnType, (ty) -> environment.rules.IsString(ty, false), webFields)) {
          body++;
        }
        if (consider("json", webReturnType, (ty) -> environment.rules.IsNativeMessage(ty, false), webFields)) {
          body++;
        }
        if (consider("redirect", webReturnType, (ty) -> environment.rules.IsString(ty, false), webFields)) {
          body++;
        }
        if (consider("forward", webReturnType, (ty) -> environment.rules.IsString(ty, false), webFields)) {
          body++;
        }
        if (consider("identity", webReturnType, (ty) -> environment.rules.IsString(ty, false), webFields)) {
          body++;
        }
        if (consider("asset", webReturnType, (ty) -> environment.rules.IsAsset(ty, false), webFields)) {
          consider("asset_transform", webReturnType, (ty) -> environment.rules.IsString(ty, false), webFields);
          body++;
        }
        consider("cors", webReturnType, (ty) -> environment.rules.IsBoolean(ty, false), webFields);
        consider("cache_ttl_seconds", webReturnType, (ty) -> environment.rules.IsInteger(ty, false), webFields);
        consider("status", webReturnType, (ty) -> environment.rules.IsInteger(ty, false), webFields);

        if (method.equals("options")) {
          if (body != 0) {
            environment.document.createError(this, String.format("The return statement within a @web expects no body fields; got " + body + " instead"));
          }
        } else {
          if (body != 1) {
            environment.document.createError(this, String.format("The return statement within a @web expects exactly one body type; got " + body + " instead"));
          }
        }
      } else {
        environment.document.createError(this, String.format("The return statement within a @web expects a message type"));
      }
    } else {
      if (expression != null) {
        if (expectedReturnType != null) {
          final var givenReturnType = expression.typing(environment.scopeWithComputeContext(ComputeContext.Computation), expectedReturnType);
          if (!environment.rules.CanTypeAStoreTypeB(expectedReturnType, givenReturnType, StorageTweak.None, false)) {
            return ControlFlow.Open;
          }
        } else {
          environment.document.createError(this, String.format("The return statement expects no expression"));
        }
      } else if (expectedReturnType != null) {
        environment.document.createError(this, String.format("The return statement expected an expression of type `%s`", expectedReturnType.getAdamaType()));
      }
    }
    return ControlFlow.Returns;
  }

  @Override
  public void writeJava(final StringBuilderWithTabs sb, final Environment environment) {
    if (authorizationFields != null) {
      sb.append("{").tabUp().writeNewline();
      String exprName = "__capture" + environment.autoVariable();
      sb.append("RTx").append(authorizationReturnType.name).append(" ").append(exprName).append(" = ");
      expression.writeJava(sb, environment.scopeWithComputeContext(ComputeContext.Computation));
      sb.append(";").writeNewline();
      sb.append("return new AuthResponse()");
      for (String authField : authorizationFields) {
        sb.append(".").append(authField).append("(").append(exprName).append(".").append(authField).append(")");
      }
      sb.append(";").tabDown().writeNewline();
      sb.append("}");
    } else if (webFields != null) {
      sb.append("{").tabUp().writeNewline();
      String exprName = "__capture" + environment.autoVariable();
      sb.append("RTx").append(webReturnType.name).append(" ").append(exprName).append(" = ");
      expression.writeJava(sb, environment.scopeWithComputeContext(ComputeContext.Computation));
      sb.append(";").writeNewline();
      sb.append("return new WebResponse()");
      for (String webField : webFields) {
        sb.append(".").append(webField).append("(").append(exprName).append(".").append(webField).append(")");
      }
      sb.append(";").tabDown().writeNewline();
      sb.append("}");
    } else {
      sb.append("return");
      if (expression != null) {
        sb.append(" ");
        expression.writeJava(sb, environment.scopeWithComputeContext(ComputeContext.Computation));
      }
      sb.append(";");
    }
  }

  @Override
  public void free(FreeEnvironment environment) {
    if (expression != null) {
      expression.free(environment);
    }
  }
}
