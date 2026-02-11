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
package ape.translator.jvm;

import ape.ErrorCodes;
import ape.common.ErrorCodeException;
import ape.common.ExceptionLogger;
import ape.common.keys.PrivateKeyBundle;
import ape.runtime.contracts.DocumentMonitor;
import ape.runtime.deploy.CachedByteCode;
import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.ops.TestMockUniverse;
import ape.runtime.ops.TestReportBuilder;
import ape.runtime.remote.Deliverer;
import ape.runtime.remote.ServiceRegistry;
import ape.runtime.remote.client.HeaderDecryptor;
import ape.runtime.sys.CoreRequestContext;
import ape.runtime.sys.LivingDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.TreeMap;

/** responsible for compiling java code into a LivingDocumentFactory */
public class LivingDocumentFactory {
  private static final Logger LOG = LoggerFactory.getLogger(LivingDocumentFactory.class);
  private static final ExceptionLogger LOGGER = ExceptionLogger.FOR(LOG);
  public final String space;
  public final String reflection;
  private final Constructor<?> constructor;
  private final Method creationPolicyMethod;
  private final Method inventionPolicyMethod;
  private final Method canSendWhileDisconnectPolicyMethod;
  public final int maximum_history;
  public final boolean delete_on_close;
  public final ServiceRegistry registry;
  public final Deliverer deliverer;
  public final long memoryUsage;
  public final boolean appMode;
  public final int appDelay;
  public final int temporalResolutionMilliseconds;
  public boolean readonly;
  public final long sweep_export_delay;

  public LivingDocumentFactory(CachedByteCode code, Deliverer deliverer, TreeMap<Integer, PrivateKeyBundle> keys) throws ErrorCodeException {
    try {
      this.space = code.spaceName;
      this.deliverer = deliverer;
      long _memory = 0;
      for (byte[] bytes : code.classBytes.values()) {
        _memory += bytes.length;
      }
      this.memoryUsage = _memory + 65536;
      // NOTE: we copy because the loader will destroy aspects of the hashmap, #wild
      final var loader = new ByteArrayClassLoader(new HashMap<>(code.classBytes));
      final Class<?> clazz = Class.forName(code.className, true, loader);
      constructor = clazz.getConstructor(DocumentMonitor.class);
      creationPolicyMethod = clazz.getMethod("__onCanCreate", CoreRequestContext.class);
      inventionPolicyMethod = clazz.getMethod("__onCanInvent", CoreRequestContext.class);
      canSendWhileDisconnectPolicyMethod = clazz.getMethod("__onCanSendWhileDisconnected", CoreRequestContext.class);
      HashMap<String, Object> config = (HashMap<String, Object>) (clazz.getMethod("__config").invoke(null));
      maximum_history = extractMaximumHistory(config);
      delete_on_close = extractDeleteOnClose(config);
      readonly = extractReadOnlyMode(config);
      int freq = extractFrequency(config);
      sweep_export_delay = extractSweepExportDelay(config);
      appMode = freq > 0;
      appDelay = freq;
      this.reflection = code.reflection;
      this.registry = new ServiceRegistry(code.spaceName);
      this.registry.resolve((HashMap<String, HashMap<String, Object>>) (clazz.getMethod("__services").invoke(null)), keys);
      clazz.getMethod("__create_generic_clients", ServiceRegistry.class, HeaderDecryptor.class).invoke(null, this.registry, new HeaderDecryptor(keys));
      this.temporalResolutionMilliseconds = extractTemporalResolution(config);
    } catch (final Exception ex) {
      throw new ErrorCodeException(ErrorCodes.FACTORY_CANT_BIND_JAVA_CODE, ex);
    }
  }

  public boolean canInvent(CoreRequestContext context) throws ErrorCodeException {
    try {
      return (Boolean) inventionPolicyMethod.invoke(null, context);
    } catch (Exception ex) {
      throw ErrorCodeException.detectOrWrap(ErrorCodes.FACTORY_CANT_INVOKE_CAN_INVENT, ex, LOGGER);
    }
  }

  public boolean canCreate(CoreRequestContext context) throws ErrorCodeException {
    try {
      return (Boolean) creationPolicyMethod.invoke(null, context);
    } catch (Exception ex) {
      throw ErrorCodeException.detectOrWrap(ErrorCodes.FACTORY_CANT_INVOKE_CAN_CREATE, ex, LOGGER);
    }
  }

  public boolean canSendWhileDisconnected(CoreRequestContext context) throws ErrorCodeException {
    try {
      return (Boolean) canSendWhileDisconnectPolicyMethod.invoke(null, context);
    } catch (Exception ex) {
      throw ErrorCodeException.detectOrWrap(ErrorCodes.FACTORY_CANT_INVOKE_CAN_SEND_WHILE_DISCONNECTED, ex, LOGGER);
    }
  }

  private static int extractMaximumHistory(HashMap<String, Object> config) {
    Object value = config.get("maximum_history");
    if (value != null && value instanceof Integer) {
      return ((Integer) value).intValue();
    } else {
      return 1000;
    }
  }

  private static int extractTemporalResolution(HashMap<String, Object> config) {
    Object value = config.get("temporal_resolution_ms");
    if (value != null && value instanceof Integer) {
      return ((Integer) value).intValue();
    } else {
      return 0; // preserves existing behavior
    }
  }

  private static boolean extractDeleteOnClose(HashMap<String, Object> config) {
    Object value = config.get("delete_on_close");
    if (value != null && value instanceof Boolean) {
      return ((Boolean) value).booleanValue();
    } else {
      return false;
    }
  }

  private static boolean extractReadOnlyMode(HashMap<String, Object> config) {
    Object value = config.get("readonly");
    if (value != null && value instanceof Boolean) {
      return ((Boolean) value).booleanValue();
    } else {
      return false;
    }
  }

  private static int extractFrequency(HashMap<String, Object> config) {
    Object value = config.get("frequency");
    if (value != null && value instanceof Integer) {
      return ((Integer) value).intValue();
    } else {
      return 0;
    }
  }
  private static long extractSweepExportDelay(HashMap<String, Object> config) {
    Object value = config.get("sweep_export_delay");
    if (value != null && value instanceof Integer) {
      return ((Integer) value).intValue();
    } else if (value != null && value instanceof Long) {
        return ((Integer) value).longValue();
    } else {
      return 0;
    }
  }

  @SuppressWarnings("unchecked")
  public void populateTestReport(final TestReportBuilder report, final DocumentMonitor monitor, final String entropy) throws Exception {
    var candidate = prepareTestCandidate(monitor, entropy);
    final var tests = candidate.__getTests();
    for (final String test : tests) {
      report.annotate(test, (HashMap<String, Object>) new JsonStreamReader(candidate.__run_test(report, test)).readJavaTree());
      candidate = prepareTestCandidate(monitor, entropy);
    }
  }

  private LivingDocument prepareTestCandidate(final DocumentMonitor monitor, final String entropy) throws Exception {
    final var candidate = create(monitor);
    TestMockUniverse tmu = new TestMockUniverse(space, candidate);
    candidate.__lateBind("space", "key", tmu, tmu);
    JsonStreamWriter writer = new JsonStreamWriter();
    writer.beginObject();
    writer.writeObjectFieldIntro("command");
    writer.writeString("construct");
    writer.writeObjectFieldIntro("timestamp");
    writer.writeString("0");
    writer.writeObjectFieldIntro("entropy");
    writer.writeString(entropy);
    writer.writeObjectFieldIntro("key");
    writer.writeString("key");
    writer.writeObjectFieldIntro("origin");
    writer.writeString("origin");
    writer.writeObjectFieldIntro("ip");
    writer.writeString("1.2.3.4");
    writer.writeObjectFieldIntro("who");
    writer.writeNtPrincipal(NtPrincipal.NO_ONE);
    writer.writeObjectFieldIntro("arg");
    writer.beginObject();
    writer.endObject();
    writer.endObject();
    candidate.__transact(writer.toString(), this);
    return candidate;
  }

  public LivingDocument create(final DocumentMonitor monitor) throws ErrorCodeException {
    try {
      return (LivingDocument) constructor.newInstance(monitor);
    } catch (final Exception ex) {
      throw ErrorCodeException.detectOrWrap(ErrorCodes.FACTORY_CANT_CREATE_OBJECT_DUE_TO_CATASTROPHE, ex, LOGGER);
    }
  }
}
