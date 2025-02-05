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

import ape.runtime.natives.*;
import ape.runtime.natives.*;

public class PrivateLazyDeltaWriter {
  public static final Runnable DO_NOTHING = () -> {
  };
  public final NtPrincipal who;
  public Object cacheObject;
  public final Object viewerState;
  private final Runnable onEndWithManifest;
  private final Runnable onManifest;
  private final PrivateLazyDeltaWriter parent;
  private final JsonStreamWriter writer;
  private boolean manifested;
  private final int viewId;

  private PrivateLazyDeltaWriter(final NtPrincipal who, Object cacheObject, final JsonStreamWriter writer, final PrivateLazyDeltaWriter parent, final Runnable onManifest, final Runnable onEndWithManifest, final Object viewerState, int viewId) {
    this.who = who;
    this.cacheObject = cacheObject;
    this.writer = writer;
    this.parent = parent;
    this.onManifest = onManifest;
    this.onEndWithManifest = onEndWithManifest;
    this.viewerState = viewerState;
    this.viewId = viewId;
    manifested = false;
  }

  public int getViewId() {
    return viewId;
  }

  public static PrivateLazyDeltaWriter bind(final NtPrincipal who, final JsonStreamWriter writer, Object viewerState, int viewId) {
    return new PrivateLazyDeltaWriter(who, null, writer, null, DO_NOTHING, DO_NOTHING, viewerState, viewId);
  }

  public boolean end() {
    if (manifested) {
      onEndWithManifest.run();
    }
    return manifested;
  }

  public void manifest() {
    if (parent != null) {
      parent.manifest();
    }
    if (!manifested) {
      manifested = true;
      onManifest.run();
    }
  }

  public void setCacheObject(Object cacheObject) {
    this.cacheObject = cacheObject;
  }

  public Object getCacheObject() {
    return cacheObject;
  }

  public PrivateLazyDeltaWriter planArray() {
    return new PrivateLazyDeltaWriter(who,  cacheObject, writer, this, () -> writer.beginArray(), () -> writer.endArray(), viewerState, viewId);
  }

  public PrivateLazyDeltaWriter planField(final int fieldId) {
    return new PrivateLazyDeltaWriter(who,  cacheObject, writer, this, () -> writer.writeObjectFieldIntro("" + fieldId), DO_NOTHING, viewerState, viewId);
  }

  public PrivateLazyDeltaWriter planField(final String fieldName) {
    return new PrivateLazyDeltaWriter(who,  cacheObject, writer, this, () -> writer.writeObjectFieldIntro(fieldName), DO_NOTHING, viewerState, viewId);
  }

  public PrivateLazyDeltaWriter planObject() {
    return new PrivateLazyDeltaWriter(who, cacheObject, writer,this, () -> writer.beginObject(), () -> writer.endObject(), viewerState, viewId);
  }

  public void writeBool(final boolean b) {
    manifest();
    writer.writeBoolean(b);
  }

  public void writeDouble(final double d) {
    manifest();
    writer.writeDouble(d);
  }

  public void writeNtComplex(final NtComplex c) {
    manifest();
    writer.writeNtComplex(c);
  }

  public void writeNtDate(final NtDate d) {
    manifest();
    writer.writeNtDate(d);
  }

  public void writeNtDateTime(final NtDateTime d) {
    manifest();
    writer.writeNtDateTime(d);
  }

  public void writeNtTime(final NtTime d) {
    manifest();
    writer.writeNtTime(d);
  }

  public void writeNtTimeSpan(final NtTimeSpan d) {
    manifest();
    writer.writeNtTimeSpan(d);
  }

  public void writeFastString(final String str) {
    manifest();
    writer.writeFastString(str);
  }

  public void writeInt(final int x) {
    manifest();
    writer.writeInteger(x);
  }

  public void writeNull() {
    manifest();
    writer.writeNull();
  }

  public void writeString(final String str) {
    manifest();
    writer.writeString(str);
  }

  public void injectJson(final String json) {
    manifest();
    writer.injectJson(json);
  }

  public JsonStreamWriter force() {
    manifest();
    return writer;
  }
}
