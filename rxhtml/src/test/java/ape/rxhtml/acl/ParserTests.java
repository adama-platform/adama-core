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
package ape.rxhtml.acl;

import ape.rxhtml.acl.commands.*;
import ape.rxhtml.acl.commands.*;
import ape.rxhtml.template.Environment;
import ape.rxhtml.template.config.Feedback;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class ParserTests {
  @Test
  public void fragments() {
    ArrayList<String> f;
    {
      f = Parser.fragmentize("set:x='42 42'");
      Assert.assertEquals(1, f.size());
      Assert.assertEquals("set:x=42 42", f.get(0));
    }
    {
      f = Parser.fragmentize("set:x='42 42' toggle:x   goto:'[ happy ]'   ");
      Assert.assertEquals(3, f.size());
      Assert.assertEquals("set:x=42 42", f.get(0));
      Assert.assertEquals("toggle:x", f.get(1));
      Assert.assertEquals("goto:[ happy ]", f.get(2));
    }
  }

  @Test
  public void cmd_goto1() throws Exception {
    Goto goto_ = (Goto) (Parser.parse("goto:/view/x-yz-{data}").get(0));
    assertIs(goto_, "var a={};\n" + "$.YS(State,a,'data');\n" + "$.onGO(DOM,'type',State,function(){ return \"/view/x-yz-\" + $.F(a,'data');});\n");
  }

  private static void assertIs(Command command, String expected) {
    Environment env = Environment.fresh(Feedback.NoOp, "test");
    if (command instanceof BulkCommand) {
      Assert.fail();
    } else {
      command.write(env.stateVar("State"), "type", "DOM");
    }
    Assert.assertEquals(expected, env.writer.toString());
  }

  private static void assertIsBulk(BulkCommand command, String expected) {
    Environment env = Environment.fresh(Feedback.NoOp, "test");
    command.writeBulk(env.stateVar("State"), "DOM", "$ARR");
    Assert.assertEquals(expected, env.writer.toString());
  }

  @Test
  public void cmd_goto2() throws Exception {
    Goto goto_ = (Goto) (Parser.parse("goto:/fixed").get(0));
    assertIs(goto_, "$.onGO(DOM,'type',State,'/fixed');\n");
  }

  @Test
  public void custom() throws Exception {
    Custom custom = (Custom) (Parser.parse("custom:xyz").get(0));
    Assert.assertEquals("xyz", custom.command);
    assertIs(custom, "$.exCC(DOM,'type',State,'xyz');\n");
  }

  @Test
  public void forceAuth() throws Exception {
    ForceAuth fa = (ForceAuth) (Parser.parse("force-auth:name=identity").get(0));
    Assert.assertEquals("name", fa.name);
    Assert.assertEquals("identity", fa.identity);
    assertIs(fa, "$.onFORCE_AUTH(DOM,'type','name','identity');\n");
  }

  @Test
  public void decide1() throws Exception {
    Decide decide = (Decide) (Parser.parse("decide:xzzzzzzzzzz").get(0));
    Assert.assertEquals("xzzzzzzzzzz", decide.channel);
    Assert.assertEquals("id", decide.key);
    Assert.assertEquals("id", decide.path);
    assertIs(decide, "$.exD(DOM,'type',State,'id','xzzzzzzzzzz','id');\n");
  }

  @Test
  public void decide2() throws Exception {
    Decide decide = (Decide) (Parser.parse("decide:xzzzzzzzzzz|wut").get(0));
    Assert.assertEquals("xzzzzzzzzzz", decide.channel);
    Assert.assertEquals("wut", decide.key);
    Assert.assertEquals("id", decide.path);
    assertIs(decide, "$.exD(DOM,'type',State,'id','xzzzzzzzzzz','wut');\n");
  }

  @Test
  public void decide3() throws Exception {
    Decide decide = (Decide) (Parser.parse("decide:xzzzzzzzzzz|wut|the").get(0));
    Assert.assertEquals("xzzzzzzzzzz", decide.channel);
    Assert.assertEquals("wut", decide.key);
    Assert.assertEquals("the", decide.path);
    assertIs(decide, "$.exD(DOM,'type',State,'the','xzzzzzzzzzz','wut');\n");
  }

  @Test
  public void dec1() throws Exception {
    Decrement dec = (Decrement) (Parser.parse("dec:xyz").get(0));
    Assert.assertEquals("view:xyz", dec.path);
    assertIs(dec, "$.onD(DOM,'type',$.pV(State),'xyz', -1);\n");
  }

  @Test
  public void dec2() throws Exception {
    Decrement dec = (Decrement) (Parser.parse("dec:data:xyz").get(0));
    Assert.assertEquals("data:xyz", dec.path);
    assertIs(dec, "$.onD(DOM,'type',$.pD(State),'xyz', -1);\n");
  }

  @Test
  public void decrement() throws Exception {
    Decrement dec = (Decrement) (Parser.parse("decrement:view:xyz").get(0));
    Assert.assertEquals("view:xyz", dec.path);
    assertIs(dec, "$.onD(DOM,'type',$.pV(State),'xyz', -1);\n");
  }

  @Test
  public void inc1() throws Exception {
    Increment inc = (Increment) (Parser.parse("inc:xyz").get(0));
    Assert.assertEquals("view:xyz", inc.path);
    assertIs(inc, "$.onD(DOM,'type',$.pV(State),'xyz', 1);\n");
  }

  @Test
  public void inc2() throws Exception {
    Increment inc = (Increment) (Parser.parse("inc:data:xyz").get(0));
    Assert.assertEquals("data:xyz", inc.path);
    assertIs(inc, "$.onD(DOM,'type',$.pD(State),'xyz', 1);\n");
  }

  @Test
  public void increment() throws Exception {
    Increment inc = (Increment) (Parser.parse("increment:view:xyz").get(0));
    Assert.assertEquals("view:xyz", inc.path);
    assertIs(inc, "$.onD(DOM,'type',$.pV(State),'xyz', 1);\n");
  }

  @Test
  public void toggle1() throws Exception {
    Toggle toggle = (Toggle) (Parser.parse("toggle:xyz").get(0));
    Assert.assertEquals("view:xyz", toggle.path);
    assertIs(toggle, "$.onT(DOM,'type',$.pV(State),'xyz');\n");
  }

  @Test
  public void toggle2() throws Exception {
    Toggle toggle = (Toggle) (Parser.parse("toggle:data:xyz").get(0));
    Assert.assertEquals("data:xyz", toggle.path);
    assertIs(toggle, "$.onT(DOM,'type',$.pD(State),'xyz');\n");
  }

  @Test
  public void set1() throws Exception {
    Set set = (Set) (Parser.parse("set:xyz=val").get(0));
    Assert.assertEquals("view:xyz", set.path);
    Assert.assertEquals("val", set.value);
    assertIsBulk(set, "$ARR.push($.bS(DOM,$.pV(State),'xyz','val'));\n");
  }

  @Test
  public void set2() throws Exception {
    Set set = (Set) (Parser.parse("set:data:xyz=true").get(0));
    Assert.assertEquals("data:xyz", set.path);
    Assert.assertEquals("true", set.value);
    assertIsBulk(set, "$ARR.push($.bS(DOM,$.pD(State),'xyz',true));\n");
  }

  @Test
  public void set3() throws Exception {
    Set set = (Set) (Parser.parse("set:xyz=123").get(0));
    Assert.assertEquals("view:xyz", set.path);
    Assert.assertEquals("123", set.value);
    assertIsBulk(set, "$ARR.push($.bS(DOM,$.pV(State),'xyz',123));\n");
  }

  @Test
  public void set4() throws Exception {
    Set set = (Set) (Parser.parse("set:xyz={xyz}").get(0));
    Assert.assertEquals("view:xyz", set.path);
    Assert.assertEquals("{xyz}", set.value);
    assertIsBulk(set, "var a={};\n" + "$.YS(State,a,'xyz');\n" + "$ARR.push($.bS(DOM,$.pV(State),'xyz',function(){ return $.F(a,'xyz');}));\n");
  }

  @Test
  public void set_raise() throws Exception {
    Set set = (Set) (Parser.parse("raise:xyx").get(0));
    Assert.assertEquals("view:xyx", set.path);
    Assert.assertEquals("true", set.value);
    assertIsBulk(set, "$ARR.push($.bS(DOM,$.pV(State),'xyx',true));\n");
  }

  @Test
  public void set_lower() throws Exception {
    Set set = (Set) (Parser.parse("lower:xyz").get(0));
    Assert.assertEquals("view:xyz", set.path);
    Assert.assertEquals("false", set.value);
    assertIsBulk(set, "$ARR.push($.bS(DOM,$.pV(State),'xyz',false));\n");
  }

  @Test
  public void submit() throws Exception {
    Submit set = (Submit) (Parser.parse("submit").get(0));
    assertIsBulk(set, "$ARR.push($.bSB(DOM));\n");
  }

  @Test
  public void nuke() throws Exception {
    BulkCommand nuke = (BulkCommand) (Parser.parse("nuke").get(0));
    assertIsBulk(nuke, "$ARR.push($.bNK(DOM));\n");
  }
}
