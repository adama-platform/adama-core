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
package ape.translator.tree.types.reactive;

import ape.runtime.json.JsonStreamWriter;
import ape.translator.codegen.*;
import ape.translator.tree.types.structures.*;
import ape.translator.codegen.*;
import ape.translator.env.Environment;
import ape.translator.parser.Formatter;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.DocumentPosition;
import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.types.ReflectionSource;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.TyNativeFunctional;
import ape.translator.tree.types.natives.functions.FunctionOverloadInstance;
import ape.translator.tree.types.natives.functions.FunctionPaint;
import ape.translator.tree.types.natives.functions.FunctionStyleJava;
import ape.translator.tree.types.natives.functions.TyNativeFunctionInternalFieldReplacement;
import ape.translator.tree.types.structures.*;
import ape.translator.tree.types.topo.TypeCheckerRoot;
import ape.translator.tree.types.traits.DetailNeedsSettle;
import ape.translator.tree.types.traits.IsKillable;
import ape.translator.tree.types.traits.IsStructure;
import ape.translator.tree.types.traits.details.DetailHasDeltaType;
import ape.translator.tree.types.traits.details.DetailTypeHasMethods;
import ape.translator.tree.types.traits.details.DetailTypeProducesRootLevelCode;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

public class TyReactiveRecord extends TyType implements //
    IsStructure, //
    DetailNeedsSettle, //
    DetailHasDeltaType, //
    IsKillable, //
    DetailTypeProducesRootLevelCode, //
    DetailTypeHasMethods {
  public String name;
  public Token nameToken;
  public Token recordToken;
  public StructureStorage storage;

  public TyReactiveRecord(final Token recordToken, final Token nameToken, final StructureStorage storage) {
    super(TypeBehavior.ReadWriteWithSetGet);
    this.recordToken = recordToken;
    this.nameToken = nameToken;
    name = nameToken.text;
    this.storage = storage;
    ingest(recordToken);
    ingest(nameToken);
    ingest(storage);
  }

  @Override
  public void compile(final StringBuilderWithTabs sb, final Environment environment) {
    final var classFields = new StringBuilderWithTabs().tabUp().tabUp();
    final var classConstructor = new StringBuilderWithTabs().tabUp().tabUp().tabUp();
    final var classLinker = new StringBuilderWithTabs().tabUp().tabUp().tabUp();
    CodeGenRecords.writeCommonBetweenRecordAndRoot(storage, classConstructor, classLinker, classFields, environment.scope(), true);
    classConstructor.append("if (__owner instanceof RxTable) {").tabUp().writeNewline();
    var colNum = 0;
    for (final IndexDefinition idefn : storage.indices) {
      classFields.append("private final ReactiveIndexInvalidator __INDEX_").append(idefn.nameToken.text).append(";").writeNewline();
      classConstructor.append("__INDEX_").append(idefn.nameToken.text).append(" = new ReactiveIndexInvalidator(((RxTable<RTx").append(name).append(">)(__owner)).getIndex((short)").append("" + colNum).append("), this) {").tabUp().tabUp().writeNewline();
      classConstructor.append("@Override").writeNewline();
      classConstructor.append("public int pullValue() {").tabUp().writeNewline();
      final var fd = storage.fields.get(idefn.nameToken.text);
      if (fd != null) {
        classConstructor.append("  return ");
        CodeGenIndexing.IndexClassification classification = new CodeGenIndexing.IndexClassification(fd.type);
        classConstructor.append(String.format(classification.indexValueMethod, idefn.nameToken.text));
      }
      classConstructor.append(";").tabDown().writeNewline();
      classConstructor.append("}").tabDown().writeNewline();
      classConstructor.append("};").tabDown().writeNewline();
      classConstructor.append(idefn.nameToken.text).append(".__subscribe(__INDEX_").append(idefn.nameToken.text).append(");").writeNewline();
      colNum++;
    }
    classConstructor.append("/* ok */").tabDown().writeNewline();
    classConstructor.append("} else {").tabUp().writeNewline();
    for (final IndexDefinition idefn : storage.indices) {
      classConstructor.append("__INDEX_").append(idefn.nameToken.text).append(" = null;").writeNewline();
    }
    classConstructor.append("/* ok */").tabDown().writeNewline();
    classConstructor.append("}").writeNewline();
    CodeGenIndexing.writeIndexConstant(name, storage, sb, environment);
    sb.append("private class RTx" + name + " extends RxRecordBase<RTx").append(name).append("> {").tabUp().writeNewline();
    sb.append("private final RTx" + name + " __this;").writeNewline();
    sb.append(classFields.toString());
    sb.append("private RTx" + name + "(RxParent __owner) {");
    final var classConstructorStripped = classConstructor.toString().stripTrailing();
    sb.tabUp().writeNewline();
    sb.append(classConstructorStripped);
    sb.append("").tabDown().writeNewline().append("}").writeNewline();
    for (final DefineMethod dm : storage.methods) {
      dm.writeFunctionJava(sb, environment);
    }
    CodeGenRecords.writePrivacyCommonBetweenRecordAndRoot(storage, sb, environment, false);
    CodeGenIndexing.writeIndices(name, storage, sb, environment);
    CodeGenRecords.writeCommitAndRevert(storage, sb, environment, false);
    CodeGenReport.writeRxReport(storage, sb, environment);
    String linkerCompact = classLinker.toString().stripTrailing();
    sb.append("@Override").writeNewline();
    sb.append("public RTx" + name + " __link() {").tabUp().writeNewline();
    if (linkerCompact.trim().length() > 0) {
      sb.append(linkerCompact).writeNewline();
    }
    sb.append("return this;").tabDown().writeNewline();
    sb.append("}").writeNewline();
    {
      sb.append("@Override").writeNewline();
      sb.append("public void __invalidateIndex(TablePubSub __pubsub) {");
      int countdown = storage.indices.size();
      if (countdown > 0) {
        sb.tabUp().writeNewline();
      }
      int indexVal = 0;
      for (IndexDefinition index : storage.indices) {
        countdown--;
        sb.append("__pubsub.index(").append("" + indexVal).append(",").append(index.nameToken.text).append(".getIndexValue());");
        if (countdown == 0) {
          sb.tabDown();
        }
        sb.writeNewline();
        indexVal++;
      }
      sb.append("}").writeNewline();
    }
    {
      sb.append("@Override").writeNewline();
      sb.append("public void __pumpIndexEvents(TablePubSub __pubsub) {");
      int countdown = storage.indices.size();
      if (countdown > 0) {
        sb.tabUp().writeNewline();
      }
      int indexVal = 0;
      for (IndexDefinition index : storage.indices) {
        countdown--;
        sb.append(index.nameToken.text).append(".setWatcher(__value -> __pubsub.index(").append(indexVal + "").append(", __value));");
        if (countdown == 0) {
          sb.tabDown();
        }
        sb.writeNewline();
        indexVal++;
      }
      sb.append("}").writeNewline();
    }
    if (storage.hasPostIngestion()) {
      sb.append("@Override").writeNewline();
      sb.append("public void __postIngest() ");
      storage.getPostIngestion().writeJava(sb, environment);
      sb.writeNewline();
    }
    sb.append("@Override").writeNewline();
    sb.append("public String __name() {").tabUp().writeNewline();
    sb.append("return \"").append(name).append("\";").tabDown().writeNewline();
    sb.append("}").writeNewline();
    sb.append("@Override").writeNewline();
    sb.append("public void __deindex() {").tabUp().writeNewline();
    for (final IndexDefinition idefn : storage.indices) {
      sb.append("__INDEX_").append(idefn.nameToken.text).append(".deindex();").writeNewline();
    }
    sb.append("/* ok */").tabDown().writeNewline();
    sb.append("}").writeNewline();
    sb.append("public void __reindex() {").tabUp().writeNewline();
    for (final IndexDefinition idefn : storage.indices) {
      sb.append("__INDEX_").append(idefn.nameToken.text).append(".reindex();").writeNewline();
    }
    sb.append("/* ok */").tabDown().writeNewline();
    sb.append("}").writeNewline();
    sb.append("@Override").writeNewline();
    sb.append("public int __id() {").tabUp().writeNewline();
    sb.append("return id.get();").tabDown().writeNewline();
    sb.append("}").writeNewline();
    sb.append("@Override").writeNewline();
    sb.append("public void __setId(int __id, boolean __force) {").tabUp().writeNewline();
    sb.append("if (__force) {").tabUp().writeNewline();
    sb.append("id.forceSet(__id);").tabDown().writeNewline();
    sb.append("} else {").tabUp().writeNewline();
    sb.append("id.set(__id);").tabDown().writeNewline();
    sb.append("}").tabDown().writeNewline();
    sb.append("}").tabDown().writeNewline();
    sb.append("}").writeNewline();
    CodeGenDeltaClass.writeRecordDeltaClass(storage, sb, environment, "RTx" + name, false);
    CodeGenDynCompare.writeDynCompare(storage, sb, environment, "RTx" + name);
  }

  @Override
  public void format(Formatter formatter) {
    storage.format(formatter);
  }

  @Override
  public void emitInternal(final Consumer<Token> yielder) {
    yielder.accept(recordToken);
    yielder.accept(nameToken);
    storage.emit(yielder);
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
    return new TyReactiveRecord(recordToken, nameToken, storage).withPosition(position);
  }

  @Override
  public void typing(final Environment environment) {
  }

  @Override
  public void writeTypeReflectionJson(JsonStreamWriter writer, ReflectionSource source) {
    if (source == ReflectionSource.Root) {
      writer.beginObject();
      writer.writeObjectFieldIntro("nature");
      writer.writeString("reactive_record");
      writeAnnotations(writer);
      writer.writeObjectFieldIntro("name");
      writer.writeString(name);
      writer.writeObjectFieldIntro("fields");
      storage.writeTypeReflectionJson(writer);
      writer.endObject();
    } else {
      writer.beginObject();
      writer.writeObjectFieldIntro("nature");
      writer.writeString("reactive_ref");
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
  public TyNativeFunctional lookupMethod(final String name, final Environment environment) {
    if (!environment.state.isPure()) {
      if ("delete".equals(name) && storage.specialization == StorageSpecialization.Record) {
        return new TyNativeFunctionInternalFieldReplacement("__delete", FunctionOverloadInstance.WRAP(new FunctionOverloadInstance("__delete", null, new ArrayList<>(), FunctionPaint.NORMAL)), FunctionStyleJava.ExpressionThenArgs);
      }
      return storage.methodTypes.get(name);
    }
    return null;
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
  public void typing(TypeCheckerRoot checker) {
    final var fdId = storage.fields.get("id");
    if (fdId == null || !(fdId.type instanceof TyReactiveInteger)) {
      checker.issueError(this, "id must be type int");
    }
    storage.typing(name, checker);
  }

  public void transferIntoCyclicGraph(Map<String, Set<String>> graph) {
    TreeSet<String> depends = new TreeSet<>();
    for (FieldDefinition fd : storage().fieldsByOrder) {
      String depend = fd.getCycleType();
      if (depend != null) {
        depends.add(depend);
      }
    }
    graph.put(name, depends);
  }
}
