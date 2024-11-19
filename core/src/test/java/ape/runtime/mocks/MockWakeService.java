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
package ape.runtime.mocks;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.runtime.data.Key;
import ape.runtime.sys.cron.WakeService;

import java.util.ArrayList;

public class MockWakeService implements WakeService {
  public final ArrayList<String> alarms;

  public MockWakeService() {
    this.alarms = new ArrayList<>();
  }

  public String get(int k) {
    return alarms.get(k);
  }

  @Override
  public synchronized void wakeIn(Key key, long when, Callback<Void> callback) {
    alarms.add("WAKE:" + key.space + "/" + key.key + "@" + when);
    if (key.key.equals("fail")) {
      callback.failure(new ErrorCodeException(0));
      return;
    }
    callback.success(null);
  }
}
