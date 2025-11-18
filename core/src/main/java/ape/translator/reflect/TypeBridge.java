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
package ape.translator.reflect;

import ape.runtime.natives.*;
import ape.runtime.reactives.RxBoolean;
import ape.runtime.reactives.RxGrid;
import ape.translator.tree.types.natives.*;
import ape.runtime.natives.*;
import ape.translator.tree.common.TokenizedItem;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.*;
import ape.translator.tree.types.reactive.TyReactiveBoolean;
import ape.translator.tree.types.reactive.TyReactiveMap;

/** convert a known java type into an Adama type */
public class TypeBridge {

  public static TyType getAdamaSubType(String core, final Class<?>... hiddenTypes) {
    if (hiddenTypes == null || hiddenTypes.length == 0) {
      throw new RuntimeException(core + " requires @HiddenType/@HiddenTypes annotation because Java sucks");
    }
    Class<?> head = hiddenTypes[0];
    Class<?>[] tail = new Class[hiddenTypes.length - 1];
    for (int k = 0; k < tail.length; k++) {
      tail[k] = hiddenTypes[k + 1];
    }
    return getAdamaType(head, tail);
  }

  public static TyType getAdamaType(final Class<?> x, final Class<?>[] hiddenTypes) {
    if (int.class == x || Integer.class == x) {
      return new TyNativeInteger(TypeBehavior.ReadOnlyNativeValue, null, null);
    } else if (Long.class == x || long.class == x) {
      return new TyNativeLong(TypeBehavior.ReadOnlyNativeValue, null, null);
    } else if (Double.class == x || double.class == x) {
      return new TyNativeDouble(TypeBehavior.ReadOnlyNativeValue, null, null);
    } else if (Boolean.class == x || boolean.class == x) {
      return new TyNativeBoolean(TypeBehavior.ReadOnlyNativeValue, null, null);
    } else if (String.class == x) {
      return new TyNativeString(TypeBehavior.ReadOnlyNativeValue, null, null);
    } else if (String[].class == x) {
      return new TyNativeArray(TypeBehavior.ReadOnlyNativeValue, new TyNativeString(TypeBehavior.ReadOnlyNativeValue, null, null), null);
    } else if (int[].class == x) {
      return new TyNativeArray(TypeBehavior.ReadOnlyNativeValue, new TyNativeInteger(TypeBehavior.ReadOnlyNativeValue, null, null), null);
    } else if (NtPrincipal.class == x) {
      return new TyNativePrincipal(TypeBehavior.ReadOnlyNativeValue, null, null);
    } else if (NtComplex.class == x) {
      return new TyNativeComplex(TypeBehavior.ReadOnlyNativeValue, null, null);
    } else if (NtTemplate.class == x) {
      return new TyNativeTemplate(null);
    } else if (NtDate.class == x) {
      return new TyNativeDate(TypeBehavior.ReadOnlyNativeValue, null, null);
    } else if (NtDateTime.class == x) {
      return new TyNativeDateTime(TypeBehavior.ReadOnlyNativeValue, null, null);
    } else if (NtTime.class == x) {
      return new TyNativeTime(TypeBehavior.ReadOnlyNativeValue, null, null);
    } else if (NtTimeSpan.class == x) {
      return new TyNativeTimeSpan(TypeBehavior.ReadOnlyNativeValue, null, null);
    } else if (NtMatrix2.class == x) {
      return new TyNativeMatrix2(TypeBehavior.ReadOnlyNativeValue, null, null);
    } else if (NtMatrix3.class == x) {
      return new TyNativeMatrix3(TypeBehavior.ReadOnlyNativeValue, null, null);
    } else if (NtMatrix4.class == x) {
      return new TyNativeMatrix4(TypeBehavior.ReadOnlyNativeValue, null, null);
    } else if (NtMatrixH4.class == x) {
      return new TyNativeMatrixH4(TypeBehavior.ReadOnlyNativeValue, null, null);
    } else if (NtVec2.class == x) {
      return new TyNativeVec2(TypeBehavior.ReadOnlyNativeValue, null, null);
    } else if (NtVec3.class == x) {
      return new TyNativeVec3(TypeBehavior.ReadOnlyNativeValue, null, null);
    } else if (NtVec4.class == x) {
      return new TyNativeVec4(TypeBehavior.ReadOnlyNativeValue, null, null);
    } else if (Void.class == x || void.class == x) {
      return null;
    } else if (NtJson.class == x) {
       return new TyNativeJson(TypeBehavior.ReadWriteNative, null, null);
    } else if (NtDynamic.class == x) {
      return new TyNativeDynamic(TypeBehavior.ReadWriteNative, null, null);
    } else if (NtComplex.class == x) {
      return new TyNativeComplex(TypeBehavior.ReadWriteNative, null, null);
    } else if (NtList.class == x) {
      TyType subType = getAdamaSubType("NtList<>", hiddenTypes);
      return new TyNativeList(TypeBehavior.ReadOnlyNativeValue, null, null, new TokenizedItem<>(subType));
    } else if (NtMaybe.class == x) {
      TyType subType = getAdamaSubType("NtMaybe<>", hiddenTypes);
      return new TyNativeMaybe(TypeBehavior.ReadOnlyNativeValue, null, null, new TokenizedItem<>(subType));
    } else if (NtMap.class == x) {
      if (hiddenTypes == null || hiddenTypes.length != 2) {
        throw new RuntimeException("NtMap<> requires two hidden types");
      }
      TyType subTypeDomain = getAdamaSubType("NtMap<>::Domain", hiddenTypes[0]);
      TyType subTypeRange = getAdamaSubType("NtMap<>::Range", hiddenTypes[1]);
      return new TyNativeMap(TypeBehavior.ReadOnlyNativeValue, null, null, null, subTypeDomain, null, subTypeRange, null);
    } else if (NtGrid.class == x) {
      if (hiddenTypes == null || hiddenTypes.length != 2) {
        throw new RuntimeException("NtGrid<> requires two hidden types");
      }
      TyType subTypeDomain = getAdamaSubType("NtMap<>::Domain", hiddenTypes[0]);
      TyType subTypeRange = getAdamaSubType("NtMap<>::Range", hiddenTypes[1]);
      return new TyNativeGrid(TypeBehavior.ReadOnlyNativeValue, null, null, null, subTypeDomain, null, subTypeRange, null);
    }
    throw new RuntimeException("can't find:" + x.toString());
  }
}
