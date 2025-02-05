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
package ape.translator.tree.definitions;

import ape.translator.env.FreeEnvironment;
import ape.translator.parser.Formatter;
import ape.translator.parser.token.Token;
import ape.translator.tree.statements.Block;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.reactive.TyReactiveInteger;
import ape.translator.tree.types.reactive.TyReactiveTime;
import ape.translator.tree.types.topo.TypeCheckerRoot;

import java.util.function.Consumer;

public class DefineCronTask extends Definition {
  public final Token cron;
  public final Token name;
  public final Token[] schedule;
  public final Block code;

  public DefineCronTask(Token cron, Token name, Token[] schedule, Block code) {
    this.cron = cron;
    this.name = name;
    this.schedule = schedule;
    this.code = code;
    ingest(cron);
    ingest(code);
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    yielder.accept(cron);
    yielder.accept(name);
    for (int k = 0; k < schedule.length; k++) {
      yielder.accept(schedule[k]);
    }
    code.emit(yielder);
  }

  @Override
  public void format(Formatter formatter) {
    code.format(formatter);
  }

  public void typing(TypeCheckerRoot checker) {
    FreeEnvironment fe = FreeEnvironment.root();
    code.free(fe);
    checker.register(fe.free, (environment) -> {
      if (schedule[1].isIdentifier()) {
        TyType typeFound = environment.rules.Resolve(environment.document.root.storage.fields.get(schedule[1].text).type, false);
        if (typeFound == null) {
          environment.document.createError(this, "cron job '" + name.text + "' failed to find variable '" + schedule[1].text + "' in root document");
        } else {
          switch (schedule[0].text) {
            case "daily":
              if (!(typeFound instanceof TyReactiveTime)) {
                environment.document.createError(this, "cron job '" + name.text + "' found variable '" + schedule[1].text + "' to have type '" + typeFound.getAdamaType() + "' instead of time");
              }
              break;
            case "hourly":
            case "monthly":
              if (!(typeFound instanceof TyReactiveInteger)) {
                environment.document.createError(this, "cron job '" + name.text + "' found variable '" + schedule[1].text + "' to have type '" + typeFound.getAdamaType() + "' instead of int");
              }
              break;
          }
        }
      } else {
        switch (schedule[0].text) {
          case "daily": {
            int hr = Integer.parseInt(schedule[1].text);
            int min = Integer.parseInt(schedule[3].text);
            if (hr < 0 || hr > 23) {
              environment.document.createError(this, "The hour '" + hr + "' is not between 0 and 23 inclusive");
            }
            if (min < 0 || min > 59) {
              environment.document.createError(this, "The minute '" + min + "' is not between 0 and 59 inclusive");
            }
            break;
          }
          case "hourly": {
            int min = Integer.parseInt(schedule[1].text);
            if (min < 0 || min > 31) {
              environment.document.createError(this, "The minute '" + min + "' is not between 0 and 59 inclusive");
            }
            break;
          }
          case "monthly": {
            int day = Integer.parseInt(schedule[1].text);
            if (day < 0 || day > 31) {
              environment.document.createError(this, "The day of month '" + day + "' is not between 0 and 31 inclusive");
            }
            break;
          }
        }
      }
      final var next = environment.scope();
      code.typing(next);
    });
  }
}
