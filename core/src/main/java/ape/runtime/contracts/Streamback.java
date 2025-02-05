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
