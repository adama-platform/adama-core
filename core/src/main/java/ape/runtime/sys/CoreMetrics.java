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
package ape.runtime.sys;

import ape.common.metrics.CallbackMonitor;
import ape.common.metrics.Inflight;
import ape.common.metrics.MetricsFactory;

/** metrics for the core adama service */
public class CoreMetrics {
  public final CallbackMonitor serviceCreate;
  public final CallbackMonitor serviceLoad;
  public final CallbackMonitor factoryFetchCreate;
  public final CallbackMonitor factoryFetchConnect;
  public final CallbackMonitor factoryFetchLoad;
  public final CallbackMonitor factoryFetchDeploy;
  public final CallbackMonitor documentFresh;
  public final CallbackMonitor documentLoad;
  public final CallbackMonitor documentLoadBaseServiceGet;
  public final CallbackMonitor documentLoadRunLoad;
  public final CallbackMonitor documentPiggyBack;
  public final CallbackMonitor implicitCreate;
  public final CallbackMonitor deploy;
  public final CallbackMonitor create_private_view;
  public final CallbackMonitor reflect;
  public final CallbackMonitor deliver;
  public final CallbackMonitor catch_up_patch;
  public final Inflight inflight_streams;
  public final Inflight inflight_documents;
  public final CallbackMonitor document_invalidate;
  public final CallbackMonitor document_send;
  public final CallbackMonitor document_delete;
  public final CallbackMonitor document_attach;
  public final CallbackMonitor document_web_put;
  public final CallbackMonitor document_web_delete;
  public final CallbackMonitor document_apply;
  public final CallbackMonitor document_disconnect;
  public final CallbackMonitor document_connect;
  public final CallbackMonitor document_execute_patch;
  public final CallbackMonitor document_password;
  public final CallbackMonitor document_snapshot;
  public final Runnable document_queue_full;
  public final Runnable document_queue_running_behind;
  public final Runnable document_catastrophic_failure;
  public final Runnable document_load_shed;
  public final Runnable document_compacting_skipped;
  public final Runnable document_compacting;
  public final Runnable failed_invention;
  public final Runnable internal_seq_drift;
  public final Runnable document_collision;
  public final Runnable snapshot_recovery;
  public final CallbackMonitor document_load_startup;

  public final Runnable trigger_deployment;
  public final Runnable invalidation_limit_reached;

  public final CallbackMonitor document_backup;
  public final CallbackMonitor document_wake;

  public CoreMetrics(MetricsFactory metricsFactory) {
    serviceCreate = metricsFactory.makeCallbackMonitor("core_service_create");
    serviceLoad = metricsFactory.makeCallbackMonitor("core_service_load");
    factoryFetchCreate = metricsFactory.makeCallbackMonitor("core_factory_fetch_create");
    factoryFetchConnect = metricsFactory.makeCallbackMonitor("core_factory_fetch_connect");
    factoryFetchLoad = metricsFactory.makeCallbackMonitor("core_factory_fetch_load");
    factoryFetchDeploy = metricsFactory.makeCallbackMonitor("core_factory_fetch_deploy");
    documentFresh = metricsFactory.makeCallbackMonitor("core_documents_fresh");
    documentLoad = metricsFactory.makeCallbackMonitor("core_documents_load");
    documentLoadBaseServiceGet = metricsFactory.makeCallbackMonitor("core_documents_load_base_get");
    documentLoadRunLoad = metricsFactory.makeCallbackMonitor("core_documents_load_run_load");
    documentPiggyBack = metricsFactory.makeCallbackMonitor("core_documents_piggy_back");
    implicitCreate = metricsFactory.makeCallbackMonitor("core_implicit_create");
    deploy = metricsFactory.makeCallbackMonitor("core_deploy");
    create_private_view = metricsFactory.makeCallbackMonitor("core_create_private_view");
    reflect = metricsFactory.makeCallbackMonitor("core_reflect");
    deliver = metricsFactory.makeCallbackMonitor("core_deliver");
    catch_up_patch = metricsFactory.makeCallbackMonitor("core_catch_up_patch");
    document_invalidate = metricsFactory.makeCallbackMonitor("core_document_invalidate");
    document_send = metricsFactory.makeCallbackMonitor("core_document_send");
    document_delete = metricsFactory.makeCallbackMonitor("core_document_delete");
    document_attach = metricsFactory.makeCallbackMonitor("core_document_attach");
    document_web_put = metricsFactory.makeCallbackMonitor("core_document_web_put");
    document_web_delete = metricsFactory.makeCallbackMonitor("core_document_web_delete");
    document_apply = metricsFactory.makeCallbackMonitor("core_document_apply");
    document_disconnect = metricsFactory.makeCallbackMonitor("core_document_disconnect");
    document_connect = metricsFactory.makeCallbackMonitor("core_document_connect");
    document_execute_patch = metricsFactory.makeCallbackMonitor("core_document_execute_patch");
    document_password = metricsFactory.makeCallbackMonitor("core_document_document_password");
    document_snapshot = metricsFactory.makeCallbackMonitor("core_document_snapshot");
    inflight_streams = metricsFactory.inflight("core_inflight_streams");
    inflight_documents = metricsFactory.inflight("core_inflight_documents");
    document_queue_full = metricsFactory.counter("core_document_full");
    document_queue_running_behind = metricsFactory.counter("core_document_queue_running_behind");
    document_catastrophic_failure = metricsFactory.counter("core_document_catastrophic_failure");
    document_load_shed = metricsFactory.counter("core_document_load_shed");
    document_compacting_skipped = metricsFactory.counter("core_document_compacting_skipped");
    document_compacting = metricsFactory.counter("core_document_compacting");
    failed_invention = metricsFactory.counter("core_document_failed_invention");
    internal_seq_drift = metricsFactory.counter("core_document_internal_seq_drift");
    document_collision = metricsFactory.counter("core_document_collision");
    document_load_startup = metricsFactory.makeCallbackMonitor("core_document_load_startup");

    trigger_deployment = metricsFactory.counter("core_trigger_deployment");
    invalidation_limit_reached = metricsFactory.counter("core_invalidation_limit_reached");
    snapshot_recovery = metricsFactory.counter("core_document_snapshot_recovery");

    document_backup = metricsFactory.makeCallbackMonitor("core_document_backup");
    document_wake = metricsFactory.makeCallbackMonitor("core_document_wake");
  }
}
