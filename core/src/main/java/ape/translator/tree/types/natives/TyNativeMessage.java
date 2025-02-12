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
package ape.translator.tree.types.natives;

import ape.runtime.json.JsonStreamWriter;
import ape.translator.codegen.CodeGenDeltaClass;
import ape.translator.codegen.CodeGenIndexing;
import ape.translator.codegen.CodeGenMessage;
import ape.translator.env.Environment;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.DocumentPosition;
import ape.translator.parser.Formatter;
import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.definitions.DefineViewFilter;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.expressions.InjectExpression;
import ape.translator.tree.types.ReflectionSource;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.functions.FunctionPaint;
import ape.translator.tree.types.reactive.TyReactiveRecord;
import ape.translator.tree.types.reactive.TyReactiveTable;
import ape.translator.tree.types.topo.TypeCheckerRoot;
import ape.translator.tree.types.natives.functions.FunctionOverloadInstance;
import ape.translator.tree.types.natives.functions.FunctionStyleJava;
import ape.translator.tree.types.structures.DefineMethod;
import ape.translator.tree.types.structures.FieldDefinition;
import ape.translator.tree.types.structures.StructureStorage;
import ape.translator.tree.types.traits.CanBeNativeArray;
import ape.translator.tree.types.traits.IsReactiveValue;
import ape.translator.tree.types.traits.IsStructure;
import ape.translator.tree.types.traits.assign.AssignmentViaNativeOnlyForSet;
import ape.translator.tree.types.traits.details.*;
import ape.translator.tree.types.traits.details.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

public class TyNativeMessage extends TyType implements //
    IsStructure, //
    DetailTypeProducesRootLevelCode, //
    DetailHasDeltaType, //
    DetailInventDefaultValueExpression, //
    CanBeNativeArray, //
    DetailTypeHasMethods, //
    DetailNativeDeclarationIsNotStandard, //
    AssignmentViaNativeOnlyForSet {
  public Token messageToken;
  public String name;
  public Token nameToken;
  public StructureStorage storage;

  public TyNativeMessage(final TypeBehavior behavior, final Token messageToken, final Token nameToken, final StructureStorage storage) {
    super(behavior);
    this.messageToken = messageToken;
    this.nameToken = nameToken;
    name = nameToken.text;
    this.storage = storage;
    ingest(messageToken);
    ingest(storage);
  }

  public boolean hasUniqueId() {
    FieldDefinition fd = storage.fields.get("id");
    if (fd != null) {
      return fd.uniqueToken != null;
    }
    return false;
  }

  @Override
  public void compile(final StringBuilderWithTabs sb, final Environment environment) {
    // a COPY of fields to preserver order
    final var fields = new ArrayList<FieldDefinition>();
    for (final Map.Entry<String, FieldDefinition> e : storage.fields.entrySet()) {
      fields.add(e.getValue());
    }
    sb.append("private static class RTx" + name + " extends NtMessageBase");
    if (storage.isCommaSeperateValueEnabled()) {
      sb.append(" implements CanWriteCSV");
    }
    sb.append(" {").tabUp().writeNewline();
    sb.append("private final RTx" + name + " __this;").writeNewline();
    for (final FieldDefinition fd : fields) {
      sb.append("private ").append(fd.type.getJavaConcreteType(environment)).append(" ").append(fd.name);
      CodeGenMessage.writeInitValue(this, sb, fd, environment);
      sb.append(";").writeNewline();
    }
    CodeGenMessage.generateMemorySize(this, sb, environment);
    CodeGenMessage.generateReset(this, storage, sb, environment);
    CodeGenMessage.generateHashers(name, storage, sb, environment);
    CodeGenIndexing.writeIndexConstant(name, storage, sb, environment);
    CodeGenIndexing.writeIndices(name, storage, sb, environment);
    CodeGenMessage.generateJsonReaders(name, storage, sb, environment);
    if (storage.isCommaSeperateValueEnabled()) {
      CodeGenMessage.generateCSV(storage, sb);
    }
    for (final DefineMethod dm : storage.methods) {
      dm.writeFunctionJava(sb, environment.scopeStatic());
    }

    if (storage.viewFilters.size() > 0) { // hey, it's the @viewer
      Environment available = environment.scopeAsReadOnlyBoundary();
      for (FieldDefinition fd : fields) {
        available.define(fd.name, fd.type, true, fd);
      }
      for (DefineViewFilter dvf : storage.viewFilters.values()) {
        sb.append("public boolean __vf_").append(dvf.name.text).append(";").writeNewline();
      }
      for (DefineViewFilter dvf : storage.viewFilters.values()) {
        sb.append("public boolean __CVF_").append(dvf.name.text).append("()");
        dvf.code.writeJava(sb, environment.scopeAsNoCost().scopeStatic().scopeAsFilter());
        sb.writeNewline();
      }
      sb.append("public void __computeViewFilters() {").tabUp().writeNewline();
      int countdown = storage.viewFilters.size();
      for (DefineViewFilter dvf : storage.viewFilters.values()) {
        sb.append("__vf_").append(dvf.name.text).append(" = __CVF_").append(dvf.name.text).append("();");
        countdown--;
        if (countdown == 0) {
          sb.tabDown();
        }
        sb.writeNewline();
      }
      sb.append("}").writeNewline();
    }


    sb.append("@Override").writeNewline();
    if (storage.hasPostParse()) {
      sb.append("public void __parsed() throws AbortMessageException");
      storage.getPostParse().writeJava(sb, environment.scopeAsAbortable().scopeAsNoCost().scopeStatic());
      sb.writeNewline();
    } else {
      sb.append("public void __parsed() throws AbortMessageException {}").writeNewline();
    }

    { // CLOSE UP THE MESSAGE WITH A CONSTRUCTOR FOR ANONYMOUS OBJECTS
      sb.append("private RTx" + name + "() { __this = this; }");
      if (storage.fields.size() == 0) {
        sb.tabDown().writeNewline();
      } else {
        sb.writeNewline();
        sb.append("private RTx").append(name).append("(");
        var firstArg = true;
        for (final Map.Entry<String, FieldDefinition> e : storage.fields.entrySet()) {
          if (!firstArg) {
            sb.append(", ");
          }
          firstArg = false;
          sb.append(e.getValue().type.getJavaConcreteType(environment)).append(" ").append(e.getKey());
        }
        sb.append(") {").tabUp().writeNewline();
        sb.append("this.__this = this;").writeNewline();
        var countDownUntilTab = storage.fields.size();
        for (final Map.Entry<String, FieldDefinition> e : storage.fields.entrySet()) {
          sb.append("this.").append(e.getKey()).append(" = ").append(e.getKey()).append(";");
          if (--countDownUntilTab <= 0) {
            sb.tabDown();
          }
          sb.writeNewline();
        }
        sb.append("}").tabDown().writeNewline();
      }
      sb.append("}").writeNewline();
    }
    CodeGenDeltaClass.writeMessageDeltaClass(storage, sb, environment, "RTx" + name);
  }

  @Override
  public void emitInternal(final Consumer<Token> yielder) {
    yielder.accept(messageToken);
    yielder.accept(nameToken);
    storage.emit(yielder);
  }

  @Override
  public void format(Formatter formatter) {
    formatter.startLine(messageToken);
    storage.format(formatter);
  }

  @Override
  public String getAdamaType() {
    return name;
  }

  @Override
  public String getJavaBoxType(final Environment environment) {
    return String.format("RTx%s", name);
  }

  @Override
  public String getJavaConcreteType(final Environment environment) {
    return String.format("RTx%s", name);
  }

  @Override
  public TyType makeCopyWithNewPositionInternal(final DocumentPosition position, final TypeBehavior newBehavior) {
    return new TyNativeMessage(newBehavior, messageToken, nameToken, storage).withPosition(position);
  }

  @Override
  public void typing(final Environment environment) {
  }

  @Override
  public void typing(TypeCheckerRoot checker) {
    storage.typing(name, checker);

    checker.register(Collections.emptySet(), (env) -> {
      Environment available = env.scopeAsReadOnlyBoundary();
      boolean isViewer = storage.viewFilters.size() > 0;
      for (FieldDefinition fd : storage.fieldsByOrder) {
        TyType resolved = env.rules.Resolve(fd.type, false);
        if (resolved instanceof IsReactiveValue || resolved instanceof TyReactiveRecord || resolved instanceof TyReactiveTable || resolved instanceof TyNativeTable) {
          env.document.createError(TyNativeMessage.this, String.format("Messages can't have a field type of '%s'", resolved.getAdamaType()));
        }
        if (isViewer && resolved != null) { // hey, it's the @viewer
          available.define(fd.name, resolved, true, fd);
        }
      }
      if (isViewer) { // hey, it's the @viewer
        for (DefineViewFilter dvf : storage.viewFilters.values()) {
          dvf.typing(available);
        }
      }
    });
  }

  @Override
  public void writeTypeReflectionJson(JsonStreamWriter writer, ReflectionSource source) {
    if (source == ReflectionSource.Root) {
      writer.beginObject();
      writer.writeObjectFieldIntro("nature");
      writer.writeString("native_message");
      writeAnnotations(writer);
      writer.writeObjectFieldIntro("name");
      writer.writeString(name);
      writer.writeObjectFieldIntro("anonymous");
      writer.writeBoolean(storage.anonymous);
      writer.writeObjectFieldIntro("fields");
      storage.writeTypeReflectionJson(writer);
      writer.endObject();
    } else {
      writer.beginObject();
      writer.writeObjectFieldIntro("nature");
      writer.writeString("native_ref");
      writeAnnotations(writer);
      writer.writeObjectFieldIntro("ref");
      writer.writeString(name);
      writer.endObject();
    }
  }

  @Override
  public String getDeltaType(final Environment environment) {
    return "DeltaRTx" + name;
  }

  @Override
  public String getPatternWhenValueProvided(final Environment environment) {
    return "%s";
  }

  @Override
  public String getStringWhenValueNotProvided(final Environment environment) {
    return "new RTx" + name + "()";
  }

  @Override
  public Expression inventDefaultValueExpression(final DocumentPosition forWhatExpression) {
    return new InjectExpression(this) {
      @Override
      public void writeJava(final StringBuilder sb, final Environment environment) {
        sb.append("new RTx").append(name).append("()");
      }
    };
  }

  public TyNativeMessage makeAnonymousCopy() {
    return (TyNativeMessage) (new TyNativeMessage(behavior, messageToken, nameToken, storage.makeAnonymousCopy()).withPosition(this));
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public StructureStorage storage() {
    return storage;
  }

  @Override
  public TyNativeFunctional lookupMethod(String name, Environment environment) {
    if ("to_dynamic".equals(name)) {
      return new TyNativeFunctional("to_dynamic", FunctionOverloadInstance.WRAP(new FunctionOverloadInstance("to_dynamic", new TyNativeDynamic(TypeBehavior.ReadOnlyNativeValue, null, null), new ArrayList<>(), FunctionPaint.READONLY_NORMAL)), FunctionStyleJava.ExpressionThenArgs);
    }
    if ("ingest_dynamic".equals(name)) {
      ArrayList<TyType> args = new ArrayList<>();
      args.add(new TyNativeDynamic(TypeBehavior.ReadOnlyNativeValue, null, null));
      return new TyNativeFunctional("ingest_dynamic", FunctionOverloadInstance.WRAP(new FunctionOverloadInstance("ingest_dynamic", new TyNativeVoid(), args, FunctionPaint.READONLY_NORMAL)), FunctionStyleJava.ExpressionThenArgs);
    }
    if ("reset".equals(name)) {
      return new TyNativeFunctional("__reset", FunctionOverloadInstance.WRAP(new FunctionOverloadInstance("__reset", new TyNativeVoid(), new ArrayList<>(), FunctionPaint.READONLY_NORMAL)), FunctionStyleJava.ExpressionThenArgs);
    }
    return storage.methodTypes.get(name);
  }
}
