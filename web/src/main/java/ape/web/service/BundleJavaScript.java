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
package ape.web.service;

import ape.common.DefaultCopyright;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;

public class BundleJavaScript {
  public static String bundle(String fileJs, String fileWorker) throws Exception {
    String strJs = Files.readString(new File(fileJs).toPath());
    String strWorker = Files.readString(new File(fileWorker).toPath());
    StringBuilder sb = new StringBuilder();
    sb.append(DefaultCopyright.COPYRIGHT_FILE_PREFIX);
    sb.append("package ape.web.service;\n\n");
    sb.append("import java.nio.charset.StandardCharsets;\n");
    sb.append("import java.util.Base64;\n");
    sb.append("import java.util.regex.Matcher;\n");
    sb.append("import java.util.regex.Pattern;\n");
    sb.append("\n");
    sb.append("public class JavaScriptClient {\n");
    sb.append("  public static final byte[] ADAMA_JS_CLIENT_BYTES = ");
    appendStringInChunks(sb, "c", new String(Base64.getEncoder().encode(strJs.getBytes(StandardCharsets.UTF_8))));
    sb.append("  public static final byte[] BETA_ADAMA_JS_CLIENT_BYTES = new String(ADAMA_JS_CLIENT_BYTES, StandardCharsets.UTF_8).replaceAll(Pattern.quote(\"Adama.Production\"), Matcher.quoteReplacement(\"Adama.Beta\")).getBytes(StandardCharsets.UTF_8);\n");
    sb.append("  public static final byte[] ADAMA_WORKER_JS_CLIENT_BYTES = ");
    appendStringInChunks(sb, "w", new String(Base64.getEncoder().encode(strWorker.getBytes(StandardCharsets.UTF_8))));
    sb.append("  public static final byte[] BETA_ADAMA_WORKER_JS_CLIENT_BYTES = ADAMA_WORKER_JS_CLIENT_BYTES;\n");
    sb.append("}");
    return sb.toString();
  }

  public static void appendStringInChunks(StringBuilder sb, String suffix, String str) {
    sb.append("Base64.getDecoder().decode(make").append(suffix).append("());\n");
    sb.append("  private static String make").append(suffix).append("() {\n");
    sb.append("    StringBuilder sb = new StringBuilder();\n");
    int len = str.length();
    int at = 0;
    while (at < len) {
      int sz = Math.min(80, len - at);
      String fragment = str.substring(at, at + sz);
      sb.append("    sb.append(\"").append(fragment).append("\");\n");
      at += sz;
    }
    sb.append("    return sb.toString();\n");
    sb.append("  }\n");
  }
}
