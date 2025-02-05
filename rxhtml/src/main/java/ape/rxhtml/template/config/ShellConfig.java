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
package ape.rxhtml.template.config;

import ape.common.Platform;
import ape.rxhtml.template.Base;
import org.jsoup.nodes.Element;

/** configuration for the shell */
public class ShellConfig {
  public final Feedback feedback;
  public final boolean useLocalAdamaJavascript;
  public final String version;
  public final String environment;
  public final int cacheMaxAgeSeconds;

  private ShellConfig(Feedback feedback, final String version, String environment, boolean useLocalAdamaJavascript, int cacheMaxAgeSeconds) {
    this.feedback = feedback;
    this.version = version;
    this.useLocalAdamaJavascript = useLocalAdamaJavascript;
    this.environment = environment;
    this.cacheMaxAgeSeconds = cacheMaxAgeSeconds;
  }

  public static Builder start() {
    return new Builder();
  }

  public static class Builder {
    public Feedback feedback;
    public boolean useLocalAdamaJavascript;
    public String version;
    public String environment;
    public int cacheMaxAgeSeconds;

    public Builder() {
      this.feedback = Feedback.NoOp;
      this.useLocalAdamaJavascript = false;
      this.version =  Platform.JS_VERSION;
      this.environment = "prod";
      this.cacheMaxAgeSeconds = 60;
    }

    public Builder withFeedback(Feedback feedback) {
      this.feedback = feedback;
      return this;
    }

    public Builder withVersion(String version) {
      this.version = version;
      return this;
    }

    public Builder withEnvironment(String environment) {
      this.environment = environment;
      return this;
    }

    public Builder withUseLocalAdamaJavascript(boolean useLocalAdamaJavascript) {
      this.useLocalAdamaJavascript = useLocalAdamaJavascript;
      return this;
    }

    public Builder withCacheMaxAgeSeconds(int cacheMaxAgeSeconds) {
      this.cacheMaxAgeSeconds = cacheMaxAgeSeconds;
      return this;
    }

    public ShellConfig end() {
      return new ShellConfig(feedback, version, environment, useLocalAdamaJavascript, cacheMaxAgeSeconds);
    }
  }

  public boolean includeInShell(Element element) {
    return Base.checkEnv(element, environment);
  }
}
