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
package ape.caravan.index;

import io.netty.buffer.ByteBuf;
import ape.caravan.data.DiskMetrics;

import java.util.*;

/**
 * In-memory index mapping document IDs to lists of storage regions.
 * Tracks the ordered sequence of append regions for each document,
 * supports trimming old entries, deletion, and snapshot/restore for
 * WAL recovery. Reports size distribution metrics for monitoring.
 */
public class Index {
  private final HashMap<Long, ArrayList<AnnotatedRegion>> index;

  public Index() {
    this.index = new HashMap<>();
  }

  /** append a region to an id */
  public int append(long id, AnnotatedRegion region) {
    ArrayList<AnnotatedRegion> regions = index.get(id);
    if (regions == null) {
      regions = new ArrayList<>();
      index.put(id, regions);
    }
    regions.add(region);
    return regions.size();
  }

  /** return the regions bound to an object */
  public Iterator<AnnotatedRegion> get(long id) {
    List<AnnotatedRegion> regions = index.get(id);
    if (regions == null) {
      regions = Collections.emptyList();
    }
    return regions.iterator();
  }

  /** report on what is within the index */
  public void report(DiskMetrics metrics) {
    metrics.active_entries.set(index.size());
    int over_10K = 0;
    int over_20K = 0;
    int over_50K = 0;
    int over_100K = 0;
    int over_1M = 0;
    int total = 0;
    for (List<AnnotatedRegion> regions : index.values()) {
      for (AnnotatedRegion region : regions) {
        total++;
        if (region.size > 10000) {
          over_10K++;
        }
        if (region.size > 20000) {
          over_20K++;
        }
        if (region.size > 50000) {
          over_50K++;
        }
        if (region.size > 100000) {
          over_100K++;
        }
        if (region.size > 1000000) {
          over_1M++;
        }
      }
    }
    metrics.items_total.set(total);
    metrics.items_over_tenk.set(over_10K);
    metrics.items_over_twentyk.set(over_20K);
    metrics.items_over_fiftyk.set(over_50K);
    metrics.items_over_onehundredk.set(over_100K);
    metrics.items_over_onemega.set(over_1M);
  }

  public Set<Long> list() {
    return index.keySet();
  }

  /** does the index contain the given id */
  public boolean exists(long id) {
    return index.containsKey(id);
  }

  /** delete an object by id; return the regions allocated to it */
  public ArrayList<AnnotatedRegion> delete(long id) {
    return index.remove(id);
  }

  /** trim the head of an object (by id) the given maximum size; returned the returned regions */
  public ArrayList<AnnotatedRegion> trim(long id, int maxSize) {
    ArrayList<AnnotatedRegion> regions = index.get(id);
    if (regions != null && regions.size() > maxSize) {
      ArrayList<AnnotatedRegion> trimmed = new ArrayList<>();
      Iterator<AnnotatedRegion> it = regions.iterator();
      int count = regions.size() - maxSize;
      int k = 0;
      while (k < count && it.hasNext()) {
        AnnotatedRegion region = it.next();
        trimmed.add(region);
        it.remove();
        k++;
      }
      return trimmed;
    }
    return null;
  }

  /** take a snapshot of the index */
  public void snapshot(ByteBuf buf) {
    for (Map.Entry<Long, ArrayList<AnnotatedRegion>> entry : index.entrySet()) {
      buf.writeBoolean(true);
      buf.writeLongLE(entry.getKey());
      buf.writeIntLE(entry.getValue().size());
      for (AnnotatedRegion region : entry.getValue()) {
        buf.writeLongLE(region.position);
        buf.writeIntLE(region.size);
        buf.writeIntLE(region.seq);
        buf.writeLongLE(region.assetBytes);
      }
    }
    buf.writeBoolean(false);
  }

  /** load an index from a snapshot */
  public void load(ByteBuf buf) {
    index.clear();
    while (buf.readBoolean()) {
      long id = buf.readLongLE();
      int count = buf.readIntLE();
      ArrayList<AnnotatedRegion> regions = new ArrayList<>(count);
      for (int k = 0; k < count; k++) {
        long start = buf.readLongLE();
        int size = buf.readIntLE();
        int seq = buf.readIntLE();
        long assetBytes = buf.readLongLE();
        AnnotatedRegion region = new AnnotatedRegion(start, size, seq, assetBytes);
        regions.add(region);
      }
      index.put(id, regions);
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<Long, ArrayList<AnnotatedRegion>> entry : index.entrySet()) {
      sb.append(entry.getKey()).append("=");
      for (AnnotatedRegion region : entry.getValue()) {
        sb.append(region.toString());
      }
      sb.append(";");
    }
    return sb.toString();
  }
}
