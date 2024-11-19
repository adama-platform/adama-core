/*
* Adama Platform and Language
* Copyright (C) 2021 - 2025 by Adama Platform Engineering, LLC
* 
* This program is free software for non-commercial purposes: 
* you can redistribute it and/or modify it under the terms of the 
* GNU Affero General Public License as published by the Free Software Foundation,
* either version 3 of the License, or (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
* 
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package ape.web.io;

import com.fasterxml.jackson.databind.node.ObjectNode;
import ape.common.ErrorCodeException;
import ape.common.metrics.RequestResponseMonitor;

public class SimpleMetricsProxyResponder implements JsonResponder {
  private final RequestResponseMonitor.RequestResponseMonitorInstance metrics;
  private final JsonResponder responder;
  private final ObjectNode itemToLog;
  private final JsonLogger logger;
  private final long started;

  public SimpleMetricsProxyResponder(RequestResponseMonitor.RequestResponseMonitorInstance metrics, JsonResponder responder, ObjectNode itemToLog, JsonLogger logger, long started) {
    this.metrics = metrics;
    this.responder = responder;
    this.itemToLog = itemToLog;
    this.logger = logger;
    this.started = started;
  }

  @Override
  public void stream(String json) {
    metrics.extra();
    responder.stream(json);
  }

  @Override
  public void finish(String json) {
    metrics.success();
    responder.finish(json);
    itemToLog.put("success", true);
    itemToLog.put("latency", System.currentTimeMillis() - started);
    logger.log(itemToLog);
  }

  @Override
  public void error(ErrorCodeException ex) {
    metrics.failure(ex.code);
    responder.error(ex);
    itemToLog.put("success", false);
    itemToLog.put("latency", System.currentTimeMillis() - started);
    itemToLog.put("failure-code", ex.code);
    logger.log(itemToLog);
  }
}
