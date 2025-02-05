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
package ape.common.dns;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ReflectiveChannelFactory;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.dns.*;
import io.netty.resolver.dns.DnsNameResolver;
import io.netty.resolver.dns.DnsNameResolverBuilder;
import io.netty.resolver.dns.DnsServerAddressStreamProviders;
import io.netty.util.concurrent.Future;
import ape.ErrorCodes;
import ape.common.Callback;
import ape.common.ErrorCodeException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class NettyDNSTxtResolver implements DNSTxtResolver {
  private final NioEventLoopGroup eventLoop;

  public NettyDNSTxtResolver() {
    this.eventLoop = new NioEventLoopGroup();
  }

  public void shutdown() {
    eventLoop.shutdownGracefully();
  }

  @Override
  public void query(String domain, Callback<String[]> callback) {
    DefaultDnsQuestion question = new DefaultDnsQuestion(domain, DnsRecordType.TXT);
    DnsNameResolver resolver = new DnsNameResolverBuilder() //
        .nameServerProvider(DnsServerAddressStreamProviders.platformDefault()) //
        .eventLoop(eventLoop.next()) //
        .channelFactory(new ReflectiveChannelFactory<>(NioDatagramChannel.class)) //
        .build();

    resolver.resolveAll(question).addListener((Future<List<DnsRecord>> future) -> {
      if (future.isDone()) {
        if (future.isSuccess()) {
          ArrayList<String> txt = new ArrayList<>();
          for (DnsRecord record : future.get()) {
            if (record.type() == DnsRecordType.TXT && record instanceof DefaultDnsRawRecord) {
              ByteBuf buf = ((DefaultDnsRawRecord) record).content();
              int sz = buf.readableBytes();
              byte[] bytes = new byte[sz];
              buf.readBytes(bytes);
              txt.add(new String(bytes, StandardCharsets.UTF_8));
            }
          }
          callback.success(txt.toArray(new String[txt.size()]));
        } else {
          callback.failure(new ErrorCodeException(ErrorCodes.DNS_RESOLVE_FAILED));
        }
      }
    });
  }
}
