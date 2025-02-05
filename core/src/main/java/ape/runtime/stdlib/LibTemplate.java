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
