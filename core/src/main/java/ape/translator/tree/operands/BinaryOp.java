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
package ape.translator.tree.operands;

/** defines a binary operation (A op B) */
public enum BinaryOp {
  Add("+", false), //
  Divide("/", false), //
  Equal("==", false), //
  GreaterThan(">", false), //
  GreaterThanOrEqual(">=", false), //
  LessThan("<", false), //
  LessThanOrEqual("<=", false), //
  Search("=?", false), //
  LogicalXor("^^", false), //
  LogicalAnd("&&", false), //
  LogicalOr("||", false), //
  Mod("%", false), //
  Multiply("*", false), //
  NotEqual("!=", false), //
  Subtract("-", false), //
  AssignmentAdd("+=", true), //
  AssignmentSubtract("-=", true), //
  AssignmentMultiply("*=", true), //
  Inside("inside", false), //
  NotInside("outside", false), //
  ; //

  public final String javaOp;
  public final boolean leftAssignment;

  BinaryOp(final String js, boolean leftAssignment) {
    this.javaOp = js;
    this.leftAssignment = leftAssignment;
  }

  public static BinaryOp fromText(final String txt) {
    for (final BinaryOp op : BinaryOp.values()) {
      if (op.javaOp.equals(txt)) {
        return op;
      }
    }
    return null;
  }
}
