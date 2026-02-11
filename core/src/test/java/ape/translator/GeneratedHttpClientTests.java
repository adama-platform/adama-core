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
public class GeneratedHttpClientTests extends GeneratedBase {
  private String cached_DuplicateHeader_1 = null;
  private String get_DuplicateHeader_1() {
    if (cached_DuplicateHeader_1 != null) {
      return cached_DuplicateHeader_1;
    }
    cached_DuplicateHeader_1 = generateTestOutput(false, "DuplicateHeader_1", "./test_code/HttpClient_DuplicateHeader_failure.a");
    return cached_DuplicateHeader_1;
  }

  @Test
  public void testDuplicateHeaderFailure() {
    assertLiveFail(get_DuplicateHeader_1());
  }

  @Test
  public void testDuplicateHeaderNotTerribleLineNumbers() {
    assertNotTerribleLineNumbers(get_DuplicateHeader_1());
  }

  @Test
  public void testDuplicateHeaderExceptionFree() {
    assertExceptionFree(get_DuplicateHeader_1());
  }

  @Test
  public void testDuplicateHeaderTODOFree() {
    assertTODOFree(get_DuplicateHeader_1());
  }

  @Test
  public void stable_DuplicateHeader_1() {
    String live = get_DuplicateHeader_1();
    StringBuilder gold = new StringBuilder();
    gold.append("Path:HttpClient_DuplicateHeader_failure.a");
    gold.append("\n--ISSUES-------------------------------------------");
    gold.append("\n[{\"range\":{\"start\":{\"line\":6,\"character\":2,\"byte\":137},\"end\":{\"line\":6,\"character\":40,\"byte\":175}},\"severity\":1,\"source\":\"error\",\"message\":\"The header Authorization has already been defined within the same group\",\"file\":\"./test_code/HttpClient_DuplicateHeader_failure.a\"},{\"range\":{\"start\":{\"line\":7,\"character\":2,\"byte\":178},\"end\":{\"line\":7,\"character\":40,\"byte\":216}},\"severity\":1,\"source\":\"error\",\"message\":\"The header Authorization has already been defined within the same group\",\"file\":\"./test_code/HttpClient_DuplicateHeader_failure.a\"}]\"--JAVA---------------------------------------------");
    gold.append("\n");
    gold.append("\nFailedValidation");
    assertStable(live, gold);
  }
  private String cached_DuplicateMethod_2 = null;
  private String get_DuplicateMethod_2() {
    if (cached_DuplicateMethod_2 != null) {
      return cached_DuplicateMethod_2;
    }
    cached_DuplicateMethod_2 = generateTestOutput(false, "DuplicateMethod_2", "./test_code/HttpClient_DuplicateMethod_failure.a");
    return cached_DuplicateMethod_2;
  }

  @Test
  public void testDuplicateMethodFailure() {
    assertLiveFail(get_DuplicateMethod_2());
  }

  @Test
  public void testDuplicateMethodNotTerribleLineNumbers() {
    assertNotTerribleLineNumbers(get_DuplicateMethod_2());
  }

  @Test
  public void testDuplicateMethodExceptionFree() {
    assertExceptionFree(get_DuplicateMethod_2());
  }

  @Test
  public void testDuplicateMethodTODOFree() {
    assertTODOFree(get_DuplicateMethod_2());
  }

  @Test
  public void stable_DuplicateMethod_2() {
    String live = get_DuplicateMethod_2();
    StringBuilder gold = new StringBuilder();
    gold.append("Path:HttpClient_DuplicateMethod_failure.a");
    gold.append("\n--ISSUES-------------------------------------------");
    gold.append("\n[{\"range\":{\"start\":{\"line\":6,\"character\":2,\"byte\":126},\"end\":{\"line\":6,\"character\":29,\"byte\":153}},\"severity\":1,\"source\":\"error\",\"message\":\"The method foo_me has already been defined\",\"file\":\"./test_code/HttpClient_DuplicateMethod_failure.a\"}]\"--JAVA---------------------------------------------");
    gold.append("\n");
    gold.append("\nFailedValidation");
    assertStable(live, gold);
  }
  private String cached_HeaderEOS_3 = null;
  private String get_HeaderEOS_3() {
    if (cached_HeaderEOS_3 != null) {
      return cached_HeaderEOS_3;
    }
    cached_HeaderEOS_3 = generateTestOutput(false, "HeaderEOS_3", "./test_code/HttpClient_HeaderEOS_failure.a");
    return cached_HeaderEOS_3;
  }

  @Test
  public void testHeaderEOSFailure() {
    assertLiveFail(get_HeaderEOS_3());
  }

  @Test
  public void testHeaderEOSNotTerribleLineNumbers() {
    assertNotTerribleLineNumbers(get_HeaderEOS_3());
  }

  @Test
  public void testHeaderEOSExceptionFree() {
    assertExceptionFree(get_HeaderEOS_3());
  }

  @Test
  public void testHeaderEOSTODOFree() {
    assertTODOFree(get_HeaderEOS_3());
  }

  @Test
  public void stable_HeaderEOS_3() {
    String live = get_HeaderEOS_3();
    StringBuilder gold = new StringBuilder();
    gold.append("Path:HttpClient_HeaderEOS_failure.a");
    gold.append("\n--ISSUES-------------------------------------------");
    gold.append("\n[{\"range\":{\"start\":{\"line\":1,\"character\":23,\"byte\":38},\"end\":{\"line\":1,\"character\":24,\"byte\":39}},\"severity\":1,\"source\":\"error\",\"message\":\"File './test_code/HttpClient_HeaderEOS_failure.a' failed to parse: Parser was expecting a string literal after '=', but got end of stream instead. {Token: `=` @ (1,23) -> (1,24): Symbol}\",\"file\":\"./test_code/HttpClient_HeaderEOS_failure.a\"},{\"range\":{\"start\":{\"line\":0,\"character\":0,\"byte\":0},\"end\":{\"line\":0,\"character\":0,\"byte\":0}},\"severity\":1,\"source\":\"error\",\"message\":\"Import failed (Parse): Parser was expecting a string literal after '=', but got end of stream instead. {Token: `=` @ (1,23) -> (1,24): Symbol}\"}]\"--JAVA---------------------------------------------");
    gold.append("\n");
    gold.append("\nFailedValidation");
    assertStable(live, gold);
  }
  private String cached_HeaderValueNotString_4 = null;
  private String get_HeaderValueNotString_4() {
    if (cached_HeaderValueNotString_4 != null) {
      return cached_HeaderValueNotString_4;
    }
    cached_HeaderValueNotString_4 = generateTestOutput(false, "HeaderValueNotString_4", "./test_code/HttpClient_HeaderValueNotString_failure.a");
    return cached_HeaderValueNotString_4;
  }

  @Test
  public void testHeaderValueNotStringFailure() {
    assertLiveFail(get_HeaderValueNotString_4());
  }

  @Test
  public void testHeaderValueNotStringNotTerribleLineNumbers() {
    assertNotTerribleLineNumbers(get_HeaderValueNotString_4());
  }

  @Test
  public void testHeaderValueNotStringExceptionFree() {
    assertExceptionFree(get_HeaderValueNotString_4());
  }

  @Test
  public void testHeaderValueNotStringTODOFree() {
    assertTODOFree(get_HeaderValueNotString_4());
  }

  @Test
  public void stable_HeaderValueNotString_4() {
    String live = get_HeaderValueNotString_4();
    StringBuilder gold = new StringBuilder();
    gold.append("Path:HttpClient_HeaderValueNotString_failure.a");
    gold.append("\n--ISSUES-------------------------------------------");
    gold.append("\n[{\"range\":{\"start\":{\"line\":1,\"character\":25,\"byte\":40},\"end\":{\"line\":1,\"character\":28,\"byte\":43}},\"severity\":1,\"source\":\"error\",\"message\":\"File './test_code/HttpClient_HeaderValueNotString_failure.a' failed to parse: Parse was expecting a string literal, but got '123' {Token: `123` @ (1,25) -> (1,28): NumberLiteral:NumberIsInteger}\",\"file\":\"./test_code/HttpClient_HeaderValueNotString_failure.a\"},{\"range\":{\"start\":{\"line\":0,\"character\":0,\"byte\":0},\"end\":{\"line\":0,\"character\":0,\"byte\":0}},\"severity\":1,\"source\":\"error\",\"message\":\"Import failed (Parse): Parse was expecting a string literal, but got '123' {Token: `123` @ (1,25) -> (1,28): NumberLiteral:NumberIsInteger}\"}]\"--JAVA---------------------------------------------");
    gold.append("\n");
    gold.append("\nFailedValidation");
    assertStable(live, gold);
  }
  private String cached_InsecureEndpointLocalhost_5 = null;
  private String get_InsecureEndpointLocalhost_5() {
    if (cached_InsecureEndpointLocalhost_5 != null) {
      return cached_InsecureEndpointLocalhost_5;
    }
    cached_InsecureEndpointLocalhost_5 = generateTestOutput(true, "InsecureEndpointLocalhost_5", "./test_code/HttpClient_InsecureEndpointLocalhost_success.a");
    return cached_InsecureEndpointLocalhost_5;
  }

  @Test
  public void testInsecureEndpointLocalhostEmission() {
    assertEmissionGood(get_InsecureEndpointLocalhost_5());
  }

  @Test
  public void testInsecureEndpointLocalhostSuccess() {
    assertLivePass(get_InsecureEndpointLocalhost_5());
  }

  @Test
  public void testInsecureEndpointLocalhostNoFormatException() {
    assertNoFormatException(get_InsecureEndpointLocalhost_5());
  }

  @Test
  public void testInsecureEndpointLocalhostGoodWillHappy() {
    assertGoodWillHappy(get_InsecureEndpointLocalhost_5());
  }

  @Test
  public void testInsecureEndpointLocalhostExceptionFree() {
    assertExceptionFree(get_InsecureEndpointLocalhost_5());
  }

  @Test
  public void testInsecureEndpointLocalhostTODOFree() {
    assertTODOFree(get_InsecureEndpointLocalhost_5());
  }

  @Test
  public void stable_InsecureEndpointLocalhost_5() {
    String live = get_InsecureEndpointLocalhost_5();
    StringBuilder gold = new StringBuilder();
    gold.append("Path:HttpClient_InsecureEndpointLocalhost_success.a");
    gold.append("\n--EMISSION-----------------------------------------");
    gold.append("\nEmission Success, Yay");
    gold.append("\n=FORMAT===================================================");
    gold.append("\nmessage M {");
    gold.append("\n  int xyz;");
    gold.append("\n}");
    gold.append("\nmessage V {");
    gold.append("\n  int n;");
    gold.append("\n}");
    gold.append("\nclient me { endpoint \"http://127.0.0.1:999\";endpoint[beta] \"http://localhost:999\";}");
    gold.append("\n==========================================================");
    gold.append("\n--ISSUES-------------------------------------------");
    gold.append("\n[]\"--JAVA---------------------------------------------");
    gold.append("\nimport ape.common.ErrorCodeException;");
    gold.append("\nimport ape.common.Pair;");
    gold.append("\nimport ape.common.SimpleCancel;");
    gold.append("\nimport ape.common.Stream;");
    gold.append("\nimport ape.ErrorCodes;");
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
    gold.append("\npublic class InsecureEndpointLocalhost_5 extends LivingDocument {");
    gold.append("\n  @Override");
    gold.append("\n  public long __memory() {");
    gold.append("\n    long __sum = super.__memory() + 2176;");
    gold.append("\n    return __sum;");
    gold.append("\n  }");
    gold.append("\n  public InsecureEndpointLocalhost_5(DocumentMonitor __monitor) {");
    gold.append("\n    super(__monitor);");
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
    gold.append("\n    /* root */");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  public void __writeRxReport(JsonStreamWriter __writer) { }");
    gold.append("\n  public class DeltaPrivacyCache {");
    gold.append("\n    public DeltaPrivacyCache(NtPrincipal __who) {}");
    gold.append("\n  }");
    gold.append("\n  private class DeltaInsecureEndpointLocalhost_5 implements DeltaNode {");
    gold.append("\n    private boolean __emitted;");
    gold.append("\n    private DeltaInsecureEndpointLocalhost_5() {");
    gold.append("\n      __emitted = false;");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public long __memory() {");
    gold.append("\n      long __sum = 40;");
    gold.append("\n      return __sum;");
    gold.append("\n    }");
    gold.append("\n    public boolean show(InsecureEndpointLocalhost_5 __item, PrivateLazyDeltaWriter __writer) {");
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
    gold.append("\n    InsecureEndpointLocalhost_5 __self = this;");
    gold.append("\n    DeltaInsecureEndpointLocalhost_5 __state = new DeltaInsecureEndpointLocalhost_5();");
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
    gold.append("\n  private static class RTxM extends NtMessageBase {");
    gold.append("\n    private final RTxM __this;");
    gold.append("\n    private int xyz = 0;");
    gold.append("\n    @Override");
    gold.append("\n    public long __memory() {");
    gold.append("\n      long __mem = 64;");
    gold.append("\n      __mem += 4;");
    gold.append("\n      return __mem;");
    gold.append("\n    }");
    gold.append("\n    public void __reset() {");
    gold.append("\n      this.xyz = 0;");
    gold.append("\n    }");
    gold.append("\n    public void __hash(HashBuilder __hash) {");
    gold.append("\n      __hash.hashString(\"xyz\");");
    gold.append("\n      __hash.hashInteger(this.xyz);");
    gold.append("\n      __hash.hashString(\"M\");");
    gold.append("\n    }");
    gold.append("\n    private static String[] __INDEX_COLUMNS_M = new String[] {};");
    gold.append("\n    @Override");
    gold.append("\n    public String[] __getIndexColumns() {");
    gold.append("\n      return __INDEX_COLUMNS_M;");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public int[] __getIndexValues() {");
    gold.append("\n      return new int[] {};");
    gold.append("\n    }");
    gold.append("\n    private RTxM(JsonStreamReader __reader) {");
    gold.append("\n      __this = this;");
    gold.append("\n      __ingest(__reader);");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public void __ingest(JsonStreamReader __reader) {");
    gold.append("\n      __reader.mustStartObject();");
    gold.append("\n      while (__reader.notEndOfObject()) {");
    gold.append("\n        String __fieldName = __reader.fieldName();");
    gold.append("\n        switch (__fieldName) {");
    gold.append("\n          case \"xyz\":");
    gold.append("\n            this.xyz = __reader.readInteger();");
    gold.append("\n            break;");
    gold.append("\n          default:");
    gold.append("\n            __reader.skipValue();");
    gold.append("\n        }");
    gold.append("\n      }");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public void __writeOut(JsonStreamWriter __writer) {");
    gold.append("\n      __writer.beginObject();");
    gold.append("\n      __writer.writeObjectFieldIntro(\"xyz\");");
    gold.append("\n      __writer.writeInteger(xyz);");
    gold.append("\n      __writer.endObject();");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public void __parsed() throws AbortMessageException {}");
    gold.append("\n    private RTxM() { __this = this; }");
    gold.append("\n    private RTxM(int xyz) {");
    gold.append("\n      this.__this = this;");
    gold.append("\n      this.xyz = xyz;");
    gold.append("\n    }");
    gold.append("\n  }");
    gold.append("\n  private class DeltaRTxM implements DeltaNode {");
    gold.append("\n    private DInt32 __dxyz;");
    gold.append("\n    private boolean __emitted;");
    gold.append("\n    private DeltaRTxM() {");
    gold.append("\n      __dxyz = new DInt32();");
    gold.append("\n      __emitted = false;");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public long __memory() {");
    gold.append("\n      long __sum = 40;");
    gold.append("\n      __sum += __dxyz.__memory();");
    gold.append("\n      return __sum;");
    gold.append("\n    }");
    gold.append("\n    public void show(RTxM __item, PrivateLazyDeltaWriter __writer) {");
    gold.append("\n      __code_cost += 1;");
    gold.append("\n      PrivateLazyDeltaWriter __obj = __writer.planObject();");
    gold.append("\n      __dxyz.show(__item.xyz, __obj.planField(\"xyz\"));");
    gold.append("\n      if (__obj.end()) {");
    gold.append("\n        __emitted = true;");
    gold.append("\n      }");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public void clear() {");
    gold.append("\n      __dxyz.clear();");
    gold.append("\n      __code_cost += 1;");
    gold.append("\n    }");
    gold.append("\n    public void hide(PrivateLazyDeltaWriter __writer) {");
    gold.append("\n      if (__emitted) {");
    gold.append("\n        clear();");
    gold.append("\n        __emitted = false;");
    gold.append("\n        __writer.writeNull();");
    gold.append("\n      }");
    gold.append("\n    }");
    gold.append("\n  }");
    gold.append("\n  private static class RTxV extends NtMessageBase {");
    gold.append("\n    private final RTxV __this;");
    gold.append("\n    private int n = 0;");
    gold.append("\n    @Override");
    gold.append("\n    public long __memory() {");
    gold.append("\n      long __mem = 64;");
    gold.append("\n      __mem += 4;");
    gold.append("\n      return __mem;");
    gold.append("\n    }");
    gold.append("\n    public void __reset() {");
    gold.append("\n      this.n = 0;");
    gold.append("\n    }");
    gold.append("\n    public void __hash(HashBuilder __hash) {");
    gold.append("\n      __hash.hashString(\"n\");");
    gold.append("\n      __hash.hashInteger(this.n);");
    gold.append("\n      __hash.hashString(\"V\");");
    gold.append("\n    }");
    gold.append("\n    private static String[] __INDEX_COLUMNS_V = new String[] {};");
    gold.append("\n    @Override");
    gold.append("\n    public String[] __getIndexColumns() {");
    gold.append("\n      return __INDEX_COLUMNS_V;");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public int[] __getIndexValues() {");
    gold.append("\n      return new int[] {};");
    gold.append("\n    }");
    gold.append("\n    private RTxV(JsonStreamReader __reader) {");
    gold.append("\n      __this = this;");
    gold.append("\n      __ingest(__reader);");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public void __ingest(JsonStreamReader __reader) {");
    gold.append("\n      __reader.mustStartObject();");
    gold.append("\n      while (__reader.notEndOfObject()) {");
    gold.append("\n        String __fieldName = __reader.fieldName();");
    gold.append("\n        switch (__fieldName) {");
    gold.append("\n          case \"n\":");
    gold.append("\n            this.n = __reader.readInteger();");
    gold.append("\n            break;");
    gold.append("\n          default:");
    gold.append("\n            __reader.skipValue();");
    gold.append("\n        }");
    gold.append("\n      }");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public void __writeOut(JsonStreamWriter __writer) {");
    gold.append("\n      __writer.beginObject();");
    gold.append("\n      __writer.writeObjectFieldIntro(\"n\");");
    gold.append("\n      __writer.writeInteger(n);");
    gold.append("\n      __writer.endObject();");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public void __parsed() throws AbortMessageException {}");
    gold.append("\n    private RTxV() { __this = this; }");
    gold.append("\n    private RTxV(int n) {");
    gold.append("\n      this.__this = this;");
    gold.append("\n      this.n = n;");
    gold.append("\n    }");
    gold.append("\n  }");
    gold.append("\n  private class DeltaRTxV implements DeltaNode {");
    gold.append("\n    private DInt32 __dn;");
    gold.append("\n    private boolean __emitted;");
    gold.append("\n    private DeltaRTxV() {");
    gold.append("\n      __dn = new DInt32();");
    gold.append("\n      __emitted = false;");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public long __memory() {");
    gold.append("\n      long __sum = 40;");
    gold.append("\n      __sum += __dn.__memory();");
    gold.append("\n      return __sum;");
    gold.append("\n    }");
    gold.append("\n    public void show(RTxV __item, PrivateLazyDeltaWriter __writer) {");
    gold.append("\n      __code_cost += 1;");
    gold.append("\n      PrivateLazyDeltaWriter __obj = __writer.planObject();");
    gold.append("\n      __dn.show(__item.n, __obj.planField(\"n\"));");
    gold.append("\n      if (__obj.end()) {");
    gold.append("\n        __emitted = true;");
    gold.append("\n      }");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public void clear() {");
    gold.append("\n      __dn.clear();");
    gold.append("\n      __code_cost += 1;");
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
    gold.append("\n  public static void __create_generic_clients(ServiceRegistry __registry, HeaderDecryptor __decryptor) throws Exception {");
    gold.append("\n    GenericClient me = __registry.makeGenericClient();");
    gold.append("\n    HeaderGroup __headers_1 = new HeaderGroup(null);");
    gold.append("\n    __registry.add(\"me\", me);");
    gold.append("\n");
    gold.append("\n  }");
    gold.append("\n  protected GenericClient me;");
    gold.append("\n  @Override");
    gold.append("\n  public void __link(ServiceRegistry __registry) {");
    gold.append("\n    me = __registry.getClient(\"me\");");
    gold.append("\n");
    gold.append("\n  }");
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
    gold.append("\n  public SimpleCancel __export(CoreRequestContext __context, String __name, String __viewerState, Stream<String> __stream) {");
    gold.append("\n    __stream.failure(new ErrorCodeException(ErrorCodes.LIVING_DOCUMENT_NO_EXPORT_BY_NAME));");
    gold.append("\n    return SimpleCancel.NOTHING_TO_CANCEL;");
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
    gold.append("\n  @Override");
    gold.append("\n  protected void __construct_intern(CoreRequestContext _c, NtMessageBase _m) {}");
    gold.append("\n  @Override");
    gold.append("\n  protected NtMessageBase __parse_construct_arg(JsonStreamReader __reader) {");
    gold.append("\n    __reader.skipValue();");
    gold.append("\n    return NtMessageBase.NULL;");
    gold.append("\n  }");
    gold.append("\n  /* end of file */");
    gold.append("\n}");
    gold.append("\n");
    gold.append("\n--=[LivingDocumentFactory COMPILING]=---");
    gold.append("\n--=[LivingDocumentFactory MADE]=---");
    gold.append("\n--REFLECTION RESULTS-------------------------------------");
    gold.append("\n{\"types\":{\"__Root\":{\"nature\":\"reactive_record\",\"name\":\"Root\",\"fields\":{}},\"__ViewerType\":{\"nature\":\"native_message\",\"name\":\"__ViewerType\",\"anonymous\":true,\"fields\":{}},\"M\":{\"nature\":\"native_message\",\"name\":\"M\",\"anonymous\":false,\"fields\":{\"xyz\":{\"type\":{\"nature\":\"native_value\",\"type\":\"int\"},\"computed\":false,\"privacy\":\"public\"}}},\"V\":{\"nature\":\"native_message\",\"name\":\"V\",\"anonymous\":false,\"fields\":{\"n\":{\"type\":{\"nature\":\"native_value\",\"type\":\"int\"},\"computed\":false,\"privacy\":\"public\"}}}},\"channels\":{},\"channels-privacy\":{},\"constructors\":[],\"labels\":[]}");
    gold.append("\n--JAVA RUNNING-------------------------------------");
    gold.append("\n{\"command\":\"construct\",\"timestamp\":\"0\",\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"arg\":{},\"entropy\":\"0\",\"key\":\"0\",\"origin\":\"origin\",\"ip\":\"ip\"}-->{\"__constructed\":true,\"__entropy\":\"-4962768465676381896\",\"__seq\":1} need:false in:0");
    gold.append("\n{\"command\":\"invalidate\",\"timestamp\":\"25\"}-->{\"__seq\":2,\"__entropy\":\"4804307197456638271\",\"__time\":\"25\"} need:false in:0");
    gold.append("\nCPU:0");
    gold.append("\nMEMORY:2560");
    gold.append("\n{\"command\":\"invalidate\",\"timestamp\":\"50\"}-->{\"__seq\":3,\"__entropy\":\"-1034601897293430941\",\"__time\":\"50\"} need:false in:0");
    gold.append("\nNO_ONE: CREATED PRIVATE VIEW");
    gold.append("\n+ NO_ONE DELTA:{\"seq\":3}");
    gold.append("\nNO_ONE|FAILURE:184333");
    gold.append("\n{\"command\":\"invalidate\",\"timestamp\":\"75\"}-->{\"__seq\":4,\"__entropy\":\"7848011421992302230\",\"__time\":\"75\"} need:false in:0");
    gold.append("\nRANDO: CREATED PRIVATE VIEW");
    gold.append("\n+ NO_ONE DELTA:{\"seq\":4}");
    gold.append("\n+ RANDO DELTA:{\"seq\":4}");
    gold.append("\nRANDO|FAILURE:184333");
    gold.append("\n{\"command\":\"invalidate\",\"timestamp\":\"100\"}-->{\"__seq\":5,\"__entropy\":\"-8929183248358367000\",\"__time\":\"100\"} need:false in:0");
    gold.append("\nRANDO|SUCCESS:5");
    gold.append("\n+ NO_ONE DELTA:{\"seq\":5}");
    gold.append("\n+ RANDO DELTA:{\"seq\":5}");
    gold.append("\nMEMORY:2678");
    gold.append("\n--JAVA RESULTS-------------------------------------");
    gold.append("\n{\"__snapshot\":\"0/0\",\"__state\":\"\",\"__constructed\":true,\"__next_time\":\"0\",\"__last_expire_time\":\"0\",\"__blocked\":false,\"__seq\":5,\"__entropy\":\"-8929183248358367000\",\"__auto_future_id\":0,\"__connection_id\":0,\"__message_id\":0,\"__time\":\"100\",\"__timezone\":\"UTC\",\"__auto_table_row_id\":0,\"__auto_gen\":0,\"__auto_cache_id\":0,\"__cache\":{},\"__webTaskId\":0,\"__webqueue\":{},\"__replication\":{}}");
    gold.append("\n--DUMP RESULTS-------------------------------------");
    gold.append("\n{}");
    gold.append("\n--METRIC RESULTS-----------------------------------");
    gold.append("\n{\"__snapshot\":\"0/0\",\"__state\":\"\",\"__constructed\":true,\"__next_time\":\"0\",\"__last_expire_time\":\"0\",\"__blocked\":false,\"__seq\":5,\"__entropy\":\"-8929183248358367000\",\"__auto_future_id\":0,\"__connection_id\":0,\"__message_id\":0,\"__time\":\"100\",\"__timezone\":\"UTC\",\"__auto_table_row_id\":0,\"__auto_gen\":0,\"__auto_cache_id\":0,\"__cache\":{},\"__webTaskId\":0,\"__webqueue\":{},\"__replication\":{}}");
    gold.append("\n{\"__snapshot\":\"0/0\",\"__state\":\"\",\"__constructed\":true,\"__next_time\":\"0\",\"__last_expire_time\":\"0\",\"__blocked\":false,\"__seq\":5,\"__entropy\":\"-8929183248358367000\",\"__auto_future_id\":0,\"__connection_id\":0,\"__message_id\":0,\"__time\":\"100\",\"__timezone\":\"UTC\",\"__auto_table_row_id\":0,\"__auto_gen\":0,\"__auto_cache_id\":0,\"__cache\":{},\"__webTaskId\":0,\"__webqueue\":{},\"__replication\":{}}");
    gold.append("\n--JAVA TEST RESULTS--------------------------------");
    gold.append("\n");
    gold.append("\nSuccess");
    assertStable(live, gold);
  }
  private String cached_InsecureEndpoint_6 = null;
  private String get_InsecureEndpoint_6() {
    if (cached_InsecureEndpoint_6 != null) {
      return cached_InsecureEndpoint_6;
    }
    cached_InsecureEndpoint_6 = generateTestOutput(false, "InsecureEndpoint_6", "./test_code/HttpClient_InsecureEndpoint_failure.a");
    return cached_InsecureEndpoint_6;
  }

  @Test
  public void testInsecureEndpointFailure() {
    assertLiveFail(get_InsecureEndpoint_6());
  }

  @Test
  public void testInsecureEndpointNotTerribleLineNumbers() {
    assertNotTerribleLineNumbers(get_InsecureEndpoint_6());
  }

  @Test
  public void testInsecureEndpointExceptionFree() {
    assertExceptionFree(get_InsecureEndpoint_6());
  }

  @Test
  public void testInsecureEndpointTODOFree() {
    assertTODOFree(get_InsecureEndpoint_6());
  }

  @Test
  public void stable_InsecureEndpoint_6() {
    String live = get_InsecureEndpoint_6();
    StringBuilder gold = new StringBuilder();
    gold.append("Path:HttpClient_InsecureEndpoint_failure.a");
    gold.append("\n--ISSUES-------------------------------------------");
    gold.append("\n[{\"range\":{\"start\":{\"line\":8,\"character\":2,\"byte\":197},\"end\":{\"line\":8,\"character\":40,\"byte\":235}},\"severity\":1,\"source\":\"error\",\"message\":\"Only endpoints for developers or localhost are allowed to start with http://\",\"file\":\"./test_code/HttpClient_InsecureEndpoint_failure.a\"},{\"range\":{\"start\":{\"line\":11,\"character\":4,\"byte\":283},\"end\":{\"line\":11,\"character\":43,\"byte\":322}},\"severity\":1,\"source\":\"error\",\"message\":\"Only endpoints for developers or localhost are allowed to start with http://\",\"file\":\"./test_code/HttpClient_InsecureEndpoint_failure.a\"}]\"--JAVA---------------------------------------------");
    gold.append("\n");
    gold.append("\nFailedValidation");
    assertStable(live, gold);
  }
  private String cached_InvalidEndpointVersion_7 = null;
  private String get_InvalidEndpointVersion_7() {
    if (cached_InvalidEndpointVersion_7 != null) {
      return cached_InvalidEndpointVersion_7;
    }
    cached_InvalidEndpointVersion_7 = generateTestOutput(false, "InvalidEndpointVersion_7", "./test_code/HttpClient_InvalidEndpointVersion_failure.a");
    return cached_InvalidEndpointVersion_7;
  }

  @Test
  public void testInvalidEndpointVersionFailure() {
    assertLiveFail(get_InvalidEndpointVersion_7());
  }

  @Test
  public void testInvalidEndpointVersionNotTerribleLineNumbers() {
    assertNotTerribleLineNumbers(get_InvalidEndpointVersion_7());
  }

  @Test
  public void testInvalidEndpointVersionExceptionFree() {
    assertExceptionFree(get_InvalidEndpointVersion_7());
  }

  @Test
  public void testInvalidEndpointVersionTODOFree() {
    assertTODOFree(get_InvalidEndpointVersion_7());
  }

  @Test
  public void stable_InvalidEndpointVersion_7() {
    String live = get_InvalidEndpointVersion_7();
    StringBuilder gold = new StringBuilder();
    gold.append("Path:HttpClient_InvalidEndpointVersion_failure.a");
    gold.append("\n--ISSUES-------------------------------------------");
    gold.append("\n[{\"range\":{\"start\":{\"line\":1,\"character\":10,\"byte\":25},\"end\":{\"line\":1,\"character\":11,\"byte\":26}},\"severity\":1,\"source\":\"error\",\"message\":\"File './test_code/HttpClient_InvalidEndpointVersion_failure.a' failed to parse: Unable to recognize the version of the endpoint; must be either dev, beta, or prod {Token: `[` @ (1,10) -> (1,11): Symbol}\",\"file\":\"./test_code/HttpClient_InvalidEndpointVersion_failure.a\"},{\"range\":{\"start\":{\"line\":0,\"character\":0,\"byte\":0},\"end\":{\"line\":0,\"character\":0,\"byte\":0}},\"severity\":1,\"source\":\"error\",\"message\":\"Import failed (Parse): Unable to recognize the version of the endpoint; must be either dev, beta, or prod {Token: `[` @ (1,10) -> (1,11): Symbol}\"}]\"--JAVA---------------------------------------------");
    gold.append("\n");
    gold.append("\nFailedValidation");
    assertStable(live, gold);
  }
  private String cached_InvalidEndpoint_8 = null;
  private String get_InvalidEndpoint_8() {
    if (cached_InvalidEndpoint_8 != null) {
      return cached_InvalidEndpoint_8;
    }
    cached_InvalidEndpoint_8 = generateTestOutput(false, "InvalidEndpoint_8", "./test_code/HttpClient_InvalidEndpoint_failure.a");
    return cached_InvalidEndpoint_8;
  }

  @Test
  public void testInvalidEndpointFailure() {
    assertLiveFail(get_InvalidEndpoint_8());
  }

  @Test
  public void testInvalidEndpointNotTerribleLineNumbers() {
    assertNotTerribleLineNumbers(get_InvalidEndpoint_8());
  }

  @Test
  public void testInvalidEndpointExceptionFree() {
    assertExceptionFree(get_InvalidEndpoint_8());
  }

  @Test
  public void testInvalidEndpointTODOFree() {
    assertTODOFree(get_InvalidEndpoint_8());
  }

  @Test
  public void stable_InvalidEndpoint_8() {
    String live = get_InvalidEndpoint_8();
    StringBuilder gold = new StringBuilder();
    gold.append("Path:HttpClient_InvalidEndpoint_failure.a");
    gold.append("\n--ISSUES-------------------------------------------");
    gold.append("\n[{\"range\":{\"start\":{\"line\":4,\"character\":2,\"byte\":59},\"end\":{\"line\":4,\"character\":39,\"byte\":96}},\"severity\":1,\"source\":\"error\",\"message\":\"The endpoint must start with either 'http://' or 'https://'\",\"file\":\"./test_code/HttpClient_InvalidEndpoint_failure.a\"},{\"range\":{\"start\":{\"line\":5,\"character\":2,\"byte\":99},\"end\":{\"line\":5,\"character\":36,\"byte\":133}},\"severity\":1,\"source\":\"error\",\"message\":\"The endpoint must start with either 'http://' or 'https://'\",\"file\":\"./test_code/HttpClient_InvalidEndpoint_failure.a\"}]\"--JAVA---------------------------------------------");
    gold.append("\n");
    gold.append("\nFailedValidation");
    assertStable(live, gold);
  }
  private String cached_InvalidLookupType_9 = null;
  private String get_InvalidLookupType_9() {
    if (cached_InvalidLookupType_9 != null) {
      return cached_InvalidLookupType_9;
    }
    cached_InvalidLookupType_9 = generateTestOutput(false, "InvalidLookupType_9", "./test_code/HttpClient_InvalidLookupType_failure.a");
    return cached_InvalidLookupType_9;
  }

  @Test
  public void testInvalidLookupTypeFailure() {
    assertLiveFail(get_InvalidLookupType_9());
  }

  @Test
  public void testInvalidLookupTypeNotTerribleLineNumbers() {
    assertNotTerribleLineNumbers(get_InvalidLookupType_9());
  }

  @Test
  public void testInvalidLookupTypeExceptionFree() {
    assertExceptionFree(get_InvalidLookupType_9());
  }

  @Test
  public void testInvalidLookupTypeTODOFree() {
    assertTODOFree(get_InvalidLookupType_9());
  }

  @Test
  public void stable_InvalidLookupType_9() {
    String live = get_InvalidLookupType_9();
    StringBuilder gold = new StringBuilder();
    gold.append("Path:HttpClient_InvalidLookupType_failure.a");
    gold.append("\n--ISSUES-------------------------------------------");
    gold.append("\n[{\"range\":{\"start\":{\"line\":6,\"character\":2,\"byte\":107},\"end\":{\"line\":6,\"character\":31,\"byte\":136}},\"severity\":1,\"source\":\"error\",\"message\":\"The key 'xyz' will lookup a field that is netiher a string, int, double, long, or boolean\",\"file\":\"./test_code/HttpClient_InvalidLookupType_failure.a\"}]\"--JAVA---------------------------------------------");
    gold.append("\n");
    gold.append("\nFailedValidation");
    assertStable(live, gold);
  }
  private String cached_LookupNotFound_10 = null;
  private String get_LookupNotFound_10() {
    if (cached_LookupNotFound_10 != null) {
      return cached_LookupNotFound_10;
    }
    cached_LookupNotFound_10 = generateTestOutput(false, "LookupNotFound_10", "./test_code/HttpClient_LookupNotFound_failure.a");
    return cached_LookupNotFound_10;
  }

  @Test
  public void testLookupNotFoundFailure() {
    assertLiveFail(get_LookupNotFound_10());
  }

  @Test
  public void testLookupNotFoundNotTerribleLineNumbers() {
    assertNotTerribleLineNumbers(get_LookupNotFound_10());
  }

  @Test
  public void testLookupNotFoundExceptionFree() {
    assertExceptionFree(get_LookupNotFound_10());
  }

  @Test
  public void testLookupNotFoundTODOFree() {
    assertTODOFree(get_LookupNotFound_10());
  }

  @Test
  public void stable_LookupNotFound_10() {
    String live = get_LookupNotFound_10();
    StringBuilder gold = new StringBuilder();
    gold.append("Path:HttpClient_LookupNotFound_failure.a");
    gold.append("\n--ISSUES-------------------------------------------");
    gold.append("\n[{\"range\":{\"start\":{\"line\":6,\"character\":2,\"byte\":109},\"end\":{\"line\":6,\"character\":29,\"byte\":136}},\"severity\":1,\"source\":\"error\",\"message\":\"The key 'x' was not a field within the query message\",\"file\":\"./test_code/HttpClient_LookupNotFound_failure.a\"}]\"--JAVA---------------------------------------------");
    gold.append("\n");
    gold.append("\nFailedValidation");
    assertStable(live, gold);
  }
  private String cached_MethodTypesNotAvailable_11 = null;
  private String get_MethodTypesNotAvailable_11() {
    if (cached_MethodTypesNotAvailable_11 != null) {
      return cached_MethodTypesNotAvailable_11;
    }
    cached_MethodTypesNotAvailable_11 = generateTestOutput(false, "MethodTypesNotAvailable_11", "./test_code/HttpClient_MethodTypesNotAvailable_failure.a");
    return cached_MethodTypesNotAvailable_11;
  }

  @Test
  public void testMethodTypesNotAvailableFailure() {
    assertLiveFail(get_MethodTypesNotAvailable_11());
  }

  @Test
  public void testMethodTypesNotAvailableNotTerribleLineNumbers() {
    assertNotTerribleLineNumbers(get_MethodTypesNotAvailable_11());
  }

  @Test
  public void testMethodTypesNotAvailableExceptionFree() {
    assertExceptionFree(get_MethodTypesNotAvailable_11());
  }

  @Test
  public void testMethodTypesNotAvailableTODOFree() {
    assertTODOFree(get_MethodTypesNotAvailable_11());
  }

  @Test
  public void stable_MethodTypesNotAvailable_11() {
    String live = get_MethodTypesNotAvailable_11();
    StringBuilder gold = new StringBuilder();
    gold.append("Path:HttpClient_MethodTypesNotAvailable_failure.a");
    gold.append("\n--ISSUES-------------------------------------------");
    gold.append("\n[{\"range\":{\"start\":{\"line\":2,\"character\":2,\"byte\":51},\"end\":{\"line\":2,\"character\":25,\"byte\":74}},\"severity\":1,\"source\":\"error\",\"message\":\"Type not found: a message named 'Q' was not found.\",\"file\":\"./test_code/HttpClient_MethodTypesNotAvailable_failure.a\"},{\"range\":{\"start\":{\"line\":3,\"character\":2,\"byte\":77},\"end\":{\"line\":3,\"character\":27,\"byte\":102}},\"severity\":1,\"source\":\"error\",\"message\":\"Type not found: a message named 'Q' was not found.\",\"file\":\"./test_code/HttpClient_MethodTypesNotAvailable_failure.a\"},{\"range\":{\"start\":{\"line\":3,\"character\":2,\"byte\":77},\"end\":{\"line\":3,\"character\":27,\"byte\":102}},\"severity\":1,\"source\":\"error\",\"message\":\"Type not found: a message named 'B' was not found.\",\"file\":\"./test_code/HttpClient_MethodTypesNotAvailable_failure.a\"},{\"range\":{\"start\":{\"line\":4,\"character\":2,\"byte\":105},\"end\":{\"line\":4,\"character\":30,\"byte\":133}},\"severity\":1,\"source\":\"error\",\"message\":\"Type not found: a message named 'Q' was not found.\",\"file\":\"./test_code/HttpClient_MethodTypesNotAvailable_failure.a\"},{\"range\":{\"start\":{\"line\":4,\"character\":2,\"byte\":105},\"end\":{\"line\":4,\"character\":30,\"byte\":133}},\"severity\":1,\"source\":\"error\",\"message\":\"Type not found: a message named 'R' was not found.\",\"file\":\"./test_code/HttpClient_MethodTypesNotAvailable_failure.a\"},{\"range\":{\"start\":{\"line\":5,\"character\":2,\"byte\":136},\"end\":{\"line\":5,\"character\":32,\"byte\":166}},\"severity\":1,\"source\":\"error\",\"message\":\"Type not found: a message named 'Q' was not found.\",\"file\":\"./test_code/HttpClient_MethodTypesNotAvailable_failure.a\"},{\"range\":{\"start\":{\"line\":5,\"character\":2,\"byte\":136},\"end\":{\"line\":5,\"character\":32,\"byte\":166}},\"severity\":1,\"source\":\"error\",\"message\":\"Type not found: a message named 'B' was not found.\",\"file\":\"./test_code/HttpClient_MethodTypesNotAvailable_failure.a\"},{\"range\":{\"start\":{\"line\":5,\"character\":2,\"byte\":136},\"end\":{\"line\":5,\"character\":32,\"byte\":166}},\"severity\":1,\"source\":\"error\",\"message\":\"Type not found: a message named 'R' was not found.\",\"file\":\"./test_code/HttpClient_MethodTypesNotAvailable_failure.a\"}]\"--JAVA---------------------------------------------");
    gold.append("\n");
    gold.append("\nFailedValidation");
    assertStable(live, gold);
  }
  private String cached_MissingDefaultProdEndpoint_12 = null;
  private String get_MissingDefaultProdEndpoint_12() {
    if (cached_MissingDefaultProdEndpoint_12 != null) {
      return cached_MissingDefaultProdEndpoint_12;
    }
    cached_MissingDefaultProdEndpoint_12 = generateTestOutput(false, "MissingDefaultProdEndpoint_12", "./test_code/HttpClient_MissingDefaultProdEndpoint_failure.a");
    return cached_MissingDefaultProdEndpoint_12;
  }

  @Test
  public void testMissingDefaultProdEndpointFailure() {
    assertLiveFail(get_MissingDefaultProdEndpoint_12());
  }

  @Test
  public void testMissingDefaultProdEndpointNotTerribleLineNumbers() {
    assertNotTerribleLineNumbers(get_MissingDefaultProdEndpoint_12());
  }

  @Test
  public void testMissingDefaultProdEndpointExceptionFree() {
    assertExceptionFree(get_MissingDefaultProdEndpoint_12());
  }

  @Test
  public void testMissingDefaultProdEndpointTODOFree() {
    assertTODOFree(get_MissingDefaultProdEndpoint_12());
  }

  @Test
  public void stable_MissingDefaultProdEndpoint_12() {
    String live = get_MissingDefaultProdEndpoint_12();
    StringBuilder gold = new StringBuilder();
    gold.append("Path:HttpClient_MissingDefaultProdEndpoint_failure.a");
    gold.append("\n--ISSUES-------------------------------------------");
    gold.append("\n[{\"range\":{\"start\":{\"line\":0,\"character\":10,\"byte\":10},\"end\":{\"line\":3,\"character\":1,\"byte\":104}},\"severity\":1,\"source\":\"error\",\"message\":\"Group lacks a default/production endpoint\",\"file\":\"./test_code/HttpClient_MissingDefaultProdEndpoint_failure.a\"}]\"--JAVA---------------------------------------------");
    gold.append("\n");
    gold.append("\nFailedValidation");
    assertStable(live, gold);
  }
  private String cached_ParseBadMethod_13 = null;
  private String get_ParseBadMethod_13() {
    if (cached_ParseBadMethod_13 != null) {
      return cached_ParseBadMethod_13;
    }
    cached_ParseBadMethod_13 = generateTestOutput(false, "ParseBadMethod_13", "./test_code/HttpClient_ParseBadMethod_failure.a");
    return cached_ParseBadMethod_13;
  }

  @Test
  public void testParseBadMethodFailure() {
    assertLiveFail(get_ParseBadMethod_13());
  }

  @Test
  public void testParseBadMethodNotTerribleLineNumbers() {
    assertNotTerribleLineNumbers(get_ParseBadMethod_13());
  }

  @Test
  public void testParseBadMethodExceptionFree() {
    assertExceptionFree(get_ParseBadMethod_13());
  }

  @Test
  public void testParseBadMethodTODOFree() {
    assertTODOFree(get_ParseBadMethod_13());
  }

  @Test
  public void stable_ParseBadMethod_13() {
    String live = get_ParseBadMethod_13();
    StringBuilder gold = new StringBuilder();
    gold.append("Path:HttpClient_ParseBadMethod_failure.a");
    gold.append("\n--ISSUES-------------------------------------------");
    gold.append("\n[{\"range\":{\"start\":{\"line\":1,\"character\":2,\"byte\":17},\"end\":{\"line\":1,\"character\":10,\"byte\":25}},\"severity\":1,\"source\":\"error\",\"message\":\"File './test_code/HttpClient_ParseBadMethod_failure.a' failed to parse: Expected http verb, header, secret_header, or {; got 'generate' {Token: `generate` @ (1,2) -> (1,10): Identifier}\",\"file\":\"./test_code/HttpClient_ParseBadMethod_failure.a\"},{\"range\":{\"start\":{\"line\":0,\"character\":0,\"byte\":0},\"end\":{\"line\":0,\"character\":0,\"byte\":0}},\"severity\":1,\"source\":\"error\",\"message\":\"Import failed (Parse): Expected http verb, header, secret_header, or {; got 'generate' {Token: `generate` @ (1,2) -> (1,10): Identifier}\"}]\"--JAVA---------------------------------------------");
    gold.append("\n");
    gold.append("\nFailedValidation");
    assertStable(live, gold);
  }
  private String cached_ParseEOSNoMethod1_14 = null;
  private String get_ParseEOSNoMethod1_14() {
    if (cached_ParseEOSNoMethod1_14 != null) {
      return cached_ParseEOSNoMethod1_14;
    }
    cached_ParseEOSNoMethod1_14 = generateTestOutput(false, "ParseEOSNoMethod1_14", "./test_code/HttpClient_ParseEOSNoMethod1_failure.a");
    return cached_ParseEOSNoMethod1_14;
  }

  @Test
  public void testParseEOSNoMethod1Failure() {
    assertLiveFail(get_ParseEOSNoMethod1_14());
  }

  @Test
  public void testParseEOSNoMethod1NotTerribleLineNumbers() {
    assertNotTerribleLineNumbers(get_ParseEOSNoMethod1_14());
  }

  @Test
  public void testParseEOSNoMethod1ExceptionFree() {
    assertExceptionFree(get_ParseEOSNoMethod1_14());
  }

  @Test
  public void testParseEOSNoMethod1TODOFree() {
    assertTODOFree(get_ParseEOSNoMethod1_14());
  }

  @Test
  public void stable_ParseEOSNoMethod1_14() {
    String live = get_ParseEOSNoMethod1_14();
    StringBuilder gold = new StringBuilder();
    gold.append("Path:HttpClient_ParseEOSNoMethod1_failure.a");
    gold.append("\n--ISSUES-------------------------------------------");
    gold.append("\n[{\"range\":{\"start\":{\"line\":1,\"character\":16,\"byte\":31},\"end\":{\"line\":1,\"character\":17,\"byte\":32}},\"severity\":1,\"source\":\"error\",\"message\":\"File './test_code/HttpClient_ParseEOSNoMethod1_failure.a' failed to parse: Expected http verb, header, secret_header, or {; got end of stream {Token: `;` @ (1,16) -> (1,17): Symbol}\",\"file\":\"./test_code/HttpClient_ParseEOSNoMethod1_failure.a\"},{\"range\":{\"start\":{\"line\":0,\"character\":0,\"byte\":0},\"end\":{\"line\":0,\"character\":0,\"byte\":0}},\"severity\":1,\"source\":\"error\",\"message\":\"Import failed (Parse): Expected http verb, header, secret_header, or {; got end of stream {Token: `;` @ (1,16) -> (1,17): Symbol}\"}]\"--JAVA---------------------------------------------");
    gold.append("\n");
    gold.append("\nFailedValidation");
    assertStable(live, gold);
  }
  private String cached_ParseEOSNoMethod2_15 = null;
  private String get_ParseEOSNoMethod2_15() {
    if (cached_ParseEOSNoMethod2_15 != null) {
      return cached_ParseEOSNoMethod2_15;
    }
    cached_ParseEOSNoMethod2_15 = generateTestOutput(false, "ParseEOSNoMethod2_15", "./test_code/HttpClient_ParseEOSNoMethod2_failure.a");
    return cached_ParseEOSNoMethod2_15;
  }

  @Test
  public void testParseEOSNoMethod2Failure() {
    assertLiveFail(get_ParseEOSNoMethod2_15());
  }

  @Test
  public void testParseEOSNoMethod2NotTerribleLineNumbers() {
    assertNotTerribleLineNumbers(get_ParseEOSNoMethod2_15());
  }

  @Test
  public void testParseEOSNoMethod2ExceptionFree() {
    assertExceptionFree(get_ParseEOSNoMethod2_15());
  }

  @Test
  public void testParseEOSNoMethod2TODOFree() {
    assertTODOFree(get_ParseEOSNoMethod2_15());
  }

  @Test
  public void stable_ParseEOSNoMethod2_15() {
    String live = get_ParseEOSNoMethod2_15();
    StringBuilder gold = new StringBuilder();
    gold.append("Path:HttpClient_ParseEOSNoMethod2_failure.a");
    gold.append("\n--ISSUES-------------------------------------------");
    gold.append("\n[{\"range\":{\"start\":{\"line\":0,\"character\":13,\"byte\":13},\"end\":{\"line\":0,\"character\":14,\"byte\":14}},\"severity\":1,\"source\":\"error\",\"message\":\"File './test_code/HttpClient_ParseEOSNoMethod2_failure.a' failed to parse: Expected http verb, header, secret_header, or {; got end of stream {Token: `{` @ (0,13) -> (0,14): Symbol}\",\"file\":\"./test_code/HttpClient_ParseEOSNoMethod2_failure.a\"},{\"range\":{\"start\":{\"line\":0,\"character\":0,\"byte\":0},\"end\":{\"line\":0,\"character\":0,\"byte\":0}},\"severity\":1,\"source\":\"error\",\"message\":\"Import failed (Parse): Expected http verb, header, secret_header, or {; got end of stream {Token: `{` @ (0,13) -> (0,14): Symbol}\"}]\"--JAVA---------------------------------------------");
    gold.append("\n");
    gold.append("\nFailedValidation");
    assertStable(live, gold);
  }
  private String cached_ParseHappy_16 = null;
  private String get_ParseHappy_16() {
    if (cached_ParseHappy_16 != null) {
      return cached_ParseHappy_16;
    }
    cached_ParseHappy_16 = generateTestOutput(true, "ParseHappy_16", "./test_code/HttpClient_ParseHappy_success.a");
    return cached_ParseHappy_16;
  }

  @Test
  public void testParseHappyEmission() {
    assertEmissionGood(get_ParseHappy_16());
  }

  @Test
  public void testParseHappySuccess() {
    assertLivePass(get_ParseHappy_16());
  }

  @Test
  public void testParseHappyNoFormatException() {
    assertNoFormatException(get_ParseHappy_16());
  }

  @Test
  public void testParseHappyGoodWillHappy() {
    assertGoodWillHappy(get_ParseHappy_16());
  }

  @Test
  public void testParseHappyExceptionFree() {
    assertExceptionFree(get_ParseHappy_16());
  }

  @Test
  public void testParseHappyTODOFree() {
    assertTODOFree(get_ParseHappy_16());
  }

  @Test
  public void stable_ParseHappy_16() {
    String live = get_ParseHappy_16();
    StringBuilder gold = new StringBuilder();
    gold.append("Path:HttpClient_ParseHappy_success.a");
    gold.append("\n--EMISSION-----------------------------------------");
    gold.append("\nEmission Success, Yay");
    gold.append("\n=FORMAT===================================================");
    gold.append("\nmessage M {");
    gold.append("\n  int xyz;");
    gold.append("\n}");
    gold.append("\nmessage V {");
    gold.append("\n  int n;");
    gold.append("\n}");
    gold.append("\nmessage E {");
    gold.append("\n}");
    gold.append("\nclient me { header Authorization = \"Bearer Happy\";get \"/foo/[%xyz]\" <M> foo_me;get \"/foo/[%xyz]\" <M> foo_me_r -> E;put \"/foo/[%xyz]\" <M,V> go_me;endpoint \"https://www.service.service\";endpoint[dev] \"http://192.168.1.42\";{ endpoint \"https://www.service.service1\";endpoint[dev] \"http://192.168.1.51\";get \"/foo/[%xyz]\" <M> foo_me_other_service;} }");
    gold.append("\n==========================================================");
    gold.append("\n--ISSUES-------------------------------------------");
    gold.append("\n[]\"--JAVA---------------------------------------------");
    gold.append("\nimport ape.common.ErrorCodeException;");
    gold.append("\nimport ape.common.Pair;");
    gold.append("\nimport ape.common.SimpleCancel;");
    gold.append("\nimport ape.common.Stream;");
    gold.append("\nimport ape.ErrorCodes;");
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
    gold.append("\npublic class ParseHappy_16 extends LivingDocument {");
    gold.append("\n  @Override");
    gold.append("\n  public long __memory() {");
    gold.append("\n    long __sum = super.__memory() + 2176;");
    gold.append("\n    return __sum;");
    gold.append("\n  }");
    gold.append("\n  public ParseHappy_16(DocumentMonitor __monitor) {");
    gold.append("\n    super(__monitor);");
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
    gold.append("\n    /* root */");
    gold.append("\n  }");
    gold.append("\n  @Override");
    gold.append("\n  public void __writeRxReport(JsonStreamWriter __writer) { }");
    gold.append("\n  public class DeltaPrivacyCache {");
    gold.append("\n    public DeltaPrivacyCache(NtPrincipal __who) {}");
    gold.append("\n  }");
    gold.append("\n  private class DeltaParseHappy_16 implements DeltaNode {");
    gold.append("\n    private boolean __emitted;");
    gold.append("\n    private DeltaParseHappy_16() {");
    gold.append("\n      __emitted = false;");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public long __memory() {");
    gold.append("\n      long __sum = 40;");
    gold.append("\n      return __sum;");
    gold.append("\n    }");
    gold.append("\n    public boolean show(ParseHappy_16 __item, PrivateLazyDeltaWriter __writer) {");
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
    gold.append("\n    ParseHappy_16 __self = this;");
    gold.append("\n    DeltaParseHappy_16 __state = new DeltaParseHappy_16();");
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
    gold.append("\n  private static class RTxM extends NtMessageBase {");
    gold.append("\n    private final RTxM __this;");
    gold.append("\n    private int xyz = 0;");
    gold.append("\n    @Override");
    gold.append("\n    public long __memory() {");
    gold.append("\n      long __mem = 64;");
    gold.append("\n      __mem += 4;");
    gold.append("\n      return __mem;");
    gold.append("\n    }");
    gold.append("\n    public void __reset() {");
    gold.append("\n      this.xyz = 0;");
    gold.append("\n    }");
    gold.append("\n    public void __hash(HashBuilder __hash) {");
    gold.append("\n      __hash.hashString(\"xyz\");");
    gold.append("\n      __hash.hashInteger(this.xyz);");
    gold.append("\n      __hash.hashString(\"M\");");
    gold.append("\n    }");
    gold.append("\n    private static String[] __INDEX_COLUMNS_M = new String[] {};");
    gold.append("\n    @Override");
    gold.append("\n    public String[] __getIndexColumns() {");
    gold.append("\n      return __INDEX_COLUMNS_M;");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public int[] __getIndexValues() {");
    gold.append("\n      return new int[] {};");
    gold.append("\n    }");
    gold.append("\n    private RTxM(JsonStreamReader __reader) {");
    gold.append("\n      __this = this;");
    gold.append("\n      __ingest(__reader);");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public void __ingest(JsonStreamReader __reader) {");
    gold.append("\n      __reader.mustStartObject();");
    gold.append("\n      while (__reader.notEndOfObject()) {");
    gold.append("\n        String __fieldName = __reader.fieldName();");
    gold.append("\n        switch (__fieldName) {");
    gold.append("\n          case \"xyz\":");
    gold.append("\n            this.xyz = __reader.readInteger();");
    gold.append("\n            break;");
    gold.append("\n          default:");
    gold.append("\n            __reader.skipValue();");
    gold.append("\n        }");
    gold.append("\n      }");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public void __writeOut(JsonStreamWriter __writer) {");
    gold.append("\n      __writer.beginObject();");
    gold.append("\n      __writer.writeObjectFieldIntro(\"xyz\");");
    gold.append("\n      __writer.writeInteger(xyz);");
    gold.append("\n      __writer.endObject();");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public void __parsed() throws AbortMessageException {}");
    gold.append("\n    private RTxM() { __this = this; }");
    gold.append("\n    private RTxM(int xyz) {");
    gold.append("\n      this.__this = this;");
    gold.append("\n      this.xyz = xyz;");
    gold.append("\n    }");
    gold.append("\n  }");
    gold.append("\n  private class DeltaRTxM implements DeltaNode {");
    gold.append("\n    private DInt32 __dxyz;");
    gold.append("\n    private boolean __emitted;");
    gold.append("\n    private DeltaRTxM() {");
    gold.append("\n      __dxyz = new DInt32();");
    gold.append("\n      __emitted = false;");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public long __memory() {");
    gold.append("\n      long __sum = 40;");
    gold.append("\n      __sum += __dxyz.__memory();");
    gold.append("\n      return __sum;");
    gold.append("\n    }");
    gold.append("\n    public void show(RTxM __item, PrivateLazyDeltaWriter __writer) {");
    gold.append("\n      __code_cost += 1;");
    gold.append("\n      PrivateLazyDeltaWriter __obj = __writer.planObject();");
    gold.append("\n      __dxyz.show(__item.xyz, __obj.planField(\"xyz\"));");
    gold.append("\n      if (__obj.end()) {");
    gold.append("\n        __emitted = true;");
    gold.append("\n      }");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public void clear() {");
    gold.append("\n      __dxyz.clear();");
    gold.append("\n      __code_cost += 1;");
    gold.append("\n    }");
    gold.append("\n    public void hide(PrivateLazyDeltaWriter __writer) {");
    gold.append("\n      if (__emitted) {");
    gold.append("\n        clear();");
    gold.append("\n        __emitted = false;");
    gold.append("\n        __writer.writeNull();");
    gold.append("\n      }");
    gold.append("\n    }");
    gold.append("\n  }");
    gold.append("\n  private static class RTxV extends NtMessageBase {");
    gold.append("\n    private final RTxV __this;");
    gold.append("\n    private int n = 0;");
    gold.append("\n    @Override");
    gold.append("\n    public long __memory() {");
    gold.append("\n      long __mem = 64;");
    gold.append("\n      __mem += 4;");
    gold.append("\n      return __mem;");
    gold.append("\n    }");
    gold.append("\n    public void __reset() {");
    gold.append("\n      this.n = 0;");
    gold.append("\n    }");
    gold.append("\n    public void __hash(HashBuilder __hash) {");
    gold.append("\n      __hash.hashString(\"n\");");
    gold.append("\n      __hash.hashInteger(this.n);");
    gold.append("\n      __hash.hashString(\"V\");");
    gold.append("\n    }");
    gold.append("\n    private static String[] __INDEX_COLUMNS_V = new String[] {};");
    gold.append("\n    @Override");
    gold.append("\n    public String[] __getIndexColumns() {");
    gold.append("\n      return __INDEX_COLUMNS_V;");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public int[] __getIndexValues() {");
    gold.append("\n      return new int[] {};");
    gold.append("\n    }");
    gold.append("\n    private RTxV(JsonStreamReader __reader) {");
    gold.append("\n      __this = this;");
    gold.append("\n      __ingest(__reader);");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public void __ingest(JsonStreamReader __reader) {");
    gold.append("\n      __reader.mustStartObject();");
    gold.append("\n      while (__reader.notEndOfObject()) {");
    gold.append("\n        String __fieldName = __reader.fieldName();");
    gold.append("\n        switch (__fieldName) {");
    gold.append("\n          case \"n\":");
    gold.append("\n            this.n = __reader.readInteger();");
    gold.append("\n            break;");
    gold.append("\n          default:");
    gold.append("\n            __reader.skipValue();");
    gold.append("\n        }");
    gold.append("\n      }");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public void __writeOut(JsonStreamWriter __writer) {");
    gold.append("\n      __writer.beginObject();");
    gold.append("\n      __writer.writeObjectFieldIntro(\"n\");");
    gold.append("\n      __writer.writeInteger(n);");
    gold.append("\n      __writer.endObject();");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public void __parsed() throws AbortMessageException {}");
    gold.append("\n    private RTxV() { __this = this; }");
    gold.append("\n    private RTxV(int n) {");
    gold.append("\n      this.__this = this;");
    gold.append("\n      this.n = n;");
    gold.append("\n    }");
    gold.append("\n  }");
    gold.append("\n  private class DeltaRTxV implements DeltaNode {");
    gold.append("\n    private DInt32 __dn;");
    gold.append("\n    private boolean __emitted;");
    gold.append("\n    private DeltaRTxV() {");
    gold.append("\n      __dn = new DInt32();");
    gold.append("\n      __emitted = false;");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public long __memory() {");
    gold.append("\n      long __sum = 40;");
    gold.append("\n      __sum += __dn.__memory();");
    gold.append("\n      return __sum;");
    gold.append("\n    }");
    gold.append("\n    public void show(RTxV __item, PrivateLazyDeltaWriter __writer) {");
    gold.append("\n      __code_cost += 1;");
    gold.append("\n      PrivateLazyDeltaWriter __obj = __writer.planObject();");
    gold.append("\n      __dn.show(__item.n, __obj.planField(\"n\"));");
    gold.append("\n      if (__obj.end()) {");
    gold.append("\n        __emitted = true;");
    gold.append("\n      }");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public void clear() {");
    gold.append("\n      __dn.clear();");
    gold.append("\n      __code_cost += 1;");
    gold.append("\n    }");
    gold.append("\n    public void hide(PrivateLazyDeltaWriter __writer) {");
    gold.append("\n      if (__emitted) {");
    gold.append("\n        clear();");
    gold.append("\n        __emitted = false;");
    gold.append("\n        __writer.writeNull();");
    gold.append("\n      }");
    gold.append("\n    }");
    gold.append("\n  }");
    gold.append("\n  private static class RTxE extends NtMessageBase {");
    gold.append("\n    private final RTxE __this;");
    gold.append("\n    @Override");
    gold.append("\n    public long __memory() { return 64; }");
    gold.append("\n    public void __reset() {}");
    gold.append("\n    public void __hash(HashBuilder __hash) {");
    gold.append("\n      __hash.hashString(\"E\");");
    gold.append("\n    }");
    gold.append("\n    private static String[] __INDEX_COLUMNS_E = new String[] {};");
    gold.append("\n    @Override");
    gold.append("\n    public String[] __getIndexColumns() {");
    gold.append("\n      return __INDEX_COLUMNS_E;");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public int[] __getIndexValues() {");
    gold.append("\n      return new int[] {};");
    gold.append("\n    }");
    gold.append("\n    private RTxE(JsonStreamReader __reader) {");
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
    gold.append("\n    private RTxE() { __this = this; }");
    gold.append("\n  }");
    gold.append("\n  private class DeltaRTxE implements DeltaNode {");
    gold.append("\n    private boolean __emitted;");
    gold.append("\n    private DeltaRTxE() {");
    gold.append("\n      __emitted = false;");
    gold.append("\n    }");
    gold.append("\n    @Override");
    gold.append("\n    public long __memory() {");
    gold.append("\n      long __sum = 40;");
    gold.append("\n      return __sum;");
    gold.append("\n    }");
    gold.append("\n    public void show(RTxE __item, PrivateLazyDeltaWriter __writer) {");
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
    gold.append("\n  public static void __create_generic_clients(ServiceRegistry __registry, HeaderDecryptor __decryptor) throws Exception {");
    gold.append("\n    GenericClient me = __registry.makeGenericClient();");
    gold.append("\n    HeaderGroup __headers_1 = new HeaderGroup(null);");
    gold.append("\n    __headers_1.add(\"Authorization\", \"Bearer Happy\");");
    gold.append("\n    me.register(\"foo_me\", \"get\", \"https://www.service.service\", __headers_1, (o) -> GenericClient.URL(\"/foo/[%xyz]\", o));");
    gold.append("\n    me.register(\"foo_me_r\", \"get\", \"https://www.service.service\", __headers_1, (o) -> GenericClient.URL(\"/foo/[%xyz]\", o));");
    gold.append("\n    me.register(\"go_me\", \"put\", \"https://www.service.service\", __headers_1, (o) -> GenericClient.URL(\"/foo/[%xyz]\", o));");
    gold.append("\n    __registry.add(\"me\", me);");
    gold.append("\n");
    gold.append("\n  }");
    gold.append("\n  protected GenericClient me;");
    gold.append("\n  @Override");
    gold.append("\n  public void __link(ServiceRegistry __registry) {");
    gold.append("\n    me = __registry.getClient(\"me\");");
    gold.append("\n");
    gold.append("\n  }");
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
    gold.append("\n  public SimpleCancel __export(CoreRequestContext __context, String __name, String __viewerState, Stream<String> __stream) {");
    gold.append("\n    __stream.failure(new ErrorCodeException(ErrorCodes.LIVING_DOCUMENT_NO_EXPORT_BY_NAME));");
    gold.append("\n    return SimpleCancel.NOTHING_TO_CANCEL;");
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
    gold.append("\n  @Override");
    gold.append("\n  protected void __construct_intern(CoreRequestContext _c, NtMessageBase _m) {}");
    gold.append("\n  @Override");
    gold.append("\n  protected NtMessageBase __parse_construct_arg(JsonStreamReader __reader) {");
    gold.append("\n    __reader.skipValue();");
    gold.append("\n    return NtMessageBase.NULL;");
    gold.append("\n  }");
    gold.append("\n  /* end of file */");
    gold.append("\n}");
    gold.append("\n");
    gold.append("\n--=[LivingDocumentFactory COMPILING]=---");
    gold.append("\n--=[LivingDocumentFactory MADE]=---");
    gold.append("\n--REFLECTION RESULTS-------------------------------------");
    gold.append("\n{\"types\":{\"__Root\":{\"nature\":\"reactive_record\",\"name\":\"Root\",\"fields\":{}},\"__ViewerType\":{\"nature\":\"native_message\",\"name\":\"__ViewerType\",\"anonymous\":true,\"fields\":{}},\"M\":{\"nature\":\"native_message\",\"name\":\"M\",\"anonymous\":false,\"fields\":{\"xyz\":{\"type\":{\"nature\":\"native_value\",\"type\":\"int\"},\"computed\":false,\"privacy\":\"public\"}}},\"V\":{\"nature\":\"native_message\",\"name\":\"V\",\"anonymous\":false,\"fields\":{\"n\":{\"type\":{\"nature\":\"native_value\",\"type\":\"int\"},\"computed\":false,\"privacy\":\"public\"}}},\"E\":{\"nature\":\"native_message\",\"name\":\"E\",\"anonymous\":false,\"fields\":{}}},\"channels\":{},\"channels-privacy\":{},\"constructors\":[],\"labels\":[]}");
    gold.append("\n--JAVA RUNNING-------------------------------------");
    gold.append("\n{\"command\":\"construct\",\"timestamp\":\"0\",\"who\":{\"agent\":\"?\",\"authority\":\"?\"},\"arg\":{},\"entropy\":\"0\",\"key\":\"0\",\"origin\":\"origin\",\"ip\":\"ip\"}-->{\"__constructed\":true,\"__entropy\":\"-4962768465676381896\",\"__seq\":1} need:false in:0");
    gold.append("\n{\"command\":\"invalidate\",\"timestamp\":\"25\"}-->{\"__seq\":2,\"__entropy\":\"4804307197456638271\",\"__time\":\"25\"} need:false in:0");
    gold.append("\nCPU:0");
    gold.append("\nMEMORY:2560");
    gold.append("\n{\"command\":\"invalidate\",\"timestamp\":\"50\"}-->{\"__seq\":3,\"__entropy\":\"-1034601897293430941\",\"__time\":\"50\"} need:false in:0");
    gold.append("\nNO_ONE: CREATED PRIVATE VIEW");
    gold.append("\n+ NO_ONE DELTA:{\"seq\":3}");
    gold.append("\nNO_ONE|FAILURE:184333");
    gold.append("\n{\"command\":\"invalidate\",\"timestamp\":\"75\"}-->{\"__seq\":4,\"__entropy\":\"7848011421992302230\",\"__time\":\"75\"} need:false in:0");
    gold.append("\nRANDO: CREATED PRIVATE VIEW");
    gold.append("\n+ NO_ONE DELTA:{\"seq\":4}");
    gold.append("\n+ RANDO DELTA:{\"seq\":4}");
    gold.append("\nRANDO|FAILURE:184333");
    gold.append("\n{\"command\":\"invalidate\",\"timestamp\":\"100\"}-->{\"__seq\":5,\"__entropy\":\"-8929183248358367000\",\"__time\":\"100\"} need:false in:0");
    gold.append("\nRANDO|SUCCESS:5");
    gold.append("\n+ NO_ONE DELTA:{\"seq\":5}");
    gold.append("\n+ RANDO DELTA:{\"seq\":5}");
    gold.append("\nMEMORY:2678");
    gold.append("\n--JAVA RESULTS-------------------------------------");
    gold.append("\n{\"__snapshot\":\"0/0\",\"__state\":\"\",\"__constructed\":true,\"__next_time\":\"0\",\"__last_expire_time\":\"0\",\"__blocked\":false,\"__seq\":5,\"__entropy\":\"-8929183248358367000\",\"__auto_future_id\":0,\"__connection_id\":0,\"__message_id\":0,\"__time\":\"100\",\"__timezone\":\"UTC\",\"__auto_table_row_id\":0,\"__auto_gen\":0,\"__auto_cache_id\":0,\"__cache\":{},\"__webTaskId\":0,\"__webqueue\":{},\"__replication\":{}}");
    gold.append("\n--DUMP RESULTS-------------------------------------");
    gold.append("\n{}");
    gold.append("\n--METRIC RESULTS-----------------------------------");
    gold.append("\n{\"__snapshot\":\"0/0\",\"__state\":\"\",\"__constructed\":true,\"__next_time\":\"0\",\"__last_expire_time\":\"0\",\"__blocked\":false,\"__seq\":5,\"__entropy\":\"-8929183248358367000\",\"__auto_future_id\":0,\"__connection_id\":0,\"__message_id\":0,\"__time\":\"100\",\"__timezone\":\"UTC\",\"__auto_table_row_id\":0,\"__auto_gen\":0,\"__auto_cache_id\":0,\"__cache\":{},\"__webTaskId\":0,\"__webqueue\":{},\"__replication\":{}}");
    gold.append("\n{\"__snapshot\":\"0/0\",\"__state\":\"\",\"__constructed\":true,\"__next_time\":\"0\",\"__last_expire_time\":\"0\",\"__blocked\":false,\"__seq\":5,\"__entropy\":\"-8929183248358367000\",\"__auto_future_id\":0,\"__connection_id\":0,\"__message_id\":0,\"__time\":\"100\",\"__timezone\":\"UTC\",\"__auto_table_row_id\":0,\"__auto_gen\":0,\"__auto_cache_id\":0,\"__cache\":{},\"__webTaskId\":0,\"__webqueue\":{},\"__replication\":{}}");
    gold.append("\n--JAVA TEST RESULTS--------------------------------");
    gold.append("\n");
    gold.append("\nSuccess");
    assertStable(live, gold);
  }
  private String cached_TooManyEndpoint_17 = null;
  private String get_TooManyEndpoint_17() {
    if (cached_TooManyEndpoint_17 != null) {
      return cached_TooManyEndpoint_17;
    }
    cached_TooManyEndpoint_17 = generateTestOutput(false, "TooManyEndpoint_17", "./test_code/HttpClient_TooManyEndpoint_failure.a");
    return cached_TooManyEndpoint_17;
  }

  @Test
  public void testTooManyEndpointFailure() {
    assertLiveFail(get_TooManyEndpoint_17());
  }

  @Test
  public void testTooManyEndpointNotTerribleLineNumbers() {
    assertNotTerribleLineNumbers(get_TooManyEndpoint_17());
  }

  @Test
  public void testTooManyEndpointExceptionFree() {
    assertExceptionFree(get_TooManyEndpoint_17());
  }

  @Test
  public void testTooManyEndpointTODOFree() {
    assertTODOFree(get_TooManyEndpoint_17());
  }

  @Test
  public void stable_TooManyEndpoint_17() {
    String live = get_TooManyEndpoint_17();
    StringBuilder gold = new StringBuilder();
    gold.append("Path:HttpClient_TooManyEndpoint_failure.a");
    gold.append("\n--ISSUES-------------------------------------------");
    gold.append("\n[{\"range\":{\"start\":{\"line\":5,\"character\":2,\"byte\":101},\"end\":{\"line\":5,\"character\":41,\"byte\":140}},\"severity\":1,\"source\":\"error\",\"message\":\"Endpoint already defined for 'prod'\",\"file\":\"./test_code/HttpClient_TooManyEndpoint_failure.a\"}]\"--JAVA---------------------------------------------");
    gold.append("\n");
    gold.append("\nFailedValidation");
    assertStable(live, gold);
  }
}
