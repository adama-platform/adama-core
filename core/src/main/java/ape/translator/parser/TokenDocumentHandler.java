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
package ape.translator.parser;

import ape.translator.env2.Scope;
import ape.translator.parser.token.Token;
import ape.translator.tree.definitions.*;
import ape.translator.tree.definitions.*;
import ape.translator.tree.definitions.config.DefineDocumentEvent;
import ape.translator.tree.privacy.DefineCustomPolicy;
import ape.translator.tree.types.natives.TyNativeEnum;
import ape.translator.tree.types.structures.BubbleDefinition;
import ape.translator.tree.types.structures.FieldDefinition;
import ape.translator.tree.types.structures.JoinAssoc;
import ape.translator.tree.types.structures.ReplicationDefinition;
import ape.translator.tree.types.traits.IsEnum;
import ape.translator.tree.types.traits.IsStructure;

import java.util.function.Consumer;

public abstract class TokenDocumentHandler implements Consumer<Token>, TopLevelDocumentHandler {
  @Override
  public void add(final BubbleDefinition bd) {
    bd.emit(this);
  }

  @Override
  public void add(final DefineConstructor dc) {
    dc.emit(this);
  }

  @Override
  public void add(DefineViewFilter viewFilter) {
    viewFilter.emit(this);
  }

  @Override
  public void add(final DefineCustomPolicy customPolicy) {
    customPolicy.emit(this);
  }

  @Override
  public void add(final DefineDispatcher dd) {
    dd.emit(this);
  }

  @Override
  public void add(final DefineDocumentEvent dce) {
    dce.emit(this);
  }

  @Override
  public void add(final DefineFunction func) {
    func.emit(this);
  }

  @Override
  public void add(final DefineHandler handler) {
    handler.emit(this);
  }

  @Override
  public void add(final DefineStateTransition transition) {
    transition.emit(this);
  }

  @Override
  public void add(final DefineTest test) {
    test.emit(this);
  }

  @Override
  public void add(final FieldDefinition fd) {
    fd.emit(this);
  }

  @Override
  public void add(final IsEnum storage) {
    if (storage instanceof TyNativeEnum) {
      ((TyNativeEnum) storage).emit(this);
    }
  }

  @Override
  public void add(final IsStructure storage) {
    storage.emit(this);
  }

  @Override
  public void add(final Token token) {
    accept(token);
  }

  @Override
  public void add(AugmentViewerState avs) {
    avs.emit(this);
  }

  @Override
  public void add(DefineRPC rpc) {
    rpc.emit(this);
  }

  @Override
  public void add(DefineStatic ds) {
    ds.emit(this);
  }

  @Override
  public void add(DefineWebGet dwg) {
    dwg.emit(this);
  }

  @Override
  public void add(DefineWebPut dwp) {
    dwp.emit(this);
  }

  @Override
  public void add(DefineWebOptions dwo) {
    dwo.emit(this);
  }

  @Override
  public void add(DefineWebDelete dwd) {
    dwd.emit(this);
  }

  @Override
  public void add(Include in, Scope rootScope) {
    in.emit(this);
  }

  @Override
  public void add(LinkService link, Scope rootScope) { link.emit(this); }

  @Override
  public void add(DefineService ds) {
    ds.emit(this);
  }

  @Override
  public void add(DefineAuthorization da) {
    da.emit(this);
  }

  @Override
  public void add(DefinePassword dp) { dp.emit(this); }

  @Override
  public void add(DefineAuthorizationPipe da) {
    da.emit(this);
  }

  @Override
  public void add(ReplicationDefinition rd) {
    rd.emit(this);
  }

  @Override
  public void add(DefineMetric dm) {
    dm.emit(this);
  }

  @Override
  public void add(DefineAssoc da) {
    da.emit(this);
  }

  @Override
  public void add(JoinAssoc ja) {
    ja.emit(this);
  }

  @Override
  public void add(DefineTemplate dt) {
    dt.emit(this);
  }

  @Override
  public void add(DefineCronTask dct) {
    dct.emit(this);
  }

  @Override
  public void add(DefineTrafficHint dth) {
    dth.emit(this);
  }
}
