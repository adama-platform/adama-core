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
package ape.runtime.sys.capacity;

import java.util.Objects;

/** a region bound machine */
public class CapacityInstance implements Comparable<CapacityInstance> {
  public final String space;
  public final String region;
  public final String machine;
  public final boolean override;

  public CapacityInstance(String space, String region, String machine, boolean override) {
    this.space = space;
    this.region = region;
    this.machine = machine;
    this.override = override;
  }

  @Override
  public int hashCode() {
    return Objects.hash(space, region, machine, override);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CapacityInstance instance = (CapacityInstance) o;
    return override == instance.override && Objects.equals(space, instance.space) && Objects.equals(region, instance.region) && Objects.equals(machine, instance.machine);
  }

  @Override
  public int compareTo(CapacityInstance o) {
    int delta = region.compareTo(o.region);
    if (delta == 0) {
      delta = machine.compareTo(o.machine);
      if (delta == 0) {
        delta = space.compareTo(o.space);
        if (delta == 0) {
          delta = Boolean.compare(override, o.override);
        }
      }
    }
    return delta;
  }
}
