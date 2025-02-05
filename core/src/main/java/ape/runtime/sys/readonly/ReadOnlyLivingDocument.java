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
package ape.runtime.sys.readonly;

import ape.runtime.contracts.Perspective;
import ape.runtime.data.Key;
import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.PrivateView;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.sys.LivingDocument;
import ape.runtime.sys.StreamHandle;
import ape.translator.jvm.LivingDocumentFactory;

/** a version of the document that is read only */
public class ReadOnlyLivingDocument {
  public final Key key;
  private final ReadOnlyReplicaThreadBase base;
  private final LivingDocument document;
  private final LivingDocumentFactory factory;
  private boolean alreadyStarted;
  private boolean dead;
  private Runnable cancel;
  private long lastActivityMS;

  public ReadOnlyLivingDocument(ReadOnlyReplicaThreadBase base, Key key, LivingDocument document, LivingDocumentFactory factory) {
    this.base = base;
    this.key = key;
    this.document = document;
    this.factory = factory;
    this.alreadyStarted = false;
    this.dead = false;
    ping();
  }

  private void ping() {
    this.lastActivityMS = base.time.nowMilliseconds();
  }

  public StreamHandle join(NtPrincipal who, String viewerState, Perspective perspective) {
    PrivateView view = document.__createView(who, perspective);
    if (viewerState != null) {
      view.ingest(new JsonStreamReader(viewerState));
    }
    if (alreadyStarted) {
      document.__forceBroadcastForViewer(who);
    }
    StreamHandle handle = new StreamHandle(view);
    ping();
    return handle;
  }

  public void setCancel(Runnable cancel) {
    ping();
    if (dead) {
      cancel.run();
    } else {
      this.cancel = cancel;
    }
  }

  public LivingDocumentFactory getFactory() {
    return factory;
  }

  public void start(String snapshot) {
    document.__insert(new JsonStreamReader(snapshot));
    document.__forceBroadcastToKeepReadonlyObserverUpToDate();
    alreadyStarted = true;
    ping();
  }

  public void change(String change) {
    document.__patch(new JsonStreamReader(change));
    document.__forceBroadcastToKeepReadonlyObserverUpToDate();
    ping();
  }

  public void forceUpdate(NtPrincipal who) {
    document.__forceBroadcastForViewer(who);
    ping();
  }

  public void kill() {
    dead = true;
    document.__nukeViews();
    if (cancel != null) {
      cancel.run();
    }
  }

  public int garbageCollectViewsFor(NtPrincipal who) {
    return document.__garbageCollectViews(who);
  }

  public int getCodeCost() {
    return document.__getCodeCost();
  }

  public long getCpuMilliseconds() {
    return document.__getCpuMilliseconds();
  }

  public void zeroOutCodeCost() {
    document.__zeroOutCodeCost();
  }

  public int getConnectionsCount() {
    return document.__getConnectionsCount();
  }

  public long getMemoryBytes() {
    return document.__memory();
  }

  public boolean testInactive() {
    long timeSinceLastActivity = base.time.nowMilliseconds() - lastActivityMS;
    return timeSinceLastActivity > base.getMillisecondsInactivityBeforeCleanup() && document.__canRemoveFromMemory();
  }

  public String getViewStateFilter() {
    return document.__getViewStateFilter();
  }
}
