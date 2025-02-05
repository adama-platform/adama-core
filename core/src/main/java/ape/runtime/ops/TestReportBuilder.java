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
package ape.runtime.ops;

import ape.runtime.json.JsonStreamWriter;

import java.util.ArrayList;
import java.util.HashMap;

/** a fun way to build test reports */
public class TestReportBuilder {
  private final HashMap<String, Object> dumps;
  private final StringBuilder report;
  private int failures;
  private String current;
  private final HashMap<String, ArrayList<String>> logs;

  public TestReportBuilder() {
    this.report = new StringBuilder();
    this.dumps = new HashMap<>();
    this.logs = new HashMap<>();
    this.current = null;
  }

  public void annotate(final String name, final HashMap<String, Object> dump) {
    if (dump.size() > 0) {
      JsonStreamWriter writer = new JsonStreamWriter();
      writer.writeTree(dump);
      report.append("...DUMP:").append(writer).append("\n");
    }
    dumps.put(name, dump);
  }

  public void log(String message, int startLine, int startChar, int endLine, int endChar) {
    if (current != null) {
      ArrayList<String> log = logs.get(current);
      if (log == null) {
        log = new ArrayList<>();
        logs.put(current, log);
      }
      log.add(message + " {line:" + startLine + "}");
    }
  }

  public void aborted() {
    report.append(" ABORTED!");
  }

  public void begin(final String name) {
    current = name;
    report.append("TEST[").append(name).append("]");
  }

  public void end(final AssertionStats stats) {
    if (stats.total > 0) {
      report.append(" = ").append(Math.round((stats.total - stats.failures) * 1000.0 / stats.total) / 10.0).append("%");
      if (stats.failures > 0) {
        report.append(" (HAS FAILURES)");
      }
      report.append("\n");
    } else {
      report.append(" HAS NO ASSERTS\n");
    }
    if (current != null) {
      ArrayList<String> log = logs.get(current);
      if (log != null) {
        for (String ln : log) {
          report.append(" - LOG:").append(ln).append("\n");
        }
      }
    }
    failures += stats.failures;
  }

  public int getFailures() {
    return failures;
  }

  @Override
  public String toString() {
    return report.toString();
  }
}
