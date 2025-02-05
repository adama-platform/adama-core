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
package ape.runtime.natives;

import ape.runtime.contracts.RxChild;
import ape.runtime.natives.algo.CanWriteCSV;
import ape.runtime.natives.algo.MessageCSVWriter;
import ape.runtime.natives.lists.ArrayNtList;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Supplier;

/** a table defined within code */
public class NtTable<Ty extends NtMessageBase> implements RxChild {
  private final ArrayList<Ty> items;
  private final Supplier<Ty> maker;

  public NtTable(final NtTable<Ty> other) {
    this.maker = other.maker;
    this.items = new ArrayList<>(other.items);
  }

  public NtTable(final Supplier<Ty> maker) {
    this.maker = maker;
    this.items = new ArrayList<>();
  }

  @Override
  public boolean __raiseInvalid() {
    return true;
  }

  public void delete() {
    items.clear();
  }

  public NtList<Ty> iterate(final boolean done) {
    return new ArrayNtList<>(items);
  }

  public Ty make() {
    final var item = maker.get();
    items.add(item);
    return item;
  }

  public int size() {
    return items.size();
  }

  public String to_csv(String header, Function<Ty, CanWriteCSV> cast) {
    StringBuilder sb = new StringBuilder();
    sb.append(header);
    for (Ty item : items) {
      sb.append("\r\n");
      MessageCSVWriter writer = new MessageCSVWriter();
      cast.apply(item).__write_csv_row(writer);
      sb.append(writer.toString());
    }
    return sb.toString();
  }
}
