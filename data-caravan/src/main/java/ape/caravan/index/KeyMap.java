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
import ape.caravan.entries.DelKey;
import ape.caravan.entries.MapKey;
import ape.runtime.data.Key;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class KeyMap {
  private final HashMap<Key, Integer> forward;
  private final HashMap<Integer, Key> reverse;
  private int idgen;

  public KeyMap() {
    this.forward = new HashMap<>();
    this.reverse = new HashMap<>();
    this.idgen = 0;
  }

  public void apply(MapKey map) {
    Key key = map.of();
    forward.put(key, map.id);
    reverse.put(map.id, key);
    if (map.id > idgen) {
      idgen = map.id;
    }
  }

  public void apply(DelKey del) {
    Integer result = forward.remove(del.of());
    if (result != null) {
      reverse.remove(result);
    }
  }

  public Integer get(Key key) {
    return forward.get(key);
  }

  public boolean exists(Key key) {
    return forward.containsKey(key);
  }

  public TreeMap<Key, Integer> copy() {
    return new TreeMap<>(forward);
  }

  public MapKey inventAndApply(Key key) {
    if (forward.containsKey(key)) {
      return null;
    }
    int id = ++idgen;
    while (true) {
      if (!reverse.containsKey(id)) {
        MapKey result = new MapKey(key, id);
        apply(result);
        return result;
      }
      id = ++idgen;
    }
  }

  /** take a snapshot of the index */
  public void snapshot(ByteBuf buf) {
    for (Map.Entry<Key, Integer> entry : forward.entrySet()) {
      buf.writeBoolean(true);
      byte[] space = entry.getKey().space.getBytes(StandardCharsets.UTF_8);
      byte[] key = entry.getKey().key.getBytes(StandardCharsets.UTF_8);
      buf.writeIntLE(space.length);
      buf.writeBytes(space);
      buf.writeIntLE(key.length);
      buf.writeBytes(key);
      buf.writeIntLE(entry.getValue());
    }
    buf.writeBoolean(false);
  }

  /** load an index from a snapshot */
  public void load(ByteBuf buf) {
    forward.clear();
    reverse.clear();
    while (buf.readBoolean()) {
      int sizeSpace = buf.readIntLE();
      byte[] bytesSpace = new byte[sizeSpace];
      buf.readBytes(bytesSpace);
      int sizeKey = buf.readIntLE();
      byte[] bytesKey = new byte[sizeKey];
      buf.readBytes(bytesKey);
      int id = buf.readIntLE();
      Key key = new Key(new String(bytesSpace, StandardCharsets.UTF_8), new String(bytesKey, StandardCharsets.UTF_8));
      forward.put(key, id);
      reverse.put(id, key);
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[keymap;");
    for (Map.Entry<Key, Integer> entry : forward.entrySet()) {
      sb.append(entry.getKey().space + "/" + entry.getKey().key + "==" + entry.getValue() + ";");
    }
    sb.append("]");
    return sb.toString();
  }
}
