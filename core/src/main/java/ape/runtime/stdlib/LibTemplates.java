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
