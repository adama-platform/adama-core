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
package ape.rxhtml.atl;

import ape.rxhtml.atl.tree.Text;
import ape.rxhtml.atl.tree.Tree;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class ParserTests {
  @Test
  public void fail() {
    String[] strs = new String[]{"{xyz", "[xyz", "[[", "[}", "{]", "{{", "[b]v"};
    for (String str : strs) {
      try {
        Parser.parse(str);
        Assert.fail(str);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  @Test
  public void simple() throws Exception {
    Tree tree = Parser.parse("xyz");
    Assert.assertTrue(tree instanceof Text);
    Assert.assertEquals(((Text) tree).text, "xyz");
    Assert.assertEquals("TEXT(xyz)", tree.debug());
    Assert.assertEquals("\"xyz\"", tree.js(Context.DEFAULT, "$X"));
    Map<String, String> vars = tree.variables();
    Assert.assertEquals(0, vars.size());
    Assert.assertEquals(0, tree.queries().size());
  }

  @Test
  public void href_regression() throws Exception {
    Tree tree = Parser.parse("/#project/{view:space}/manage");
    Assert.assertEquals("[TEXT(/#project/),LOOKUP[space],TEXT(/manage)]", tree.debug());
    Assert.assertEquals("\"/#project/\" + $.F($X,'space') + \"/manage\"", tree.js(Context.DEFAULT, "$X"));
    Assert.assertEquals(1, tree.queries().size());
    Assert.assertTrue(tree.queries().contains("view:space"));
  }

  @Test
  public void variable() throws Exception {
    Tree tree = Parser.parse("hi {first|trim} {last}");
    Assert.assertEquals("[TEXT(hi ),TRANSFORM(LOOKUP[first],trim),TEXT( ),LOOKUP[last]]", tree.debug());
    Assert.assertEquals("\"hi \" + ($.TR('trim'))($.F($X,'first')) + \" \" + $.F($X,'last')", tree.js(Context.DEFAULT, "$X"));
    Map<String, String> vars = tree.variables();
    Assert.assertEquals(2, vars.size());
    Assert.assertTrue(vars.containsKey("first"));
    Assert.assertTrue(vars.containsKey("last"));
    Assert.assertEquals(2, tree.queries().size());
    Assert.assertTrue(tree.queries().contains("first"));
    Assert.assertTrue(tree.queries().contains("last"));
  }

  @Test
  public void normal_white_space() throws Exception {
    Tree tree = Parser.parse("BLAH{nope}      many    {more}     ");
    Assert.assertEquals("[TEXT(BLAH),LOOKUP[nope],TEXT(      many    ),LOOKUP[more],TEXT(     )]", tree.debug());
    Assert.assertEquals("\"BLAH\" + $.F($X,'nope') + \"      many    \" + $.F($X,'more') + \"     \"", tree.js(Context.DEFAULT, "$X"));
  }

  @Test
  public void normalize_css() throws Exception {
    Tree tree = Parser.parse("BLAH{nope}      many    {more}     ");
    Assert.assertEquals("[TEXT(BLAH),LOOKUP[nope],TEXT(      many    ),LOOKUP[more],TEXT(     )]", tree.debug());
    Context class_context = Context.makeClassContext();
    Assert.assertEquals("\" BLAH \" + $.F($X,'nope') + \" many \" + $.F($X,'more')", tree.js(class_context, "$X"));
  }

  @Test
  public void condition_trailing() throws Exception {
    Tree tree = Parser.parse("hi [b]active[/]");
    Assert.assertEquals("[TEXT(hi ),(LOOKUP[b]) ? (TEXT(active)) : (EMPTY)]", tree.debug());
    Assert.assertEquals("\"hi \" + (($.F($X,'b')) ? (\"active\") : (\"\"))", tree.js(Context.DEFAULT, "$X"));
    Map<String, String> vars = tree.variables();
    Assert.assertEquals(1, vars.size());
    Assert.assertTrue(vars.containsKey("b"));
  }

  @Test
  public void condition_trailing_negate() throws Exception {
    Tree tree = Parser.parse("hi [!b]inactive[/]");
    Assert.assertEquals("[TEXT(hi ),(!(LOOKUP[b])) ? (TEXT(inactive)) : (EMPTY)]", tree.debug());
    Assert.assertEquals("\"hi \" + ((!($.F($X,'b'))) ? (\"inactive\") : (\"\"))", tree.js(Context.DEFAULT, "$X"));
    Map<String, String> vars = tree.variables();
    Assert.assertEquals(1, vars.size());
    Assert.assertTrue(vars.containsKey("b"));
  }

  @Test
  public void condition() throws Exception {
    Tree tree = Parser.parse("hi [b]A[#b]B[/b] there");
    Assert.assertEquals("[TEXT(hi ),(LOOKUP[b]) ? (TEXT(A)) : (TEXT(B)),TEXT( there)]", tree.debug());
    Assert.assertEquals("\"hi \" + (($.F($X,'b')) ? (\"A\") : (\"B\")) + \" there\"", tree.js(Context.DEFAULT, "$X"));
    Map<String, String> vars = tree.variables();
    Assert.assertEquals(1, vars.size());
    Assert.assertTrue(vars.containsKey("b"));
  }

  @Test
  public void condition_eq() throws Exception {
    Tree tree = Parser.parse("hi [b=xyz]A[#b=xyz]B[/b=xyz] there");
    Assert.assertEquals("[TEXT(hi ),(OP(==)[LOOKUP[b],'TEXT(xyz)']) ? (TEXT(A)) : (TEXT(B)),TEXT( there)]", tree.debug());
    Assert.assertEquals("\"hi \" + ((($.F($X,'b')==\"xyz\")) ? (\"A\") : (\"B\")) + \" there\"", tree.js(Context.DEFAULT, "$X"));
    Map<String, String> vars = tree.variables();
    Assert.assertEquals(1, vars.size());
    Assert.assertTrue(vars.containsKey("b"));
    Assert.assertEquals(1, tree.queries().size());
    Assert.assertTrue(tree.queries().contains("b"));
  }
}
