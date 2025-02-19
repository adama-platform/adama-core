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

import ape.common.graph.Cycle;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.remote.ServiceRegistry;
import ape.translator.codegen.*;
import ape.translator.tree.common.*;
import ape.translator.tree.definitions.*;
import ape.translator.tree.types.structures.*;
import ape.translator.env.Environment;
import ape.translator.env.EnvironmentState;
import ape.translator.env.topo.TopologicalSort;
import ape.translator.env2.Scope;
import ape.translator.parser.Parser;
import ape.translator.parser.TopLevelDocumentHandler;
import ape.translator.parser.exceptions.AdamaLangException;
import ape.translator.parser.exceptions.ParseException;
import ape.translator.parser.exceptions.ScanException;
import ape.translator.parser.token.Token;
import ape.translator.parser.token.TokenEngine;
import ape.translator.tree.definitions.config.DefineDocumentEvent;
import ape.translator.tree.definitions.config.DocumentConfig;
import ape.translator.tree.definitions.web.UriTable;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.privacy.DefineCustomPolicy;
import ape.translator.tree.privacy.PrivatePolicy;
import ape.translator.tree.types.ReflectionSource;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.natives.TyNativeTemplate;
import ape.translator.tree.types.reactive.TyReactiveLong;
import ape.translator.tree.types.topo.TypeCheckerRoot;
import ape.translator.tree.types.natives.TyNativeEnum;
import ape.translator.tree.types.natives.TyNativeFunctional;
import ape.translator.tree.types.natives.TyNativeMessage;
import ape.translator.tree.types.natives.functions.FunctionOverloadInstance;
import ape.translator.tree.types.natives.functions.FunctionStyleJava;
import ape.translator.tree.types.reactive.TyReactiveRecord;
import ape.translator.tree.types.traits.IsEnum;
import ape.translator.tree.types.traits.IsStructure;
import ape.translator.tree.types.traits.details.DetailTypeProducesRootLevelCode;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

public class Document implements TopLevelDocumentHandler {
  public final HashMap<String, String> channelToMessageType;
  public final ArrayList<DefineDocumentEvent> events;
  public final ArrayList<DefineConstructor> constructors;
  public final ArrayList<DefineFunction> functionDefinitions;
  public final HashMap<String, DefineFunction> pureFunctions;
  public final HashMap<String, TyNativeFunctional> functionTypes;
  public final HashMap<String, TyNativeTemplate> templateTypes;
  public final ArrayList<DefineHandler> handlers;
  public final TyReactiveRecord root;
  public final ArrayList<DefineTest> tests;
  public final LinkedHashMap<String, DefineStateTransition> transitions;
  public final LinkedHashMap<String, TyType> types;
  public final TyNativeMessage viewerType;
  public final HashMap<String, Expression> configs;
  private final TreeMap<String, LatentCodeSnippet> dedupedLatentCodeSnippets;
  private final ArrayList<DocumentError> errorLists;
  private final HashSet<String> functionsDefines;
  private final ArrayList<LatentCodeSnippet> latentCodeSnippets;
  private final ArrayList<File> searchPaths;
  private final TypeCheckerRoot typeChecker;
  public final ArrayList<DefineAuthorization> auths;
  public final ArrayList<DefineAuthorizationPipe> authPipes;
  public final ArrayList<DefinePassword> passwords;
  private int autoClassId;
  private String className;
  public final UriTable webGet;
  public final UriTable webPut;
  public final UriTable webOptions;
  public final UriTable webDelete;
  private final HashMap<String, String> includes;
  public final LinkedHashMap<String, DefineService> services;
  public final LinkedHashMap<String, DefineClientService> clientServices;
  private final HashSet<String> defined;
  private final HashSet<String> viewDefined;
  public final LinkedHashMap<String, DefineMetric> metrics;
  public final LinkedHashMap<String, DefineAssoc> assocs;
  public final LinkedHashMap<String, DefineTemplate> templates;
  public final LinkedHashMap<String, DefineCronTask> cronTasks;
  private File includeRoot;
  private final SymbolIndex index;
  public DefineTrafficHint trafficHint;

  public Document() {
    autoClassId = 0;
    errorLists = new ArrayList<>();
    typeChecker = new TypeCheckerRoot();
    root = new TyReactiveRecord(null, Token.WRAP("Root"), new StructureStorage(Token.WRAP("Root"), StorageSpecialization.Record, false, true, null));
    types = new LinkedHashMap<>();
    handlers = new ArrayList<>();
    transitions = new LinkedHashMap<>();
    tests = new ArrayList<>();
    events = new ArrayList<>();
    channelToMessageType = new HashMap<>();
    searchPaths = new ArrayList<>();
    constructors = new ArrayList<>();
    latentCodeSnippets = new ArrayList<>();
    dedupedLatentCodeSnippets = new TreeMap<>();
    className = "DemoDocument";
    functionDefinitions = new ArrayList<>();
    functionTypes = new HashMap<>();
    templateTypes = new HashMap<>();
    functionsDefines = new HashSet<>();
    pureFunctions = new HashMap<>();
    configs = new HashMap<>();
    viewerType = new TyNativeMessage(TypeBehavior.ReadOnlyNativeValue, null, Token.WRAP("__ViewerType"), new StructureStorage(Token.WRAP("__ViewerType"), StorageSpecialization.Message, true, false, null));
    types.put("__ViewerType", viewerType);
    webGet = new UriTable();
    webPut = new UriTable();
    webOptions = new UriTable();
    webDelete = new UriTable();
    includes = new HashMap<>();
    services = new LinkedHashMap<>();
    clientServices = new LinkedHashMap<>();
    defined = new HashSet<>();
    viewDefined = new HashSet<>();
    auths = new ArrayList<>();
    passwords = new ArrayList<>();
    metrics = new LinkedHashMap<>();
    assocs = new LinkedHashMap<>();
    templates = new LinkedHashMap<>();
    authPipes = new ArrayList<>();
    cronTasks = new LinkedHashMap<>();
    includeRoot = null;
    index = new SymbolIndex();
    trafficHint = null;
  }

  public SymbolIndex getSymbolIndex() {
    return this.index;
  }

  public void setIncludes(Map<String, String> include) {
    this.includes.putAll(include);
  }

  public void setIncludeRoot(File includeRoot) {
    this.includeRoot = includeRoot;
  }

  public void writeTypeReflectionJson(JsonStreamWriter writer) {
    writer.beginObject();

    // types
    writer.writeObjectFieldIntro("types");
    writer.beginObject();
    writer.writeObjectFieldIntro("__Root");
    root.writeTypeReflectionJson(writer, ReflectionSource.Root);
    for (Map.Entry<String, TyType> type : types.entrySet()) {
      writer.writeObjectFieldIntro(type.getKey());
      type.getValue().writeTypeReflectionJson(writer, ReflectionSource.Root);
    }
    writer.endObject();

    writer.writeObjectFieldIntro("channels");
    writer.beginObject();
    for (Map.Entry<String, String> mapping : channelToMessageType.entrySet()) {
      writer.writeObjectFieldIntro(mapping.getKey());
      writer.writeString(mapping.getValue());
    }
    writer.endObject();

    writer.writeObjectFieldIntro("channels-privacy");
    writer.beginObject();
    for (DefineHandler dh : handlers) {
      if (!channelToMessageType.containsKey(dh.channel)) {
        continue;
      }
      writer.writeObjectFieldIntro(dh.channel);
      writer.beginObject();
      writer.writeObjectFieldIntro("open");
      writer.writeBoolean(dh.isOpen());
      writer.writeObjectFieldIntro("privacy");
      writer.beginArray();
      if (dh.guard != null) {
        for (TokenizedItem<String> policy : dh.guard.policies) {
          writer.writeString(policy.item);
        }
      }
      writer.endArray();
      writer.endObject();
    }
    writer.endObject();

    String unified_constructor = null;
    writer.writeObjectFieldIntro("constructors");
    writer.beginArray();
    for (DefineConstructor dc : constructors) {
      if (dc.messageNameToken != null) {
        writer.writeString(dc.messageNameToken.text);
      }
      if (dc.unifiedMessageTypeNameToUse != null) {
        unified_constructor = dc.unifiedMessageTypeNameToUse;
      }
    }
    writer.endArray();

    if (unified_constructor != null) {
      writer.writeObjectFieldIntro("constructor");
      writer.writeString(unified_constructor);
    }

    writer.writeObjectFieldIntro("labels");
    writer.beginArray();
    for (String label : transitions.keySet()) {
      writer.writeString(label);
    }
    writer.endArray();

    // TODO: rpc (once I sort them out)
    writer.endObject();
  }

  @Override
  public void add(final BubbleDefinition bd) {
    if (root.storage.has(bd.nameToken.text)) {
      typeChecker.issueError(bd, String.format("Global field '%s' was already defined", bd.nameToken.text));
      return;
    }
    root.storage().addFromRoot(bd, typeChecker);
  }

  @Override
  public void add(final DefineConstructor dc) {
    constructors.add(dc);
    // TODO ADD BACK (SEE BELOW)
    // dc.typing(typeChecker);
  }

  @Override
  public void add(DefineViewFilter viewFilter) {
    if (viewerType.storage.viewFilters.containsKey(viewFilter.name.text)) {
      typeChecker.issueError(viewFilter, String.format("Global view filter '%s' was already defined", viewFilter.name.text));
      return;
    }
    viewerType.storage.viewFilters.put(viewFilter.name.text, viewFilter);
  }

  @Override
  public void add(final DefineCustomPolicy customPolicy) {
    if (root.storage.policies.containsKey(customPolicy.name.text)) {
      typeChecker.issueError(customPolicy, String.format("Global policy '%s' was already defined", customPolicy.name.text));
      return;
    }
    root.storage.policies.put(customPolicy.name.text, customPolicy);
    customPolicy.typing(typeChecker);
  }

  @Override
  public void add(final DefineDispatcher dd) {
    final var type = types.get(dd.enumNameToken.text);
    if (type != null && type instanceof TyNativeEnum) {
      dd.typing(typeChecker);
      ((TyNativeEnum) type).storage.associate(dd);
    } else {
      if (type == null) {
        typeChecker.issueError(dd, String.format("Dispatcher '%s' was unable to find the given enumeration type of '%s'", dd.functionName.text, dd.enumNameToken.text));
      } else {
        typeChecker.issueError(dd, String.format("Dispatcher '%s' found '%s', but it was '%s'", dd.functionName.text, dd.enumNameToken.text, type.getAdamaType()));
      }
    }
  }

  @Override
  public void add(final DefineDocumentEvent dce) {
    dce.typing(typeChecker);
    events.add(dce);
  }

  @Override
  public void add(Include in, Scope rootScope) {
    if (in.import_name.equals("main")) {
      typeChecker.issueError(in, "main is a reserved word for importing");
    }

    String codeToParseIntoDoc = includes.get(in.import_name);
    if (codeToParseIntoDoc == null) {
      typeChecker.issueError(in, String.format("Failed to include '%s' as it was not bound to the deployment", in.import_name));
    } else {
      final var tokenEngine = new TokenEngine( includeRoot != null ? new File(includeRoot, in.import_name + ".adama").getAbsolutePath() : in.import_name + ".adama", codeToParseIntoDoc.codePoints().iterator());
      final var parser = new Parser(tokenEngine, index, rootScope);
      try {
        parser.document().accept(this);
      } catch (AdamaLangException ale) {
        typeChecker.issueError(in, String.format("Inclusion of '%s' resulted in an error; '%s'", in.import_name, ale.getMessage()));
      }
    }
  }

  @Override
  public void add(LinkService link, Scope rootScope) {
    int id = inventClassId();
    String defn = ServiceRegistry.getLinkDefinition(link.name.text, id, link.toParams(), link.names(), (err) -> {
      typeChecker.issueError(link, err);
    });
    if (defn == null) {
      typeChecker.issueError(link, String.format("The link '%s' was not found.", link.name.text));
      return;
    }
    final var tokenEngine = new TokenEngine("link:" + link.name.text, defn.codePoints().iterator());
    final var parser = new Parser(tokenEngine, index, rootScope);
    try {
      parser.document().accept(this);
    } catch (AdamaLangException ale) {
      typeChecker.issueError(link, String.format("Linkage of '%s' resulted in an error; '%s'", link.name.text, ale.getMessage()));
    }
  }

  @Override
  public void add(DefineService ds) {
    if (defined.contains(ds.name.text)) {
      typeChecker.issueError(ds, String.format("The service '%s' was already defined.", ds.name.text));
    }
    services.put(ds.name.text, ds);
    defined.add(ds.name.text);
    ds.typing(typeChecker);
  }

  @Override
  public void add(DefineClientService dhttp) {
    if (defined.contains(dhttp.name.text)) {
      typeChecker.issueError(dhttp, String.format("The http service '%s' was already defined.", dhttp.name.text));
    }
    clientServices.put(dhttp.name.text, dhttp);
    defined.add(dhttp.name.text);
    dhttp.typing(typeChecker);
  }

  @Override
  public void add(ReplicationDefinition rd) {
    if (defined.contains(rd.name.text)) {
      typeChecker.issueError(rd, String.format("The replication '%s' has a conflicting name.", rd.name.text));
    }
    defined.add(rd.name.text);
    root.storage.addFromRoot(rd, typeChecker);
  }

  @Override
  public void add(DefineAuthorization da) {
    if (auths.size() >= 1) {
      typeChecker.issueError(da, "Only one @authorize action allowed");
    }
    auths.add(da);
    da.typing(typeChecker);
  }

  @Override
  public void add(DefineAuthorizationPipe da) {
   if (authPipes.size() >= 1) {
     typeChecker.issueError(da, "Only one @authorization action allowed");
   }
   authPipes.add(da);
   da.typing(typeChecker);
  }

  @Override
  public void add(DefinePassword dp) {
    if (passwords.size() >= 1) {
      typeChecker.issueError(dp, "Only one @password action allowed");
    }
    passwords.add(dp);
    dp.typing(typeChecker);
  }

  @Override
  public void add(final DefineFunction func) {
    if (defined.contains(func.name)) {
      typeChecker.issueError(func, String.format("The %s '%s' was already defined.", func.specialization == FunctionSpecialization.Pure ? "function" : "procedure", func.name));
    }
    if (func.specialization == FunctionSpecialization.Pure) {
      pureFunctions.put(func.name, func);
    }
    functionsDefines.add(func.name);
    functionDefinitions.add(func);
    func.typing(typeChecker);
  }

  @Override
  public void add(final DefineHandler handler) {
    handlers.add(handler);
    channelToMessageType.put(handler.channel, handler.typeName);
    if (functionsDefines.contains(handler.channel) || defined.contains(handler.channel)) {
      typeChecker.issueError(handler, String.format("Handler '%s' was already defined.", handler.channel));
    }
    defined.add(handler.channel);
    handler.typing(typeChecker);
  }

  @Override
  public void add(final DefineStateTransition transition) {
    transitions.put(transition.name, transition);
    transition.typing(typeChecker);
  }

  @Override
  public void add(final DefineTest test) {
    tests.add(test);
    test.typing(typeChecker);
  }

  @Override
  public void add(final FieldDefinition fd) {
    if (root.storage.has(fd.name) || defined.contains(fd.name)) {
      typeChecker.issueError(fd, String.format("Global field '%s' was already defined", fd.name));
      return;
    }
    defined.add(fd.name);
    root.storage.addFromRoot(fd, typeChecker);
  }

  @Override
  public void add(final IsEnum storage) {
    if (storage instanceof TyType) {
      if (types.containsKey(storage.name())) {
        TyType prior = types.get(storage.name());
        typeChecker.issueError((TyType) storage, String.format("The enumeration '%s' was already defined.", storage.name()));
        typeChecker.issueError(prior, String.format("The enumeration '%s' was defined here.", storage.name()));
        return;
      }
      storage.storage().typing(typeChecker);;
      for (final String s : storage.storage().duplicates) {
        typeChecker.issueError((TyType) storage, String.format("The enumeration '%s' has duplicates for '%s' defined.", storage.name(), s));
      }
      types.put(storage.name(), (TyType) storage);
    }
  }

  @Override
  public void add(final IsStructure storage) {
    if (storage instanceof TyType) {
      if (types.containsKey(storage.name())) {
        TyType prior = types.get(storage.name());
        typeChecker.issueError((TyType) storage, String.format("The %s '%s' was already defined.", storage instanceof TyNativeMessage ? "message" : "record", storage.name()));
        typeChecker.issueError(prior, String.format("The %s '%s' was defined here.", prior instanceof TyNativeMessage ? "message" : "record", storage.name()));
      }
      types.put(storage.name(), (TyType) storage);
      storage.typing(typeChecker);
    }
  }

  @Override
  public void add(final Token token) {
    // no-op
  }

  @Override
  public void add(AugmentViewerState avs) {
    if (viewDefined.contains(avs.name.text)) {
      typeChecker.issueError(avs, String.format("View field '%s' was already defined.", avs.name.text));
    }
    viewDefined.add(avs.name.text);
    viewerType.storage.add(new FieldDefinition(null, null, avs.type, avs.name, null, null, null, null, null, null, null, avs.semicolon));
    avs.typing(typeChecker);
  }

  @Override
  public void add(DefineRPC rpc) {
    TyNativeMessage nativeMessageType = rpc.genTyNativeMessage();
    types.put(rpc.genMessageTypeName(), nativeMessageType);
    channelToMessageType.put(rpc.name.text, rpc.genMessageTypeName());
    rpc.typing(typeChecker);
  }

  @Override
  public void add(DefineCronTask dct) {
    if (defined.contains(dct.name.text) || root.storage.has("__" + dct.name.text)) {
      createError(dct, String.format("Cron task has a conflicting name", dct.name.text));
      return;
    }
    defined.add(dct.name.text);
    FieldDefinition lastTimeBreach = new FieldDefinition(new PrivatePolicy(dct.cron), dct.cron, new TyReactiveLong(false, dct.cron), dct.name.cloneWithNewText("__" + dct.name.text), null, null, null, null, null, null, null, null);
    root.storage.add(lastTimeBreach);
    cronTasks.put(dct.name.text, dct);
    dct.typing(typeChecker);
  }

  @Override
  public void add(DefineWebGet dwg) {
    dwg.typing(typeChecker);
    if (!webGet.map(dwg.uri, dwg)) {
      createError(dwg, String.format("Web get path %s has a conflict", dwg.uri));
    }
  }

  @Override
  public void add(DefineWebDelete dwd) {
    dwd.typing(typeChecker);
    if (!webDelete.map(dwd.uri, dwd)) {
      createError(dwd, String.format("Web delete path %s has a conflict", dwd.uri));
    }
  }

  @Override
  public void add(DefineWebPut dwp) {
    dwp.typing(typeChecker);
    if (!webPut.map(dwp.uri, dwp)) {
      createError(dwp, String.format("Web put path %s has a conflict", dwp.uri));
    }
  }

  @Override
  public void add(DefineWebOptions dwo) {
    dwo.typing(typeChecker);
    if (!webOptions.map(dwo.uri, dwo)) {
      createError(dwo, String.format("Web options path %s has a conflict", dwo.uri));
    }
  }

  @Override
  public void add(DefineStatic ds) {
    ds.typing(typeChecker);
    events.addAll(ds.events);
    for (DocumentConfig config : ds.configs) {
      configs.put(config.name.text, config.value);
    }
  }

  @Override
  public void add(DefineMetric dm) {
    if (metrics.containsKey(dm.nameToken.text)) {
      typeChecker.issueError(dm, String.format("Metric '%s' was already defined.", dm.nameToken.text));
    } else {
      metrics.put(dm.nameToken.text, dm);
    }
    dm.typing(typeChecker);
  }

  @Override
  public void add(DefineTrafficHint trafficHint) {
    if (this.trafficHint != null) {
      typeChecker.issueError(trafficHint, "Traffic hint was already defined.");
      return;
    }
    this.trafficHint = trafficHint;
    trafficHint.typing(typeChecker);
  }

  @Override
  public void add(DefineAssoc da) {
    if (assocs.containsKey(da.name.text)) {
      typeChecker.issueError(da, String.format("Assoc '%s' was already defined.", da.name.text));
      return;
    }
    da.typing(typeChecker);
    assocs.put(da.name.text, da);
  }

  @Override
  public void add(JoinAssoc ja) {
    root.storage.addFromRoot(ja, typeChecker);
  }

  @Override
  public void add(DefineTemplate dt) {
    if (defined.contains(dt.nameToken.text)) {
      typeChecker.issueError(dt, String.format("Template '%s' was already defined; name used", dt.nameToken.text));
      return;
    }
    typeChecker.define(dt.nameToken, Collections.emptySet(), (env) -> dt.value.typing(env, null));
    templates.put(dt.nameToken.text, dt);
  }

  /**
   * @param filename the filename to import
   * @param position the position within the document (can't be null, use DocumentPosition.ZERO for
   * initial import)
   */
  public void processMain(final String filename, final DocumentPosition position) {
    final var file = search(filename);
    if (!file.exists()) {
      createError(position, String.format("File '%s' was not found", filename));
      return;
    }
    try {
      final var tokenEngine = new TokenEngine(filename, Files.readString(file.toPath()).codePoints().iterator());
      final var parser = new Parser(tokenEngine, index, Scope.makeRootDocument());
      parser.document().accept(this);
    } catch (final ScanException e) {
      createError(position, String.format("File '%s' failed to lex: %s", filename, e.getMessage()));
      createError(position, String.format("Import failed (Lex): %s", e.getMessage()));
    } catch (final ParseException e) {
      createError(e.toDocumentPosition(), String.format("File '%s' failed to parse: %s", filename, e.getMessage()));
      createError(position, String.format("Import failed (Parse): %s", e.getMessage()));
    } catch (final Exception e) {
      createError(position, String.format("File '%s' failed to import due '" + e.getMessage() + "'", filename));
    }
  }

  /**
   * search for the given filename in the search paths; consumer must check if file exists or not
   */
  private File search(final String filename) {
    var file = new File(filename);
    final var search = searchPaths.iterator();
    while (!file.exists() && search.hasNext()) {
      file = new File(search.next(), filename);
    }
    return file;
  }

  /** create an error with a reference to a tutorial */
  public DocumentError createError(final DocumentPosition position, final String message) {
    final var err = new DocumentError(position, message);
    errorLists.add(err);
    return err;
  }

  public void add(final LatentCodeSnippet snippet) {
    latentCodeSnippets.add(snippet);
  }

  public void add(final String key, final LatentCodeSnippet snippet) {
    dedupedLatentCodeSnippets.put(key, snippet);
  }

  /** add a search path for importing files */
  public void addSearchPath(final File path) {
    searchPaths.add(path);
  }

  /** check the document is valid */
  public boolean check(final EnvironmentState state) {
    final var environment = Environment.fresh(this, state);
    // we wall all functions to give them their ID and then index them
    final var functionIndex = new HashMap<String, ArrayList<DefineFunction>>();
    for (final DefineFunction df : functionDefinitions) {
      df.getFuncId(environment);
      var index = functionIndex.get(df.name);
      if (index == null) {
        index = new ArrayList<>();
        functionIndex.put(df.name, index);
      }
      index.add(df);
    }
    for (final DefineDocumentEvent de : events) {
      if (de.which == DocumentEvent.AskInvention) {
        for (DefineConstructor c : constructors) {
          if (c.messageTypeToken != null) {
            createError(de, "Invention requires all constructors to not accept messages");
          }
        }
      }
    }
    for (final Map.Entry<String, DefineTemplate> entry : templates.entrySet()) {
      templateTypes.put(entry.getKey(), (TyNativeTemplate) entry.getValue().value.typing(environment, null));
    }
    for (final Map.Entry<String, ArrayList<DefineFunction>> entry : functionIndex.entrySet()) {
      final var instances = new ArrayList<FunctionOverloadInstance>();
      for (final DefineFunction df : entry.getValue()) {
        instances.add(df.toFunctionOverloadInstance());
      }
      final var functional = new TyNativeFunctional(entry.getKey(), instances, FunctionStyleJava.InjectNameThenArgs);
      functionTypes.put(entry.getKey(), functional);
      typeChecker.register(Collections.emptySet(), env -> functional.typing(env));
    }
    viewerType.typing(typeChecker);
    typeChecker.check(environment);
    {
      TreeMap<String, Set<String>> graph = new TreeMap<>();
      for (TyType type : types.values()) {
        if (type instanceof TyReactiveRecord) {
          ((TyReactiveRecord) type).transferIntoCyclicGraph(graph);
        }
      }
      String cycle = Cycle.detect(graph);
      if (cycle != null) {
        createError(DocumentPosition.ZERO, "A cycle was detected within records: " + cycle);
      }
    }
    TyType constructorMessageType = null;
    for (final DefineConstructor dc : constructors) {
      if (dc.messageTypeToken != null) {
        TyType currentType = environment.rules.FindMessageStructure(dc.messageTypeToken.text, dc, false);
        if (currentType != null && currentType instanceof TyNativeMessage) {
          currentType = ((TyNativeMessage) currentType).makeAnonymousCopy();
          if (constructorMessageType != null) {
            constructorMessageType = environment.rules.GetMaxType(constructorMessageType, currentType, false);
          } else {
            constructorMessageType = currentType;
          }
        }
      }
    }
    if (constructorMessageType != null) {
      constructorMessageType = environment.rules.EnsureRegisteredAndDedupe(constructorMessageType, false);
    }
    for (final DefineConstructor dc : constructors) {
      dc.unifiedMessageType = constructorMessageType;
      dc.internalTyping(environment); // TODO: remove
    }

    TopologicalSort<String> preventSerializationRecursion = new TopologicalSort<>();
    for (TyType type : types.values()) {
      if (type instanceof IsStructure) {
        String base = ((IsStructure) type).storage().name.text;
        preventSerializationRecursion.add(base, base, ((IsStructure) type).storage().getStructureDependencies(environment));
      }
    }
    preventSerializationRecursion.sort();
    for (String structPartOfCycle : preventSerializationRecursion.cycles()) {
      createError(DocumentPosition.ZERO, "The record/message '" + structPartOfCycle + "' has the potential to create an infinite serialization");
    }

    return !hasErrors();
  }

  /** does the document have errors */
  public boolean hasErrors() {
    return errorLists.size() > 0;
  }

  /** compile the document to java */
  public String compileJava(final EnvironmentState state) {
    root.storage.reorder();
    var environment = Environment.fresh(this, state);
    if (state.options.disableBillingCost) {
      environment = environment.scopeAsNoCost();
    }
    final var sb = new StringBuilderWithTabs();
    CodeGenDocument.writePrelude(sb, environment);
    sb.append("public class " + className + " extends LivingDocument {").tabUp().writeNewline();
    CodeGenRecords.writeRootDocument(root.storage, sb, environment);
    for (final TyType ty : types.values()) {
      if (ty instanceof DetailTypeProducesRootLevelCode) {
        ((DetailTypeProducesRootLevelCode) ty).compile(sb, environment);
      }
    }
    CodeGenTemplates.writeTemplates(sb, environment);
    CodeGenFunctions.writeFunctionsJava(sb, environment);
    CodeGenServices.writeServices(sb, environment);
    CodeGenViewStateFilter.writeViewStateFilter(sb, environment);
    CodeGenMessageHandling.writeMessageHandlers(sb, environment);
    CodeGenMetrics.writeMetricsDump(sb, environment);
    CodeGenTraffic.writeTrafficHint(sb, environment);
    CodeGenDebug.writeDebugInfo(sb, environment);
    CodeGenJoins.writeGraphs(sb, environment);
    CodeGenAuth.writeAuth(sb, environment);
    CodeGenCron.writeCronExecution(sb, environment);
    CodeGenCron.writeCronReset(sb, environment);
    CodeGenCron.writeCronPredict(sb, environment);
    CodeGenWeb.writeWebHandlers(sb, environment);
    CodeGenStateMachine.writeStateMachine(sb, environment);
    CodeGenEventHandlers.writeEventHandlers(sb, environment);
    CodeGenConfig.writeConfig(sb, environment);
    CodeGenTests.writeTests(sb, environment);
    CodeGenConstructor.writeConstructors(sb, environment);
    // code snippets which are done after everything
    for (final LatentCodeSnippet lcs : latentCodeSnippets) {
      lcs.writeLatentJava(sb);
    }
    for (final LatentCodeSnippet lcs : dedupedLatentCodeSnippets.values()) {
      lcs.writeLatentJava(sb);
    }
    sb.append("/* end of file */").tabDown().writeNewline();
    sb.append("}").writeNewline(); // end file
    return sb.toString();
  }

  public IsStructure findPriorMessage(final StructureStorage search, final Environment environment) {
    for (final TyType other : types.values()) {
      if (other instanceof TyNativeMessage) {
        if (((IsStructure) other).storage().match(search, environment)) {
          return (IsStructure) other;
        }
      }
    }
    return null;
  }

  /** get the class name */
  public String getClassName() {
    return className;
  }

  /** set the class name */
  public void setClassName(final String className) {
    this.className = className;
  }

  /** invent a class id */
  public int inventClassId() {
    return autoClassId++;
  }

  /** export errors in LSP format */
  public String errorsJson() {
    JsonStreamWriter writer = new JsonStreamWriter();
    writer.beginArray();
    errorLists.forEach(err -> {
      writer.injectJson(err.json());
    });
    writer.endArray();
    return writer.toString();
  }
}
