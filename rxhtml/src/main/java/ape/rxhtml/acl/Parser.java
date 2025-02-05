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
package ape.rxhtml.acl;

import ape.rxhtml.acl.commands.*;
import ape.rxhtml.acl.commands.*;
import ape.rxhtml.atl.ParseException;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class Parser {

  public static ArrayList<Command> parse(String command) throws ParseException  {
    ArrayList<Command> commands = new ArrayList<>();
    for (String phrase : fragmentize(command)) {
      int kColon = phrase.indexOf(':');
      if (kColon > 0) {
        String cmd = phrase.substring(0, kColon).trim().toLowerCase();
        String body = phrase.substring(kColon + 1).trim();
        switch (cmd) {
          case "submit": {
            commands.add(new SubmitById(body.trim()));
            break;
          }
          case "toggle": {
            commands.add(new Toggle(body));
            break;
          }
          case "increment":
          case "inc": {
            commands.add(new Increment(body));
            break;
          }
          case "custom":
            commands.add(new Custom(body));
            break;
          case "decrement":
          case "dec": {
            commands.add(new Decrement(body));
            break;
          }
          case "raise": {
            commands.add(new Set(body, "true"));
            break;
          }
          case "lower": {
            commands.add(new Set(body, "false"));
            break;
          }
          case "fire": {
            commands.add(new Fire(body));
          }
          break;
          case "decide": {
            String[] parts = body.split(Pattern.quote("|"));
            commands.add(new Decide(parts[0], parts.length > 1 ? parts[1] : "id", parts.length > 2 ? parts[2] : "id"));
            break;
          }
          case "choose": {
            String[] parts = body.split(Pattern.quote("|"));
            commands.add(new Choose(parts[0], parts.length > 1 ? parts[1] : "id", parts.length > 2 ? parts[2] : "id"));
            break;
          }
          case "finalize": {
            commands.add(new Finalize(body));
            break;
          }
          case "goto": {
            commands.add(new Goto(body));
            break;
          }
          case "set": {
            int kEq = body.indexOf('=');
            if (kEq > 0) {
              commands.add(new Set(body.substring(0, kEq).trim(), body.substring(kEq + 1).trim()));
            }
            break;
          }
          case "ot":
          case "order-toggle": {
            int kEq = body.indexOf('=');
            if (kEq > 0) {
              commands.add(new OrderToggle(body.substring(0, kEq).trim(), body.substring(kEq + 1).trim()));
            }
            break;
          }
          case "te": {
            commands.add(new TransferError(body));
            break;
          }
          case "scroll": {
            commands.add(new Scroll(body));
            break;
          }
          case "tm": {
            String[] parts = body.split(Pattern.quote("|"));
            int offX = parts.length > 1 ? TransferMouse.parseIntOrZero(parts[0]) : 0;
            int offY = parts.length > 2 ? TransferMouse.parseIntOrZero(parts[1]) : 0;
            commands.add(new TransferMouse(parts[0], offX, offY));
            break;
          }
          case "force-auth": {
            int kEq = body.indexOf('=');
            if (kEq > 0) {
              commands.add(new ForceAuth(body.substring(0, kEq).trim(), body.substring(kEq + 1).trim()));
            }
            break;
          }
          case "manifest-add": { // value is a web manifest URL
            commands.add(new ManifestBaseCommand("a", body));
            break;
          }
          case "manifest-use": { // value is an ID
            commands.add(new ManifestBaseCommand("u", body));
            break;
          }
          case "manifest-delete":
          case "manifest-del": { // value is an ID
            commands.add(new ManifestBaseCommand("d", body));
            break;
          }
        }
      } else {
        if ("reset".equals(phrase)) {
          commands.add(new Reset());
        } else if ("submit".equals(phrase)) {
          commands.add(new Submit());
        } else if ("resume".equals(phrase)) {
          commands.add(new Resume());
        } else if ("sign-out".equalsIgnoreCase(phrase)) {
          commands.add(new SignOut());
        } else if ("nuke".equals(phrase)) {
          commands.add(new Nuke());
        } else if ("uncheck".equals(phrase)) {
          commands.add(new Uncheck());
        } else if ("toggle-password".equals(phrase)) {
          commands.add(new TogglePassword());
        } else if ("reload".equals(phrase)) {
          commands.add(new Reload());
        }
      }
    }
    return commands;
  }

  public static ArrayList<String> fragmentize(String command) {
    ArrayList<String> fragments = new ArrayList<>();
    String tail = command;
    while (tail.length() > 0) {
      int kCut = tail.indexOf(' ');
      if (kCut > 0) {
        int kQuote = tail.indexOf('\'');
        if (kQuote < kCut) {
          int kEnd = tail.indexOf('\'', kQuote + 1);
          if (kEnd > kCut) {
            kCut = kEnd;
          }
        }
        String add = tail.substring(0, kCut).replaceAll(Pattern.quote("'"), "");
        fragments.add(add);
        tail = tail.substring(kCut + 1).trim();
      } else {
        fragments.add(tail);
        return fragments;
      }
    }
    return fragments;
  }
}
