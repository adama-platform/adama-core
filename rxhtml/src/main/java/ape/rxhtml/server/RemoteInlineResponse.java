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
package ape.rxhtml.server;

/** the response from a remote inline */
public class RemoteInlineResponse {
  public final int status;
  public final String body;
  public final String title;
  public final String redirect;
  public final String identity;
  public final String contentType;

  private RemoteInlineResponse(int status, String body, String title, String redirect, String identity, String contentType) {
    this.status = status;
    this.body = body;
    this.title = title;
    this.redirect = redirect;
    this.identity = identity;
    this.contentType = contentType;
  }

  public static RemoteInlineResponse body(int status, String body, String title, String identity) {
    return new RemoteInlineResponse(status, body, title, null, identity, null);
  }

  public static RemoteInlineResponse json(int status, String body, String identity) {
    return new RemoteInlineResponse(status, body, null, null, identity, "application/json");
  }

  public static RemoteInlineResponse xml(int status, String body, String identity) {
    return new RemoteInlineResponse(status, body, null, null, identity, "text/xml");
  }

  public static RemoteInlineResponse redirect(String redirect, String identity) {
    return new RemoteInlineResponse(302, null, null, redirect, identity, null);
  }
}
