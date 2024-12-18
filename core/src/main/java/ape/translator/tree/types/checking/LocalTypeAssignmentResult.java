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
