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

import ape.common.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import ape.ErrorCodes;
import ape.common.cache.AsyncSharedLRUCache;
import ape.common.cache.Measurable;
import ape.common.cache.SyncCacheLRU;
import ape.runtime.sys.domains.Domain;
import ape.runtime.sys.domains.DomainFinder;
import ape.web.contracts.CertificateFinder;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Having a certificate for a domain means the domain is owned by the Adama server.
 * The certificate proves domain ownership because only the domain owner can provision
 * a valid TLS certificate for it. This is used for CORS origin validation and SNI routing.
 */
public class CertificateBoot {
  private static final ExceptionLogger EXLOGGER = ExceptionLogger.FOR(CertificateBoot.class);

  public static class MeasuredSslContext implements Measurable {
    public final SslContext context;

    public MeasuredSslContext(SslContext context) {
      this.context = context;
    }

    @Override
    public long measure() {
      return 1;
    }
  }

  public static CertificateFinder make(AtomicBoolean alive, WebConfig webConfig, DomainFinder df, SimpleExecutor executor) {
    SyncCacheLRU<String, MeasuredSslContext> realCache = new SyncCacheLRU<>(TimeSource.REAL_TIME, webConfig.minDomainsToHoldTo, webConfig.maxDomainsToHoldTo, webConfig.maxDomainsToHoldTo * 2, webConfig.maxDomainAge, (domain, context) -> {});
    AsyncSharedLRUCache<String, MeasuredSslContext> cache = new AsyncSharedLRUCache<>(executor, realCache, (domain, callback) -> {
      df.find(domain, new Callback<Domain>() {
        @Override
        public void success(Domain lookup) {
          try {
            if (lookup != null && lookup.certificate != null) {
              ObjectNode certificate = Json.parseJsonObject(lookup.certificate);
              ByteArrayInputStream keyInput = new ByteArrayInputStream(certificate.get("key").textValue().getBytes(StandardCharsets.UTF_8));
              ByteArrayInputStream certInput = new ByteArrayInputStream(certificate.get("cert").textValue().getBytes(StandardCharsets.UTF_8));
              SslContext contextToUse = SslContextBuilder.forServer(certInput, keyInput)
                  .applicationProtocolConfig(new ApplicationProtocolConfig(
                      ApplicationProtocolConfig.Protocol.ALPN,
                      ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                      ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                      ApplicationProtocolNames.HTTP_2,
                      ApplicationProtocolNames.HTTP_1_1))
                  .build();
              callback.success(new MeasuredSslContext(contextToUse));
            } else {
              callback.failure(new ErrorCodeException(ErrorCodes.DOMAIN_TRANSLATE_FAILURE));
            }
          } catch (Exception ex) {
            callback.failure(ErrorCodeException.detectOrWrap(ErrorCodes.DOMAIN_LOOKUP_FAILURE, ex, EXLOGGER));
          }
        }

        @Override
        public void failure(ErrorCodeException ex) {
          callback.failure(ex);
        }
      });
    });
    cache.startSweeping(alive, webConfig.maxDomainAge / 4, webConfig.maxDomainAge / 2);
    return (rawDomain, callback) -> {
      String _domainToLookup = rawDomain;
      { // hyper fast optimistic path
        if (_domainToLookup == null) { // no SNI provided -> use default
          callback.success(null);
          return;
        }
        if (!webConfig.specialDomains.contains(_domainToLookup)) {
          if (_domainToLookup.endsWith("." + webConfig.regionalDomain)) { // the regional domain -> use default
            callback.success(null);
            return;
          }
          for (String globalDomain : webConfig.globalDomains) {
            if (_domainToLookup.endsWith("." + globalDomain)) {
              _domainToLookup = "wildcard." + globalDomain;
              break;
            }
          }
        }
      }

      cache.get(_domainToLookup, new Callback<MeasuredSslContext>() {
        @Override
        public void success(MeasuredSslContext value) {
          callback.success(value.context);
        }

        @Override
        public void failure(ErrorCodeException ex) {
          callback.failure(ex);
        }
      });
    };
  }
}
