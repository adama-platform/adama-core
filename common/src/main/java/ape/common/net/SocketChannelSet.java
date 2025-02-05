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
package ape.common.net;

import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.SocketChannel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

/** we monitor the inflight connections so we can terminate them all */
public class SocketChannelSet {
  private final HashMap<Integer, SocketChannel> map;
  private final Random rng;

  public SocketChannelSet() {
    this.map = new HashMap<>();
    this.rng = new Random();
  }

  public synchronized int add(SocketChannel channel) {
    while (true) {
      int id = rng.nextInt();
      if (!map.containsKey(id)) {
        map.put(id, channel);
        return id;
      }
    }
  }

  public synchronized void remove(int key) {
    map.remove(key);
  }

  public void kill() {
    ArrayList<ChannelFuture> futures = new ArrayList<>();
    for (SocketChannel channel : killUnderLock()) {
      futures.add(channel.close());
    }
    for (ChannelFuture future : futures) {
      future.syncUninterruptibly();
    }
  }

  private synchronized Collection<SocketChannel> killUnderLock() {
    ArrayList<SocketChannel> list = new ArrayList<>(map.values());
    map.clear();
    return list;
  }
}
