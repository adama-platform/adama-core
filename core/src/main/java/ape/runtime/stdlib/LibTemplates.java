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
import ape.runtime.stdlib.intern.TemplateAccountRecoveryTemplateHtml;
import ape.runtime.stdlib.intern.TemplateMultiLineEmailTemplateHtml;

public class LibTemplates {

  public static String accountRecoveryDefaultEmail(String tempPassword, String resetUrl, String instantUrl) {
    ObjectNode data = Json.newJsonObject();
    data.put("temporary_password", tempPassword);
    data.put("reset_url", resetUrl);
    data.put("instant_url", instantUrl);
    StringBuilder output = new StringBuilder();
    TemplateAccountRecoveryTemplateHtml.TEMPLATE.render(new Settings(true), data, output);
    return output.toString();
  }

  public static String multilineEmailWithButton(String title, String first_line, String second_line, String body, String button_url, String button, String final_line) {
    ObjectNode data = Json.newJsonObject();
    data.put("title", title);
    data.put("first_line", first_line);
    data.put("second_line", second_line);
    data.put("body", body);
    data.put("button_url", button_url);
    data.put("button", button);
    data.put("final_line", final_line);
    StringBuilder output = new StringBuilder();
    TemplateMultiLineEmailTemplateHtml.TEMPLATE.render(new Settings(true), data, output);
    return output.toString();
  }
}
