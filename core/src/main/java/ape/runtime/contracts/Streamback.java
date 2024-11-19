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
package ape.runtime.contracts;

import ape.common.ErrorCodeException;
import ape.runtime.sys.CoreStream;

/** This is like a callback, but for an infinite stream. */
public interface Streamback {
  /** the stream has been setup and can be interacted with via the core stream */
  void onSetupComplete(CoreStream stream);

  /** inform the client of the traffic hint */
  void traffic(String trafficHint);

  /** inform the client of a status update */
  void status(StreamStatus status);

  /** inform the client of new data */
  void next(String data);

  /** inform the client that a failure has occurred */
  void failure(ErrorCodeException exception);

  /** the stream has a status representing what is happening at the given moment */
  enum StreamStatus {
    Connected, Disconnected
  }
}
