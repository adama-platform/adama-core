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
package ape.translator.tree;

import ape.translator.env.ComputeContext;
import ape.translator.parser.token.Token;
import ape.translator.tree.definitions.DocumentEvent;
import ape.translator.tree.definitions.FunctionSpecialization;
import ape.translator.tree.definitions.MessageHandlerBehavior;
import ape.translator.tree.expressions.testing.EnvLookupName;
import ape.translator.tree.expressions.ConversionStyle;
import ape.translator.tree.expressions.constants.DynamicNullConstant;
import ape.translator.tree.operands.AssignmentOp;
import ape.translator.tree.operands.BinaryOp;
import ape.translator.tree.operands.PostfixMutateOp;
import ape.translator.tree.operands.PrefixMutateOp;
import ape.translator.tree.statements.ControlFlow;
import ape.translator.tree.statements.control.AlterControlFlowMode;
import ape.translator.tree.types.checking.properties.*;
import ape.translator.tree.types.checking.properties.*;
import ape.translator.tree.types.structures.StorageSpecialization;
import ape.translator.tree.types.traits.details.IndexLookupStyle;
import org.junit.Assert;
import org.junit.Test;

public class SillyEnumCoverageTests {
  @Test
  public void coverage() {
    Assert.assertNull(BinaryOp.fromText("NOPEx"));
    PostfixMutateOp.fromText("!x");
    PrefixMutateOp.fromText("!x");
    AssignmentOp.fromText("!x");
    WrapInstruction.None.toString();
    WrapInstruction.valueOf("WrapBWithMaybe");
    MessageHandlerBehavior.EnqueueItemIntoNativeChannel.toString();
    MessageHandlerBehavior.valueOf("EnqueueItemIntoNativeChannel");
    DocumentEvent.ClientConnected.toString();
    DocumentEvent.valueOf("ClientConnected");
    FunctionSpecialization.Pure.toString();
    FunctionSpecialization.valueOf("Pure");
    ConversionStyle.Maybe.toString();
    ConversionStyle.valueOf("Maybe");
    AlterControlFlowMode.Abort.toString();
    AlterControlFlowMode.valueOf("Abort");
    ControlFlow.Open.toString();
    ControlFlow.valueOf("Open");
  }

  @Test
  public void dyn_null() {
    new DynamicNullConstant(Token.WRAP("null")).emit((t) -> {});
  }

  @Test
  public void coverageSimple() {
    AssignableEmbedType.None.toString();
    CanAssignResult.No.toString();
    CanBumpResult.No.toString();
    CanMathResult.No.toString();
    CanTestEqualityResult.No.toString();
    StorageTweak.None.toString();
    WrapInstruction.None.toString();
    StorageSpecialization.Message.toString();
    ComputeContext.Assignment.toString();
    EnvLookupName.Blocked.toString();
    IndexLookupStyle.ExpressionLookupMethod.toString();
    IndexLookupStyle.ExpressionGetOrCreateMethod.toString();
    ComputeContext.Computation.toString();
  }
}
