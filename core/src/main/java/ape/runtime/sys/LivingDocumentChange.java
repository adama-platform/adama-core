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
package ape.runtime.sys;

import ape.runtime.data.RemoteDocumentUpdate;
import ape.runtime.json.PrivateView;

import java.util.List;

/**
 * A living document is changing; this represents the side-effects of that change for both the data
 * store (i.e. durability) and the clients (i. the people). This change is bundled such that these
 * two concerns can be ordered such that clients never see artifacts of uncommitted changes.
 */
public class LivingDocumentChange {

  /** the data change */
  public final RemoteDocumentUpdate update;
  public final Object response;
  private final List<Broadcast> broadcasts;
  public final boolean needBroadcast;

  /** wrap both the update and broadcasts into a nice package */
  public LivingDocumentChange(RemoteDocumentUpdate update, List<Broadcast> broadcasts, Object response, boolean needBroadcast) {
    this.update = update;
    this.broadcasts = broadcasts;
    this.response = response;
    this.needBroadcast = needBroadcast;
  }

  /** complete the update. This is to be called once the change is made durable */
  public void complete() {
    if (broadcasts != null) {
      for (Broadcast bc : broadcasts) {
        bc.complete();
      }
    }
  }

  /** a closure to combine who to send too */
  public static class Broadcast {
    private final PrivateView view;
    private final String data;

    public Broadcast(PrivateView view, String data) {
      this.view = view;
      this.data = data;
    }

    public void complete() {
      view.deliver(data);
    }
  }
}
