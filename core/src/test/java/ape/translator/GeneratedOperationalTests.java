/*
* Adama Platform and Language
* Copyright (C) 2021 - 2025 by Adama Platform Engineering, LLC
* 
* This program is free software for non-commercial purposes: 
* you can redistribute it and/or modify it under the terms of the 
* GNU Affero General Public License as published by the Free Software Foundation,
* either version 3 of the License, or (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
* 
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package ape.translator;

import org.junit.Test;

public class GeneratedOperationalTests extends GeneratedBase {
  private String cached_Goodwill_1 = null;
  private String get_Goodwill_1() {
    if (cached_Goodwill_1 != null) {
      return cached_Goodwill_1;
    }
    cached_Goodwill_1 = generateTestOutput(true, "Goodwill_1", "./test_code/Operational_Goodwill_success.a");
    return cached_Goodwill_1;
  }

  @Test
  public void testGoodwillEmission() {
    assertEmissionGood(get_Goodwill_1());
  }

  @Test
  public void testGoodwillSuccess() {
    assertLivePass(get_Goodwill_1());
  }

  @Test
  public void testGoodwillNoFormatException() {
    assertNoFormatException(get_Goodwill_1());
  }

  @Test
  public void testGoodwillGoodWillHappy() {
    assertGoodWillHappy(get_Goodwill_1());
  }

  @Test
  public void testGoodwillExceptionFree() {
    assertExceptionFree(get_Goodwill_1());
  }

  @Test
  public void testGoodwillTODOFree() {
    assertTODOFree(get_Goodwill_1());
  }

  @Test
  public void stable_Goodwill_1() {
    String live = get_Goodwill_1();
    StringBuilder gold = new StringBuilder();
    gold.append("Path:Operational_Goodwill_success.a");
    gold.append("\n--EMISSION-----------------------------------------");
    gold.append("\nEmission Success, Yay");
    gold.append("\n=FORMAT===================================================");
    gold.append("\nint z;");
    gold.append("\n@construct {");
    gold.append("\n  transition #setup in 0.01;");
    gold.append("\n}");
    gold.append("\n#setup {");
    gold.append("\n  z = 0;");
    gold.append("\n  while(true) {");
    gold.append("\n    z++;");
    gold.append("\n  }");
    gold.append("\n");
    gold.append("\n}");
    gold.append("\n");
    gold.append("\n==========================================================");
    gold.append("\n--ISSUES-------------------------------------------");
    gold.append("\n[]\"--JAVA---------------------------------------------");
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
    gold.append("\npublic class Goodwill_1 extends LivingDocument {");
    gold.append("\n  private final RxInt32 z;");
    gold.append("\n  @Override");
    gold.append("\n  public long __memory() {");
    gold.append("\n    long __sum = super.__memory() + 2176;");
    gold.append("\n    __sum += z.__memory();");
    gold.append("\n    return __sum;");
    gold.append("\n  }");
    gold.append("\n  public Goodwill_1(DocumentMonitor __monitor) {");
    gold.append("\n    super(__monitor);");
    gold.append("\n    z = new RxInt32(this, 0);");
    gold.append("\n    __goodwillBudget = 100000;");
    gold.append("\n    __goodwillLimitOfBudget = 100000;");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  public void __settle(Set<Integer> __viewers) {");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  public void __insert(JsonStreamReader __reader) {");
    gold.append("\n    if (__reader.startObject()) {");
    gold.append("\n      while(__reader.notEndOfObject()) {");
    gold.append("\n        String __fieldName = __reader.fieldName();");
    gold.append("\n        switch (__fieldName) {");
    gold.append("\n          case \"z\":");
    gold.append("\n            z.__insert(__reader);");
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
    gold.append("\n          case \"z\":");
    gold.append("\n            z.__patch(__reader);");
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
    gold.append("\n    __writer.writeObjectFieldIntro(\"z\");");
    gold.append("\n    z.__dump(__writer);");
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
    gold.append("\n    z.__commit(\"z\", __forward, __reverse);");
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
    gold.append("\n    __auto_table_row_id.__revert();");
    gold.append("\n    __webTaskId.__revert();");
    gold.append("\n    z.__revert();");
    gold.append("\n    /* root */");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  public void __writeRxReport(JsonStreamWriter __writer) { }");
    gold.append("\n  public class DeltaPrivacyCache {");
    gold.append("\n    public DeltaPrivacyCache(NtPrincipal __who) {}");
    gold.append("\n  }");
    gold.append("\n  private class DeltaGoodwill_1 implements DeltaNode {");
    gold.append("\n    private boolean __emitted;");
    gold.append("\n    private DeltaGoodwill_1() {");
    gold.append("\n      __emitted = false;");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public long __memory() {");
    gold.append("\n      long __sum = 40;");
    gold.append("\n      return __sum;");
    gold.append("\n    }");
    gold.append("\n    public boolean show(Goodwill_1 __item, PrivateLazyDeltaWriter __writer) {");
    gold.append("\n      DeltaPrivacyCache __policy_cache = new DeltaPrivacyCache(__writer.who);");
    gold.append("\n      __writer.setCacheObject(__policy_cache);");
    gold.append("\n      PrivateLazyDeltaWriter __obj = __writer.planObject();");
    gold.append("\n      __obj.manifest();");
    gold.append("\n      if (__obj.end()) {");
    gold.append("\n        __emitted = true;");
    gold.append("\n      }");
    gold.append("\n      return true;");
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
    gold.append("\n  @Override");
    gold.append("\n  public Set<String> __get_intern_strings() {");
    gold.append("\n    HashSet<String> __interns = new HashSet<>();");
    gold.append("\n    __interns.add(\"\");");
    gold.append("\n    __interns.add(\"?\");");
    gold.append("\n    return __interns;");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  public PrivateView __createPrivateView(NtPrincipal __who, Perspective ___perspective) {");
    gold.append("\n    Goodwill_1 __self = this;");
    gold.append("\n    DeltaGoodwill_1 __state = new DeltaGoodwill_1();");
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
    gold.append("\n  public static HashMap<String, HashMap<String, Object>> __services() {");
    gold.append("\n    HashMap<String, HashMap<String, Object>> __map = new HashMap<>();");
    gold.append("\n    return __map;");
    gold.append("\n  }");
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
    gold.append("\n  @Override");
    gold.append("\n  public String __auth(CoreRequestContext __context, String username, String password) {");
    gold.append("\n    return null;");
    gold.append("\n  }");
    gold.append("\n  public AuthResponse __authpipe(CoreRequestContext __context, String __message) {");
    gold.append("\n    return null;");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  public void __password(CoreRequestContext __context, String __pw) {}");
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
    gold.append("\n  private void __step_setup() {");
    gold.append("\n    __code_cost += 3;");
    gold.append("\n    __track(0);");
    gold.append("\n    z.set(0);");
    gold.append("\n    __track(1);");
    gold.append("\n    while (__goodwill(8, 8, 8, 12) && (true)) {");
    gold.append("\n      __code_cost += 2;");
    gold.append("\n      __track(2);");
    gold.append("\n      z.bumpUpPost();");
    gold.append("\n    }");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  protected void __invoke_label(String __new_state) {");
    gold.append("\n    switch(__new_state) {");
    gold.append("\n      case \"setup\":");
    gold.append("\n        __step_setup();");
    gold.append("\n        return;");
    gold.append("\n    }");
    gold.append("\n  }");
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
    gold.append("\n    __code_cost += 2;");
    gold.append("\n    __track(3);");
    gold.append("\n    __transitionStateMachine(\"setup\", 0.01);");
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
    gold.append("\n{\"types\":{\"__Root\":{\"nature\":\"reactive_record\",\"name\":\"Root\",\"fields\":{\"z\":{\"type\":{\"nature\":\"reactive_value\",\"type\":\"int\"},\"computed\":false,\"privacy\":\"private\"}}},\"__ViewerType\":{\"nature\":\"native_message\",\"name\":\"__ViewerType\",\"anonymous\":true,\"fields\":{}}},\"channels\":{},\"channels-privacy\":{},\"constructors\":[],\"labels\":[\"setup\"]}");
    gold.append("\n--JAVA RUNNING-------------------------------------");
    gold.append("\n{\"command\":\"construct\",\"timestamp\":\"0\",\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"arg\":{},\"entropy\":\"0\",\"key\":\"0\",\"origin\":\"origin\",\"ip\":\"ip\"}-->{\"__state\":\"setup\",\"__constructed\":true,\"__next_time\":\"10\",\"__entropy\":\"-4962768465676381896\",\"__messages\":null,\"__seq\":1} need:true in:25");
    gold.append("\nCPU:200003");
    gold.append("\nMEMORY:2608");
    gold.append("\nNO_ONE: FAILED PRIVATE VIEW DUE TO:950384");
    gold.append("\nNO_ONE|FAILURE:184333");
    gold.append("\nRANDO: FAILED PRIVATE VIEW DUE TO:950384");
    gold.append("\nRANDO|FAILURE:184333");
    gold.append("\nRANDO|FAILURE:950384");
    gold.append("\nMEMORY:2726");
    gold.append("\n--JAVA RESULTS-------------------------------------");
    gold.append("\n{\"__snapshot\":\"0/0\",\"z\":0,\"__state\":\"setup\",\"__constructed\":true,\"__next_time\":\"10\",\"__last_expire_time\":\"0\",\"__blocked\":false,\"__seq\":1,\"__entropy\":\"-4962768465676381896\",\"__auto_future_id\":0,\"__connection_id\":0,\"__message_id\":0,\"__time\":\"0\",\"__timezone\":\"UTC\",\"__auto_table_row_id\":0,\"__auto_gen\":0,\"__auto_cache_id\":0,\"__cache\":{},\"__webTaskId\":0,\"__webqueue\":{},\"__replication\":{}}");
    gold.append("\n--DUMP RESULTS-------------------------------------");
    gold.append("\n{}");
    gold.append("\n--METRIC RESULTS-----------------------------------");
    gold.append("\n{\"__snapshot\":\"0/0\",\"z\":0,\"__state\":\"setup\",\"__constructed\":true,\"__next_time\":\"10\",\"__last_expire_time\":\"0\",\"__blocked\":false,\"__seq\":1,\"__entropy\":\"-4962768465676381896\",\"__auto_future_id\":0,\"__connection_id\":0,\"__message_id\":0,\"__time\":\"0\",\"__timezone\":\"UTC\",\"__auto_table_row_id\":0,\"__auto_gen\":0,\"__auto_cache_id\":0,\"__cache\":{},\"__webTaskId\":0,\"__webqueue\":{},\"__replication\":{}}");
    gold.append("\n{\"__snapshot\":\"0/0\",\"z\":0,\"__state\":\"setup\",\"__constructed\":true,\"__next_time\":\"10\",\"__last_expire_time\":\"0\",\"__blocked\":false,\"__seq\":1,\"__entropy\":\"-4962768465676381896\",\"__auto_future_id\":0,\"__connection_id\":0,\"__message_id\":0,\"__time\":\"0\",\"__timezone\":\"UTC\",\"__auto_table_row_id\":0,\"__auto_gen\":0,\"__auto_cache_id\":0,\"__cache\":{},\"__webTaskId\":0,\"__webqueue\":{},\"__replication\":{}}");
    gold.append("\n--JAVA TEST RESULTS--------------------------------");
    gold.append("\n");
    gold.append("\nSuccess");
    assertStable(live, gold);
  }
}
