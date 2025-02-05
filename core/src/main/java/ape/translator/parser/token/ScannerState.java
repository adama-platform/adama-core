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

/** The scanner use a simple state machine, and this is the state of the scanner */
enum ScannerState {
  /** scanner is scanning for an identifier */
  ScanIdentifer,
  /** scanner is scanning for a numeric literal */
  ScanNumberLiteral,
  /** scanner is scanning for a double quote to end the string */
  ScanStringLiteral,
  /** scanner is scanning with a double quoted string, and is currently escaping a value */
  ScanStringLiteralEscape,
  /**
   * scanner is scanning with a double quoted string and is currently reading four unicode values
   */
  ScanStringLiteralUnicodeHexEscape,
  /** scanner is building a bundle of symbols */
  ScanSymbol,
  /** scanner is scanning a comment until the pairing, like --> */
  ScanUntilEndOfComment,
  /** scanner is scanning a comment until end of line */
  ScanUntilEndOfLine,
  /** scanner is scanning white space */
  ScanWhitespace,
  /** A template */
  ScanTemplate,
  /** scanner is in an unknown state and requires input to decide next step */
  Unknown,
}
