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
package ape.runtime.async;

import ape.runtime.natives.NtPrincipal;

/**
 * This represents a future which has been vended to the runtime.
 *
 * <p>A future which has been vended, and it is assigned a unique id. Given how the async element
 * works within Adama, it is vital that order of futures vended be stable.
 */
public class OutstandingFuture {
  public final String channel;
  public final int id;
  public final NtPrincipal who;
  public String json;
  private boolean claimed;
  private boolean taken;

  /**
   * @param id the unique id of the future (for client's reference)
   * @param channel the channel for the future to wait on
   * @param who the client we are waiting on
   */
  public OutstandingFuture(final int id, final String channel, final NtPrincipal who) {
    this.id = id;
    this.channel = channel;
    this.who = who;
    claimed = true; // creation is an act of claiming
    taken = false;
  }

  /** has this future been claimed and not taken */
  public boolean outstanding() {
    return claimed && !taken;
  }

  /** release the claim and free it up */
  public void reset() {
    claimed = false;
    taken = false;
  }

  /** take the future */
  public void take() {
    taken = true;
  }

  /** does this future match the given channel and person; that is, can this future pair up */
  public boolean test(final String testChannel, final NtPrincipal testClientId) {
    if (channel.equals(testChannel) && who.equals(testClientId) && !claimed) {
      claimed = true;
      return true;
    }
    return false;
  }
}
