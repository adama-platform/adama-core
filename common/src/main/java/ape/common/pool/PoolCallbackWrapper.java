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
package ape.common.pool;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.Living;

/** adapt an existing callback to register success/failure signals onto a PoolItem */
public class PoolCallbackWrapper<X, S extends Living> implements Callback<X> {
  private final Callback<X> wrapped;
  private final PoolItem<S> item;

  public PoolCallbackWrapper(Callback<X> wrapped, PoolItem<S> item) {
    this.wrapped = wrapped;
    this.item = item;
  }

  @Override
  public void success(X value) {
    try {
      wrapped.success(value);
    } finally {
      item.returnToPool();
    }
  }

  @Override
  public void failure(ErrorCodeException ex) {
    try {
      wrapped.failure(ex);
    } finally {
      item.signalFailure();
    }
  }
}
