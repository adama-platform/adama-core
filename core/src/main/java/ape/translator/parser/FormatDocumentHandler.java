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
import ape.translator.tree.types.structures.BubbleDefinition;
import ape.translator.tree.types.structures.FieldDefinition;
import ape.translator.tree.types.structures.JoinAssoc;
import ape.translator.tree.types.structures.ReplicationDefinition;
import ape.translator.tree.types.traits.IsEnum;
import ape.translator.tree.types.traits.IsStructure;

/** format a document, yay! */
public class FormatDocumentHandler implements TopLevelDocumentHandler{

  public final Formatter formatter;

  public FormatDocumentHandler(Formatter formatter) {
    this.formatter = formatter;
  }

  @Override
  public void add(BubbleDefinition bd) {
    bd.format(formatter);
  }

  @Override
  public void add(DefineConstructor dc) {
    dc.format(formatter);
  }

  @Override
  public void add(DefineViewFilter viewFilter) {
    viewFilter.format(formatter);
  }

  @Override
  public void add(DefineCustomPolicy customPolicy) {
    customPolicy.format(formatter);
  }

  @Override
  public void add(DefineDispatcher dd) {
    dd.format(formatter);
  }

  @Override
  public void add(DefineDocumentEvent dce) {
    dce.format(formatter);
  }

  @Override
  public void add(DefineFunction func) {
    func.format(formatter);
  }

  @Override
  public void add(DefineHandler handler) {
    handler.format(formatter);
  }

  @Override
  public void add(DefineStateTransition transition) {
    transition.format(formatter);
  }

  @Override
  public void add(DefineTest test) {
    test.format(formatter);
  }

  @Override
  public void add(FieldDefinition fd) {
    fd.format(formatter);
  }

  @Override
  public void add(IsEnum storage) {
    storage.format(formatter);
  }

  @Override
  public void add(IsStructure storage) {
    storage.format(formatter);
  }

  @Override
  public void add(Token token) {
  }

  @Override
  public void add(AugmentViewerState avs) {
    avs.format(formatter);
  }

  @Override
  public void add(DefineRPC rpc) {
    rpc.format(formatter);
  }

  @Override
  public void add(DefineStatic ds) {
    ds.format(formatter);
  }

  @Override
  public void add(DefineWebGet dwg) {
    dwg.format(formatter);
  }

  @Override
  public void add(DefineWebPut dwp) {
    dwp.format(formatter);
  }

  @Override
  public void add(DefineWebOptions dwo) {
    dwo.format(formatter);
  }

  @Override
  public void add(DefineWebDelete dwd) {
    dwd.format(formatter);
  }

  @Override
  public void add(Include in, Scope rootScope) {
    in.format(formatter);
  }

  @Override
  public void add(LinkService link, Scope rootScope) {
    link.format(formatter);
  }

  @Override
  public void add(DefineService ds) {
    ds.format(formatter);
  }

  @Override
  public void add(DefineAuthorization da) {
    da.format(formatter);
  }

  @Override
  public void add(DefinePassword dp) {
    dp.format(formatter);
  }

  @Override
  public void add(DefineAuthorizationPipe da) {
    da.format(formatter);
  }

  @Override
  public void add(ReplicationDefinition rd) {
    rd.format(formatter);
  }

  @Override
  public void add(DefineMetric dm) {
    dm.format(formatter);
  }

  @Override
  public void add(DefineAssoc da) {
    da.format(formatter);
  }

  @Override
  public void add(JoinAssoc ja) {
    ja.format(formatter);
  }

  @Override
  public void add(DefineTemplate dt) {
    dt.format(formatter);
  }

  @Override
  public void add(DefineCronTask dct) {
    dct.format(formatter);
  }

  @Override
  public void add(DefineTrafficHint dth) {
    dth.format(formatter);
  }
}
