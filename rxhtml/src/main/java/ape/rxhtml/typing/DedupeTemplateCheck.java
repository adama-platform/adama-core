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

import java.util.Objects;

/** a simple tuple to combine a template name that has been checked against a structure */
public class DedupeTemplateCheck implements Comparable<DedupeTemplateCheck> {
  public final String templateName;
  public final String structureName;
  public final String privacySet;

  public DedupeTemplateCheck(String templateName, String structureName, String privacySet) {
    this.templateName = templateName;
    this.structureName = structureName;
    this.privacySet = privacySet;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DedupeTemplateCheck that = (DedupeTemplateCheck) o;
    return Objects.equals(templateName, that.templateName) && Objects.equals(structureName, that.structureName) && Objects.equals(privacySet, that.privacySet);
  }

  @Override
  public int hashCode() {
    return Objects.hash(templateName, structureName, privacySet);
  }

  @Override
  public int compareTo(DedupeTemplateCheck o) {
    int delta = templateName.compareTo(o.templateName);
    if (delta == 0) {
      delta = structureName.compareTo(o.structureName);
    }
    if (delta == 0) {
      delta = privacySet.compareTo(o.privacySet);
    }
    return delta;
  }
}
