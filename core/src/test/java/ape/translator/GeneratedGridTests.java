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
package ape.translator;

import javax.annotation.processing.Generated;
import org.junit.Test;

@Generated("ape.support.testgen.TestClass")
public class GeneratedGridTests extends GeneratedBase {
  private String cached_BadAccess_1 = null;
  private String get_BadAccess_1() {
    if (cached_BadAccess_1 != null) {
      return cached_BadAccess_1;
    }
    cached_BadAccess_1 = generateTestOutput(false, "BadAccess_1", "./test_code/Grid_BadAccess_failure.a");
    return cached_BadAccess_1;
  }

  @Test
  public void testBadAccessFailure() {
    assertLiveFail(get_BadAccess_1());
  }

  @Test
  public void testBadAccessNotTerribleLineNumbers() {
    assertNotTerribleLineNumbers(get_BadAccess_1());
  }

  @Test
  public void testBadAccessExceptionFree() {
    assertExceptionFree(get_BadAccess_1());
  }

  @Test
  public void testBadAccessTODOFree() {
    assertTODOFree(get_BadAccess_1());
  }

  @Test
  public void stable_BadAccess_1() {
    String live = get_BadAccess_1();
    StringBuilder gold = new StringBuilder();
    gold.append("Path:Grid_BadAccess_failure.a");
    gold.append("\n--ISSUES-------------------------------------------");
    gold.append("\n[{\"range\":{\"start\":{\"line\":0,\"character\":12,\"byte\":12},\"end\":{\"line\":0,\"character\":15,\"byte\":15}},\"severity\":1,\"source\":\"error\",\"message\":\"Type check failure: the type 'int' is unable to store type 'double'.\",\"file\":\"./test_code/Grid_BadAccess_failure.a\"},{\"range\":{\"start\":{\"line\":0,\"character\":12,\"byte\":12},\"end\":{\"line\":0,\"character\":15,\"byte\":15}},\"severity\":1,\"source\":\"error\",\"message\":\"Type check failure: the type 'int' is unable to store type 'double'.\",\"file\":\"./test_code/Grid_BadAccess_failure.a\"}]\"--JAVA---------------------------------------------");
    gold.append("\n");
    gold.append("\nFailedValidation");
    assertStable(live, gold);
  }
  private String cached_BadSet_2 = null;
  private String get_BadSet_2() {
    if (cached_BadSet_2 != null) {
      return cached_BadSet_2;
    }
    cached_BadSet_2 = generateTestOutput(false, "BadSet_2", "./test_code/Grid_BadSet_failure.a");
    return cached_BadSet_2;
  }

  @Test
  public void testBadSetFailure() {
    assertLiveFail(get_BadSet_2());
  }

  @Test
  public void testBadSetNotTerribleLineNumbers() {
    assertNotTerribleLineNumbers(get_BadSet_2());
  }

  @Test
  public void testBadSetExceptionFree() {
    assertExceptionFree(get_BadSet_2());
  }

  @Test
  public void testBadSetTODOFree() {
    assertTODOFree(get_BadSet_2());
  }

  @Test
  public void stable_BadSet_2() {
    String live = get_BadSet_2();
    StringBuilder gold = new StringBuilder();
    gold.append("Path:Grid_BadSet_failure.a");
    gold.append("\n--ISSUES-------------------------------------------");
    gold.append("\n[{\"range\":{\"start\":{\"line\":0,\"character\":17,\"byte\":17},\"end\":{\"line\":0,\"character\":23,\"byte\":23}},\"severity\":1,\"source\":\"error\",\"message\":\"Type check failure: the type 'r<double>' is unable to store type 'string'.\",\"file\":\"./test_code/Grid_BadSet_failure.a\"}]\"--JAVA---------------------------------------------");
    gold.append("\n");
    gold.append("\nFailedValidation");
    assertStable(live, gold);
  }
  private String cached_EOS_3 = null;
  private String get_EOS_3() {
    if (cached_EOS_3 != null) {
      return cached_EOS_3;
    }
    cached_EOS_3 = generateTestOutput(false, "EOS_3", "./test_code/Grid_EOS_failure.a");
    return cached_EOS_3;
  }

  @Test
  public void testEOSFailure() {
    assertLiveFail(get_EOS_3());
  }

  @Test
  public void testEOSNotTerribleLineNumbers() {
    assertNotTerribleLineNumbers(get_EOS_3());
  }

  @Test
  public void testEOSExceptionFree() {
    assertExceptionFree(get_EOS_3());
  }

  @Test
  public void testEOSTODOFree() {
    assertTODOFree(get_EOS_3());
  }

  @Test
  public void stable_EOS_3() {
    String live = get_EOS_3();
    StringBuilder gold = new StringBuilder();
    gold.append("Path:Grid_EOS_failure.a");
    gold.append("\n--ISSUES-------------------------------------------");
    gold.append("\n[{\"range\":{\"start\":{\"line\":0,\"character\":15,\"byte\":15},\"end\":{\"line\":0,\"character\":16,\"byte\":16}},\"severity\":1,\"source\":\"error\",\"message\":\"File './test_code/Grid_EOS_failure.a' failed to parse: Parser was expecting a reactive type, but got an end of stream instead. {Token: `,` @ (0,15) -> (0,16): Symbol}\",\"file\":\"./test_code/Grid_EOS_failure.a\"},{\"range\":{\"start\":{\"line\":0,\"character\":0,\"byte\":0},\"end\":{\"line\":0,\"character\":0,\"byte\":0}},\"severity\":1,\"source\":\"error\",\"message\":\"Import failed (Parse): Parser was expecting a reactive type, but got an end of stream instead. {Token: `,` @ (0,15) -> (0,16): Symbol}\"}]\"--JAVA---------------------------------------------");
    gold.append("\n");
    gold.append("\nFailedValidation");
    assertStable(live, gold);
  }
  private String cached_Simple_4 = null;
  private String get_Simple_4() {
    if (cached_Simple_4 != null) {
      return cached_Simple_4;
    }
    cached_Simple_4 = generateTestOutput(true, "Simple_4", "./test_code/Grid_Simple_success.a");
    return cached_Simple_4;
  }

  @Test
  public void testSimpleEmission() {
    assertEmissionGood(get_Simple_4());
  }

  @Test
  public void testSimpleSuccess() {
    assertLivePass(get_Simple_4());
  }

  @Test
  public void testSimpleNoFormatException() {
    assertNoFormatException(get_Simple_4());
  }

  @Test
  public void testSimpleGoodWillHappy() {
    assertGoodWillHappy(get_Simple_4());
  }

  @Test
  public void testSimpleExceptionFree() {
    assertExceptionFree(get_Simple_4());
  }

  @Test
  public void testSimpleTODOFree() {
    assertTODOFree(get_Simple_4());
  }

  @Test
  public void stable_Simple_4() {
    String live = get_Simple_4();
    StringBuilder gold = new StringBuilder();
    gold.append("Path:Grid_Simple_success.a");
    gold.append("\n--EMISSION-----------------------------------------");
    gold.append("\nEmission Success, Yay");
    gold.append("\n=FORMAT===================================================");
    gold.append("\npublic grid<int, double> heights;");
    gold.append("\npublic grid<int, bool> dungeon;");
    gold.append("\npublic formula hf = heights.flatten(0);");
    gold.append("\npublic formula dung_f = dungeon.flatten(false);");
    gold.append("\nrecord R {");
    gold.append("\n  public double h;");
    gold.append("\n  public int type;");
    gold.append("\n}");
    gold.append("\npublic grid<int, R> cells;");
    gold.append("\n@construct {");
    gold.append("\n  heights[0, 0] = 1;");
    gold.append("\n  heights[0, 4] = 2;");
    gold.append("\n  cells[0, 2].h = 4;");
    gold.append("\n  dungeon[0,1] = true;");
    gold.append("\n}");
    gold.append("\n");
    gold.append("\n==========================================================");
    gold.append("\n--ISSUES-------------------------------------------");
    gold.append("\n[]\"--JAVA---------------------------------------------");
    gold.append("\nimport ape.common.Pair;");
    gold.append("\nimport ape.runtime.async.*;");
    gold.append("\nimport ape.runtime.contracts.*;");
    gold.append("\nimport ape.runtime.delta.*;");
    gold.append("\nimport ape.runtime.exceptions.*;");
    gold.append("\nimport ape.runtime.graph.*;");
    gold.append("\nimport ape.runtime.index.*;");
    gold.append("\nimport ape.runtime.json.*;");
    gold.append("\nimport ape.runtime.natives.*;");
    gold.append("\nimport ape.runtime.natives.algo.*;");
    gold.append("\nimport ape.runtime.natives.lists.*;");
    gold.append("\nimport ape.runtime.ops.*;");
    gold.append("\nimport ape.runtime.reactives.*;");
    gold.append("\nimport ape.runtime.reactives.tables.*;");
    gold.append("\nimport ape.runtime.remote.*;");
    gold.append("\nimport ape.runtime.remote.client.*;");
    gold.append("\nimport ape.runtime.remote.replication.*;");
    gold.append("\nimport ape.runtime.stdlib.*;");
    gold.append("\nimport ape.runtime.sys.*;");
    gold.append("\nimport ape.runtime.sys.cron.*;");
    gold.append("\nimport ape.runtime.sys.web.*;");
    gold.append("\nimport ape.runtime.text.*;");
    gold.append("\nimport java.time.*;");
    gold.append("\nimport java.util.function.Consumer;");
    gold.append("\nimport java.util.function.Function;");
    gold.append("\nimport java.util.ArrayList;");
    gold.append("\nimport java.util.Comparator;");
    gold.append("\nimport java.util.HashMap;");
    gold.append("\nimport java.util.HashSet;");
    gold.append("\nimport java.util.Map;");
    gold.append("\nimport java.util.Set;");
    gold.append("\npublic class Simple_4 extends LivingDocument {");
    gold.append("\n  private final RxGrid<Integer,RxDouble> heights;");
    gold.append("\n  private final RxGrid<Integer,RxBoolean> dungeon;");
    gold.append("\n  private final RxGrid<Integer,RTxR> cells;");
    gold.append("\n  private final RxLazy<double[]> hf;");
    gold.append("\n  private final RxLazy<boolean[]> dung_f;");
    gold.append("\n  @Override");
    gold.append("\n  public long __memory() {");
    gold.append("\n    long __sum = super.__memory() + 2176;");
    gold.append("\n    __sum += heights.__memory();");
    gold.append("\n    __sum += dungeon.__memory();");
    gold.append("\n    __sum += cells.__memory();");
    gold.append("\n    __sum += hf.__memory();");
    gold.append("\n    __sum += dung_f.__memory();");
    gold.append("\n    return __sum;");
    gold.append("\n  }");
    gold.append("\n  public Simple_4(DocumentMonitor __monitor) {");
    gold.append("\n    super(__monitor);");
    gold.append("\n    heights = new RxGrid<Integer,RxDouble>(this, new RxMap.IntegerCodec<RxDouble>() { @Override public RxDouble make(RxParent __parent) { return new RxDouble(__parent, 0.0);} });");
    gold.append("\n    dungeon = new RxGrid<Integer,RxBoolean>(this, new RxMap.IntegerCodec<RxBoolean>() { @Override public RxBoolean make(RxParent __parent) { return new RxBoolean(__parent, false);} });");
    gold.append("\n    cells = new RxGrid<Integer,RTxR>(this, new RxMap.IntegerCodec<RTxR>() { @Override public RTxR make(RxParent __parent) { return new RTxR(__parent).__link();} });");
    gold.append("\n    hf = new RxLazy<double[]>(this, () -> (double[])(heights.flatten((double)(0))), null);");
    gold.append("\n    dung_f = new RxLazy<boolean[]>(this, () -> (boolean[])(dungeon.flatten(false)), null);");
    gold.append("\n    heights.__subscribe(hf);");
    gold.append("\n    dungeon.__subscribe(dung_f);");
    gold.append("\n    __goodwillBudget = 100000;");
    gold.append("\n    __goodwillLimitOfBudget = 100000;");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  public void __settle(Set<Integer> __viewers) {");
    gold.append("\n    heights.__settle(__viewers);");
    gold.append("\n    dungeon.__settle(__viewers);");
    gold.append("\n    cells.__settle(__viewers);");
    gold.append("\n    hf.__settle(__viewers);");
    gold.append("\n    dung_f.__settle(__viewers);");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  public void __insert(JsonStreamReader __reader) {");
    gold.append("\n    if (__reader.startObject()) {");
    gold.append("\n      while(__reader.notEndOfObject()) {");
    gold.append("\n        String __fieldName = __reader.fieldName();");
    gold.append("\n        switch (__fieldName) {");
    gold.append("\n          case \"heights\":");
    gold.append("\n            heights.__insert(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"dungeon\":");
    gold.append("\n            dungeon.__insert(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"cells\":");
    gold.append("\n            cells.__insert(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__state\":");
    gold.append("\n            __state.__insert(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__constructed\":");
    gold.append("\n            __constructed.__insert(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__next_time\":");
    gold.append("\n            __next_time.__insert(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__last_expire_time\":");
    gold.append("\n            __last_expire_time.__insert(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__blocked\":");
    gold.append("\n            __blocked.__insert(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__seq\":");
    gold.append("\n            __seq.__insert(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__entropy\":");
    gold.append("\n            __entropy.__insert(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__auto_future_id\":");
    gold.append("\n            __auto_future_id.__insert(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__connection_id\":");
    gold.append("\n            __connection_id.__insert(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__message_id\":");
    gold.append("\n            __message_id.__insert(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__time\":");
    gold.append("\n            __time.__insert(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__timezone\":");
    gold.append("\n            __timezone.__insert(__reader);");
    gold.append("\n            __timezoneCachedZoneId = ZoneId.of(__timezone.get());");
    gold.append("\n            break;");
    gold.append("\n          case \"__auto_table_row_id\":");
    gold.append("\n            __auto_table_row_id.__insert(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__auto_gen\":");
    gold.append("\n            __auto_gen.__insert(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__auto_cache_id\":");
    gold.append("\n            __auto_cache_id.__insert(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__cache\":");
    gold.append("\n            __cache.__insert(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__webTaskId\":");
    gold.append("\n            __webTaskId.__insert(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__dedupe\":");
    gold.append("\n            __hydrateDeduper(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__clients\":");
    gold.append("\n            __hydrateClients(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__messages\":");
    gold.append("\n            __hydrateMessages(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__enqueued\":");
    gold.append("\n            __hydrateEnqueuedTaskManager(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__webqueue\":");
    gold.append("\n            __hydrateWebQueue(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__timeouts\":");
    gold.append("\n            __hydrateTimeouts(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__replication\":");
    gold.append("\n            __hydrateReplicationEngine(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__log\":");
    gold.append("\n            __hydrateLog(__reader);");
    gold.append("\n            break;");
    gold.append("\n          default:");
    gold.append("\n            __reader.skipValue();");
    gold.append("\n        }");
    gold.append("\n      }");
    gold.append("\n    }");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  public void __patch(JsonStreamReader __reader) {");
    gold.append("\n    if (__reader.startObject()) {");
    gold.append("\n      while(__reader.notEndOfObject()) {");
    gold.append("\n        String __fieldName = __reader.fieldName();");
    gold.append("\n        switch (__fieldName) {");
    gold.append("\n          case \"heights\":");
    gold.append("\n            heights.__patch(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"dungeon\":");
    gold.append("\n            dungeon.__patch(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"cells\":");
    gold.append("\n            cells.__patch(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__state\":");
    gold.append("\n            __state.__patch(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__constructed\":");
    gold.append("\n            __constructed.__patch(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__next_time\":");
    gold.append("\n            __next_time.__patch(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__last_expire_time\":");
    gold.append("\n            __last_expire_time.__patch(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__blocked\":");
    gold.append("\n            __blocked.__patch(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__seq\":");
    gold.append("\n            __seq.__patch(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__entropy\":");
    gold.append("\n            __entropy.__patch(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__auto_future_id\":");
    gold.append("\n            __auto_future_id.__patch(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__connection_id\":");
    gold.append("\n            __connection_id.__patch(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__message_id\":");
    gold.append("\n            __message_id.__patch(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__time\":");
    gold.append("\n            __time.__patch(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__timezone\":");
    gold.append("\n            __timezone.__patch(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__auto_table_row_id\":");
    gold.append("\n            __auto_table_row_id.__patch(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__auto_gen\":");
    gold.append("\n            __auto_gen.__patch(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__auto_cache_id\":");
    gold.append("\n            __auto_cache_id.__patch(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__cache\":");
    gold.append("\n            __cache.__patch(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__webTaskId\":");
    gold.append("\n            __webTaskId.__patch(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__dedupe\":");
    gold.append("\n            __hydrateDeduper(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__clients\":");
    gold.append("\n            __hydrateClients(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__messages\":");
    gold.append("\n            __hydrateMessages(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__enqueued\":");
    gold.append("\n            __hydrateEnqueuedTaskManager(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__webqueue\":");
    gold.append("\n            __hydrateWebQueue(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__timeouts\":");
    gold.append("\n            __hydrateTimeouts(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__replication\":");
    gold.append("\n            __hydrateReplicationEngine(__reader);");
    gold.append("\n            break;");
    gold.append("\n          case \"__log\":");
    gold.append("\n            __hydrateLog(__reader);");
    gold.append("\n            break;");
    gold.append("\n          default:");
    gold.append("\n            __reader.skipValue();");
    gold.append("\n        }");
    gold.append("\n      }");
    gold.append("\n    }");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  public void __dump(JsonStreamWriter __writer) {");
    gold.append("\n    __writer.beginObject();");
    gold.append("\n    __writer.writeObjectFieldIntro(\"__snapshot\");");
    gold.append("\n    __writer.writeString(__space + \"/\" + __key);");
    gold.append("\n    __writer.writeObjectFieldIntro(\"heights\");");
    gold.append("\n    heights.__dump(__writer);");
    gold.append("\n    __writer.writeObjectFieldIntro(\"dungeon\");");
    gold.append("\n    dungeon.__dump(__writer);");
    gold.append("\n    __writer.writeObjectFieldIntro(\"cells\");");
    gold.append("\n    cells.__dump(__writer);");
    gold.append("\n    __writer.writeObjectFieldIntro(\"__state\");");
    gold.append("\n    __state.__dump(__writer);");
    gold.append("\n    __writer.writeObjectFieldIntro(\"__constructed\");");
    gold.append("\n    __constructed.__dump(__writer);");
    gold.append("\n    __writer.writeObjectFieldIntro(\"__next_time\");");
    gold.append("\n    __next_time.__dump(__writer);");
    gold.append("\n    __writer.writeObjectFieldIntro(\"__last_expire_time\");");
    gold.append("\n    __last_expire_time.__dump(__writer);");
    gold.append("\n    __writer.writeObjectFieldIntro(\"__blocked\");");
    gold.append("\n    __blocked.__dump(__writer);");
    gold.append("\n    __writer.writeObjectFieldIntro(\"__seq\");");
    gold.append("\n    __seq.__dump(__writer);");
    gold.append("\n    __writer.writeObjectFieldIntro(\"__entropy\");");
    gold.append("\n    __entropy.__dump(__writer);");
    gold.append("\n    __writer.writeObjectFieldIntro(\"__auto_future_id\");");
    gold.append("\n    __auto_future_id.__dump(__writer);");
    gold.append("\n    __writer.writeObjectFieldIntro(\"__connection_id\");");
    gold.append("\n    __connection_id.__dump(__writer);");
    gold.append("\n    __writer.writeObjectFieldIntro(\"__message_id\");");
    gold.append("\n    __message_id.__dump(__writer);");
    gold.append("\n    __writer.writeObjectFieldIntro(\"__time\");");
    gold.append("\n    __time.__dump(__writer);");
    gold.append("\n    __writer.writeObjectFieldIntro(\"__timezone\");");
    gold.append("\n    __timezone.__dump(__writer);");
    gold.append("\n    __writer.writeObjectFieldIntro(\"__auto_table_row_id\");");
    gold.append("\n    __auto_table_row_id.__dump(__writer);");
    gold.append("\n    __writer.writeObjectFieldIntro(\"__auto_gen\");");
    gold.append("\n    __auto_gen.__dump(__writer);");
    gold.append("\n    __writer.writeObjectFieldIntro(\"__auto_cache_id\");");
    gold.append("\n    __auto_cache_id.__dump(__writer);");
    gold.append("\n    __writer.writeObjectFieldIntro(\"__cache\");");
    gold.append("\n    __cache.__dump(__writer);");
    gold.append("\n    __writer.writeObjectFieldIntro(\"__webTaskId\");");
    gold.append("\n    __webTaskId.__dump(__writer);");
    gold.append("\n    __dumpDeduper(__writer);");
    gold.append("\n    __dumpClients(__writer);");
    gold.append("\n    __dumpMessages(__writer);");
    gold.append("\n    __dumpEnqueuedTaskManager(__writer);");
    gold.append("\n    __dumpTimeouts(__writer);");
    gold.append("\n    __dumpWebQueue(__writer);");
    gold.append("\n    __dumpReplicationEngine(__writer);");
    gold.append("\n    __writer.endObject();");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  public void __commit(String __name, JsonStreamWriter __forward, JsonStreamWriter __reverse) {");
    gold.append("\n    __state.__commit(\"__state\", __forward, __reverse);");
    gold.append("\n    __constructed.__commit(\"__constructed\", __forward, __reverse);");
    gold.append("\n    __next_time.__commit(\"__next_time\", __forward, __reverse);");
    gold.append("\n    __last_expire_time.__commit(\"__last_expire_time\", __forward, __reverse);");
    gold.append("\n    __blocked.__commit(\"__blocked\", __forward, __reverse);");
    gold.append("\n    __seq.__commit(\"__seq\", __forward, __reverse);");
    gold.append("\n    __entropy.__commit(\"__entropy\", __forward, __reverse);");
    gold.append("\n    __auto_future_id.__commit(\"__auto_future_id\", __forward, __reverse);");
    gold.append("\n    __connection_id.__commit(\"__connection_id\", __forward, __reverse);");
    gold.append("\n    __message_id.__commit(\"__message_id\", __forward, __reverse);");
    gold.append("\n    __time.__commit(\"__time\", __forward, __reverse);");
    gold.append("\n    __timezone.__commit(\"__timezone\", __forward, __reverse);");
    gold.append("\n    __auto_table_row_id.__commit(\"__auto_table_row_id\", __forward, __reverse);");
    gold.append("\n    __auto_gen.__commit(\"__auto_gen\", __forward, __reverse);");
    gold.append("\n    __auto_cache_id.__commit(\"__auto_cache_id\", __forward, __reverse);");
    gold.append("\n    __cache.__commit(\"__cache\", __forward, __reverse);");
    gold.append("\n    __webTaskId.__commit(\"__webTaskId\", __forward, __reverse);");
    gold.append("\n    heights.__commit(\"heights\", __forward, __reverse);");
    gold.append("\n    dungeon.__commit(\"dungeon\", __forward, __reverse);");
    gold.append("\n    cells.__commit(\"cells\", __forward, __reverse);");
    gold.append("\n    /* root */");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  public void __revert() {");
    gold.append("\n    __state.__revert();");
    gold.append("\n    __constructed.__revert();");
    gold.append("\n    __next_time.__revert();");
    gold.append("\n    __last_expire_time.__revert();");
    gold.append("\n    __blocked.__revert();");
    gold.append("\n    __seq.__revert();");
    gold.append("\n    __entropy.__revert();");
    gold.append("\n    __auto_future_id.__revert();");
    gold.append("\n    __connection_id.__revert();");
    gold.append("\n    __message_id.__revert();");
    gold.append("\n    __time.__revert();");
    gold.append("\n    __timezone.__revert();");
    gold.append("\n    __webTaskId.__revert();");
    gold.append("\n    heights.__revert();");
    gold.append("\n    dungeon.__revert();");
    gold.append("\n    cells.__revert();");
    gold.append("\n    /* root */");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  public void __writeRxReport(JsonStreamWriter __writer) { }");
    gold.append("\n  public class DeltaPrivacyCache {");
    gold.append("\n    public DeltaPrivacyCache(NtPrincipal __who) {}");
    gold.append("\n  }");
    gold.append("\n  private class DeltaSimple_4 implements DeltaNode {");
    gold.append("\n    private DGrid<Integer,DDouble> __dheights;");
    gold.append("\n    private DGrid<Integer,DBoolean> __ddungeon;");
    gold.append("\n    private DGrid<Integer,DeltaRTxR> __dcells;");
    gold.append("\n    private int __ghf;");
    gold.append("\n    private DList<DDouble> __dhf;");
    gold.append("\n    private int __gdung_f;");
    gold.append("\n    private DList<DBoolean> __ddung_f;");
    gold.append("\n    private boolean __emitted;");
    gold.append("\n    private DeltaSimple_4() {");
    gold.append("\n      __dheights = new DGrid<Integer,DDouble>();");
    gold.append("\n      __ddungeon = new DGrid<Integer,DBoolean>();");
    gold.append("\n      __dcells = new DGrid<Integer,DeltaRTxR>();");
    gold.append("\n      __ghf = -1;");
    gold.append("\n      __dhf = new DList<DDouble>();");
    gold.append("\n      __gdung_f = -1;");
    gold.append("\n      __ddung_f = new DList<DBoolean>();");
    gold.append("\n      __emitted = false;");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public long __memory() {");
    gold.append("\n      long __sum = 40;");
    gold.append("\n      __sum += __dheights.__memory();");
    gold.append("\n      __sum += __ddungeon.__memory();");
    gold.append("\n      __sum += __dcells.__memory();");
    gold.append("\n      __sum += __dhf.__memory();");
    gold.append("\n      __sum += __ddung_f.__memory();");
    gold.append("\n      return __sum;");
    gold.append("\n    }");
    gold.append("\n    public boolean show(Simple_4 __item, PrivateLazyDeltaWriter __writer) {");
    gold.append("\n      DeltaPrivacyCache __policy_cache = new DeltaPrivacyCache(__writer.who);");
    gold.append("\n      __writer.setCacheObject(__policy_cache);");
    gold.append("\n      __code_cost += 5;");
    gold.append("\n      PrivateLazyDeltaWriter __obj = __writer.planObject();");
    gold.append("\n      __obj.manifest();");
    gold.append("\n      {");
    gold.append("\n        PrivateLazyDeltaWriter __map0 = __obj.planField(\"heights\").planObject();");
    gold.append("\n        DGrid<Integer,DDouble> __deltaMap1 = __dheights;");
    gold.append("\n        DGrid<Integer,DDouble>.Walk __deltaMapWalker2 = __deltaMap1.begin();");
    gold.append("\n        for (NtPair<Pair<Integer>,RxDouble> __mapEntry3 : __item.heights) {");
    gold.append("\n          DDouble __deltaElement4 = __deltaMapWalker2.next(__mapEntry3.key, () -> new DDouble());");
    gold.append("\n          __deltaElement4.show(__mapEntry3.value.get(), __map0.planField(\"\" + __mapEntry3.key));");
    gold.append("\n        }");
    gold.append("\n        __deltaMapWalker2.end(__map0);");
    gold.append("\n        __map0.end();");
    gold.append("\n      }");
    gold.append("\n      {");
    gold.append("\n        PrivateLazyDeltaWriter __map5 = __obj.planField(\"dungeon\").planObject();");
    gold.append("\n        DGrid<Integer,DBoolean> __deltaMap6 = __ddungeon;");
    gold.append("\n        DGrid<Integer,DBoolean>.Walk __deltaMapWalker7 = __deltaMap6.begin();");
    gold.append("\n        for (NtPair<Pair<Integer>,RxBoolean> __mapEntry8 : __item.dungeon) {");
    gold.append("\n          DBoolean __deltaElement9 = __deltaMapWalker7.next(__mapEntry8.key, () -> new DBoolean());");
    gold.append("\n          __deltaElement9.show(__mapEntry8.value.get(), __map5.planField(\"\" + __mapEntry8.key));");
    gold.append("\n        }");
    gold.append("\n        __deltaMapWalker7.end(__map5);");
    gold.append("\n        __map5.end();");
    gold.append("\n      }");
    gold.append("\n      {");
    gold.append("\n        PrivateLazyDeltaWriter __map10 = __obj.planField(\"cells\").planObject();");
    gold.append("\n        DGrid<Integer,DeltaRTxR> __deltaMap11 = __dcells;");
    gold.append("\n        DGrid<Integer,DeltaRTxR>.Walk __deltaMapWalker12 = __deltaMap11.begin();");
    gold.append("\n        for (NtPair<Pair<Integer>,RTxR> __mapEntry13 : __item.cells) {");
    gold.append("\n          DeltaRTxR __deltaElement14 = __deltaMapWalker12.next(__mapEntry13.key, () -> new DeltaRTxR());");
    gold.append("\n          __deltaElement14.show(__mapEntry13.value, __map10.planField(\"\" + __mapEntry13.key));");
    gold.append("\n        }");
    gold.append("\n        __deltaMapWalker12.end(__map10);");
    gold.append("\n        __map10.end();");
    gold.append("\n      }");
    gold.append("\n      if (__ghf != __item.hf.getGeneration()) {");
    gold.append("\n        {");
    gold.append("\n          DList<DDouble> __deltaList16 = __dhf;");
    gold.append("\n          PrivateLazyDeltaWriter __list15 = __obj.planField(\"hf\").planObject();");
    gold.append("\n          int __index19 = 0;");
    gold.append("\n          for (Double __listElement17 : __item.hf.get()) {");
    gold.append("\n            DDouble __deltaElement18 = __deltaList16.getPrior(__index19, () -> new DDouble());");
    gold.append("\n            __deltaElement18.show(__listElement17, __list15.planField(__index19));");
    gold.append("\n            __index19++;");
    gold.append("\n          }");
    gold.append("\n          __deltaList16.rectify(__index19, __list15);");
    gold.append("\n          __list15.end();");
    gold.append("\n        }");
    gold.append("\n        __ghf = __item.hf.getGeneration();");
    gold.append("\n      }");
    gold.append("\n      if (__gdung_f != __item.dung_f.getGeneration()) {");
    gold.append("\n        {");
    gold.append("\n          DList<DBoolean> __deltaList21 = __ddung_f;");
    gold.append("\n          PrivateLazyDeltaWriter __list20 = __obj.planField(\"dung_f\").planObject();");
    gold.append("\n          int __index24 = 0;");
    gold.append("\n          for (Boolean __listElement22 : __item.dung_f.get()) {");
    gold.append("\n            DBoolean __deltaElement23 = __deltaList21.getPrior(__index24, () -> new DBoolean());");
    gold.append("\n            __deltaElement23.show(__listElement22, __list20.planField(__index24));");
    gold.append("\n            __index24++;");
    gold.append("\n          }");
    gold.append("\n          __deltaList21.rectify(__index24, __list20);");
    gold.append("\n          __list20.end();");
    gold.append("\n        }");
    gold.append("\n        __gdung_f = __item.dung_f.getGeneration();");
    gold.append("\n      }");
    gold.append("\n      if (__obj.end()) {");
    gold.append("\n        __emitted = true;");
    gold.append("\n      }");
    gold.append("\n      return true;");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public void clear() {");
    gold.append("\n      __dheights.clear();");
    gold.append("\n      __ddungeon.clear();");
    gold.append("\n      __dcells.clear();");
    gold.append("\n      __dhf.clear();");
    gold.append("\n      __ddung_f.clear();");
    gold.append("\n      __code_cost += 5;");
    gold.append("\n    }");
    gold.append("\n    public void hide(PrivateLazyDeltaWriter __writer) {");
    gold.append("\n      if (__emitted) {");
    gold.append("\n        clear();");
    gold.append("\n        __emitted = false;");
    gold.append("\n        __writer.writeNull();");
    gold.append("\n      }");
    gold.append("\n    }");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  public Set<String> __get_intern_strings() {");
    gold.append("\n    HashSet<String> __interns = new HashSet<>();");
    gold.append("\n    __interns.add(\"\");");
    gold.append("\n    __interns.add(\"?\");");
    gold.append("\n    return __interns;");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  public PrivateView __createPrivateView(NtPrincipal __who, Perspective ___perspective) {");
    gold.append("\n    Simple_4 __self = this;");
    gold.append("\n    DeltaSimple_4 __state = new DeltaSimple_4();");
    gold.append("\n    RTx__ViewerType __viewerState = new RTx__ViewerType();");
    gold.append("\n    int __viewId = __genViewId();");
    gold.append("\n    return new PrivateView(__viewId, __who, ___perspective) {");
    gold.append("\n      @Override");
    gold.append("\n      public long memory() {");
    gold.append("\n        return __state.__memory();");
    gold.append("\n      }");
    gold.append("\n      @Override");
    gold.append("\n      public void dumpViewer(JsonStreamWriter __writer) {");
    gold.append("\n        __viewerState.__writeOut(__writer);");
    gold.append("\n      }");
    gold.append("\n      @Override");
    gold.append("\n      public void ingest(JsonStreamReader __reader) {");
    gold.append("\n        __viewerState.__ingest(__reader);");
    gold.append("\n      }");
    gold.append("\n      @Override");
    gold.append("\n      public void update(JsonStreamWriter __writer) {");
    gold.append("\n        __state.show(__self, PrivateLazyDeltaWriter.bind(__who, __writer, __viewerState, __viewId));");
    gold.append("\n      }");
    gold.append("\n    };");
    gold.append("\n  }");
    gold.append("\n  private static class RTx__ViewerType extends NtMessageBase {");
    gold.append("\n    private final RTx__ViewerType __this;");
    gold.append("\n    @Override");
    gold.append("\n    public long __memory() { return 64; }");
    gold.append("\n    public void __reset() {}");
    gold.append("\n    public void __hash(HashBuilder __hash) {");
    gold.append("\n      __hash.hashString(\"anonymous\");");
    gold.append("\n    }");
    gold.append("\n    private static String[] __INDEX_COLUMNS___ViewerType = new String[] {};");
    gold.append("\n    @Override");
    gold.append("\n    public String[] __getIndexColumns() {");
    gold.append("\n      return __INDEX_COLUMNS___ViewerType;");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public int[] __getIndexValues() {");
    gold.append("\n      return new int[] {};");
    gold.append("\n    }");
    gold.append("\n    private RTx__ViewerType(JsonStreamReader __reader) {");
    gold.append("\n      __this = this;");
    gold.append("\n      __ingest(__reader);");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public void __ingest(JsonStreamReader __reader) {");
    gold.append("\n      __reader.mustSkipObject();");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public void __writeOut(JsonStreamWriter __writer) {");
    gold.append("\n      __writer.beginObject();");
    gold.append("\n      __writer.endObject();");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public void __parsed() throws AbortMessageException {}");
    gold.append("\n    private RTx__ViewerType() { __this = this; }");
    gold.append("\n  }");
    gold.append("\n  private class DeltaRTx__ViewerType implements DeltaNode {");
    gold.append("\n    private boolean __emitted;");
    gold.append("\n    private DeltaRTx__ViewerType() {");
    gold.append("\n      __emitted = false;");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public long __memory() {");
    gold.append("\n      long __sum = 40;");
    gold.append("\n      return __sum;");
    gold.append("\n    }");
    gold.append("\n    public void show(RTx__ViewerType __item, PrivateLazyDeltaWriter __writer) {");
    gold.append("\n      PrivateLazyDeltaWriter __obj = __writer.planObject();");
    gold.append("\n      if (__obj.end()) {");
    gold.append("\n        __emitted = true;");
    gold.append("\n      }");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public void clear() {");
    gold.append("\n      __code_cost += 0;");
    gold.append("\n    }");
    gold.append("\n    public void hide(PrivateLazyDeltaWriter __writer) {");
    gold.append("\n      if (__emitted) {");
    gold.append("\n        clear();");
    gold.append("\n        __emitted = false;");
    gold.append("\n        __writer.writeNull();");
    gold.append("\n      }");
    gold.append("\n    }");
    gold.append("\n  }");
    gold.append("\n  private static String[] __INDEX_COLUMNS_R = new String[] {};");
    gold.append("\n  private class RTxR extends RxRecordBase<RTxR> {");
    gold.append("\n    private final RTxR __this;");
    gold.append("\n    private final RxInt32 id;");
    gold.append("\n    private final RxDouble h;");
    gold.append("\n    private final RxInt32 type;");
    gold.append("\n    private RTxR(RxParent __owner) {");
    gold.append("\n      super(__owner);");
    gold.append("\n      this.__this = this;");
    gold.append("\n      id = new RxInt32(this, 0);");
    gold.append("\n      h = new RxDouble(this, 0.0);");
    gold.append("\n      type = new RxInt32(this, 0);");
    gold.append("\n      if (__owner instanceof RxTable) {");
    gold.append("\n        /* ok */");
    gold.append("\n      } else {");
    gold.append("\n        /* ok */");
    gold.append("\n      }");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public long __memory() {");
    gold.append("\n      long __sum = super.__memory() + 0;");
    gold.append("\n      __sum += id.__memory();");
    gold.append("\n      __sum += h.__memory();");
    gold.append("\n      __sum += type.__memory();");
    gold.append("\n      return __sum;");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public String[] __getIndexColumns() {");
    gold.append("\n      return __INDEX_COLUMNS_R;");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public int[] __getIndexValues() {");
    gold.append("\n      return new int[] {};");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public Object __fieldOf(String __name) {");
    gold.append("\n      switch (__name) {");
    gold.append("\n        case \"id\":");
    gold.append("\n          return id;");
    gold.append("\n        case \"h\":");
    gold.append("\n          return h;");
    gold.append("\n        case \"type\":");
    gold.append("\n          return type;");
    gold.append("\n        default:");
    gold.append("\n          return null;");
    gold.append("\n      }");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public void __settle(Set<Integer> __viewers) {");
    gold.append("\n      __lowerInvalid();");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public void __insert(JsonStreamReader __reader) {");
    gold.append("\n      if (__reader.startObject()) {");
    gold.append("\n        while(__reader.notEndOfObject()) {");
    gold.append("\n          String __fieldName = __reader.fieldName();");
    gold.append("\n          switch (__fieldName) {");
    gold.append("\n            case \"id\":");
    gold.append("\n              id.__insert(__reader);");
    gold.append("\n              break;");
    gold.append("\n            case \"h\":");
    gold.append("\n              h.__insert(__reader);");
    gold.append("\n              break;");
    gold.append("\n            case \"type\":");
    gold.append("\n              type.__insert(__reader);");
    gold.append("\n              break;");
    gold.append("\n            default:");
    gold.append("\n              __reader.skipValue();");
    gold.append("\n          }");
    gold.append("\n        }");
    gold.append("\n      }");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public void __patch(JsonStreamReader __reader) {");
    gold.append("\n      if (__reader.startObject()) {");
    gold.append("\n        while(__reader.notEndOfObject()) {");
    gold.append("\n          String __fieldName = __reader.fieldName();");
    gold.append("\n          switch (__fieldName) {");
    gold.append("\n            case \"id\":");
    gold.append("\n              id.__patch(__reader);");
    gold.append("\n              break;");
    gold.append("\n            case \"h\":");
    gold.append("\n              h.__patch(__reader);");
    gold.append("\n              break;");
    gold.append("\n            case \"type\":");
    gold.append("\n              type.__patch(__reader);");
    gold.append("\n              break;");
    gold.append("\n            default:");
    gold.append("\n              __reader.skipValue();");
    gold.append("\n          }");
    gold.append("\n        }");
    gold.append("\n      }");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public void __dump(JsonStreamWriter __writer) {");
    gold.append("\n      __writer.beginObject();");
    gold.append("\n      __writer.writeObjectFieldIntro(\"id\");");
    gold.append("\n      id.__dump(__writer);");
    gold.append("\n      __writer.writeObjectFieldIntro(\"h\");");
    gold.append("\n      h.__dump(__writer);");
    gold.append("\n      __writer.writeObjectFieldIntro(\"type\");");
    gold.append("\n      type.__dump(__writer);");
    gold.append("\n      __writer.endObject();");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public void __commit(String __name, JsonStreamWriter __forward, JsonStreamWriter __reverse) {");
    gold.append("\n      if (__isDirty()) {");
    gold.append("\n        __forward.writeObjectFieldIntro(__name);");
    gold.append("\n        __forward.beginObject();");
    gold.append("\n        __reverse.writeObjectFieldIntro(__name);");
    gold.append("\n        __reverse.beginObject();");
    gold.append("\n        id.__commit(\"id\", __forward, __reverse);");
    gold.append("\n        h.__commit(\"h\", __forward, __reverse);");
    gold.append("\n        type.__commit(\"type\", __forward, __reverse);");
    gold.append("\n        __forward.endObject();");
    gold.append("\n        __reverse.endObject();");
    gold.append("\n        __lowerDirtyCommit();");
    gold.append("\n      }");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public void __revert() {");
    gold.append("\n      if (__isDirty()) {");
    gold.append("\n        __isDying = false;");
    gold.append("\n        id.__revert();");
    gold.append("\n        h.__revert();");
    gold.append("\n        type.__revert();");
    gold.append("\n        __lowerDirtyRevert();");
    gold.append("\n      }");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public void __killFields() {}");
    gold.append("\n    @Override");
    gold.append("\n    public void __writeRxReport(JsonStreamWriter __writer) { }");
    gold.append("\n    @Override");
    gold.append("\n    public RTxR __link() {");
    gold.append("\n      return this;");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public void __invalidateIndex(TablePubSub __pubsub) {}");
    gold.append("\n    @Override");
    gold.append("\n    public void __pumpIndexEvents(TablePubSub __pubsub) {}");
    gold.append("\n    @Override");
    gold.append("\n    public String __name() {");
    gold.append("\n      return \"R\";");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public void __deindex() {");
    gold.append("\n      /* ok */");
    gold.append("\n    }");
    gold.append("\n    public void __reindex() {");
    gold.append("\n      /* ok */");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public int __id() {");
    gold.append("\n      return id.get();");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public void __setId(int __id, boolean __force) {");
    gold.append("\n      if (__force) {");
    gold.append("\n        id.forceSet(__id);");
    gold.append("\n      } else {");
    gold.append("\n        id.set(__id);");
    gold.append("\n      }");
    gold.append("\n    }");
    gold.append("\n  }");
    gold.append("\n  private class DeltaRTxR implements DeltaNode {");
    gold.append("\n    private DDouble __dh;");
    gold.append("\n    private DInt32 __dtype;");
    gold.append("\n    private boolean __emitted;");
    gold.append("\n    private DeltaRTxR() {");
    gold.append("\n      __dh = new DDouble();");
    gold.append("\n      __dtype = new DInt32();");
    gold.append("\n      __emitted = false;");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public long __memory() {");
    gold.append("\n      long __sum = 40;");
    gold.append("\n      __sum += __dh.__memory();");
    gold.append("\n      __sum += __dtype.__memory();");
    gold.append("\n      return __sum;");
    gold.append("\n    }");
    gold.append("\n    public boolean show(RTxR __item, PrivateLazyDeltaWriter __writer) {");
    gold.append("\n      DeltaPrivacyCache __policy_cache = (DeltaPrivacyCache) __writer.getCacheObject();");
    gold.append("\n      __code_cost += 2;");
    gold.append("\n      PrivateLazyDeltaWriter __obj = __writer.planObject();");
    gold.append("\n      __dh.show(__item.h.get(), __obj.planField(\"h\"));");
    gold.append("\n      __dtype.show(__item.type.get(), __obj.planField(\"type\"));");
    gold.append("\n      if (__obj.end()) {");
    gold.append("\n        __emitted = true;");
    gold.append("\n      }");
    gold.append("\n      return true;");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public void clear() {");
    gold.append("\n      __dh.clear();");
    gold.append("\n      __dtype.clear();");
    gold.append("\n      __code_cost += 2;");
    gold.append("\n    }");
    gold.append("\n    public void hide(PrivateLazyDeltaWriter __writer) {");
    gold.append("\n      if (__emitted) {");
    gold.append("\n        clear();");
    gold.append("\n        __emitted = false;");
    gold.append("\n        __writer.writeNull();");
    gold.append("\n      }");
    gold.append("\n    }");
    gold.append("\n  }");
    gold.append("\n  class DynCmp_RTxR implements Comparator<RTxR> {");
    gold.append("\n    private final CompareField[] parsed;");
    gold.append("\n    DynCmp_RTxR(String instructions) {");
    gold.append("\n      this.parsed = DynCompareParser.parse(instructions);");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public int compare(RTxR __a, RTxR __b) {");
    gold.append("\n      for (CompareField field : parsed) {");
    gold.append("\n        int delta = 0;");
    gold.append("\n        switch (field.name) {");
    gold.append("\n          case \"id\":");
    gold.append("\n            delta = __a.id.compareTo(__b.id);");
    gold.append("\n            break;");
    gold.append("\n          case \"h\":");
    gold.append("\n            delta = __a.h.compareTo(__b.h);");
    gold.append("\n            break;");
    gold.append("\n          case \"type\":");
    gold.append("\n            delta = __a.type.compareTo(__b.type);");
    gold.append("\n            break;");
    gold.append("\n        }");
    gold.append("\n        if (delta != 0) {");
    gold.append("\n          return field.desc ? -delta : delta;");
    gold.append("\n        }");
    gold.append("\n      }");
    gold.append("\n      return 0;");
    gold.append("\n    }");
    gold.append("\n  }");
    gold.append("\n  public static HashMap<String, HashMap<String, Object>> __services() {");
    gold.append("\n    HashMap<String, HashMap<String, Object>> __map = new HashMap<>();");
    gold.append("\n    return __map;");
    gold.append("\n  }");
    gold.append("\n  public static void __create_generic_clients(ServiceRegistry __registry, HeaderDecryptor decryptor) throws Exception {}");
    gold.append("\n  @Override");
    gold.append("\n  public void __link(ServiceRegistry __registry) {}");
    gold.append("\n  @Override");
    gold.append("\n  public Service __findService(String __name) { return null; }");
    gold.append("\n  @Override");
    gold.append("\n  public String __getViewStateFilter() {");
    gold.append("\n    return \"[]\";");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  protected boolean __is_direct_channel(String channel) {");
    gold.append("\n    return false;");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  protected void __handle_direct(CoreRequestContext context, String channel, Object __message) throws AbortMessageException {");
    gold.append("\n    return;");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  protected void __route(AsyncTask task) {");
    gold.append("\n    return;");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  protected Object __parse_message(String channel, JsonStreamReader __reader) {");
    gold.append("\n    __reader.skipValue();");
    gold.append("\n    return NtMessageBase.NULL;");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  protected void __reset_future_queues() {");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  public String __metrics() { return \"{}\"; }");
    gold.append("\n  @Override");
    gold.append("\n  public String __traffic(CoreRequestContext __context) { return \"\"; }");
    gold.append("\n  @Override");
    gold.append("\n  public void __debug(JsonStreamWriter __writer) {}");
    gold.append("\n  @Override");
    gold.append("\n  protected long __computeGraphs() { return 0; }");
    gold.append("\n  @Override");
    gold.append("\n  public boolean __open_channel(String name) {");
    gold.append("\n    return false;");
    gold.append("\n  }");
    gold.append("\n  public AuthResponse __authpipe(CoreRequestContext __context, String __message) {");
    gold.append("\n    return null;");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  public void __make_cron_progress() {}");
    gold.append("\n  @Override");
    gold.append("\n  protected void __reset_cron() {}");
    gold.append("\n  @Override");
    gold.append("\n  public Long __predict_cron_wake_time() { return null; }");
    gold.append("\n  @Override");
    gold.append("\n  protected WebResponse __get_internal(CoreRequestContext __context, WebGet __request) throws AbortMessageException {");
    gold.append("\n    WebPath __path = new WebPath(__request.uri);");
    gold.append("\n    return null;");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  protected WebResponse __put_internal(CoreRequestContext __context, WebPut __request) throws AbortMessageException {");
    gold.append("\n    WebPath __path = new WebPath(__request.uri);");
    gold.append("\n    return null;");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  protected WebResponse __delete_internal(CoreRequestContext __context, WebDelete __request) throws AbortMessageException {");
    gold.append("\n    WebPath __path = new WebPath(__request.uri);");
    gold.append("\n    return null;");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  public WebResponse __options(CoreRequestContext __context, WebGet __request) {");
    gold.append("\n    WebPath __path = new WebPath(__request.uri);");
    gold.append("\n    return null;");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  protected void __invoke_label(String __new_state) {}");
    gold.append("\n  public static boolean __onCanCreate(CoreRequestContext __context) {");
    gold.append("\n    return false;");
    gold.append("\n  }");
    gold.append("\n  public static boolean __onCanInvent(CoreRequestContext __context) {");
    gold.append("\n    return false;");
    gold.append("\n  }");
    gold.append("\n  public static boolean __onCanSendWhileDisconnected(CoreRequestContext __context) {");
    gold.append("\n    return false;");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  public void __onLoad() {}");
    gold.append("\n  @Override");
    gold.append("\n  public boolean __onCanAssetAttached(CoreRequestContext __cvalue) {");
    gold.append("\n    return false;");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  public void __onAssetAttached(CoreRequestContext __cvalue, NtAsset __pvalue) {}");
    gold.append("\n  @Override");
    gold.append("\n  public boolean __delete(CoreRequestContext __cvalue) {");
    gold.append("\n    return false;");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  public boolean __onConnected(CoreRequestContext __cvalue) {");
    gold.append("\n    return false;");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  public void __onDisconnected(CoreRequestContext __cvalue) {}");
    gold.append("\n  public static HashMap<String, Object> __config() {");
    gold.append("\n    HashMap<String, Object> __map = new HashMap<>();");
    gold.append("\n    return __map;");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  public String[] __getTests() {");
    gold.append("\n    return new String[] {};");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  public void __test(TestReportBuilder report, String testName) throws AbortMessageException {}");
    gold.append("\n  private void __construct_0(CoreRequestContext __context, NtPrincipal __who, NtMessageBase __object) {");
    gold.append("\n    __code_cost += 5;");
    gold.append("\n    __track(0);");
    gold.append("\n    heights.lookup(0, 0).set(1);");
    gold.append("\n    __track(1);");
    gold.append("\n    heights.lookup(0, 4).set(2);");
    gold.append("\n    __track(2);");
    gold.append("\n    cells.lookup(0, 2).h.set(4);");
    gold.append("\n    __track(3);");
    gold.append("\n    dungeon.lookup(0, 1).set(true);");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  protected NtMessageBase __parse_construct_arg(JsonStreamReader __reader) {");
    gold.append("\n    __reader.skipValue();");
    gold.append("\n    return NtMessageBase.NULL;");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  protected void __construct_intern(CoreRequestContext __context, NtMessageBase __object) {");
    gold.append("\n    __construct_0(__context, __context.who, __object);");
    gold.append("\n  }");
    gold.append("\n  /* end of file */");
    gold.append("\n}");
    gold.append("\n");
    gold.append("\n--=[LivingDocumentFactory COMPILING]=---");
    gold.append("\n--=[LivingDocumentFactory MADE]=---");
    gold.append("\n--REFLECTION RESULTS-------------------------------------");
    gold.append("\n{\"types\":{\"__Root\":{\"nature\":\"reactive_record\",\"name\":\"Root\",\"fields\":{\"heights\":{\"type\":{\"nature\":\"reactive_grid\",\"domain\":{\"nature\":\"native_value\",\"type\":\"int\"},\"range\":{\"nature\":\"reactive_value\",\"type\":\"double\"}},\"computed\":false,\"privacy\":\"public\"},\"dungeon\":{\"type\":{\"nature\":\"reactive_grid\",\"domain\":{\"nature\":\"native_value\",\"type\":\"int\"},\"range\":{\"nature\":\"reactive_value\",\"type\":\"bool\"}},\"computed\":false,\"privacy\":\"public\"},\"hf\":{\"type\":{\"nature\":\"native_array\",\"type\":{\"nature\":\"native_value\",\"type\":\"double\"}},\"computed\":true,\"privacy\":\"public\"},\"dung_f\":{\"type\":{\"nature\":\"native_array\",\"type\":{\"nature\":\"native_value\",\"type\":\"bool\"}},\"computed\":true,\"privacy\":\"public\"},\"cells\":{\"type\":{\"nature\":\"reactive_grid\",\"domain\":{\"nature\":\"native_value\",\"type\":\"int\"},\"range\":{\"nature\":\"reactive_ref\",\"ref\":\"R\"}},\"computed\":false,\"privacy\":\"public\"}}},\"__ViewerType\":{\"nature\":\"native_message\",\"name\":\"__ViewerType\",\"anonymous\":true,\"fields\":{}},\"R\":{\"nature\":\"reactive_record\",\"name\":\"R\",\"fields\":{\"id\":{\"type\":{\"nature\":\"reactive_value\",\"type\":\"int\"},\"computed\":false,\"privacy\":\"private\"},\"h\":{\"type\":{\"nature\":\"reactive_value\",\"type\":\"double\"},\"computed\":false,\"privacy\":\"public\"},\"type\":{\"type\":{\"nature\":\"reactive_value\",\"type\":\"int\"},\"computed\":false,\"privacy\":\"public\"}}}},\"channels\":{},\"channels-privacy\":{},\"constructors\":[],\"labels\":[]}");
    gold.append("\n--JAVA RUNNING-------------------------------------");
    gold.append("\n{\"command\":\"construct\",\"timestamp\":\"0\",\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"arg\":{},\"entropy\":\"0\",\"key\":\"0\",\"origin\":\"origin\",\"ip\":\"ip\"}-->{\"__constructed\":true,\"__entropy\":\"-4962768465676381896\",\"heights\":{\"0:0\":1.0,\"0:4\":2.0},\"dungeon\":{\"0:1\":true},\"cells\":{\"0:2\":{\"h\":4.0}},\"__seq\":1} need:false in:0");
    gold.append("\n{\"command\":\"invalidate\",\"timestamp\":\"25\"}-->{\"__seq\":2,\"__entropy\":\"4804307197456638271\",\"__time\":\"25\"} need:false in:0");
    gold.append("\nCPU:45");
    gold.append("\nMEMORY:4516");
    gold.append("\n{\"command\":\"invalidate\",\"timestamp\":\"50\"}-->{\"__seq\":3,\"__entropy\":\"-1034601897293430941\",\"__time\":\"50\"} need:false in:0");
    gold.append("\nNO_ONE: CREATED PRIVATE VIEW");
    gold.append("\n+ NO_ONE DELTA:{\"data\":{\"heights\":{\"0:0\":1.0,\"0:4\":2.0},\"dungeon\":{\"0:1\":true},\"cells\":{\"0:2\":{\"h\":4.0,\"type\":0}},\"hf\":{\"0\":1.0,\"1\":0.0,\"2\":0.0,\"3\":0.0,\"4\":2.0,\"@s\":5},\"dung_f\":{\"0\":false,\"1\":true,\"@s\":2}},\"seq\":3}");
    gold.append("\nNO_ONE|FAILURE:184333");
    gold.append("\n{\"command\":\"invalidate\",\"timestamp\":\"75\"}-->{\"__seq\":4,\"__entropy\":\"7848011421992302230\",\"__time\":\"75\"} need:false in:0");
    gold.append("\nRANDO: CREATED PRIVATE VIEW");
    gold.append("\n+ NO_ONE DELTA:{\"seq\":4}");
    gold.append("\n+ RANDO DELTA:{\"data\":{\"heights\":{\"0:0\":1.0,\"0:4\":2.0},\"dungeon\":{\"0:1\":true},\"cells\":{\"0:2\":{\"h\":4.0,\"type\":0}},\"hf\":{\"0\":1.0,\"1\":0.0,\"2\":0.0,\"3\":0.0,\"4\":2.0,\"@s\":5},\"dung_f\":{\"0\":false,\"1\":true,\"@s\":2}},\"seq\":4}");
    gold.append("\nRANDO|FAILURE:184333");
    gold.append("\n{\"command\":\"invalidate\",\"timestamp\":\"100\"}-->{\"__seq\":5,\"__entropy\":\"-8929183248358367000\",\"__time\":\"100\"} need:false in:0");
    gold.append("\nRANDO|SUCCESS:5");
    gold.append("\n+ NO_ONE DELTA:{\"seq\":5}");
    gold.append("\n+ RANDO DELTA:{\"seq\":5}");
    gold.append("\nMEMORY:6746");
    gold.append("\n--JAVA RESULTS-------------------------------------");
    gold.append("\n{\"__snapshot\":\"0/0\",\"heights\":{\"0:0\":1.0,\"0:4\":2.0},\"dungeon\":{\"0:1\":true},\"cells\":{\"0:2\":{\"id\":0,\"h\":4.0,\"type\":0}},\"__state\":\"\",\"__constructed\":true,\"__next_time\":\"0\",\"__last_expire_time\":\"0\",\"__blocked\":false,\"__seq\":5,\"__entropy\":\"-8929183248358367000\",\"__auto_future_id\":0,\"__connection_id\":0,\"__message_id\":0,\"__time\":\"100\",\"__timezone\":\"UTC\",\"__auto_table_row_id\":0,\"__auto_gen\":0,\"__auto_cache_id\":0,\"__cache\":{},\"__webTaskId\":0,\"__webqueue\":{},\"__replication\":{}}");
    gold.append("\n--DUMP RESULTS-------------------------------------");
    gold.append("\n{}");
    gold.append("\n--METRIC RESULTS-----------------------------------");
    gold.append("\n{\"__snapshot\":\"0/0\",\"heights\":{\"0:0\":1.0,\"0:4\":2.0},\"dungeon\":{\"0:1\":true},\"cells\":{\"0:2\":{\"id\":0,\"h\":4.0,\"type\":0}},\"__state\":\"\",\"__constructed\":true,\"__next_time\":\"0\",\"__last_expire_time\":\"0\",\"__blocked\":false,\"__seq\":5,\"__entropy\":\"-8929183248358367000\",\"__auto_future_id\":0,\"__connection_id\":0,\"__message_id\":0,\"__time\":\"100\",\"__timezone\":\"UTC\",\"__auto_table_row_id\":0,\"__auto_gen\":0,\"__auto_cache_id\":0,\"__cache\":{},\"__webTaskId\":0,\"__webqueue\":{},\"__replication\":{}}");
    gold.append("\n{\"__snapshot\":\"0/0\",\"heights\":{\"0:0\":1.0,\"0:4\":2.0},\"dungeon\":{\"0:1\":true},\"cells\":{\"0:2\":{\"id\":0,\"h\":4.0,\"type\":0}},\"__state\":\"\",\"__constructed\":true,\"__next_time\":\"0\",\"__last_expire_time\":\"0\",\"__blocked\":false,\"__seq\":5,\"__entropy\":\"-8929183248358367000\",\"__auto_future_id\":0,\"__connection_id\":0,\"__message_id\":0,\"__time\":\"100\",\"__timezone\":\"UTC\",\"__auto_table_row_id\":0,\"__auto_gen\":0,\"__auto_cache_id\":0,\"__cache\":{},\"__webTaskId\":0,\"__webqueue\":{},\"__replication\":{}}");
    gold.append("\n--JAVA TEST RESULTS--------------------------------");
    gold.append("\n");
    gold.append("\nSuccess");
    assertStable(live, gold);
  }
}
