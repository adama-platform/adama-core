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
package ape.runtime.deploy;

import ape.common.*;
import ape.ErrorCodes;
import ape.common.keys.PrivateKeyBundle;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.remote.Deliverer;
import ape.translator.env.CompilerOptions;
import ape.translator.env.EnvironmentState;
import ape.translator.env.GlobalObjectPool;
import ape.translator.env.RuntimeEnvironment;
import ape.translator.env2.Scope;
import ape.translator.jvm.LivingDocumentFactory;
import ape.translator.parser.Parser;
import ape.translator.parser.exceptions.AdamaLangException;
import ape.translator.parser.token.TokenEngine;
import ape.translator.tree.Document;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** the async compiler all unified into one big thing */
public class AsyncCompiler {
  public static String normalizeSpaceNameForClass(String space) {
    return space.replaceAll(Pattern.quote("-"), Matcher.quoteReplacement("_"));
  }

  public static void forge(RuntimeEnvironment runtime, String name, DeploymentFactory prior, DeploymentPlan plan, Deliverer deliverer, TreeMap<Integer, PrivateKeyBundle> keys, AsyncByteCodeCache cache, Callback<DeploymentFactory> callback) {
    ConcurrentHashMap<String, LivingDocumentFactory> factories = new ConcurrentHashMap<>();
    AtomicLong _memoryUsed = new AtomicLong();
    AtomicBoolean failedWithBetterError = new AtomicBoolean(false);
    Runnable successLatch = NoErrorMultiCallbackLatchImpl.WRAP(() -> {
      callback.success(new DeploymentFactory(name, plan, _memoryUsed.get(), new HashMap<>(factories)));
    }, plan.versions.size());
    for (Map.Entry<String, DeployedVersion> entry : plan.versions.entrySet()) {
      DeployedVersion version = entry.getValue();
      LivingDocumentFactory factory = null;
      if (prior != null) {
        if (prior.plan.versions.containsKey(entry.getKey())) {
          if (prior.plan.versions.get(entry.getKey()).equals(version)) {
            factory = prior.factories.get(entry.getKey());
          }
        }
      }
      if (factory == null) {
        // First, we compile the Adama code to java
        try {
          CompilerOptions.Builder builder = CompilerOptions.start();
          if (plan.instrument) {
            builder = builder.instrument();
          }
          final var options = builder.make();
          final var globals = GlobalObjectPool.createPoolWithStdLib(runtime);
          final var state = new EnvironmentState(globals, options, runtime);
          final var document = new Document();
          MessageDigest digest = Hashing.sha384();
          digest.update(version.main.getBytes(StandardCharsets.UTF_8));
          for (Map.Entry<String, String> includeEntry : version.includes.entrySet()) {
            digest.update(includeEntry.getKey().getBytes(StandardCharsets.UTF_8));
            digest.update(includeEntry.getValue().getBytes(StandardCharsets.UTF_8));
          }
          String className = normalizeSpaceNameForClass(name) + "_" + Hashing.finishAndEncodeHex(digest);
          document.setClassName(className);
          document.setIncludes(version.includes);
          final var tokenEngine = new TokenEngine("main", version.main.codePoints().iterator());
          final var parser = new Parser(tokenEngine, document.getSymbolIndex(), Scope.makeRootDocument());
          parser.document().accept(document);
          if (!document.check(state.scope())) {
            if (failedWithBetterError.compareAndSet(false, true)) {
              callback.failure(new ErrorCodeException(ErrorCodes.DEPLOYMENT_CANT_TYPE_LANGUAGE, document.errorsJson()));
            }
          } else {
            final var java = document.compileJava(state);
            JsonStreamWriter reflection = new JsonStreamWriter();
            document.writeTypeReflectionJson(reflection);
            cache.fetchOrCompile(name, className, java, reflection.toString(), new ExceptionCallback<>() {
              @Override
              public void invoke(CachedByteCode code) throws ErrorCodeException {
                LivingDocumentFactory newFactory = new LivingDocumentFactory(code, deliverer, keys);
                _memoryUsed.addAndGet(newFactory.memoryUsage);
                factories.put(entry.getKey(), newFactory);
                successLatch.run();
              }

              @Override
              public void failure(ErrorCodeException ex) {
                if (failedWithBetterError.compareAndSet(false, true)) {
                  callback.failure(ex);
                }
              }
            });
          }
        } catch (AdamaLangException ex) {
          if (failedWithBetterError.compareAndSet(false, true)) {
            callback.failure(new ErrorCodeException(ErrorCodes.FACTORY_CANT_BIND_JAVA_CODE));
          }
          return;
        }
      } else {
        factories.put(entry.getKey(), factory);
        _memoryUsed.addAndGet(factory.memoryUsage);
        successLatch.run();
      }
    }
  }
}
