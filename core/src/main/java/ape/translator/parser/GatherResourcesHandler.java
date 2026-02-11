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

import ape.translator.env2.Scope;
import ape.translator.parser.exceptions.AdamaLangException;
import ape.translator.parser.token.Token;
import ape.translator.parser.token.TokenEngine;
import ape.translator.tree.SymbolIndex;
import ape.translator.tree.definitions.*;
import ape.translator.tree.definitions.config.DefineDocumentEvent;
import ape.translator.tree.privacy.DefineCustomPolicy;
import ape.translator.tree.types.structures.BubbleDefinition;
import ape.translator.tree.types.structures.FieldDefinition;
import ape.translator.tree.types.structures.JoinAssoc;
import ape.translator.tree.types.structures.ReplicationDefinition;
import ape.translator.tree.types.traits.IsEnum;
import ape.translator.tree.types.traits.IsStructure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Function;

/** helper class to gather the includes and resolve them recursively */
public class GatherResourcesHandler implements TopLevelDocumentHandler {
  public final Function<String, String> resolver;
  public final HashSet<String> includes;
  public final ArrayList<String> errors;

  public GatherResourcesHandler(Function<String, String> resolver) {
    this.resolver = resolver;
    this.includes = new HashSet<>();
    this.errors = new ArrayList<>();
  }

  @Override
  public void add(BubbleDefinition bd) {

  }

  @Override
  public void add(DefineConstructor dc) {
  }

  @Override
  public void add(DefineViewFilter viewFilter) {
  }

  @Override
  public void add(DefineCustomPolicy customPolicy) {
  }

  @Override
  public void add(DefineDispatcher dd) {
  }

  @Override
  public void add(DefineDocumentEvent dce) {
  }

  @Override
  public void add(DefineFunction func) {

  }

  @Override
  public void add(DefineHandler handler) {

  }

  @Override
  public void add(DefineStateTransition transition) {

  }

  @Override
  public void add(DefineTest test) {

  }

  @Override
  public void add(FieldDefinition fd) {

  }

  @Override
  public void add(IsEnum storage) {

  }

  @Override
  public void add(IsStructure storage) {

  }

  @Override
  public void add(Token token) {

  }

  @Override
  public void add(AugmentViewerState avs) {

  }

  @Override
  public void add(DefineRPC rpc) {

  }

  @Override
  public void add(DefineStatic ds) {

  }

  @Override
  public void add(DefineWebGet dwg) {

  }

  @Override
  public void add(DefineWebPut dwp) {

  }

  @Override
  public void add(DefineWebOptions dwo) {

  }

  @Override
  public void add(DefineWebDelete dwd) {

  }

  @Override
  public void add(Include in, Scope rootScope) {
    includes.add(in.import_name);
    String code = resolver.apply(in.import_name);
    if (code != null) {
      final var tokenEngine = new TokenEngine(in.import_name, code.codePoints().iterator());
      final var parser = new Parser(tokenEngine, new SymbolIndex(), rootScope);
      try {
        parser.document().accept(this);
      } catch (AdamaLangException ale) {
        errors.add(in.import_name + ":" + ale.getMessage());
      }
    } else {
      errors.add("could not resolve: " + in.import_name);
    }
  }

  @Override
  public void add(LinkService link, Scope rootScope) {
  }

  @Override
  public void add(DefineService ds) {
  }

  @Override
  public void add(DefineAuthorizationPipe da) {
  }

  @Override
  public void add(ReplicationDefinition rd) {}

  @Override
  public void add(DefineMetric dm) {}

  @Override
  public void add(DefineAssoc da) {
  }

  @Override
  public void add(JoinAssoc ja) {
  }

  @Override
  public void add(DefineTemplate dt) {
  }

  @Override
  public void add(DefineCronTask dct) {
  }

  @Override
  public void add(DefineTrafficHint dth) {
  }

  @Override
  public void add(DefineClientService dhttp) {
  }

  @Override
  public void add(DefineExport de) {
  }
}
