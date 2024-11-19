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

import com.fasterxml.jackson.databind.node.ObjectNode;
import ape.common.Json;
import ape.common.template.Settings;
import ape.runtime.natives.NtDynamic;
import ape.runtime.natives.NtMaybe;
import ape.runtime.natives.NtTemplate;
import ape.translator.reflect.Extension;
import ape.translator.reflect.HiddenType;

/** library for NtTemplate */
public class LibTemplate {

  private static final Settings HTML = new Settings(true);
  private static final Settings RAW = new Settings(false);

  @Extension
  public static @HiddenType(clazz = String.class) NtMaybe<String> html(NtTemplate template, NtDynamic inputs) {
    try {
      ObjectNode parsed = Json.parseJsonObject(inputs.json);
      StringBuilder sb = new StringBuilder();
      template.template.render(HTML, parsed, sb);
      return new NtMaybe<>(sb.toString());
    } catch (Exception ex) {
      return new NtMaybe<>();
    }
  }

  @Extension
  public static @HiddenType(clazz = String.class) NtMaybe<String> raw(NtTemplate template, NtDynamic inputs) {
    try {
      ObjectNode parsed = Json.parseJsonObject(inputs.json);
      StringBuilder sb = new StringBuilder();
      template.template.render(RAW, parsed, sb);
      return new NtMaybe<>(sb.toString());
    } catch (Exception ex) {
      return new NtMaybe<>();
    }
  }
}
