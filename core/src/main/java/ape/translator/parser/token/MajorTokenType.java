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
package ape.translator.parser.token;

/** defines the type of a token found within a file */
public enum MajorTokenType {
  /** a comment which be a block /* or // */
  Comment(true),
  /** identifiers: [A-Za-z_][A-Za-z_0-9]* */
  Identifier(false),
  /** special identifiers which are keywords in the language like for, while, do, if, etc... */
  Keyword(false),
  /** a state machine label identifier: '#' [A-Za-z_0-9]* */
  Label(false),
  /**
   * a 'rough' number literal of integers, hex encoded integers, floating point, scientific floating
   * point
   */
  NumberLiteral(false),
  /** a double quoted string literal */
  StringLiteral(false),
  /** a single symbol: '+', '-' */
  Symbol(false),
  /** whitespace like spaces (' '), tabs ('\t'), or newlines (\'n') */
  Whitespace(true),
  /** a string template **/
  Template(false);

  /** is the token hidden from the parse tree */
  public final boolean hidden;

  MajorTokenType(final boolean hidden) {
    this.hidden = hidden;
  }
}
