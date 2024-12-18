/*
* Adama Platform and Language
* Copyright (C) 2021 - 2025 by Adama Platform Engineering, LLC
* 
* This program is free software for non-commercial purposes: 
* you can redistribute it and/or modify it under the terms of the 
* GNU Affero General Public License as published by the Free Software Foundation,
* either version 3 of the License, or (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
* 
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package ape.common;

import java.util.concurrent.atomic.AtomicBoolean;

/** a very simple timeout against a simple executor */
public class SimpleTimeout {
  public static Runnable make(SimpleExecutor executor, long ms, Runnable action) {
    AtomicBoolean completed = new AtomicBoolean(false);
    executor.schedule(new NamedRunnable("simple-timeout") {
      @Override
      public void execute() throws Exception {
        if (!completed.get()) {
          action.run();
        }
      }
    }, ms);
    return () -> {
      completed.set(true);
    };
  }

  public static <T> Callback<T> WRAP(Runnable first, Callback<T> callback) {
    return new Callback<T>() {
      @Override
      public void success(T value) {
        first.run();
        callback.success(value);
      }

      @Override
      public void failure(ErrorCodeException ex) {
        first.run();
        callback.failure(ex);
      }
    };
  }
}
