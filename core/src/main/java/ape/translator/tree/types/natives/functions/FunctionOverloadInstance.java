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
package ape.translator.tree.types.natives.functions;

import ape.translator.env.Environment;
import ape.translator.tree.common.DocumentPosition;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.checking.properties.StorageTweak;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * a function overload instance is set of arguments bound to the same name such that the types of
 * parameters decide which function to use.
 */
public class FunctionOverloadInstance extends DocumentPosition {
  public final ArrayList<String> hiddenSuffixArgs;
  public final LinkedHashSet<String> dependencies;
  public final LinkedHashSet<String> assocs;
  public final AtomicReference<String> withinRecord;
  public final LinkedHashSet<String> recordDependencies;
  public final TreeSet<String> viewerFields;
  public final String javaFunction;
  public final boolean pure;
  public final TyType returnType;
  public final ArrayList<TyType> types;
  public final boolean castArgs;
  public final boolean castReturn;
  public final boolean aborts;
  public final boolean viewer;
  private TyType thisType;

  public FunctionOverloadInstance(final String javaFunction, final TyType returnType, final ArrayList<TyType> types, FunctionPaint paint) {
    this.javaFunction = javaFunction;
    this.returnType = returnType;
    this.types = types;
    this.pure = paint.pure;
    this.castArgs = paint.castArgs;
    this.castReturn = paint.castReturn;
    this.aborts = paint.aborts;
    this.viewer = paint.viewer;
    this.hiddenSuffixArgs = new ArrayList<>();
    this.dependencies = new LinkedHashSet<>();
    this.assocs = new LinkedHashSet<>();
    if (this.viewer) {
      hiddenSuffixArgs.add("__viewer");
    }
    this.recordDependencies = new LinkedHashSet<>();
    this.withinRecord = new AtomicReference<>("n/a");
    this.viewerFields = new TreeSet<>();
    this.thisType = thisType;
  }

  public void setThisType(TyType thisType) {
    this.thisType = thisType;
  }

  public TyType getThisType() {
    return thisType;
  }

  public static ArrayList<FunctionOverloadInstance> WRAP(final FunctionOverloadInstance foi) {
    final var list = new ArrayList<FunctionOverloadInstance>();
    list.add(foi);
    return list;
  }

  public int score(final Environment environment, final ArrayList<TyType> args) {
    var score = 0;
    if (args.size() != types.size()) {
      score = Math.abs(args.size() - types.size()) * 2;
    }
    for (var iter = 0; iter < Math.min(args.size(), types.size()); iter++) {
      if (!environment.rules.CanTypeAStoreTypeB(types.get(iter), args.get(iter), StorageTweak.FunctionScore, true)) {
        score++;
      }
      if (!environment.rules.CanTypeAStoreTypeB(args.get(iter), types.get(iter), StorageTweak.FunctionScore, true)) {
        score++;
      }
    }
    return score;
  }

  public void test(final DocumentPosition position, final Environment environment, final ArrayList<TyType> args) {
    if (args.size() != types.size()) {
      environment.document.createError(position, String.format("Function invoked with wrong number of arguments. Expected %d, got %d", types.size(), args.size()));
    }
    for (var iter = 0; iter < Math.min(args.size(), types.size()); iter++) {
      environment.rules.CanTypeAStoreTypeB(types.get(iter), args.get(iter), StorageTweak.None, false);
    }
  }

  public void testOverlap(final FunctionOverloadInstance other, final Environment environment) {
    if (types.size() != other.types.size()) {
      return;
    }
    var sameCount = 0;
    for (var iter = 0; iter < types.size(); iter++) {
      final var l2r = environment.rules.CanTypeAStoreTypeB(types.get(iter), other.types.get(iter), StorageTweak.None, true);
      final var r2l = environment.rules.CanTypeAStoreTypeB(other.types.get(iter), types.get(iter), StorageTweak.None, true);
      if (l2r && r2l) {
        sameCount++;
      }
    }
    if (sameCount == types.size()) {
      environment.document.createError(this, "Overloaded function has many identical calls");
    }
  }

  public void typing(final Environment environment) {
    if (returnType != null) {
      returnType.typing(environment);
    }
    for (final TyType argType : types) {
      argType.typing(environment);
    }
  }
}
