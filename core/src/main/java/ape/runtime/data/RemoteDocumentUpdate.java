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
package ape.runtime.data;

import ape.runtime.json.JsonAlgebra;
import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.natives.NtPrincipal;

import java.util.ArrayList;

/** the remote copy should change */
public class RemoteDocumentUpdate {
  /** the request that is changing the document */
  public final String request;

  /** the request as as redo patch */
  public final String redo;

  /** the undo patch to revert this change */
  public final String undo;

  /** the beginning sequencer of this change */
  public final int seqBegin;

  /** the end sequencer of this change */
  public final int seqEnd;


  /** who was responsible for the update */
  public final NtPrincipal who;

  /**
   * this update is incomplete with respect to time, and this will ensure we schedule an
   * invalidation in the future
   */
  public final boolean requiresFutureInvalidation;

  /**
   * if requiresFutureInvalidation, then how many milliseconds should the system wait to invoke
   * invalidation
   */
  public final int whenToInvalidateMilliseconds;

  /**
   * how many bytes were written with this update
   */
  public final long assetBytes;

  /** what is the type of the update */
  public final UpdateType updateType;

  public RemoteDocumentUpdate(final int seqBegin, final int seqEnd, NtPrincipal who, final String request, final String redo, final String undo, final boolean requiresFutureInvalidation, int whenToInvalidateMilliseconds, long assetBytes, UpdateType updateType) {
    this.seqBegin = seqBegin;
    this.seqEnd = seqEnd;
    this.who = who;
    this.request = request;
    this.redo = redo;
    this.undo = undo;
    this.requiresFutureInvalidation = requiresFutureInvalidation;
    this.whenToInvalidateMilliseconds = whenToInvalidateMilliseconds;
    this.assetBytes = assetBytes;
    this.updateType = updateType;
  }

  public static RemoteDocumentUpdate[] compact(RemoteDocumentUpdate[] updates) {
    if (updates.length == 1) {
      return updates;
    }
    ArrayList<RemoteDocumentUpdate> newUpdates = new ArrayList<>(updates.length);
    int k = 0;
    while (k < updates.length) {
      RemoteDocumentUpdate consider = updates[k];
      while (k + 1 < updates.length && updates[k + 1].updateType == UpdateType.Invalidate) {
        consider = merge(consider, updates[k + 1]);
        k++;
      }
      newUpdates.add(consider);
      k++;
    }
    return newUpdates.toArray(new RemoteDocumentUpdate[newUpdates.size()]);
  }

  private static RemoteDocumentUpdate merge(RemoteDocumentUpdate a, RemoteDocumentUpdate b) {
    return new RemoteDocumentUpdate(a.seqBegin, b.seqEnd, //
        a.who, a.request, //
        mergeJson(a.redo, b.redo), //
        mergeJson(b.undo, a.undo), b.requiresFutureInvalidation, b.whenToInvalidateMilliseconds, //
        a.assetBytes + b.assetBytes, a.updateType);
  }

  private static String mergeJson(String a, String b) {
    JsonStreamReader readerA = new JsonStreamReader(a);
    JsonStreamReader readerB = new JsonStreamReader(b);
    JsonStreamWriter writer = new JsonStreamWriter();
    writer.writeTree(JsonAlgebra.merge(readerA.readJavaTree(), readerB.readJavaTree(), true));
    return writer.toString();
  }
}
