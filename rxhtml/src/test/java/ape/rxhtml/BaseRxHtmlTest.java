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

import com.fasterxml.jackson.databind.node.ObjectNode;
import ape.common.Json;
import ape.common.StringHelper;
import ape.rxhtml.template.config.Feedback;
import ape.rxhtml.template.config.ShellConfig;
import ape.support.GenerateTemplateTests;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public abstract class BaseRxHtmlTest {
  private RxHtmlBundle cachedResult = null;
  private StringBuilder issuesLive;

  public RxHtmlBundle result() {
    if (cachedResult == null) {
      File typesFolder = new File("test_templates/types");
      issuesLive = new StringBuilder();
      Feedback feedback = (element, warning) -> issuesLive.append("WARNING:").append(warning).append("\n");
      Document useDoc = Jsoup.parse(source());
      String partial = StringHelper.splitNewlineAndTabify(useDoc.getElementsByTag("forest").html().replaceAll("\r", ""), "");
      cachedResult = RxHtmlTool.convertStringToTemplateForest(partial, typesFolder, ShellConfig.start().withVersion("GENMODE").withEnvironment("test").withFeedback(feedback).withUseLocalAdamaJavascript(dev()).end());
    }
    return cachedResult;
  }

  @Test
  public void stable_code() throws Exception {
    RxHtmlBundle result = result();
    String live = GenerateTemplateTests.fixTestGold(result.toString());
    Assert.assertEquals(gold().replaceAll("\r", ""), live.trim().replaceAll("\r", ""));
  }

  @Test
  public void stable_issues() {
    RxHtmlBundle result = result();
    Assert.assertEquals(issues().trim(), issuesLive.toString().trim());
  }

  @Test
  public void stable_schema() {
    RxHtmlBundle result = result();
    ObjectNode expected = Json.parseJsonObject(schema());
    ObjectNode computed = Json.parseJsonObject(result.diagnostics.viewSchema.toPrettyString());
    Assert.assertEquals(expected, computed);
  }

  /** test any developer mode settings */
  public abstract boolean dev();

  /** the source code of the template */
  public abstract String source();

  /** the output the test is supposed to generate */
  public abstract String gold();

  /** The issues the test is supposed to have */
  public abstract String issues();

  /** get the schema */
  public abstract String schema();
}
