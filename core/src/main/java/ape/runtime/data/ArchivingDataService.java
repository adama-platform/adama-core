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
package ape.runtime.data;

import ape.common.Callback;

/** a data service which backup data */
public interface ArchivingDataService extends DataService {
  /** restore a file (must be idempotent) */
  void restore(Key key, String archiveKey, Callback<Void> callback);

  /** backup a document, returning an archiveKey */
  void backup(Key key, Callback<BackupResult> callback);

  /** delete an overwritten back up */
  void cleanUp(Key key, String archiveKey);
}
