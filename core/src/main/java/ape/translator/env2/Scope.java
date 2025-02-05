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

import ape.translator.tree.definitions.DocumentEvent;
import ape.translator.tree.types.natives.functions.FunctionPaint;

/** Scope is a place for variables to be define */
public class Scope {
  private final Scope parent;

  private Scope(Scope parent) {
    this.parent = parent;
  }

  public static Scope makeRootDocument() {
    return new Scope(null);
  }

  public Scope makeWebHandler(String verb) {
    return new Scope(this);
  }

  public Scope makeStateMachineTransition() {
    return new Scope(this);
  }

  public Scope makeDocumentEvent(DocumentEvent event) {
    return new Scope(this);
  }

  public Scope makeAuthorize() {
    return new Scope(this);
  }

  public Scope makePassword() {
    return new Scope(this);
  }

  public Scope makeTest() {
    return new Scope(this);
  }

  public Scope makeProcedure(FunctionPaint fp) {
    return new Scope(this);
  }

  public Scope makeMethod(FunctionPaint fp) {
    return new Scope(this);
  }

  public Scope makeFunction(FunctionPaint fp) {
    return new Scope(this);
  }

  public Scope makePolicy() {
    return new Scope(this);
  }

  public Scope makeFilter() {
    return new Scope(this);
  }

  public Scope makeBubble() {
    return new Scope(this);
  }

  public Scope makeLambdaScope() {
    return new Scope(this);
  }

  public Scope makeReplication() {
    return new Scope(this);
  }

  public Scope makeStaticScope() {
    return new Scope(this);
  }

  public Scope makeLinkScope() {
    return new Scope(this);
  }

  public Scope makeIngestionHandler() {
    return new Scope(this);
  }

  public Scope makeServiceScope() {
    return new Scope(this);
  }

  public Scope makeConstructor() {
    return new Scope(this);
  }

  public Scope makeDispatch() {
    return new Scope(this);
  }

  public Scope makeConstant() {
    return new Scope(this);
  }

  public Scope makeMessageHandler() {
    return new Scope(this);
  }

  public Scope makeBranchScope() {
    return new Scope(this);
  }

  public Scope makeRecordType() {
    return new Scope(this);
  }

  public Scope makeMessageType() {
    return new Scope(this);
  }

  public Scope makeMessageEvent() {
    return new Scope(this);
  }

  public Scope makeCronTask() {
    return new Scope(this);
  }
}
