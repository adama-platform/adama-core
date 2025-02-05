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
package ape.rxhtml.template;

import ape.rxhtml.template.sp.*;
import ape.rxhtml.template.sp.*;

import java.util.ArrayList;
import java.util.Locale;

public class StatePath {
  public final String command;
  public final String name;
  public final boolean simple;
  public final ArrayList<PathInstruction> instructions;

  private StatePath(ArrayList<PathInstruction> instructions, String command, String name, boolean simple) {
    this.instructions = instructions;
    this.command = command;
    this.name = name;
    this.simple = simple;
  }

  /** resolve the path */
  public static StatePath resolve(String path, String stateVar) {
    // NOTE: this is a very quick and dirty implementation
    String command = stateVar;
    String toParse = path.trim();
    ArrayList<PathInstruction> instructions = new ArrayList<>();
    int kColon = toParse.indexOf(':');
    if (kColon > 0) {
      String switchTo = toParse.substring(0, kColon).trim().toLowerCase(Locale.ENGLISH);
      if ("view".equals(switchTo)) {
        toParse = toParse.substring(kColon + 1).stripLeading();
        command = "$.pV(" + command + ")";
        instructions.add(new SwitchTo("view"));
      } else if ("data".equals(switchTo)) {
        toParse = toParse.substring(kColon + 1).stripLeading();
        command = "$.pD(" + command + ")";
        instructions.add(new SwitchTo("data"));
      }
    }

    // strip remaining data/view switches and only respect the first one
    kColon = toParse.indexOf(':');
    while (kColon > 0) {
      String switchTo = toParse.substring(0, kColon).trim().toLowerCase(Locale.ENGLISH);
      if ("view".equals(switchTo) || "data".equals(switchTo)) {
        toParse = toParse.substring(kColon + 1).stripLeading();
        kColon = toParse.indexOf(':');
      } else {
        kColon = -1;
      }
    }

    while (true) {
      if (toParse.startsWith("/")) {
        toParse = toParse.substring(1).stripLeading();
        command = "$.pR(" + command + ")";
        instructions.add(new GoRoot());
      } else if (toParse.startsWith("../")) {
        toParse = toParse.substring(3).stripLeading();
        command = "$.pU(" + command + ")";
        instructions.add(new GoParent());
      } else {
        int kDecide = first(toParse.indexOf('/'), toParse.indexOf('.'));
        if (kDecide > 0) {
          String scopeInto = toParse.substring(0, kDecide).stripTrailing();
          toParse = toParse.substring(kDecide + 1).stripLeading();
          command = "$.pI(" + command + ",'" + scopeInto + "')";
          instructions.add(new DiveInto(scopeInto));
        } else {
          return new StatePath(instructions, command, toParse, command.equals(stateVar));
        }
      }
    }
  }

  private static int first(int kDot, int kSlash) {
    // if they are both present, then return the minimum
    if (kDot > 0 && kSlash > 0) {
      return Math.min(kDot, kSlash);
    }
    if (kDot > 0) {
      return kDot;
    } else {
      return kSlash;
    }
  }

  /** is this a root level constant part of the view (assuming no implicit scope) */
  public boolean isRootLevelViewConstant() {
    for (PathInstruction instruction : instructions) {
      if (instruction instanceof DiveInto || instruction instanceof GoParent) {
        return false;
      }
      if (instruction instanceof SwitchTo) {
        if (!("view".equals(((SwitchTo) instruction).dest))) {
          return false;
        }
      }
    }
    return true;
  }
}
