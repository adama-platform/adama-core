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
package ape.runtime.sys;

import ape.common.Callback;
import ape.runtime.data.DocumentRestore;
import ape.runtime.data.Key;

/** a restore request */
public class QueuedRestoreRequest {
  public final CoreRequestContext context;
  public final Key key;
  public final DocumentRestore snapshot;
  public final Callback<Void> callback;

  public QueuedRestoreRequest(CoreRequestContext context, Key key, DocumentRestore snapshot, Callback<Void> callback) {
    this.context = context;
    this.key = key;
    this.snapshot = snapshot;
    this.callback = callback;
  }
}
