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
}
