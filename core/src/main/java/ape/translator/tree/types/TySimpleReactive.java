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
package ape.translator.tree.types;

import ape.translator.env.Environment;
import ape.translator.parser.token.Token;
import ape.translator.parser.Formatter;
import ape.translator.tree.types.traits.IsReactiveValue;
import ape.translator.tree.types.traits.assign.AssignmentViaSetter;
import ape.translator.tree.types.traits.details.DetailComputeRequiresGet;
import ape.translator.tree.types.traits.details.DetailInventDefaultValueExpression;

import java.util.function.Consumer;

public abstract class TySimpleReactive extends TyType implements DetailComputeRequiresGet, //
    IsReactiveValue, //
    DetailInventDefaultValueExpression, //
    AssignmentViaSetter //
{
  public final String reactiveTreeType;
  public final Token token;
  public final boolean readonly;

  public TySimpleReactive(final boolean readonly, final Token token, final String reactiveTreeType) {
    super(readonly ? TypeBehavior.ReadOnlyWithGet : TypeBehavior.ReadWriteWithSetGet);
    this.readonly = readonly;
    this.token = token;
    this.reactiveTreeType = reactiveTreeType;
    ingest(token);
  }

  @Override
  public void emitInternal(final Consumer<Token> yielder) {
    yielder.accept(token);
  }

  @Override
  public void format(Formatter formatter) {
  }

  @Override
  public String getJavaBoxType(final Environment environment) {
    return reactiveTreeType;
  }

  @Override
  public String getJavaConcreteType(final Environment environment) {
    return reactiveTreeType;
  }

  @Override
  public void typing(final Environment environment) {
  }
}
