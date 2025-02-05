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
package ape.runtime.deploy;

import ape.common.ErrorCodeException;
import org.junit.Assert;
import org.junit.Test;

public class DeploymentPlanTests {

  @Test
  public void badHash() {
    try {
      DeploymentPlan.hash(null, null);
      Assert.fail();
    } catch (RuntimeException ex) {

    }
  }

  @Test
  public void includes_1() throws Exception {
    new DeploymentPlan(
        "{\"versions\":{\"x\":{\"main\":\"@include x;\",\"includes\":{\"x\":\"\"}}},\"default\":\"x\",\"plan\":[{\"version\":\"x\",\"percent\":50,\"prefix\":\"k\",\"seed\":\"a2\"}]}",
        (t, errorCode) -> {});
  }

  @Test
  public void includes_skip() throws Exception {
    new DeploymentPlan(
        "{\"versions\":{\"x\":{\"main\":\"\",\"includes\":123}},\"default\":\"x\",\"plan\":[{\"version\":\"x\",\"percent\":50,\"prefix\":\"k\",\"seed\":\"a2\"}]}",
        (t, errorCode) -> {});
  }

  @Test
  public void includes_2() throws Exception {
    new DeploymentPlan(
        "{\"versions\":{\"x\":{\"main\":\"@include x;\",\"includes\":{\"x\":\"@include y;\",\"y\":\"\"}}},\"default\":\"x\",\"plan\":[{\"version\":\"x\",\"percent\":50,\"prefix\":\"k\",\"seed\":\"a2\"}]}",
        (t, errorCode) -> {});
  }

  @Test
  public void happy() throws Exception {
    DeploymentPlan plan = new DeploymentPlan(
        "{\"versions\":{\"x\":\"\",\"y\":\"\",\"z\":\"\"},\"default\":\"z\",\"plan\":[{\"version\":\"x\",\"percent\":0,\"keys\":[\"1\",\"2\"],\"prefix\":\"k\",\"seed\":\"a2\"},{\"version\":\"y\",\"percent\":50,\"prefix\":\"\",\"seed\":\"a2\"}]}",
        (t, errorCode) -> {});
    Assert.assertEquals("x", plan.pickVersion("1"));
    Assert.assertEquals("x", plan.pickVersion("2"));
    Assert.assertEquals("y", plan.pickVersion("100"));
    Assert.assertEquals("y", plan.pickVersion("101"));
    Assert.assertEquals("z", plan.pickVersion("102"));
    Assert.assertEquals("z", plan.pickVersion("103"));
    Assert.assertEquals("z", plan.pickVersion("104"));
    Assert.assertEquals("z", plan.pickVersion("105"));
    Assert.assertEquals("y", plan.pickVersion("106"));
  }

  @Test
  public void happy2() throws Exception {
    DeploymentPlan plan = new DeploymentPlan(
        "{\"versions\":{\"x\":\"\",\"y\":\"\",\"z\":\"\"},\"default\":\"z\",\"plan\":[{\"version\":\"x\",\"percent\":0,\"keys\":[\"1\",\"2\"],\"prefix\":\"k\",\"seed\":\"a2\"},{\"version\":\"y\",\"percent\":900,\"prefix\":\"y\",\"seed\":\"a2\"}]}",
        (t, errorCode) -> {});
    Assert.assertEquals("x", plan.pickVersion("1"));
    Assert.assertEquals("x", plan.pickVersion("2"));
    Assert.assertEquals("y", plan.pickVersion("y3"));
  }

  @Test
  public void happyInstrumented() throws Exception {
    DeploymentPlan plan = new DeploymentPlan(
        "{\"instrument\":true,\"versions\":{\"x\":\"\",\"y\":\"\",\"z\":\"\"},\"default\":\"z\",\"plan\":[{\"version\":\"x\",\"percent\":0,\"keys\":[\"1\",\"2\"],\"prefix\":\"k\",\"seed\":\"a2\"},{\"version\":\"y\",\"percent\":900,\"prefix\":\"y\",\"seed\":\"a2\"}]}",
        (t, errorCode) -> {});
    Assert.assertTrue(plan.instrument);
  }

  public void parseBadTest(String json, int expectedError) {
    try {
      new DeploymentPlan(json, (t, errorCode) -> {});
      Assert.fail();
    } catch (ErrorCodeException ex) {
      Assert.assertEquals(expectedError, ex.code);
    }
  }

  @Test
  public void stage_no_version() {
    parseBadTest("{\"versions\":{\"x\":\"\"},\"default\":\"x\",\"plan\":[{}]}", 199768);
  }

  @Test
  public void stage_version_doest_exist() {
    parseBadTest(
        "{\"versions\":{\"x\":\"\"},\"default\":\"x\",\"plan\":[{\"version\":\"y\"}]}", 120895);
  }

  @Test
  public void stage_extra_field() {
    parseBadTest(
        "{\"versions\":{\"x\":\"\"},\"default\":\"x\",\"plan\":[{\"version\":\"x\",\"z\":true}]}",
        116812);
  }

  @Test
  public void stage_bad_field_keys() {
    parseBadTest(
        "{\"versions\":{\"x\":\"\"},\"default\":\"x\",\"plan\":[{\"version\":\"x\",\"keys\":true}]}",
        199886);
  }

  @Test
  public void stage_bad_element() {
    parseBadTest("{\"versions\":{\"x\":\"\"},\"default\":\"x\",\"plan\":[true]}", 176703);
  }

  @Test
  public void plan_bad_type() {
    parseBadTest("{\"versions\":{\"x\":\"\"},\"default\":\"x\",\"plan\":\"z\"}", 126012);
  }

  @Test
  public void no_default() {
    parseBadTest("{\"versions\":{\"x\":\"\"}}", 143948);
  }

  @Test
  public void default_invalid() {
    parseBadTest("{\"versions\":{\"x\":\"\"},\"default\":\"y\"}", 145980);
  }

  @Test
  public void no_versions() {
    parseBadTest("{}", 115788);
  }

  @Test
  public void empty_versions() {
    parseBadTest("{\"versions\":{}}", 115788);
  }

  @Test
  public void bad_versions_type() {
    parseBadTest("{\"versions\":true}", 155711);
  }

  @Test
  public void invalid_field() {
    parseBadTest("{\"x\":{}}", 143430);
  }

  @Test
  public void must_be_obj() {
    parseBadTest("[]", 117818);
  }

  @Test
  public void must_be_json() {
    parseBadTest("x", 146561);
  }
}
