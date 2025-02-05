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
package ape.runtime.reactives;

import ape.runtime.reactives.maps.MapSubscription;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/** filters invalidation events based on the keys within a map */
public class RxMapGuard<DomainTy> implements MapSubscription<DomainTy> {
  private final RxDependent owner;

  private TreeMap<Integer, FireGate> children;
  private FireGate current;
  private FireGate root;

  /** gate standing between invalidations and a child's view */
  private class FireGate {
    private boolean all;
    private TreeSet<DomainTy> keys;
    private boolean viewFired;

    public FireGate() {
      this.all = false;
      this.keys = null;
      this.viewFired = false;
    }

    private void raiseFireAndReset() {
      this.all = false;
      this.keys = null;
      this.viewFired = true;
      owner.invalidateParent();
    }

    private void justReset() {
      this.all = false;
      this.keys = null;
      this.viewFired = false;
    }

    public boolean key(DomainTy key) {
      if (viewFired) {
        return false;
      }
      if (all) {
        raiseFireAndReset();
        return true;
      }
      if (keys != null) {
        if (keys.contains(key)) {
          raiseFireAndReset();
          return true;
        }
      }
      return false;
    }
  }

  public RxMapGuard(RxDependent owner) {
    this.owner = owner;
    this.children = null;
    this.root = new FireGate();
    this.current = root;
  }

  @Override
  public boolean alive() {
    if (owner != null) {
      return owner.alive();
    }
    return true;
  }

  /** reset the state */
  public void reset() {
    root.justReset();
    root.viewFired = false;
    current = root;
  }

  public void resetView(int viewId) {
    FireGate cv = new FireGate();
    current = cv;
    if (children == null) {
      children = new TreeMap<>();
    }
    children.put(viewId, cv);
  }

  public void finishView() {
    current = root;
  }

  public boolean isFired(int viewId) {
    if (children != null) {
      FireGate cv = children.get(viewId);
      if (cv != null) {
        return cv.viewFired;
      }
    }
    return false;
  }

  @Override
  public boolean changed(DomainTy key) {
    if (children != null) {
      for (Map.Entry<Integer, FireGate> cv : children.entrySet()) {
        cv.getValue().key(key);
      }
    } else {
      if (root.key(key)) {
        owner.__raiseInvalid();
      }
    }
    return false;
  }

  /** [capture] everything was read, so optimize for just that */
  public void readAll() {
    current.all = true;
  }

  /** [capture] a primary key was read */
  public void readKey(DomainTy pkey) {
    if (current.all) {
      return;
    }
    if (current.keys == null) {
      current.keys = new TreeSet<>();
    }
    current.keys.add(pkey);
  }

  public void __settle(Set<Integer> views) {
    // TODO: debounce and call cleanup child views
  }
}
