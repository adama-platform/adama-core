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
package ape.runtime.sys.web;

import ape.runtime.natives.NtDynamic;
import ape.runtime.natives.NtMap;

import java.util.TreeMap;

/** represents a get request */
public class WebGet {
  public final WebContext context;
  public final String uri;
  public final NtMap<String, String> headers;
  public final NtDynamic parameters;

  public WebGet(WebContext context, String uri, TreeMap<String, String> headers, NtDynamic parameters) {
    this.context = context;
    this.uri = uri;
    this.headers = new NtMap<>();
    this.headers.storage.putAll(headers);
    this.parameters = parameters;
  }
}
