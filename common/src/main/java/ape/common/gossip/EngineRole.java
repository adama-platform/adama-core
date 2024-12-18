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
package ape.common.gossip;

import java.util.Random;

public enum EngineRole {
  SuperNode(15, 4), Node(50, 5);

  public final int waitBasis;
  public final int waitRounds;

  EngineRole(int waitBasis, int waitRounds) {
    this.waitBasis = waitBasis;
    this.waitRounds = waitRounds;
  }

  public static int computeWait(Random jitter, EngineRole role) {
    int wait = role.waitBasis;
    for (int k = 0; k < role.waitRounds; k++) {
      wait += jitter.nextInt(role.waitBasis);
    }
    return wait;
  }
}
