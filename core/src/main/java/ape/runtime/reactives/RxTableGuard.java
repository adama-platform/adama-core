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

import ape.runtime.reactives.tables.TableSubscription;

import java.util.*;

/**
 * Fine-grained invalidation filter for table-dependent computed values.
 * Tracks which primary keys and index values were actually read during
 * formula evaluation, then filters incoming change events to only fire
 * invalidations when relevant data changes. Supports per-viewer tracking
 * via FireGate instances for efficient incremental view updates. This
 * minimizes unnecessary recomputation when unrelated table rows change.
 */
public class RxTableGuard implements TableSubscription {
  private final RxDependent owner;
  private TreeMap<Integer, FireGate> children;
  private FireGate current;
  private FireGate root;

  /** gate standing between invalidations and a child's view */
  private class FireGate {
    private boolean all;
    private TreeSet<Integer> primaryKeys;
    private TreeMap<Integer, TreeSet<Integer>> indices;
    private boolean viewFired;

    public FireGate() {
      this.all = false;
      this.primaryKeys = null;
      this.indices = null;
      this.viewFired = false;
    }

    private void justReset() {
      this.all = false;
      this.primaryKeys = null;
      this.indices = null;
      this.viewFired = false;
    }

    private void raiseFireAndReset() {
      this.all = false;
      this.primaryKeys = null;
      this.indices = null;
      this.viewFired = true;
      owner.invalidateParent();
    }

    public boolean index(int field, int value) {
      if (viewFired) {
        return false;
      }
      if (all) {
        raiseFireAndReset();
        return true;
      }
      if (indices != null) {
        TreeSet<Integer> vals = indices.get(field);
        if (vals != null) {
          if (vals.contains(value)) {
            raiseFireAndReset();
            return true;
          }
        }
      }
      return false;
    }

    public boolean primary(int primaryKey) {
      if (viewFired) {
        return false;
      }
      if (all) {
        raiseFireAndReset();
        return true;
      }
      if (primaryKeys != null) {
        if (primaryKeys.contains(primaryKey)) {
          raiseFireAndReset();
          return true;
        }
      }
      return false;
    }
  }

  public RxTableGuard(RxDependent owner) {
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

  /** there was a change in an index */
  @Override
  public void index(int field, int value) {
    if (children != null) {
      for (Map.Entry<Integer, FireGate> cv : children.entrySet()) {
        cv.getValue().index(field, value);
      }
    } else {
      if (root.index(field, value)) {
        owner.__raiseInvalid();
      }
    }
  }

  /** there was a change in a primary key */
  @Override
  public boolean primary(int primaryKey) {
    if (children != null) {
      for (Map.Entry<Integer, FireGate> cv : children.entrySet()) {
        cv.getValue().primary(primaryKey);
      }
    } else {
      if (root.primary(primaryKey)) {
        owner.__raiseInvalid();
      }
    }
    return false;
  }

  /** reset the state */
  public void reset() {
    root.justReset();
    root.viewFired = false;
    current = root;
  }

  /** [capture] everything was read, so optimize for just that */
  public void readAll() {
    current.all = true;
  }

  /** [capture] a primary key was read */
  public void readPrimaryKey(int pkey) {
    if (current.all) {
      return;
    }
    if (current.primaryKeys == null) {
      current.primaryKeys = new TreeSet<>();
    }
    current.primaryKeys.add(pkey);
  }

  /** [capture] an index value was read */
  public void readIndexValue(int index, int value) {
    if (current.all) {
      return;
    }
    if (current.indices == null) {
      current.indices = new TreeMap<>();
    }
    TreeSet<Integer> vals = current.indices.get(index);
    if (vals == null) {
      vals = new TreeSet<>();
      current.indices.put(index, vals);
    }
    vals.add(value);
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

  public void __settle(Set<Integer> views) {
    if (children != null && views != null) {
      if (children.size() > 2 * views.size()) {
        Iterator<Map.Entry<Integer, FireGate>> it = children.entrySet().iterator();
        while (it.hasNext()) {
          if (!views.contains(it.next().getKey())) {
            it.remove();
          }
        }
      }
    }
  }
}
