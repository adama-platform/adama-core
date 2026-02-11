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
package ape.web.service;

import ape.common.ConfigObject;
import ape.common.TimeSource;
import ape.common.rate.TokenRateLimiter;

import java.io.File;
import java.util.Locale;
import java.util.TreeSet;

/**
 * Configuration container for HTTP/WebSocket server settings.
 * Includes port bindings, content limits, timeouts, thread pool sizes,
 * domain routing rules, connection pool parameters, and cache directories.
 * Loaded from ConfigObject with sensible defaults for all values.
 */
public class WebConfig {
  /** Controls when the MCP (Model Context Protocol) endpoint is available. */
  public enum MCPMode {
    /** MCP is completely disabled */
    off,
    /** MCP is only available in devbox mode (localhost / 127.0.0.1) -- the default */
    devbox,
    /** MCP is available to all connections */
    live
  }
  public final String healthCheckPath;
  public final String deepHealthCheckPath;
  public final int maxContentLengthSize;
  public final int maxWebSocketFrameSize;
  public final int port;
  public final int redirectPort;
  public final int timeoutWebsocketHandshake;
  public final int heartbeatTimeMilliseconds;
  public final int idleReadSeconds;
  public final int bossThreads;
  public final int workerThreads;
  public final TreeSet<String> specialDomains;
  public final String regionalDomain;
  public final String[] globalDomains;
  public final int sharedConnectionPoolMaxLifetimeMilliseconds;
  public final int sharedConnectionPoolMaxUsageCount;
  public final int sharedConnectionPoolMaxPoolSize;
  public final File cacheRoot;
  public final String adamaJarDomain;
  public final int minDomainsToHoldTo;
  public final int maxDomainsToHoldTo;
  public final int maxDomainAge;
  public final boolean beta;
  public final File transformRoot;
  public final int maxTransformDimension;
  public final long maxSourcePixels;
  public final int maxTransformInstructionLength;
  public final int transformTimeoutMs;
  public final int maxTransformInflight;
  public final TokenRateLimiter websocketRateLimiter;
  public final int websocketRateLimitMaxAttempts;
  public final int websocketRateLimitDelay;
  public final int websocketRateLimitJitter;
  public final TokenRateLimiter onceRateLimiter;
  public final int onceRateLimitMaxAttempts;
  public final int onceRateLimitDelay;
  public final int onceRateLimitJitter;
  public final int messageRateLimitPerSecond;
  public final int messageRateLimitMaxErrors;
  public final boolean useXForwardedFor;
  public final TreeSet<String> headerBlacklist;
  public final MCPMode mcpMode;

  public WebConfig(ConfigObject config) {
    // HTTP properties
    this.port = config.intOf("http-port", 8080);
    this.redirectPort = config.intOf("http-redirect-port", 8085);
    this.maxContentLengthSize = config.intOf("http-max-content-length-size", 12582912);
    this.healthCheckPath = config.strOf("http-health-check-path", "/~health_check_lb");
    this.deepHealthCheckPath = config.strOf("http-deep-health-check-path", "/~deep_health_check_status_page");
    // WebSocket properties
    this.timeoutWebsocketHandshake = config.intOf("websocket-handshake-timeout-ms", 2500);
    this.idleReadSeconds = config.intOf("http-read-idle-sec", 60);
    this.maxWebSocketFrameSize = config.intOf("websocket-max-frame-size", 4 * 1024 * 1024);
    this.heartbeatTimeMilliseconds = config.intOf("websocket-heart-beat-ms", 1000);
    this.bossThreads = config.intOf("http-boss-threads", 2);
    this.workerThreads = config.intOf("http-worker-threads", 16);
    this.regionalDomain = config.strOf("regional-domain", "adama-platform.com");
    this.adamaJarDomain = config.strOf("adama-jar-domain", ".adama-platform.com");
    this.globalDomains = config.stringsOf("global-domains", new String[] { "adama.games" });
    this.specialDomains = new TreeSet<>();
    this.beta = config.boolOf("beta", false);
    for (String sd : config.stringsOf("special-domains", new String[] { "www.adama-platform.com", "ide.adama-platform.com", "book.adama-platform.com" })) {
      specialDomains.add(sd);
    }
    this.sharedConnectionPoolMaxLifetimeMilliseconds = config.intOf("shared-connection-max-lifetime-ms", 10000);
    this.sharedConnectionPoolMaxUsageCount = config.intOf("shared-connection-max-usage-count", 50);
    this.sharedConnectionPoolMaxPoolSize = config.intOf("shared-connection-max-pool-size", 50);
    this.cacheRoot = new File(config.strOf("cache-root", "cache"));
    this.transformRoot = new File(config.strOf("transform-root", "transform-cache"));
    // Domain Cache
    this.minDomainsToHoldTo = config.intOf("cert-cache-min-domains", 64);
    this.maxDomainsToHoldTo = config.intOf("cert-cache-max-domains", 2048);
    this.maxDomainAge = config.intOf("cert-cache-max-age", 5 * 60 * 1000);
    this.maxTransformDimension = config.intOf("max-transform-dimension", 2048);
    this.maxSourcePixels = (long) config.intOf("max-source-pixels", 25_000_000);
    this.maxTransformInstructionLength = config.intOf("max-transform-instruction-length", 256);
    this.transformTimeoutMs = config.intOf("transform-timeout-ms", 30000);
    this.maxTransformInflight = config.intOf("max-transform-inflight", 64);
    // WebSocket rate limit (stricter - expensive connections)
    ConfigObject wsRateConfig = config.child("websocket-rate-limit");
    this.websocketRateLimiter = new TokenRateLimiter(
        wsRateConfig.intOf("max-tokens", 200),
        wsRateConfig.intOf("window-ms", 60000),
        wsRateConfig.intOf("max-grant", 10),
        wsRateConfig.intOf("minimum-wait", 100),
        TimeSource.REAL_TIME);
    this.websocketRateLimitMaxAttempts = wsRateConfig.intOf("max-attempts", 5);
    this.websocketRateLimitDelay = wsRateConfig.intOf("delay", 25);
    this.websocketRateLimitJitter = wsRateConfig.intOf("jitter", 10);
    // Once rate limit (more generous - lighter operations)
    ConfigObject onceRateConfig = config.child("once-rate-limit");
    this.onceRateLimiter = new TokenRateLimiter(
        onceRateConfig.intOf("max-tokens", 500),
        onceRateConfig.intOf("window-ms", 60000),
        onceRateConfig.intOf("max-grant", 20),
        onceRateConfig.intOf("minimum-wait", 50),
        TimeSource.REAL_TIME);
    this.onceRateLimitMaxAttempts = onceRateConfig.intOf("max-attempts", 5);
    this.onceRateLimitDelay = onceRateConfig.intOf("delay", 25);
    this.onceRateLimitJitter = onceRateConfig.intOf("jitter", 10);
    // Per-connection WebSocket message rate limit (synchronous)
    ConfigObject msgRateConfig = config.child("websocket-message-rate-limit");
    this.messageRateLimitPerSecond = msgRateConfig.intOf("per-second", 1000);
    this.messageRateLimitMaxErrors = msgRateConfig.intOf("max-errors", 10);
    // X-Forwarded-For: only enable behind a trusted reverse proxy / load balancer
    this.useXForwardedFor = config.boolOf("use-x-forwarded-for", false);
    // Headers that documents are not allowed to override in HTTP responses
    this.headerBlacklist = new TreeSet<>();
    for (String h : config.stringsOf("header-blacklist", new String[] {
        "set-cookie", "x-content-type-options", "strict-transport-security",
        "referrer-policy", "x-frame-options", "content-security-policy",
        "access-control-allow-origin", "access-control-allow-methods",
        "access-control-allow-credentials", "access-control-allow-headers",
        "transfer-encoding", "content-length", "content-encoding",
        "connection", "keep-alive", "host",
        "location", "refresh", "content-disposition"
    })) {
      headerBlacklist.add(h.toLowerCase(Locale.ROOT));
    }
    // MCP mode: off, devbox (default), or live
    String mcpModeStr = config.strOf("mcp-mode", "devbox");
    MCPMode parsedMode;
    try {
      parsedMode = MCPMode.valueOf(mcpModeStr);
    } catch (IllegalArgumentException ex) {
      parsedMode = MCPMode.devbox;
    }
    this.mcpMode = parsedMode;
  }

  public void validateForServerUse() throws Exception {
    if (!cacheRoot.exists()) {
      cacheRoot.mkdir();
    }
    if (!transformRoot.exists()) {
      transformRoot.mkdir();
    }

    if (cacheRoot.exists() && !cacheRoot.isDirectory()) {
      throw new Exception("Cache root is not a directory");
    }
    if (transformRoot.exists() && !transformRoot.isDirectory()) {
      throw new Exception("Transform root is not a directory");
    }
  }
}
