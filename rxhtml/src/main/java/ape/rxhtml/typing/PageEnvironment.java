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
package ape.rxhtml.typing;

import org.jsoup.nodes.Element;

import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

public class PageEnvironment {
  private final HashMap<String, DataScope> connections;
  public final PrivacyFilter privacy;
  public final DataScope scope;
  private final Element fragmentProvider;
  private final HashMap<String, Element> templates;
  private final HashSet<String> allTemplatesUnusued;
  private final HashSet<String> templatesUsedByPage;

  public PageEnvironment(PrivacyFilter privacy, DataScope scope, Element fragmentProvider, HashMap<String, Element> templates, HashMap<String, DataScope> connections, HashSet<String> allTemplatesUnusued, HashSet<String> templatesUsedByPage) {
    this.privacy = privacy;
    this.scope = scope;
    this.fragmentProvider = fragmentProvider;
    this.templates = templates;
    this.connections = connections;
    this.allTemplatesUnusued = allTemplatesUnusued;
    this.templatesUsedByPage = templatesUsedByPage;
  }

  public void registerConnection(String name, DataScope scope) {
    connections.put(name, scope);
  }

  public PageEnvironment maybePickConnection(String name) {
    DataScope scope = connections.get(name);
    if (scope == null) {
      return null;
    }
    return withDataScope(scope);
  }

  public Element getFragmentProvider() {
    return fragmentProvider;
  }

  public Element findTemplate(String name) {
    allTemplatesUnusued.remove(name);
    Element template = templates.get(name);
    if (template != null) {
      templatesUsedByPage.add(name);
    }
    return template;
  }

  public PageEnvironment withFragmentProvider(Element fragmentProvider) {
    return new PageEnvironment(privacy, scope, fragmentProvider, templates, connections, allTemplatesUnusued, templatesUsedByPage);
  }

  public PageEnvironment withDataScope(DataScope scope) {
    return new PageEnvironment(privacy, scope, fragmentProvider, templates, connections, allTemplatesUnusued, templatesUsedByPage);
  }

  public static PageEnvironment newPage(String privacy, HashMap<String, Element> templates, HashSet<String> allTemplatesUnused, HashSet<String> templatesUsedByName) {
    return new PageEnvironment(new PrivacyFilter(privacy.split(Pattern.quote(","))), null, null, templates, new HashMap<>(), allTemplatesUnused, templatesUsedByName);
  }
}
