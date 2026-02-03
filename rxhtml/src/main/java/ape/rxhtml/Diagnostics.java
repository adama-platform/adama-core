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
package ape.rxhtml;

import com.fasterxml.jackson.databind.node.ObjectNode;
import ape.rxhtml.template.Task;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Diagnostic information produced during RxHTML compilation.
 * Contains metadata useful for tooling, optimization, and debugging including
 * CSS class usage frequency, async tasks, inferred view schema, and output size metrics.
 */
public class Diagnostics {
  public final HashMap<String, Integer> cssFreq;
  public final ArrayList<Task> tasks;
  public final ObjectNode viewSchema;
  public final int javascriptSize;

  public Diagnostics(HashMap<String, Integer> cssFreq, ArrayList<Task> tasks, ObjectNode viewSchema, int javascriptSize) {
    this.cssFreq = cssFreq;
    this.tasks = tasks;
    this.viewSchema = viewSchema;
    this.javascriptSize = javascriptSize;
  }
}
