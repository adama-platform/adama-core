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

import ape.runtime.json.JsonStreamReader;
import ape.runtime.natives.NtJson;
import ape.runtime.natives.NtMaybe;
import ape.translator.reflect.HiddenType;

public class LibJson {
  public static @HiddenType(clazz = NtJson.class) NtMaybe<NtJson> parse(String str) {
    try {
      JsonStreamReader reader = new JsonStreamReader(str);
      return new NtMaybe<>(new NtJson(reader.readJavaTree()));
    } catch (Exception ex) {
      return new NtMaybe<>();
    }
  }

  public static NtJson parsen(String str) {
    try {
      JsonStreamReader reader = new JsonStreamReader(str);
      return new NtJson(reader.readJavaTree());
    } catch (Exception ex) {
      return new NtJson();
    }
  }
}
