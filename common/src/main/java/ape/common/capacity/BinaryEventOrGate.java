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
package ape.common.capacity;

/** Two events are deduped into one */
public class BinaryEventOrGate {
  private final BoolConsumer event;
  private boolean a;
  private boolean b;
  private boolean result;

  public BinaryEventOrGate(final BoolConsumer event) {
    this.event = event;
    this.a = false;
    this.b = false;
    this.result = false;
  }

  public void a(Boolean value) {
    this.a = value;
    update();
  }

  private void update() {
    boolean next = a || b;
    if (result != next) {
      this.result = next;
      event.accept(this.result);
    }
  }

  public void b(Boolean value) {
    this.b = value;
    update();
  }
}
