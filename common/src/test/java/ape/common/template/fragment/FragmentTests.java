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
package ape.common.template.fragment;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class FragmentTests {
  @Test
  public void coverage() {
    Fragment f = new Fragment(FragmentType.Text, "X");
  }

  @Test
  public void restore_closing_bracket() {
    ArrayList<Fragment> fragments = Fragment.parse("Hi [");
    Assert.assertEquals(1, fragments.size());
    Assert.assertEquals("Text:[Hi []", fragments.get(0).toString());
  }

  @Test
  public void fragmentization_simple() {
    ArrayList<Fragment> fragments = Fragment.parse("Hi [[world]] \t\n[[person]]");
    Assert.assertEquals(4, fragments.size());
    Assert.assertEquals("Text:[Hi ]", fragments.get(0).toString());
    Assert.assertEquals("Expression:[world]", fragments.get(1).toString());
    Assert.assertEquals("Text:[ \t\n" + "]", fragments.get(2).toString());
    Assert.assertEquals("Expression:[person]", fragments.get(3).toString());
  }

  @Test
  public void fragmentization_compound() {
    ArrayList<Fragment> fragments = Fragment.parse("Hi [[world|dirty]]");
    Assert.assertEquals(2, fragments.size());
    Assert.assertEquals("Text:[Hi ]", fragments.get(0).toString());
    Assert.assertEquals("Expression:[world, |, dirty]", fragments.get(1).toString());
  }

  @Test
  public void fragmentization_brace_solo() {
    ArrayList<Fragment> fragments = Fragment.parse("Hi [ [ {");
    Assert.assertEquals(1, fragments.size());
    Assert.assertEquals("Text:[Hi [ [ {]", fragments.get(0).toString());
  }


  @Test
  public void fragmentization_brace_empty() {
    ArrayList<Fragment> fragments = Fragment.parse("Hi [[]]");
    Assert.assertEquals(2, fragments.size());
    Assert.assertEquals("Text:[Hi ]", fragments.get(0).toString());
    Assert.assertEquals("Text:[[]", fragments.get(1).toString());
  }

  @Test
  public void fragmentization_empty_param() {
    ArrayList<Fragment> fragments = Fragment.parse("Hi [[thing |+-]]");
    Assert.assertEquals(2, fragments.size());
    Assert.assertEquals("Text:[Hi ]", fragments.get(0).toString());
    Assert.assertEquals("Expression:[thing, |, +, -]", fragments.get(1).toString());
  }

  @Test
  public void fragmentization_conditions() {
    ArrayList<Fragment> fragments = Fragment.parse("Hi [[#world]] \t\n[[/world]][[^p]]not-p[[/p]]");
    Assert.assertEquals(7, fragments.size());
    Assert.assertEquals("Text:[Hi ]", fragments.get(0).toString());
    Assert.assertEquals("If:[world]", fragments.get(1).toString());
    Assert.assertEquals("Text:[ \t\n" + "]", fragments.get(2).toString());
    Assert.assertEquals("End:[world]", fragments.get(3).toString());
    Assert.assertEquals("IfNot:[p]", fragments.get(4).toString());
    Assert.assertEquals("Text:[not-p]", fragments.get(5).toString());
    Assert.assertEquals("End:[p]", fragments.get(6).toString());
  }

  @Test
  public void incomplete() {
    try {
      Fragment.parse("Hi [[#world]xyz");
      Assert.fail();
    } catch (RuntimeException re) {
      Assert.assertEquals("']' encountered without additional ']' during scan", re.getMessage());
    }
  }

  @Test
  public void eos1() {
    try {
      Fragment.parse("Hi [[#world");
      Assert.fail();
    } catch (RuntimeException re) {
      Assert.assertEquals("scan() failed due to missing ']'", re.getMessage());
    }
  }

  @Test
  public void eos2() {
    try {
      Fragment.parse("Hi [[#world]");
      Assert.fail();
    } catch (RuntimeException re) {
      Assert.assertEquals("']' encountered without additional ']' during scan", re.getMessage());
    }
  }
}
