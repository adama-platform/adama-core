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
package ape.rxhtml;

import ape.common.StringHelper;
import ape.common.html.InjectCoordInline;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** simple bundler to repackage a set of RxHTML files into one file */
public class Bundler {
  public static String bundle(File commonPath, List<File> files, boolean inject) throws Exception {
    StringBuilder output = new StringBuilder();
    output.append("<forest>\n");
    for (File file : files) {
      String nameToReport = file.getName();
      try {
        String commonRoot = commonPath.getAbsolutePath();
        String fullPath = file.getAbsolutePath();
        nameToReport = fullPath.substring(commonRoot.length()).replaceAll(Pattern.quote("\\"), Matcher.quoteReplacement("/"));
      } catch (Exception failedToResolveAbsolutePath) {
      }
      Document useDoc;
      if (inject) {
        useDoc = Jsoup.parse(InjectCoordInline.execute(Files.readString(file.toPath()), nameToReport));
      } else {
        useDoc = Jsoup.parse(file);
      }
      output.append(StringHelper.splitNewlineAndTabify(useDoc.getElementsByTag("forest").html().replaceAll("\r", ""), ""));
    }
    output.append("</forest>\n");
    return output.toString();
  }
}
