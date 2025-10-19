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
package ape.api;

import ape.common.ErrorCodeException;
import ape.web.io.JsonLogger;
import ape.web.io.JsonResponder;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class LoggedProxyResponder implements JsonResponder {
  private final JsonResponder responder;
  private final ObjectNode itemToLog;
  private final JsonLogger logger;

  public LoggedProxyResponder(JsonResponder responder, ObjectNode itemToLog, JsonLogger logger) {
    this.responder = responder;
    this.itemToLog = itemToLog;
    this.logger = logger;
  }

  @Override
  public void stream(String json) {
    responder.stream(json);
  }

  @Override
  public void finish(String json) {
    responder.finish(json);
    itemToLog.put("success", true);
    logger.log(itemToLog);
  }

  @Override
  public void error(ErrorCodeException ex) {
    responder.error(ex);
    itemToLog.put("success", false);
    itemToLog.put("failure-code", ex.code);
    logger.log(itemToLog);
  }
}
