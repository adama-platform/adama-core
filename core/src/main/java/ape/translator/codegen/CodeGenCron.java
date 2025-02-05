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
package ape.translator.codegen;

import ape.translator.env.Environment;
import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.definitions.DefineCronTask;

import java.util.Map;

public class CodeGenCron {
  public static void writeCronExecution(final StringBuilderWithTabs sb, Environment env) {
    for (Map.Entry<String, DefineCronTask> entry : env.document.cronTasks.entrySet()) {
      sb.append("private void __cron_body_").append(entry.getKey()).append("()");
      entry.getValue().code.writeJava(sb, env);
      sb.writeNewline();
    }
    sb.append("@Override").writeNewline();
    int countdown = env.document.cronTasks.size();
    if (countdown == 0) {
      sb.append("public void __make_cron_progress() {}").writeNewline();
      return;
    }
    sb.append("public void __make_cron_progress() {").tabUp().writeNewline();
    sb.append("CronTask __current;").writeNewline();
    sb.append("long __now = __time.get();").writeNewline();
    sb.append("ZoneId __fromTZ = ZoneId.systemDefault();").writeNewline();
    sb.append("ZoneId __toTZ = __zoneId();").writeNewline();
    for (Map.Entry<String, DefineCronTask> entry : env.document.cronTasks.entrySet()) {
      DefineCronTask dct = entry.getValue();
      if (dct.schedule[1].isIdentifier()) {
        sb.append("__current = CronChecker.").append(dct.schedule[0].text).append("(__").append(dct.name.text).append(", __now, ").append(dct.schedule[1].text).append(", __fromTZ, __toTZ);").writeNewline();
      } else{
        switch (dct.schedule[0].text) {
          case "daily":
            sb.append("__current = CronChecker.daily(__").append(dct.name.text).append(", __now, ").append(dct.schedule[1].text).append(", ").append(dct.schedule[3].text).append(", __fromTZ, __toTZ);").writeNewline();
            break;
          case "hourly": // note: these share the same signature with different function names
          case "monthly":
            sb.append("__current = CronChecker.").append(dct.schedule[0].text).append("(__").append(dct.name.text).append(", __now, ").append(dct.schedule[1].text).append(", __fromTZ, __toTZ);").writeNewline();
            break;
          default:
            throw new RuntimeException("unknown schedule type:" + dct.schedule[0].text);
        }
      }
      sb.append("if (__current.fire) ").append("{").tabUp().writeNewline();
      sb.append("__cron_body_").append(entry.getKey()).append("();").tabDown().writeNewline();
      sb.append("}").writeNewline();
      sb.append("__optimisticNextCronCheck = __current.integrate(__optimisticNextCronCheck);");
      countdown--;
      if (countdown == 0) {
        sb.tabDown();
      }
      sb.writeNewline();
    }
    sb.append("}").writeNewline();
  }
  public static void writeCronReset(final StringBuilderWithTabs sb, Environment env) {
    sb.append("@Override").writeNewline();
    int countdown = env.document.cronTasks.size();
    if (countdown == 0) {
      sb.append("protected void __reset_cron() {}").writeNewline();
      return;
    }
    sb.append("protected void __reset_cron() {").tabUp().writeNewline();
    sb.append("long __now = __time.get();").writeNewline();
    for (Map.Entry<String, DefineCronTask> entry : env.document.cronTasks.entrySet()) {
      DefineCronTask dct = entry.getValue();
      sb.append("__").append(dct.name.text).append(".set(__now);");
      countdown--;
      if (countdown == 0) {
        sb.tabDown();
      }
      sb.writeNewline();
    }
    sb.append("}").writeNewline();
  }

  public static void writeCronPredict(final StringBuilderWithTabs sb, Environment env) {
    sb.append("@Override").writeNewline();
    int countdown = env.document.cronTasks.size();
    if (countdown == 0) {
      sb.append("public Long __predict_cron_wake_time() { return null; }").writeNewline();
      return;
    }
    sb.append("public Long __predict_cron_wake_time() {").tabUp().writeNewline();
    sb.append("Long __predict = null;").writeNewline();
    sb.append("long __now = __time.get();").writeNewline();
    sb.append("ZoneId __fromTZ = ZoneId.systemDefault();").writeNewline();
    sb.append("ZoneId __toTZ = __zoneId();").writeNewline();
    for (Map.Entry<String, DefineCronTask> entry : env.document.cronTasks.entrySet()) {
      DefineCronTask dct = entry.getValue();
      if (dct.schedule[1].isIdentifier()) {
        sb.append("__predict = CronPredict.").append(dct.schedule[0].text).append("(__predict, __now, ").append(dct.schedule[1].text).append(", __fromTZ, __toTZ);").writeNewline();
      } else{
        switch (dct.schedule[0].text) {
          case "daily":
            sb.append("__predict = CronPredict.daily(__predict, __now, ").append(dct.schedule[1].text).append(", ").append(dct.schedule[3].text).append(", __fromTZ, __toTZ);").writeNewline();
            break;
          case "hourly": // note: these share the same signature with different function names
          case "monthly":
            sb.append("__predict = CronPredict.").append(dct.schedule[0].text).append("(__predict, __now, ").append(dct.schedule[1].text).append(", __fromTZ, __toTZ);").writeNewline();
            break;
          default:
            throw new RuntimeException("unknown schedule type:" + dct.schedule[0].text);
        }
      }
    }
    sb.append("  return __predict;").tabDown().writeNewline();
    sb.append("}").writeNewline();
  }
}
