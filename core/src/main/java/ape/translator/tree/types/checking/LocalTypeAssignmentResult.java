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
package ape.translator.tree.types.checking;

import ape.translator.env.ComputeContext;
import ape.translator.env.Environment;
import ape.translator.tree.common.DocumentPosition;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.checking.properties.CanAssignResult;
import ape.translator.tree.types.checking.properties.StorageTweak;
import ape.translator.tree.types.natives.TyNativeLong;
import ape.translator.tree.types.reactive.TyReactiveComplex;

public class LocalTypeAssignmentResult {
  private final Environment environment;
  private final Expression expression;
  private final Expression ref;
  public CanAssignResult assignResult = CanAssignResult.No;
  public TyType ltype = null;
  public TyType rtype = null;

  public LocalTypeAssignmentResult(final Environment environment, final Expression ref, final Expression expression) {
    this.environment = environment;
    this.ref = ref;
    this.expression = expression;
  }

  public boolean bad() {
    return ltype == null || rtype == null;
  }

  private void common(String op) {
    ltype = environment.rules.Resolve(ref.typing(environment.scopeWithComputeContext(ComputeContext.Assignment), null), false);
    rtype = environment.rules.Resolve(expression.typing(environment.scopeWithComputeContext(ComputeContext.Computation), null), false);
    if (ltype != null && rtype != null) {
      if (ltype.behavior.isReadOnly) {
        environment.document.createError(DocumentPosition.sum(ltype, rtype), String.format("'%s' is unable to %s '%s' due to readonly left reference.", ltype.getAdamaType(), op, rtype.getAdamaType()));
      }
    }
  }

  public void ingest() {
    common("ingest");
    if (!environment.rules.CanAIngestB(ltype, rtype, false)) {
      environment.document.createError(ref, "Unable to  the right hand side");
    }
    assignResult = CanAssignResult.YesWithIngestionCodeGen;
  }

  public void set() {
    common("set");
    if (ltype instanceof TyReactiveComplex) {
      if (environment.rules.IsNumeric(rtype, true) || rtype instanceof TyNativeLong) {
        assignResult = CanAssignResult.YesWithSetter;
        return;
      }
    }
    assignResult = environment.rules.CanAssignWithSet(ltype, rtype, false);
    environment.rules.CanTypeAStoreTypeB(ltype, rtype, StorageTweak.None, false);
  }
}
