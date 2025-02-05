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

import ape.common.Callback;

import java.util.List;

/** an interface for managing capacity */
public interface CapacityOverseer {

  /** list all the capacity for a space in the world */
  void listAllSpace(String space, Callback<List<CapacityInstance>> callback);

  /** list all the capacity for a space within a region */
  void listWithinRegion(String space, String region, Callback<List<CapacityInstance>> callback);

  /** list all the spaces bound to a machine within a region */
  void listAllOnMachine(String region, String machine, Callback<List<CapacityInstance>> callback);

  /** add capacity for a space to a host within a region */
  void add(String space, String region, String machine, Callback<Void> callback);

  /** remove capacity for a space from a host and region */
  void remove(String space, String region, String machine, Callback<Void> callback);

  /** nuke all capacity for a space */
  void nuke(String space, Callback<Void> callback);

  /** pick a new host for a space within a region (that is stable) */
  void pickStableHostForSpace(String space, String region, Callback<String> callback);

  /** pick a new host for a space that hasn't been allocated */
  void pickNewHostForSpace(String space, String region, Callback<String> callback);
}
