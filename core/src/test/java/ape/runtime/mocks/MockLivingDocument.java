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
package ape.runtime.mocks;

import ape.ErrorCodes;
import ape.common.ErrorCodeException;
import ape.common.SimpleCancel;
import ape.common.Stream;
import ape.runtime.async.AsyncTask;
import ape.runtime.contracts.DocumentMonitor;
import ape.runtime.contracts.Perspective;
import ape.runtime.data.Key;
import ape.runtime.exceptions.AbortMessageException;
import ape.runtime.json.JsonStreamReader;
import ape.runtime.json.JsonStreamWriter;
import ape.runtime.json.PrivateView;
import ape.runtime.natives.NtAsset;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.natives.NtMessageBase;
import ape.runtime.ops.TestReportBuilder;
import ape.runtime.remote.Service;
import ape.runtime.remote.ServiceRegistry;
import ape.runtime.remote.replication.MockReplicationService;
import ape.runtime.sys.AuthResponse;
import ape.runtime.sys.CoreRequestContext;
import ape.runtime.sys.LivingDocument;
import ape.runtime.sys.web.WebDelete;
import ape.runtime.sys.web.WebGet;
import ape.runtime.sys.web.WebPut;
import ape.runtime.sys.web.WebResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MockLivingDocument extends LivingDocument {
  public final ArrayList<NtPrincipal> connects;
  public final ArrayList<NtPrincipal> disconnects;
  public final MockReplicationService rservice;

  public MockLivingDocument() {
    super(null);
    connects = new ArrayList<>();
    disconnects = new ArrayList<>();
    rservice = new MockReplicationService();
  }

  public MockLivingDocument(final DocumentMonitor monitor) {
    super(monitor);
    connects = new ArrayList<>();
    disconnects = new ArrayList<>();
    rservice = new MockReplicationService();
  }

  @Override
  public String __traffic(CoreRequestContext __context) {
    return "main";
  }

  @Override
  public String __metrics() {
    return "{}";
  }

  @Override
  public void __make_cron_progress() {
  }

  @Override
  public Long __predict_cron_wake_time() {
    return null;
  }

  @Override
  public void __reset_cron() {
  }

  @Override
  protected boolean __is_direct_channel(String channel) {
    return "__direct".equals(channel);
  }

  @Override
  protected void __handle_direct(CoreRequestContext who, String channel, Object message) throws AbortMessageException {
  }

  @Override
  public void __writeRxReport(JsonStreamWriter __writer) {
  }

  @Override
  protected void __link(ServiceRegistry registry) {
  }

  @Override
  public void __onLoad() {
  }

  @Override
  protected void __debug(JsonStreamWriter __writer) {

  }

  @Override
  public Service __findService(String name) {
    if ("rservice".equals(name)) {
      return rservice;
    }
    return null;
  }

  @Override
  protected long __computeGraphs() {
    return 0L;
  }

  @Override
  public AuthResponse __authpipe(CoreRequestContext __context, String __messsage) {
    return null;
  }

  @Override
  public String __getViewStateFilter() {
    return "[]";
  }

  @Override
  public boolean __open_channel(String name) {
    return false;
  }

  @Override
  public Set<String> __get_intern_strings() {
    return new HashSet<>();
  }

  @Override
  protected void __construct_intern(CoreRequestContext context, final NtMessageBase message) {}

  @Override
  public PrivateView __createPrivateView(final NtPrincipal __who, final Perspective __perspective) {
    return null;
  }

  @Override
  public WebResponse __get_internal(CoreRequestContext __context, WebGet __get) {
    return null;
  }

  @Override
  public WebResponse __options(CoreRequestContext __context, WebGet __get) {
    return null;
  }

  @Override
  protected WebResponse __put_internal(CoreRequestContext __context, WebPut __get) {
    return null;
  }

  @Override
  protected WebResponse __delete_internal(CoreRequestContext __context, WebDelete __delete) {
    return null;
  }

  @Override
  public void __dump(final JsonStreamWriter __writer) {}

  @Override
  public String[] __getTests() {
    return new String[0];
  }

  @Override
  public void __revert() {}

  @Override
  protected Object __parse_message(final String channel, final JsonStreamReader reader) {
    return null;
  }

  @Override
  public void __insert(final JsonStreamReader __reader) {}

  @Override
  public void __patch(JsonStreamReader __reader) {}

  @Override
  protected void __invoke_label(final String __new_state) {}

  @Override
  public boolean __onConnected(final CoreRequestContext context) {
    connects.add(context.who);
    return true;
  }

  @Override
  public boolean __delete(CoreRequestContext context) {
    return context.who.authority.equals("overlord");
  }

  @Override
  public void __onDisconnected(final CoreRequestContext context) {
    disconnects.add(context.who);
  }

  @Override
  public void __onAssetAttached(CoreRequestContext __cvalue, NtAsset __asset) {}

  @Override
  public boolean __onCanAssetAttached(CoreRequestContext __cvalue) {
    return false;
  }

  @Override
  protected NtMessageBase __parse_construct_arg(final JsonStreamReader message) {
    return null;
  }

  @Override
  protected void __reset_future_queues() {}

  @Override
  protected void __route(final AsyncTask task) {}

  @Override
  public void __test(final TestReportBuilder report, final String testName) {}

  @Override
  public void __commit(
      final String name, final JsonStreamWriter writer, final JsonStreamWriter reverse) {}

  @Override
  public void __settle(Set<Integer> viewers) {
  }

  @Override
  public SimpleCancel __export(CoreRequestContext __context, String __name, String __viewerState, Stream<String> __stream) {
    __stream.failure(new ErrorCodeException(ErrorCodes.LIVING_DOCUMENT_NO_EXPORT_BY_NAME));
    return SimpleCancel.NOTHING_TO_CANCEL;
  }
}
