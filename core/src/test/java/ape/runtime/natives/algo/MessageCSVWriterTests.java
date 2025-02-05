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
package ape.runtime.natives.algo;

import ape.runtime.natives.*;
import ape.runtime.natives.*;
import org.junit.Assert;
import org.junit.Test;

import java.time.ZonedDateTime;

public class MessageCSVWriterTests {
  @Test
  public void flow() {
    MessageCSVWriter writer = new MessageCSVWriter();
    writer.write(new NtTime(1, 2));
    writer.write(new NtDateTime(ZonedDateTime.parse("2023-04-24T17:57:19.802528800-05:00[America/Chicago]")));
    writer.write(new NtDate(1,2, 1000));
    writer.write(new NtTimeSpan(42));
    writer.write(new NtPrincipal("agent", "auth"));
    writer.write(new NtComplex(3.14, 2.71));
    Assert.assertEquals("01:02,2023-04-24T17:57:19.802528800-05:00[America/Chicago],1-02-1000,42.0,agent@auth,3.14 2.71i", writer.toString());
  }
}
