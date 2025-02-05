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
package ape.translator.tree.definitions.web;

import ape.common.web.UriMatcher;
import ape.translator.env2.Scope;
import ape.translator.parser.Parser;
import ape.translator.parser.token.TokenEngine;
import ape.translator.tree.SymbolIndex;
import org.junit.Assert;
import org.junit.Test;

public class UriTests {
  @Test
  public void double_coverage() {
    Assert.assertTrue(Uri.isDouble("1.2"));
    Assert.assertFalse(Uri.isDouble("x"));
  }
  @Test
  public void long_coverage() {
    Assert.assertTrue(Uri.isLong("1234123431245"));
    Assert.assertFalse(Uri.isLong("x"));
  }
  @Test
  public void int_coverage() {
    Assert.assertTrue(Uri.isInteger("123"));
    Assert.assertFalse(Uri.isInteger("x"));
  }
  private UriMatcher of(String path) throws Exception {
    TokenEngine engine = new TokenEngine("test", path.codePoints().iterator());
    Parser parser = new Parser(engine, new SymbolIndex(), Scope.makeRootDocument());
    return parser.uri().matcher();
  }
  @Test
  public void matching_simple_fixed_1() throws Exception {
    Assert.assertFalse(of("/nope/yep").matches("/yep"));
    Assert.assertFalse(of("/nope/yep").matches("/nope/xyz"));
    Assert.assertFalse(of("/yep/xyz").matches("/yep"));
    Assert.assertTrue(of("/yep/xyz").matches("/yep/xyz"));
  }
  @Test
  public void matching_simple_fixed() throws Exception {
    Assert.assertFalse(of("/nope").matches("/yep"));
    Assert.assertTrue(of("/yep").matches("/yep"));
  }
  @Test
  public void matching_simple_root() throws Exception {
    Assert.assertFalse(of("/nope").matches("/"));
    Assert.assertTrue(of("/").matches("/"));
  }
  @Test
  public void matching_str() throws Exception {
    Assert.assertTrue(of("/$id:string").matches("/name"));
    Assert.assertFalse(of("/$id:string").matches("/name/blah"));
    Assert.assertTrue(of("/$id*").matches("/name/blah/blah/blah"));
  }
  @Test
  public void matching_int() throws Exception {
    Assert.assertTrue(of("/$id:int").matches("/123"));
    Assert.assertTrue(of("/$id:int/name").matches("/123/name"));
    Assert.assertFalse(of("/$id:int").matches("/xyz"));
  }
  @Test
  public void matching_long() throws Exception {
    Assert.assertTrue(of("/$id:long").matches("/123"));
    Assert.assertTrue(of("/$id:long/name").matches("/123/name"));
    Assert.assertFalse(of("/$id:long").matches("/xyz"));
  }
  @Test
  public void matching_double() throws Exception {
    Assert.assertTrue(of("/$id:double").matches("/123"));
    Assert.assertTrue(of("/$id:double/name").matches("/123/name"));
    Assert.assertFalse(of("/$id:double").matches("/xyz"));
  }
  @Test
  public void matching_bool() throws Exception {
    Assert.assertTrue(of("/$id:bool").matches("/true"));
    Assert.assertTrue(of("/$id:bool/name").matches("/false/name"));
    Assert.assertFalse(of("/$id:bool").matches("/xyz"));
  }
  @Test
  public void matching_long_path() throws Exception {
    Assert.assertTrue(of("/start/$id:string/fixed/$val:int").matches("/start/xyz/fixed/123"));
    Assert.assertFalse(of("/start/$id:string/fixed/$val:int").matches("/stop/xyz/fixed/123"));
    Assert.assertFalse(of("/start/$id:string/fixed/$val:int").matches("/stop"));
    Assert.assertFalse(of("/start/$id:string/fixed/$val:int").matches("/stop/xyz"));
    Assert.assertFalse(of("/start/$id:string/fixed/$val:int").matches("/stop/xyz/nope"));
    Assert.assertFalse(of("/start/$id:string/fixed/$val:int").matches("/stop/xyz/fixed"));
    Assert.assertFalse(of("/start/$id:string/fixed/$val:int").matches("/stop/xyz/fixed/xyz"));
    Assert.assertFalse(of("/start/$id:string/fixed/$val:int").matches("/stop/xyz/fixed/123/more"));
  }
}
