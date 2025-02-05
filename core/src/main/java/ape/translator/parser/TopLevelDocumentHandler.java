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
import ape.translator.parser.token.Token;
import ape.translator.tree.definitions.*;
import ape.translator.tree.definitions.config.DefineDocumentEvent;
import ape.translator.tree.privacy.DefineCustomPolicy;
import ape.translator.tree.types.structures.BubbleDefinition;
import ape.translator.tree.types.structures.FieldDefinition;
import ape.translator.tree.types.structures.JoinAssoc;
import ape.translator.tree.types.structures.ReplicationDefinition;
import ape.translator.tree.types.traits.IsEnum;
import ape.translator.tree.types.traits.IsStructure;

/** the Parser will pump these messages */
public interface TopLevelDocumentHandler {
  void add(BubbleDefinition bd);

  void add(DefineConstructor dc);

  void add(DefineViewFilter viewFilter);
  
  void add(DefineCustomPolicy customPolicy);

  void add(DefineDispatcher dd);

  void add(DefineDocumentEvent dce);

  void add(DefineFunction func);

  void add(DefineHandler handler);

  void add(DefineStateTransition transition);

  void add(DefineTest test);

  void add(FieldDefinition fd);

  void add(IsEnum storage);

  void add(IsStructure storage);

  void add(Token token);

  void add(AugmentViewerState avs);

  void add(DefineRPC rpc);

  void add(DefineStatic ds);

  void add(DefineWebGet dwg);

  void add(DefineWebPut dwp);

  void add(DefineWebOptions dwo);

  void add(DefineWebDelete dwd);

  void add(Include in, Scope rootScope);

  void add(LinkService link, Scope rootScope);

  void add(DefineService ds);

  void add(DefineAuthorization da);

  void add(DefinePassword dp);

  void add(DefineAuthorizationPipe da);

  void add(ReplicationDefinition rd);

  void add(DefineMetric dm);

  void add(DefineAssoc da);

  void add(JoinAssoc ja);

  void add(DefineTemplate dt);

  void add(DefineCronTask dct);

  void add(DefineTrafficHint dth);

  void add(DefineClientService dhttp);
}
