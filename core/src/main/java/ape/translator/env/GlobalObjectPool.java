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
package ape.translator.env;

import ape.runtime.stdlib.*;
import ape.translator.tree.types.natives.*;
import ape.runtime.stdlib.runtime.LibRuntimeBeta;
import ape.runtime.stdlib.runtime.LibRuntimeProduction;
import ape.runtime.stdlib.runtime.LibRuntimeTooling;
import ape.translator.reflect.GlobalFactory;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.functions.FunctionOverloadInstance;
import ape.translator.tree.types.natives.functions.FunctionPaint;
import ape.translator.tree.types.natives.functions.FunctionStyleJava;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

/** a pool of global objects like Math, Random, String */
public class GlobalObjectPool {
  protected final HashMap<String, HashMap<String, TyNativeFunctional>> extensions;
  private final HashMap<String, TyNativeGlobalObject> globalObjects;

  private GlobalObjectPool() {
    globalObjects = new HashMap<>();
    extensions = new HashMap<>();
  }

  public static GlobalObjectPool createPoolWithStdLib(RuntimeEnvironment runtime) {
    final TyNativeString tyStr = new TyNativeString(TypeBehavior.ReadOnlyNativeValue, null, null);
    final TyNativeInteger tyInt = new TyNativeInteger(TypeBehavior.ReadOnlyNativeValue, null, null);
    final TyNativeDouble tyDbl = new TyNativeDouble(TypeBehavior.ReadOnlyNativeValue, null, null);
    final TyNativeLong tyLng = new TyNativeLong(TypeBehavior.ReadOnlyNativeValue, null, null);
    final TyNativeBoolean tyBool = new TyNativeBoolean(TypeBehavior.ReadOnlyNativeValue, null, null);
    final TyNativePrincipal tyPrincipal = new TyNativePrincipal(TypeBehavior.ReadOnlyNativeValue, null, null);
    final TyNativeVoid tyVoid = new TyNativeVoid();
    final TyNativeDynamic tyDyn = new TyNativeDynamic(TypeBehavior.ReadOnlyNativeValue, null, null);

    final var pool = new GlobalObjectPool();
    pool.add(GlobalFactory.makeGlobal("String", LibString.class, pool.extensions));
    pool.add(GlobalFactory.makeGlobal("Math", LibMath.class, pool.extensions));
    pool.add(GlobalFactory.makeGlobal("Statistics", LibStatistics.class, pool.extensions));
    pool.add(GlobalFactory.makeGlobal("Date", LibDate.class, pool.extensions));
    pool.add(GlobalFactory.makeGlobal("TimeSpan", LibTimeSpan.class, pool.extensions));
    pool.add(GlobalFactory.makeGlobal("Map", LibMap.class, pool.extensions));
    pool.add(GlobalFactory.makeGlobal("Templates", LibTemplates.class, pool.extensions));
    pool.add(GlobalFactory.makeGlobal("Template", LibTemplate.class, pool.extensions));
    pool.add(GlobalFactory.makeGlobal("Search", LibSearch.class, pool.extensions));
    pool.add(GlobalFactory.makeGlobal("Token", LibToken.class, pool.extensions));
    pool.add(GlobalFactory.makeGlobal("HTML", LibHTML.class, pool.extensions));

    switch (runtime) {
      case Tooling:
        pool.add(GlobalFactory.makeGlobal("Runtime", LibRuntimeTooling.class, pool.extensions));
        break;
      case DevBox:
      case Beta:
        pool.add(GlobalFactory.makeGlobal("Runtime", LibRuntimeBeta.class, pool.extensions));
        break;
      case Production:
        pool.add(GlobalFactory.makeGlobal("Runtime", LibRuntimeProduction.class, pool.extensions));
        break;
    }

    final var client = new TyNativeGlobalObject("Principal", null, false);
    client.setParentOverride(GlobalFactory.makeGlobal("Principal", LibPrincipal.class, pool.extensions));
    client.functions.put("principalOf", generateInternalDocumentFunction("__principalOf", tyStr, tyPrincipal, "principalOf", pool.extensions));
    client.functions.put("isFromDocument", generateInternalDocumentFunction("__isFromDocument", tyPrincipal, tyBool, "isFromDocument", pool.extensions));
    client.functions.put("isFromSpace", generateInternalDocumentFunction("__isFromSpace", tyPrincipal, tyBool, "isFromSpace", pool.extensions));
    pool.add(client);

    pool.add(GlobalFactory.makeGlobal("Dynamic", LibDynamic.class, pool.extensions));
    pool.add(GlobalFactory.makeGlobal("Json", LibJson.class, pool.extensions));
    pool.add(GlobalFactory.makeGlobal("Graph", LibGraph.class, pool.extensions));
    final var document = new TyNativeGlobalObject("Document", null, false);
    document.functions.put("destroy", generateInternalDocumentFunction("__destroyDocument", tyVoid));
    document.functions.put("rewind", generateInternalDocumentFunction("__rewindDocument", tyInt, tyVoid, null, null));
    document.functions.put("key", generateInternalDocumentFunction("__getKey", tyStr));
    document.functions.put("space", generateInternalDocumentFunction("__getSpace", tyStr));
    document.functions.put("seq", generateInternalDocumentFunction("__getSeq", tyInt));
    document.functions.put("disconnect", generateInternalDocumentFunction("__disconnect", tyPrincipal, tyVoid, null, null));
    document.functions.put("patch", generateInternalDocumentFunction("__applyPatch", tyDyn, tyVoid, null, null));
    pool.add(document);

    final var viewer = new TyNativeGlobalObject("ViewState", null, false);
    viewer.functions.put("merge", generateInternalDocumentFunction("__mergeViewState", tyDyn, tyBool, null, null));
    viewer.functions.put("goto", generateInternalDocumentFunction("__gotoViewState", tyStr, tyBool, null, null));
    viewer.functions.put("send",generateInternalDocumentFunction("__sendViewState", tyStr, tyDyn, tyBool, null, null));
    viewer.functions.put("log",generateInternalDocumentFunction("__logViewState", tyStr, tyBool, null, null));
    pool.add(viewer);

    final var random = new TyNativeGlobalObject("Random", null, false);
    random.functions.put("genBoundInt", generateInternalDocumentFunction("__randomBoundInt", tyInt, tyInt, null, null));
    random.functions.put("genInt", generateInternalDocumentFunction("__randomInt", tyInt));
    random.functions.put("genDouble", generateInternalDocumentFunction("__randomDouble", tyDbl));
    random.functions.put("getDoubleGaussian", generateInternalDocumentFunction("__randomGaussian", tyDbl));
    random.functions.put("genLong", generateInternalDocumentFunction("__randomLong", tyLng));
    pool.add(random);
    final var time = new TyNativeGlobalObject("Time", null, false);
    time.functions.put("now", subscribe("__time", generateInternalDocumentFunction("__timeNow", tyLng)));
    time.functions.put("today", subscribe("__today", generateInternalDocumentFunction("__dateOfToday", new TyNativeDate(TypeBehavior.ReadOnlyNativeValue, null, null))));
    time.functions.put("datetime", subscribe("__timeDelay", generateInternalDocumentFunction("__datetimeNow", new TyNativeDateTime(TypeBehavior.ReadOnlyNativeValue, null, null))));
    time.functions.put("datetimeLive", subscribe("__time", generateInternalDocumentFunction("__datetimeNow", new TyNativeDateTime(TypeBehavior.ReadOnlyNativeValue, null, null))));
    time.functions.put("time", generateInternalDocumentFunction("__timeOfToday", new TyNativeTime(TypeBehavior.ReadOnlyNativeValue, null, null)));
    time.functions.put("zone", generateInternalDocumentFunction("__timeZone", tyStr));
    time.functions.put("setZone", generateInternalDocumentFunction("__setTimeZone", tyStr, tyBool, null, null));
    time.setParentOverride((GlobalFactory.makeGlobal("LibTime", LibTime.class, pool.extensions)));
    pool.add(time);
    return pool;
  }

  /** common policy for watch to ignore various names and types */
  public static boolean ignoreCapture(String name, TyType ty) {
    if (ty instanceof TyNativeGlobalObject || ty instanceof TyNativeFunctional || ty instanceof TyNativeTemplate) {
      return true;
    }
    switch (name) {
      case "__time":
      case "__today":
        return true;
      default:
        return false;
    }
  }

  private static TyNativeFunctional subscribe(String depend, TyNativeFunctional func) {
    for (FunctionOverloadInstance foi : func.overloads) {
      foi.dependencies.add(depend);
    }
    return func;
  }

  public void add(final TyNativeGlobalObject globalObject) {
    globalObjects.put(globalObject.globalName, globalObject);
  }

  private static TyNativeFunctional generateInternalDocumentFunction(final String name, final TyType returnType) {
    final var overloads = new ArrayList<FunctionOverloadInstance>();
    final var args = new ArrayList<TyType>();
    overloads.add(new FunctionOverloadInstance(name, returnType, args, FunctionPaint.READONLY_NORMAL));
    return new TyNativeFunctional(name, overloads, FunctionStyleJava.InjectNameThenArgs);
  }

  private static TyNativeFunctional generateInternalDocumentFunction(final String name, final TyType arg, final TyType returnType, String adamaName, final HashMap<String, HashMap<String, TyNativeFunctional>> extensions) {
    final var overloads = new ArrayList<FunctionOverloadInstance>();
    final var args = new ArrayList<TyType>();
    args.add(arg);
    FunctionOverloadInstance foi = new FunctionOverloadInstance(name, returnType, args, FunctionPaint.READONLY_NORMAL);
    overloads.add(foi);
    if (extensions != null) {
      HashMap<String, ArrayList<FunctionOverloadInstance>> byFirstParameterType = new HashMap<>();
      GlobalFactory.prepareForExtension(foi, byFirstParameterType);
      GlobalFactory.injectExtension(adamaName, byFirstParameterType, extensions);
    }
    return new TyNativeFunctional(name, overloads, FunctionStyleJava.InjectNameThenArgs);
  }

  private static TyNativeFunctional generateInternalDocumentFunction(final String name, final TyType arg1, TyType arg2, final TyType returnType, String adamaName, final HashMap<String, HashMap<String, TyNativeFunctional>> extensions) {
    final var overloads = new ArrayList<FunctionOverloadInstance>();
    final var args = new ArrayList<TyType>();
    args.add(arg1);
    args.add(arg2);
    FunctionOverloadInstance foi = new FunctionOverloadInstance(name, returnType, args, FunctionPaint.READONLY_NORMAL);
    overloads.add(foi);
    if (extensions != null) {
      HashMap<String, ArrayList<FunctionOverloadInstance>> byFirstParameterType = new HashMap<>();
      GlobalFactory.prepareForExtension(foi, byFirstParameterType);
      GlobalFactory.injectExtension(adamaName, byFirstParameterType, extensions);
    }
    return new TyNativeFunctional(name, overloads, FunctionStyleJava.InjectNameThenArgs);
  }

  public TyNativeFunctional findExtension(TyType type, String name) {
    HashMap<String, TyNativeFunctional> extensionsOnType = extensions.get(type.getAdamaType());
    if (extensionsOnType != null) {
      return extensionsOnType.get(name);
    }
    return null;
  }

  public TyNativeGlobalObject get(final String name) {
    return globalObjects.get(name);
  }

  public TreeSet<String> imports() {
    final var x = new TreeSet<String>();
    for (final TyNativeGlobalObject o : globalObjects.values()) {
      if (o.importPackage != null) {
        x.add(o.importPackage);
      }
    }
    return x;
  }
}
