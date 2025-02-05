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

import ape.runtime.contracts.RxChild;
import ape.runtime.contracts.RxParent;
import ape.runtime.reactives.maps.MapGuardTarget;

import java.util.ArrayList;

public abstract class RxDependent extends RxNerfedBase implements RxChild {
  protected ArrayList<GuardPairCommon> guards;

  /** combine guard pairs regardless of the data type */
  protected interface GuardPairCommon {
    /** for formulas */
    public void start();
    public void finish();

    /** for viewer centric bubbles */
    public void startView(int viewId);
    public void finishView();
    public boolean isFired(int viewId);
  }

  /** guard a table */
  protected class GuardPairTable implements GuardPairCommon {
    protected final RxTable<?> table;
    protected final RxTableGuard guard;

    protected GuardPairTable(RxTable<?> table, RxTableGuard guard) {
      this.table = table;
      this.guard = guard;
    }

    @Override
    public void start() {
      guard.reset();
      table.pushGuard(guard);
    }

    @Override
    public void finish() {
      table.popGuard();
    }

    @Override
    public void startView(int viewId) {
      guard.resetView(viewId);
      table.pushGuard(guard);
    }

    @Override
    public void finishView() {
      guard.finishView();
      table.popGuard();
    }

    @Override
    public boolean isFired(int viewId) {
      return guard.isFired(viewId);
    }
  }

  /** guard a map */
  protected class GuardPairMap implements GuardPairCommon {
    protected final MapGuardTarget map;
    protected final RxMapGuard<?> guard;

    protected GuardPairMap(MapGuardTarget map, RxMapGuard<?> guard) {
      this.map = map;
      this.guard = guard;
    }

    @Override
    public void start() {
      guard.reset();
      map.pushGuard(guard);
    }

    @Override
    public void finish() {
      map.popGuard();
    }

    @Override
    public void startView(int viewId) {
      guard.resetView(viewId);
      map.pushGuard(guard);
    }

    @Override
    public void finishView() {
      guard.finishView();
      map.popGuard();
    }

    @Override
    public boolean isFired(int viewId) {
      return guard.isFired(viewId);
    }
  }

  protected RxDependent(RxParent __parent) {
    super(__parent);
    this.guards = null;
  }

  /** is the thing alive */
  public abstract boolean alive();

  /** [formula mode] start capturing the reads */
  public void start() {
    if (guards != null) {
      for (GuardPairCommon gp : guards) {
        gp.start();
      }
    }
  }

  /** [formula mode] finish up capturing reads */
  public void finish() {
    if (guards != null) {
      for (GuardPairCommon gp : guards) {
        gp.finish();
      }
    }
  }

  /** connect a tableguard to a table */
  public void __guard(RxTable<?> table, RxTableGuard guard) {
    if (guards == null) {
      guards = new ArrayList<>();
    }
    guards.add(new GuardPairTable(table, guard));
  }

  public void __guard(MapGuardTarget map, RxMapGuard<?> guard) {
    if (guards == null) {
      guards = new ArrayList<>();
    }
    guards.add(new GuardPairMap(map, guard));
  }

  /** [bubble version] start capturing reads */
  public void startView(int viewId) {
    if (guards != null) {
      for (GuardPairCommon gp : guards) {
        gp.startView(viewId);
      }
    }
  }

  /** [bubble version] stop capturing reads */
  public void finishView() {
    if (guards != null) {
      for (GuardPairCommon gp : guards) {
        gp.finishView();
      }
    }
  }

  /** is the given view in a fired state */
  public boolean isFired(int viewId) {
    if (guards != null) {
      for (GuardPairCommon gp : guards) {
        if (gp.isFired(viewId)) {
          return true;
        }
      }
    }
    return false;
  }

  public void invalidateParent() {
    if (__parent != null) {
      __parent.__invalidateUp();
    }
  }
}
