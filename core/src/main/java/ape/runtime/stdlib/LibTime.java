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
package ape.runtime.stdlib;

import ape.runtime.natives.NtMaybe;
import ape.runtime.natives.NtTime;
import ape.runtime.natives.NtTimeSpan;
import ape.translator.reflect.Extension;
import ape.translator.reflect.HiddenType;

public class LibTime {
  @Extension
  public static int toInt(NtTime t) {
    return t.toInt();
  }

  @Extension
  public static NtTime extendWithinDay(NtTime t, NtTimeSpan s) {
    int end = ((int) (t.toInt() * 60 + s.seconds)) / 60;
    if (end >= 1440) end = 1439;
    if (end < 0) end = 0;
    return new NtTime(end / 60, end % 60);
  }

  @Extension
  public static NtTime cyclicAdd(NtTime t, NtTimeSpan s) {
    int next = ((int) (t.toInt() * 60 + s.seconds)) / 60;
    next %= 1400;
    if (next < 0) {
      next += 1400;
    }
    return new NtTime(next / 60, next % 60);
  }

  public static @HiddenType(clazz = NtTime.class) NtMaybe<NtTime> make(int hr, int min) {
    if (0 <= hr && hr <= 23 && 0 <= min && min <= 59) {
      return new NtMaybe<>(new NtTime(hr, min));
    } else {
      return new NtMaybe<>();
    }
  }

  public static boolean overlaps(NtTime aStart, NtTime aEnd, NtTime bStart, NtTime bEnd) {
    return LibMath.intersects(aStart.toInt(), aEnd.toInt(), bStart.toInt(), bEnd.toInt());
  }
}
