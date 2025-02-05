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
package ape.runtime.sys;

import ape.runtime.json.JsonStreamWriter;
import ape.runtime.natives.NtMessageBase;

public class AuthResponse {
  public String hash;
  public String agent;
  public String channel;
  public String success;

  public AuthResponse() {
    this.hash = null;
    this.agent = null;
    this.channel = null;
    this.success = null;
  }

  public AuthResponse hash(String hash) {
    this.hash = hash;
    return this;
  }

  public AuthResponse agent(String agent) {
    this.agent = agent;
    return this;
  }

  public AuthResponse channel(String channel) {
    this.channel = channel;
    return this;
  }

  public AuthResponse success(NtMessageBase message) {
    JsonStreamWriter writer = new JsonStreamWriter();
    message.__writeOut(writer);
    this.success = writer.toString();
    return this;
  }
}
