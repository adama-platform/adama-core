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
package ape.runtime.reactives.maps;

import ape.runtime.contracts.RxParent;

import java.util.*;

/** simple pubsub fanout for MapSubscriptions's under a parent  */
public class MapPubSub<DomainTy> implements MapSubscription<DomainTy> {
  private final RxParent owner;
  private final ArrayList<MapSubscription<DomainTy>> subscriptions;
  private final HashSet<DomainTy> seen;

  public MapPubSub(RxParent owner) {
    this.owner = owner;
    this.subscriptions = new ArrayList<>();
    this.seen = new HashSet<>();
  }

  @Override
  public boolean alive() {
    if (owner != null) {
      return owner.__isAlive();
    }
    return true;
  }

  @Override
  public boolean changed(DomainTy key) {
    if (seen.contains(key)) {
      return false;
    }
    seen.add(key);
    for (MapSubscription<DomainTy> subscription : subscriptions) {
      subscription.changed(key);
    }
    return true;
  }

  public void settle() {
    seen.clear();
  }

  public int count() {
    return subscriptions.size();
  }

  public void subscribe(MapSubscription ms) {
    subscriptions.add(ms);
  }

  public long __memory() {
    return 128 * subscriptions.size() + 1024;
  }

  public void gc() {
    Iterator<MapSubscription<DomainTy>> it = subscriptions.iterator();
    while (it.hasNext()) {
      if (!it.next().alive()) {
        it.remove();
      }
    }
  }
}
