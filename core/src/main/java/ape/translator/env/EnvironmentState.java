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

import java.util.concurrent.atomic.AtomicInteger;

/** properties about the environment */
public class EnvironmentState {
  public final AtomicInteger autoId;
  public final GlobalObjectPool globals;
  public final CompilerOptions options;
  private ComputeContext computationContext;
  private boolean isMessageHandler;
  private boolean isNoCost;
  private boolean isStateMachineTransition;
  private boolean isStatic;
  private boolean pure;
  private boolean reactiveExpression;
  private boolean readonly;
  private boolean define;
  private boolean testing;
  private boolean isPolicy;
  private boolean isFilter;
  private boolean isBubble;
  private boolean isWeb;
  private boolean isTrafficHint;
  private boolean isConstructor;
  private boolean isDocumentEvent;
  private String webMethod;
  private String cacheObject;
  private boolean readonlyEnv;
  private boolean abortion;
  private boolean authorize;
  private boolean viewer;
  private String inRecord;
  public final RuntimeEnvironment runtime;

  private EnvironmentState(final EnvironmentState prior) {
    autoId = prior.autoId;
    globals = prior.globals;
    options = prior.options;
    pure = prior.pure;
    testing = prior.testing;
    isNoCost = prior.isNoCost;
    isMessageHandler = prior.isMessageHandler;
    isStateMachineTransition = prior.isStateMachineTransition;
    computationContext = prior.computationContext;
    reactiveExpression = prior.reactiveExpression;
    isStatic = prior.isStatic;
    isPolicy = prior.isPolicy;
    isBubble = prior.isBubble;
    isWeb = prior.isWeb;
    isTrafficHint = prior.isTrafficHint;
    webMethod = prior.webMethod;
    cacheObject = prior.cacheObject;
    readonly = false;
    define = false;
    readonlyEnv = prior.readonlyEnv;
    isConstructor = prior.isConstructor;
    isDocumentEvent = prior.isDocumentEvent;
    abortion = prior.abortion;
    authorize = prior.authorize;
    viewer = prior.viewer;
    inRecord = prior.inRecord;
    isFilter = prior.isFilter;
    runtime = prior.runtime;
  }

  public EnvironmentState(final GlobalObjectPool globals, final CompilerOptions options, RuntimeEnvironment runtime) {
    autoId = new AtomicInteger(0);
    this.globals = globals;
    this.options = options;
    isMessageHandler = false;
    isNoCost = false;
    pure = false;
    isStateMachineTransition = false;
    testing = false;
    readonly = false;
    define = false;
    reactiveExpression = false;
    computationContext = ComputeContext.Unknown;
    isStatic = false;
    isPolicy = false;
    isBubble = false;
    isWeb = false;
    isTrafficHint = false;
    webMethod = null;
    cacheObject = null;
    isConstructor = false;
    isDocumentEvent = false;
    abortion = false;
    authorize = false;
    viewer = false;
    inRecord = null;
    isFilter = false;
    this.runtime = runtime;
  }

  public boolean hasNoCost() {
    return isNoCost;
  }

  public boolean isStatic() {
    return isStatic;
  }

  public boolean isPolicy() {
    return isPolicy;
  }

  public boolean isBubble() {
    return isBubble;
  }

  public boolean hasViewer() { return viewer; }

  public boolean isWeb() {
    return isWeb;
  }

  public boolean isTrafficHint() {
    return isTrafficHint;
  }

  public String getWebMethod() {
    return webMethod;
  }

  public String getCacheObject() {
    return cacheObject;
  }

  public boolean isContextComputation() {
    return computationContext == ComputeContext.Computation;
  }

  public boolean isContextAssignment() {
    return computationContext == ComputeContext.Assignment;
  }

  /** is the current environment operating inside a message handler */
  public boolean isMessageHandler() {
    return isMessageHandler;
  }

  /** does the current environment allow aborting */
  public boolean isAbortable() {
    return abortion;
  }

  /** does the current environment exist within an @authorize */
  public boolean isAuthorize() {
    return authorize;
  }

  public boolean isDocumentEvent() {
    return isDocumentEvent;
  }

  /** is the current environment operating in a purity model */
  public boolean isPure() {
    return pure;
  }

  /** is the current environment a reactive expression */
  public boolean isReactiveExpression() {
    return reactiveExpression;
  }

  public boolean isReadonly() {
    return readonly;
  }

  public boolean isReadonlyEnvironment() {
    return readonlyEnv;
  }

  public boolean isConstructor() {
    return isConstructor;
  }

  /** is the current environment for state machine transition code */
  public boolean isStateMachineTransition() {
    return isStateMachineTransition;
  }

  /** is the current environment for test code */
  public boolean isTesting() {
    return testing;
  }

  public EnvironmentState scopeMessageHandler() {
    final var next = new EnvironmentState(this);
    next.isMessageHandler = true;
    next.cacheObject = "__cache";
    next.define = true;
    return next;
  }

  public EnvironmentState scopeNoCost() {
    final var next = new EnvironmentState(this);
    next.isNoCost = true;
    return next;
  }

  public EnvironmentState scopeStatic() {
    final var next = new EnvironmentState(this);
    next.isStatic = true;
    next.define = true;
    return next;
  }


  public EnvironmentState scopeInRecord(String name) {
    final var next = new EnvironmentState(this);
    next.inRecord = name;
    return next;
  }

  public boolean shouldDumpRecordMethodSubscriptions(String recordName) {
    if (recordName != null) {
      return recordName.equals(inRecord);
    }
    return false;
  }

  public EnvironmentState scopePolicy() {
    final var next = new EnvironmentState(this);
    next.isPolicy = true;
    next.define = true;
    return next;
  }

  public EnvironmentState scopeFilter() {
    final var next = new EnvironmentState(this);
    next.isFilter = true;
    next.define = true;
    return next;
  }

  public EnvironmentState scopeBubble() {
    final var next = new EnvironmentState(this);
    next.isBubble = true;
    return next;
  }

  public EnvironmentState scopeWeb(String method) {
    final var next = new EnvironmentState(this);
    next.isWeb = true;
    next.webMethod = method;
    next.define = true;
    return next;
  }

  public EnvironmentState scopeTraffic() {
    final var next = new EnvironmentState(this);
    next.isTrafficHint = true;
    return next;
  }

  public EnvironmentState scopeConstructor() {
    final var next = new EnvironmentState(this);
    next.isConstructor = true;
    next.define = true;
    return next;
  }

  public EnvironmentState scopeDocumentEvent() {
    final var next = new EnvironmentState(this);
    next.isDocumentEvent = true;
    next.define = true;
    return next;
  }

  public EnvironmentState scopePure() {
    final var next = new EnvironmentState(this);
    next.pure = true;
    return next;
  }

  public EnvironmentState scopeReactiveExpression() {
    final var next = new EnvironmentState(this);
    next.reactiveExpression = true;
    return next;
  }

  public EnvironmentState scopeReadonly() {
    final var next = new EnvironmentState(this);
    next.readonly = true;
    next.readonlyEnv = true;
    return next;
  }


  public EnvironmentState scopeDefineLimit() {
    final var next = new EnvironmentState(this);
    next.define = true;
    return next;
  }

  public boolean isDefineBoundary() {
    return define;
  }

  public EnvironmentState scopeAbortion() {
    final var next = new EnvironmentState(this);
    next.abortion = true;
    return next;
  }

  public EnvironmentState scopeViewer() {
    final var next = new EnvironmentState(this);
    next.viewer = true;
    return next;
  }

  public EnvironmentState scopeWithCache(String cacheObject) {
    final var next = new EnvironmentState(this);
    next.cacheObject = cacheObject;
    return next;
  }

  public EnvironmentState scopeStateMachineTransition() {
    final var next = new EnvironmentState(this);
    next.isStateMachineTransition = true;
    next.cacheObject = "__cache";
    next.define = true;
    return next;
  }

  public EnvironmentState scopeTesting() {
    final var next = new EnvironmentState(this);
    next.testing = true;
    return next;
  }

  public EnvironmentState scopeAuthorize() {
    final var next = new EnvironmentState(this);
    next.readonly = true;
    next.readonlyEnv = true;
    next.authorize = true;
    next.define = true;
    return next;
  }

  public EnvironmentState scope() {
    return new EnvironmentState(this);
  }

  public EnvironmentState scopeWithComputeContext(final ComputeContext newComputeContext) {
    if (computationContext != newComputeContext) {
      final var next = new EnvironmentState(this);
      next.computationContext = newComputeContext;
      return next;
    }
    return this;
  }
}
