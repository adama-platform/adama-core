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
package ape.rxhtml.typing;

import com.fasterxml.jackson.databind.node.ObjectNode;
import ape.common.Json;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.function.Consumer;

public class DataScopeTests {

  public static class ErrorAccum implements Consumer<String> {
    public ArrayList<String> log;

    public ErrorAccum() {
      this.log = new ArrayList<>();
    }

    @Override
    public void accept(String e) {
      System.err.println(e);
      log.add(e);
    }
  };

  public static class UsedAccum implements UnusedReport {
    public TreeSet<String> set;
    public UsedAccum() {
      this.set = new TreeSet<>();
    }
    @Override
    public void reportUnused(String type, String field) {
      set.add(type + "::" + field);
    }
  }

  @Test
  public void scope_iterate_into_root_list() {
    ObjectNode forest = Json.parseJsonObject("{\"types\":{\"__Root\":{\"nature\":\"reactive_record\",\"name\":\"Root\",\"fields\":" + //
        "{\"_users\":{\"type\":{\"nature\":\"reactive_table\",\"record_name\":\"User\"},\"privacy\":\"private\"}" + //
        ",\"_projects\":{\"type\":{\"nature\":\"reactive_table\",\"record_name\":\"Project\"},\"privacy\":\"private\"}," + //
        "\"projects\":{\"type\":{\"nature\":\"native_list\",\"type\":{\"nature\":\"reactive_ref\",\"ref\":\"Project\"}},\"privacy\":\"public\"}," +
        "\"others\":{\"type\":{\"nature\":\"native_list\",\"type\":{\"nature\":\"reactive_ref\",\"ref\":\"User\"}},\"privacy\":\"public\"}," +
        "\"current_project\":{\"type\":{\"nature\":\"native_maybe\",\"type\":{\"nature\":\"reactive_ref\",\"ref\":\"Project\"}},\"privacy\":\"bubble\"}," +
        "\"current_task\":{\"type\":{\"nature\":\"native_maybe\",\"type\":{\"nature\":\"native_ref\",\"ref\":\"Task\"}},\"privacy\":\"bubble\"}}}," +
        "\"User\":{\"nature\":\"reactive_record\",\"name\":\"User\",\"fields\":{\"id\":{\"type\":{\"nature\":\"reactive_value\",\"type\":\"int\"},\"privacy\":\"public\"}," +
        "\"who\":{\"type\":{\"nature\":\"reactive_value\",\"type\":\"principal\"},\"privacy\":\"private\"},\"name\":{\"type\":{\"nature\":\"reactive_value\",\"type\":\"string\"},\"privacy\":\"public\"}," +
        "\"email\":{\"type\":{\"nature\":\"reactive_value\",\"type\":\"string\"},\"privacy\":\"public\"},\"password_hash\":{\"type\":{\"nature\":\"reactive_value\",\"type\":\"string\"},\"privacy\":\"private\"},\"temp_password_hash\":{\"type\":{\"nature\":\"reactive_value\",\"type\":\"string\"},\"privacy\":\"private\"},\"temp_password_hash_expires\":{\"type\":{\"nature\":\"reactive_value\",\"type\":\"datetime\"},\"privacy\":\"private\"}}}}}");

    UsedAccum reportA = new UsedAccum();
    UnusedReport.drive(forest, reportA);
    DataScope scope = DataScope.root(forest);
    ErrorAccum ea = new ErrorAccum();
    PrivacyFilter privacy = new PrivacyFilter();
    DataSelector ds = scope.select(privacy, "projects", ea);
    Assert.assertEquals("[__Root]", ds.scope.toString());
    DataScope next = ds.iterateInto(ea);
    Assert.assertEquals("[__Root][list:Project][Project]", next.toString());
    UsedAccum reportB = new UsedAccum();
    UnusedReport.drive(forest, reportB);
    Assert.assertTrue(reportA.set.contains("__Root::projects"));
    Assert.assertFalse(reportB.set.contains("__Root::projects"));
  }

  @Test
  public void hasChannel_with_no_channels_key() {
    ObjectNode forest = Json.parseJsonObject("{\"types\":{\"__Root\":{\"nature\":\"reactive_record\",\"name\":\"Root\",\"fields\":{}}}}");
    DataScope scope = DataScope.root(forest);
    Assert.assertFalse(scope.hasChannel("anything"));
  }

  @Test
  public void hasChannel_with_channels() {
    ObjectNode forest = Json.parseJsonObject("{\"types\":{\"__Root\":{\"nature\":\"reactive_record\",\"name\":\"Root\",\"fields\":{}}},\"channels\":{\"foo\":{}}}");
    DataScope scope = DataScope.root(forest);
    Assert.assertTrue(scope.hasChannel("foo"));
    Assert.assertFalse(scope.hasChannel("bar"));
  }

  @Test
  public void validate_attribute_on_value_type() {
    ObjectNode forest = Json.parseJsonObject("{\"types\":{\"__Root\":{\"nature\":\"reactive_record\",\"name\":\"Root\",\"fields\":{" +
        "\"name\":{\"type\":{\"nature\":\"reactive_value\",\"type\":\"string\"},\"privacy\":\"public\"}," +
        "\"count\":{\"type\":{\"nature\":\"reactive_value\",\"type\":\"int\"},\"privacy\":\"public\"}," +
        "\"items\":{\"type\":{\"nature\":\"native_list\",\"type\":{\"nature\":\"reactive_ref\",\"ref\":\"Item\"}},\"privacy\":\"public\"}" +
        "}}},\"channels\":{}}");
    DataScope scope = DataScope.root(forest);
    PrivacyFilter privacy = new PrivacyFilter();
    ErrorAccum ea = new ErrorAccum();

    DataSelector nameDs = scope.select(privacy, "name", ea);
    Assert.assertEquals(0, ea.log.size());
    nameDs.validateAttribute(ea);
    Assert.assertEquals(0, ea.log.size());

    DataSelector countDs = scope.select(privacy, "count", ea);
    countDs.validateAttribute(ea);
    Assert.assertEquals(0, ea.log.size());
  }

  @Test
  public void validate_integral_on_int() {
    ObjectNode forest = Json.parseJsonObject("{\"types\":{\"__Root\":{\"nature\":\"reactive_record\",\"name\":\"Root\",\"fields\":{" +
        "\"count\":{\"type\":{\"nature\":\"reactive_value\",\"type\":\"int\"},\"privacy\":\"public\"}," +
        "\"name\":{\"type\":{\"nature\":\"reactive_value\",\"type\":\"string\"},\"privacy\":\"public\"}" +
        "}}},\"channels\":{}}");
    DataScope scope = DataScope.root(forest);
    PrivacyFilter privacy = new PrivacyFilter();
    ErrorAccum ea = new ErrorAccum();

    DataSelector countDs = scope.select(privacy, "count", ea);
    countDs.validateIntegral(ea);
    Assert.assertEquals(0, ea.log.size());

    DataSelector nameDs = scope.select(privacy, "name", ea);
    nameDs.validateIntegral(ea);
    Assert.assertEquals(1, ea.log.size());
    Assert.assertTrue(ea.log.get(0).contains("expected integral type"));
  }

  @Test
  public void validate_boolean_type() {
    ObjectNode forest = Json.parseJsonObject("{\"types\":{\"__Root\":{\"nature\":\"reactive_record\",\"name\":\"Root\",\"fields\":{" +
        "\"flag\":{\"type\":{\"nature\":\"reactive_value\",\"type\":\"bool\"},\"privacy\":\"public\"}," +
        "\"name\":{\"type\":{\"nature\":\"reactive_value\",\"type\":\"string\"},\"privacy\":\"public\"}" +
        "}}},\"channels\":{}}");
    DataScope scope = DataScope.root(forest);
    PrivacyFilter privacy = new PrivacyFilter();
    ErrorAccum ea = new ErrorAccum();

    DataSelector flagDs = scope.select(privacy, "flag", ea);
    flagDs.validateBoolean(ea);
    Assert.assertEquals(0, ea.log.size());

    DataSelector nameDs = scope.select(privacy, "name", ea);
    nameDs.validateBoolean(ea);
    Assert.assertEquals(1, ea.log.size());
    Assert.assertTrue(ea.log.get(0).contains("expected boolean type"));
  }

  @Test
  public void validate_switchable_types() {
    ObjectNode forest = Json.parseJsonObject("{\"types\":{\"__Root\":{\"nature\":\"reactive_record\",\"name\":\"Root\",\"fields\":{" +
        "\"count\":{\"type\":{\"nature\":\"reactive_value\",\"type\":\"int\"},\"privacy\":\"public\"}," +
        "\"name\":{\"type\":{\"nature\":\"reactive_value\",\"type\":\"string\"},\"privacy\":\"public\"}," +
        "\"flag\":{\"type\":{\"nature\":\"reactive_value\",\"type\":\"bool\"},\"privacy\":\"public\"}" +
        "}}},\"channels\":{}}");
    DataScope scope = DataScope.root(forest);
    PrivacyFilter privacy = new PrivacyFilter();
    ErrorAccum ea = new ErrorAccum();

    scope.select(privacy, "count", ea).validateSwitchable(ea);
    Assert.assertEquals(0, ea.log.size());

    scope.select(privacy, "name", ea).validateSwitchable(ea);
    Assert.assertEquals(0, ea.log.size());

    scope.select(privacy, "flag", ea).validateSwitchable(ea);
    Assert.assertEquals(0, ea.log.size());
  }

  @Test
  public void unused_report_handles_missing_fields() {
    ObjectNode forest = Json.parseJsonObject("{\"types\":{\"__ViewerType\":{\"nature\":\"native_message\",\"name\":\"__ViewerType\",\"anonymous\":true}}}");
    UsedAccum report = new UsedAccum();
    UnusedReport.drive(forest, report);
    Assert.assertEquals(0, report.set.size());
  }
}
