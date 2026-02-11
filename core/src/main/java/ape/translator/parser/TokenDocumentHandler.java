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
package ape.translator.parser;

import ape.translator.env.Environment;
import ape.translator.env.FreeEnvironment;
import ape.translator.env2.Scope;
import ape.translator.parser.token.Token;
import ape.translator.tree.definitions.*;
import ape.translator.tree.definitions.config.DefineDocumentEvent;
import ape.translator.tree.privacy.DefineCustomPolicy;
import ape.translator.tree.statements.ControlFlow;
import ape.translator.tree.types.natives.TyNativeEnum;
import ape.translator.tree.types.structures.BubbleDefinition;
import ape.translator.tree.types.structures.FieldDefinition;
import ape.translator.tree.types.structures.JoinAssoc;
import ape.translator.tree.types.structures.ReplicationDefinition;
import ape.translator.tree.types.topo.TypeCheckerRoot;
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

  @Override
  public void add(DefineClientService dhttp) {
    dhttp.emit(this);
  }

  @Override
  public void add(DefineExport de) { de.emit(this); }
}
