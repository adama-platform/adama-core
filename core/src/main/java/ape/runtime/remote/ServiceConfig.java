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
package ape.runtime.remote;

import ape.common.ErrorCodeException;

import java.util.Set;

/** configuration for a service per space */
public interface ServiceConfig {
  public String getSpace();

  public Set<String> getKeys();

  public int getInteger(String key, int defaultValue) throws ErrorCodeException;

  public String getString(String key, String defaultValue) throws ErrorCodeException;

  public String getDecryptedSecret(String key) throws ErrorCodeException;
}