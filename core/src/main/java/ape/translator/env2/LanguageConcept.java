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
package ape.translator.env2;

/** a language concept is a reference to fetch documentation. This is a way for the error system to provide a set of concepts down such that an AI can get a fuller grasp concepts based on errors. */
public enum LanguageConcept {
  Looping("This is a control structure for looping and doing multiple things"),
  ForLoop(Looping, "A generic C style for loop with initialization, condition, and progress"),
  ForEachLoop(Looping, "A specialized Adama foreach that uses type inference for the variable name and then iterates a list or array"),
  ;

  private final LanguageConcept parent;
  public final String help;

  public LanguageConcept parent() {
    return parent;
  }

  private LanguageConcept(final LanguageConcept parent, String help) {
    this.parent = parent;
    this.help = help;
  }

  LanguageConcept(String help) {
    this.parent = null;
    this.help = help;
  }
}
