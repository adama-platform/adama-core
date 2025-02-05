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
package ape.common.gossip;

/** this version of gossip depends on some magic numbers */
public class Constants {
  /**
   * if someone recommends a deletion, then how many milliseconds should my copy be behind by to
   * accept it.
   */
  public static long MILLISECONDS_FOR_DELETION_CANDIDATE = 7500;

  /** if I see a too candidate that is too old, then how old must it be to activately delete it */
  public static long MILLISECONDS_FOR_RECOMMEND_DELETION_CANDIDATE = 10000;

  /** item considered too old to be in the garbage collecting map */
  public static long MILLISECONDS_TO_SIT_IN_GARBAGE_MAP = 60000;

  /** maximum new entries to hold onto for recent map */
  public static int MAX_RECENT_ENTRIES = 100;

  /** maximum delete entries to hold onto */
  public static int MAX_DELETES = 50;

  /** maximum history to hold onto */
  public static int MAX_HISTORY = 25;
}
