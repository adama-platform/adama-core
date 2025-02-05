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
package ape.translator.codegen;

import ape.translator.env.Environment;
import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.definitions.DefineClientService;
import ape.translator.tree.definitions.DefineService;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class CodeGenServices {
  public static void writeServices(final StringBuilderWithTabs sb, final Environment environment) {
    sb.append("public static HashMap<String, HashMap<String, Object>> __services() {").tabUp().writeNewline();
    sb.append("HashMap<String, HashMap<String, Object>> __map = new HashMap<>();").writeNewline();
    for (Map.Entry<String, DefineService> serviceEntry : environment.document.services.entrySet()) {
      sb.append("HashMap<String, Object> ").append(serviceEntry.getKey()).append(" = new HashMap<>();").writeNewline();
      for (DefineService.ServiceAspect aspect : serviceEntry.getValue().aspects) {
        sb.append(serviceEntry.getKey()).append(".put(\"").append(aspect.name.text).append("\", ");
        aspect.expression.writeJava(sb, environment);
        sb.append(");").writeNewline();
      }
      sb.append("__map.put(\"").append(serviceEntry.getKey()).append("\",").append(serviceEntry.getKey()).append(");").writeNewline();
    }
    sb.append("return __map;").tabDown().writeNewline();
    sb.append("}").writeNewline();

    if (environment.document.clientServices.size() > 0) {
      int countdown = environment.document.clientServices.size();
      sb.append("public static void __create_generic_clients(ServiceRegistry __registry, HeaderDecryptor __decryptor) throws Exception {").tabUp().writeNewline();
      AtomicInteger uniqueId = new AtomicInteger(0);
      for (Map.Entry<String, DefineClientService> genericClient : environment.document.clientServices.entrySet()) {
        sb.append("GenericClient ").append(genericClient.getKey()).append(" = __registry.makeGenericClient();");
        sb.writeNewline();
        genericClient.getValue().writeJavaScriptDefn(sb, uniqueId, environment.state.runtime.version);
        sb.append("__registry.add(\"").append(genericClient.getKey()).append("\", ").append(genericClient.getKey()).append(");").writeNewline();
        countdown--;
        if (countdown == 0) {
          sb.tabDown();
        }
        sb.writeNewline();
      }
      sb.append("}").writeNewline();
    } else {
      sb.append("public static void __create_generic_clients(ServiceRegistry __registry, HeaderDecryptor decryptor) throws Exception {}").writeNewline();
    }

    if (environment.document.services.size() + environment.document.clientServices.size() == 0) {
      sb.append("@Override").writeNewline();
      sb.append("public void __link(ServiceRegistry __registry) {}").writeNewline();
      sb.append("@Override").writeNewline();
      sb.append("public Service __findService(String __name) { return null; }").writeNewline();
    } else {
      for (Map.Entry<String, DefineService> serviceEntry : environment.document.services.entrySet()) {
        sb.append("protected Service ").append(serviceEntry.getKey()).append(";").writeNewline();
      }
      for (Map.Entry<String, DefineClientService> serviceEntry : environment.document.clientServices.entrySet()) {
        sb.append("protected GenericClient ").append(serviceEntry.getKey()).append(";").writeNewline();
      }
      {
        int countdown = environment.document.services.size() + environment.document.clientServices.size();
        sb.append("@Override").writeNewline();
        sb.append("public void __link(ServiceRegistry __registry) {").tabUp().writeNewline();
        for (Map.Entry<String, DefineService> serviceEntry : environment.document.services.entrySet()) {
          sb.append(serviceEntry.getKey()).append(" = __registry.find(\"").append(serviceEntry.getKey()).append("\");");
          countdown--;
          if (countdown == 0) {
            sb.tabDown();
          }
          sb.writeNewline();
        }
        for (Map.Entry<String, DefineClientService> serviceEntry : environment.document.clientServices.entrySet()) {
          sb.append(serviceEntry.getKey()).append(" = ").append("__registry.getClient(\"").append(serviceEntry.getKey()).append("\");").writeNewline();
          countdown--;
          if (countdown == 0) {
            sb.tabDown();
          }
          sb.writeNewline();
        }
        sb.append("}").writeNewline();
      }
      {
        int countdown = environment.document.services.size();
        if (countdown == 0) {
          sb.append("@Override").writeNewline();
          sb.append("public Service __findService(String __name) { return null; }").writeNewline();
        } else {
          sb.append("@Override").writeNewline();
          sb.append("public Service __findService(String __name) {").tabUp().writeNewline();
          sb.append("switch (__name) {").tabUp().writeNewline();
          for (Map.Entry<String, DefineService> serviceEntry : environment.document.services.entrySet()) {
            sb.append("case \"").append(serviceEntry.getKey()).append("\":").tabUp().writeNewline();
            sb.append("return ").append(serviceEntry.getKey()).append(";").tabDown();
            countdown--;
            if (countdown == 0) {
              sb.tabDown();
            }
            sb.writeNewline();
          }
          sb.append("}").writeNewline();
          sb.append("return null;").tabDown().writeNewline();
          sb.append("}").writeNewline();
        }
      }
    }
  }
}
