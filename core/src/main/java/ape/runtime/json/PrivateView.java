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
package ape.runtime.json;

import ape.runtime.contracts.Perspective;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.sys.StreamHandle;

/** a private view of the document where private lives; code is generated to use this */
public abstract class PrivateView {
  private static final String DEFAULT_FUTURES = "\"outstanding\":[],\"blockers\":[]";
  public final Perspective perspective;
  public final NtPrincipal who;
  private final int viewId;
  private boolean alive;
  private StreamHandle handle;
  private String lastWrittenFutures;
  private Runnable refresh;

  /** construct the view based on the person (who) and the connection (perspective) */
  public PrivateView(final int viewId, final NtPrincipal who, final Perspective perspective) {
    this.viewId = viewId;
    this.alive = true;
    this.who = who;
    this.perspective = perspective;
    this.lastWrittenFutures = DEFAULT_FUTURES;
    this.refresh = null;
  }

  public void link(StreamHandle handle) {
    this.handle = handle;
  }

  public StreamHandle getHandle() {
    return this.handle;
  }

  /** this is an optimized way to update the viewer without causing an invalidate */
  public void setRefresh(Runnable refresh) {
    this.refresh = refresh;
  }

  public void triggerRefresh() {
    if (refresh != null) {
      refresh.run();
    }
  }

  /**
   * a new private view was created on a different document which is usurping the existing document.
   * Since private views leak outside the document, this creates a proxy link for the client to kill
   * both views.
   */
  public void usurp(PrivateView usurper) {
    handle.set(usurper);
    usurper.link(handle);
    synchronized (this) {
      alive = false;
    }
  }

  public int getViewId() {
    return viewId;
  }

  public void ingestViewUpdate(JsonStreamReader reader) {
    ingest(reader);
  }

  /** codegen: seed the state held by the view */
  public abstract void ingest(JsonStreamReader reader);

  /** codegen: dump the data held by the view */
  public abstract void dumpViewer(JsonStreamWriter writer);

  /** send the user a delivery update */
  public void deliver(final String delivery) {
    perspective.data(delivery);
  }

  /** is the view still alive and interesting to the user */
  public synchronized boolean isAlive() {
    return alive;
  }

  /** get the memory of the view */
  public abstract long memory();

  /** dedupe excessive outstanding and blockers sharing */
  public boolean futures(String futures) {
    String futuresToTest = futures;
    if (futuresToTest.equals(DEFAULT_FUTURES)) { // save some memory
      futuresToTest = DEFAULT_FUTURES;
    }
    if (futuresToTest == lastWrittenFutures || lastWrittenFutures.equals(futures)) {
      return false;
    } else {
      lastWrittenFutures = futures;
      return true;
    }
  }

  /** does the private view support reading data */
  public boolean hasRead() {
    return true;
  }

  /** the client is no longer interested */
  public synchronized void kill() {
    alive = false;
  }

  /** codegen: the data inside the view has been updated, stream it out to the writer */
  public abstract void update(JsonStreamWriter writer);
}
