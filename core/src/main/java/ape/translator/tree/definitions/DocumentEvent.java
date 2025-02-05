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
package ape.translator.tree.definitions;

/** an event that happens outside of message flow */
public enum DocumentEvent {
  AskCreation(true, true, "__onCanCreate", "boolean"), //
  AskInvention(true, true, "__onCanInvent", "boolean"), //
  AskSendWhileDisconnected(true, true, "__onCanSendWhileDisconnected", "boolean"), //

  Load(false, false, "__onLoad", "void"), //

  AskAssetAttachment(false, true, "__onCanAssetAttached", "boolean"), //
  AssetAttachment(false, true, "__onAssetAttached", "void", "NtAsset"), //
  Delete(false, true, "__delete", "boolean"), //
  ClientConnected(false, true, "__onConnected", "boolean"), //
  ClientDisconnected(false, true, "__onDisconnected", "void"); //

  public final boolean hasPrincipal;
  public final boolean isStaticPolicy;
  public final String prefix;
  public final String returnType;
  public final boolean hasParameter;
  public final String parameterType;

  private DocumentEvent(boolean isStaticPolicy, boolean hasPrincipal, String prefix, String returnType) {
    this.isStaticPolicy = isStaticPolicy;
    this.hasPrincipal = hasPrincipal;
    this.prefix = prefix;
    this.returnType = returnType;
    this.hasParameter = false;
    this.parameterType = null;
  }

  private DocumentEvent(boolean isStaticPolicy, boolean hasPrincipal, String prefix, String returnType, String parameterType) {
    this.isStaticPolicy = isStaticPolicy;
    this.hasPrincipal = hasPrincipal;
    this.prefix = prefix;
    this.returnType = returnType;
    this.hasParameter = parameterType != null;
    this.parameterType = parameterType;
  }
}
