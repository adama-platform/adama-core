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
package ape.caravan;

import ape.caravan.data.DurableListStore;
import ape.caravan.data.DiskMetrics;
import ape.caravan.events.LocalCache;
import ape.caravan.events.RestoreDebuggerStdErr;
import ape.caravan.events.RestoreLoader;
import ape.caravan.mocks.*;
import ape.runtime.data.*;
import ape.caravan.mocks.*;
import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.SimpleExecutor;
import ape.common.Stream;
import ape.common.metrics.NoOpMetricsFactory;
import ape.runtime.contracts.DeleteTask;
import ape.runtime.data.*;
import ape.runtime.natives.NtPrincipal;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class CaravanDataServiceTests {
  public static final Key KEY1 = new Key("space", "123");
  public static final Key KEY2 = new Key("space", "1232");
  private static final RemoteDocumentUpdate UPDATE_1 =
      new RemoteDocumentUpdate(
          1, 1, NtPrincipal.NO_ONE, "REQUEST", "{\"x\":1,\"y\":4}", "{\"x\":0,\"y\":0}", false, 0, 100, UpdateType.AddUserData);
  private static final RemoteDocumentUpdate UPDATE_2 =
      new RemoteDocumentUpdate(
          2, 2, null, "REQUEST", "{\"x\":2}", "{\"x\":1,\"z\":42}", true, 0, 200, UpdateType.AddUserData);
  private static final RemoteDocumentUpdate UPDATE_3 =
      new RemoteDocumentUpdate(
          3, 3, null, "REQUEST", "{\"x\":3}", "{\"x\":2,\"z\":42}", true, 0, 0, UpdateType.AddUserData);
  private static final RemoteDocumentUpdate UPDATE_4 =
      new RemoteDocumentUpdate(
          4, 4, null, "REQUEST", "{\"x\":4}", "{\"x\":3,\"z\":42}", true, 0, 300, UpdateType.AddUserData);

  private static class Setup {
    private final DurableListStore store;
    private final SimpleExecutor executor;
    private final File root;
    private final CaravanDataService service;
    private final Thread flusher;
    private final MockCloud cloud;

    public Setup() throws Exception {
      this.executor = SimpleExecutor.create("executor");
      this.root = new File(File.createTempFile("ADAMATEST_", "yyy").getParentFile(), "base-" + System.currentTimeMillis());
      root.delete();
      root.mkdirs();
      this.store = new DurableListStore(new DiskMetrics(new NoOpMetricsFactory()), new File(root, "STORE"), root, 1024 * 1024, 64 * 1024, 1024 * 1024 * 32);
      this.cloud = new MockCloud();
      this.service = new CaravanDataService(new CaravanMetrics(new NoOpMetricsFactory()), cloud, store, executor);
      this.flusher = new Thread(new Runnable() {
        @Override
        public void run() {
          while (true) {
            try {
              Thread.sleep(0, 800000);
              service.flush(false).await(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ie) {
              return;
            }
          }
        }
      });
      this.flusher.start();
    }

    private void clean() throws Exception {
      service.flush(true).await(10000, TimeUnit.MILLISECONDS);
      this.flusher.interrupt();
      this.flusher.join();
      store.shutdown();
    }
  }

  private void flow(BadConsumer<Setup> test) throws Exception {
    Setup setup = new Setup();
    try {
      test.accept(setup);
    } finally {
      setup.clean();
    }
  }

  @Test
  public void flow() throws Exception {
    flow((setup) -> {
      SimpleDataCallback cb_GetFailed = new SimpleDataCallback();
      setup.service.get(KEY1, cb_GetFailed);
      cb_GetFailed.assertFailure(625676);

      SimpleMockCallback cb_PatchFailsFNF = new SimpleMockCallback();
      setup.service.patch(KEY1, new RemoteDocumentUpdate[] { UPDATE_2 }, cb_PatchFailsFNF);
      cb_PatchFailsFNF.assertFailure(625676);

      SimpleIntCallback cb_CompactFailsFNF = new SimpleIntCallback();
      setup.service.snapshot(KEY1, new DocumentSnapshot(1, "{}", 1, 1234L), cb_CompactFailsFNF);
      cb_CompactFailsFNF.assertFailure(625676);

      SimpleDataCallback cb_ComputeFailsFNF_Rewind = new SimpleDataCallback();
      setup.service.compute(KEY1, ComputeMethod.Rewind, 1, cb_ComputeFailsFNF_Rewind);
      cb_ComputeFailsFNF_Rewind.assertFailure(625676);

      SimpleDataCallback cb_ComputeFailsFNF_HeadPatch= new SimpleDataCallback();
      setup.service.compute(KEY1, ComputeMethod.HeadPatch, 1, cb_ComputeFailsFNF_HeadPatch);
      cb_ComputeFailsFNF_HeadPatch.assertFailure(625676);

      SimpleMockCallback cb_DeleteSuccessFNF = new SimpleMockCallback();
      setup.service.delete(KEY1, DeleteTask.TRIVIAL, cb_DeleteSuccessFNF);
      cb_DeleteSuccessFNF.assertSuccess();

      SimpleMockCallback cb_InitSuccess = new SimpleMockCallback();
      setup.service.initialize(KEY1, UPDATE_1, cb_InitSuccess);
      cb_InitSuccess.assertSuccess();

      {
        CountDownLatch latchInventory = new CountDownLatch(1);
        setup.service.inventory(new Callback<Set<Key>>() {
          @Override
          public void success(Set<Key> keys) {
            Assert.assertTrue(keys.contains(KEY1));
            latchInventory.countDown();
          }

          @Override
          public void failure(ErrorCodeException ex) {

          }
        });
        Assert.assertTrue(latchInventory.await(5000, TimeUnit.MILLISECONDS));
      }

      SimpleDataCallback cb_GetWorksNow = new SimpleDataCallback();
      setup.service.get(KEY1, cb_GetWorksNow);
      cb_GetWorksNow.assertSuccess();
      Assert.assertEquals("{\"x\":1,\"y\":4}", cb_GetWorksNow.value);
      Assert.assertEquals(1, cb_GetWorksNow.reads);

      SimpleMockCallback cb_PatchOne = new SimpleMockCallback();
      setup.service.patch(KEY1, new RemoteDocumentUpdate[] { UPDATE_2 }, cb_PatchOne);
      cb_PatchOne.assertSuccess();

      {
        SimpleMockCallback cb_PatchFourAheadOfSchedule = new SimpleMockCallback();
        setup.service.patch(KEY1, new RemoteDocumentUpdate[] { UPDATE_4 }, cb_PatchFourAheadOfSchedule);
        cb_PatchFourAheadOfSchedule.assertFailure(621580);
      }

      SimpleMockCallback cb_PatchTwo = new SimpleMockCallback();
      setup.service.patch(KEY1, new RemoteDocumentUpdate[] { UPDATE_3, UPDATE_4 }, cb_PatchTwo);
      cb_PatchTwo.assertSuccess();

      {
        SimpleDataCallback cb_Rewind = new SimpleDataCallback();
        setup.service.compute(KEY1, ComputeMethod.Rewind, 2, cb_Rewind);
        cb_Rewind.assertSuccess();
        Assert.assertEquals(1, cb_Rewind.reads);
        Assert.assertEquals("{\"x\":1,\"z\":42}", cb_Rewind.value);
      }
      {
        SimpleDataCallback cb_Patch = new SimpleDataCallback();
        setup.service.compute(KEY1, ComputeMethod.HeadPatch, 2, cb_Patch);
        cb_Patch.assertSuccess();
        Assert.assertEquals(1, cb_Patch.reads);
        Assert.assertEquals("{\"x\":4}", cb_Patch.value);
      }

      {
        SimpleDataCallback cb_GetIsMergedResults = new SimpleDataCallback();
        setup.service.get(KEY1, cb_GetIsMergedResults);
        cb_GetIsMergedResults.assertSuccess();
        Assert.assertEquals("{\"x\":4,\"y\":4}", cb_GetIsMergedResults.value);
        Assert.assertEquals(4, cb_GetIsMergedResults.reads);
      }

      {
        SimpleMockCallback cb_Close = new SimpleMockCallback();
        setup.service.close(KEY1, cb_Close);
        cb_Close.assertSuccess();
      }

      {
        SimpleMockCallback cb_Close = new SimpleMockCallback();
        setup.service.close(KEY1, cb_Close);
        cb_Close.assertSuccess();
      }

      {
        SimpleDataCallback cb_GetIsMergedResults = new SimpleDataCallback();
        setup.service.get(KEY1, cb_GetIsMergedResults);
        cb_GetIsMergedResults.assertSuccess();
        Assert.assertEquals("{\"x\":4,\"y\":4}", cb_GetIsMergedResults.value);
        Assert.assertEquals(4, cb_GetIsMergedResults.reads);
      }

      {
        SimpleDataCallback cb_Patch = new SimpleDataCallback();
        setup.service.compute(KEY1, ComputeMethod.HeadPatch, 2, cb_Patch);
        cb_Patch.assertSuccess();
        Assert.assertEquals("{\"x\":4}", cb_Patch.value);
      }
      {
        SimpleDataCallback cb_Patch = new SimpleDataCallback();
        setup.service.compute(KEY1, ComputeMethod.HeadPatch, -100, cb_Patch);
        cb_Patch.assertSuccess();
        Assert.assertEquals("{\"x\":4,\"y\":4}", cb_Patch.value);
      }
      {
        SimpleDataCallback cb_Patch = new SimpleDataCallback();
        setup.service.compute(KEY1, ComputeMethod.HeadPatch, 100, cb_Patch);
        cb_Patch.assertFailure(787507);
      }
      {
        SimpleDataCallback cb_Rewind = new SimpleDataCallback();
        setup.service.compute(KEY1, ComputeMethod.Rewind, 2, cb_Rewind);
        cb_Rewind.assertSuccess();
        Assert.assertEquals(1, cb_Rewind.reads);
        Assert.assertEquals("{\"x\":1,\"z\":42}", cb_Rewind.value);
      }
      {
        SimpleDataCallback cb_Rewind = new SimpleDataCallback();
        setup.service.compute(KEY1, ComputeMethod.Rewind, -100, cb_Rewind);
        cb_Rewind.assertFailure(791602);
      }
      {
        SimpleDataCallback cb_Rewind = new SimpleDataCallback();
        setup.service.compute(KEY1, ComputeMethod.Rewind, 100, cb_Rewind);
        cb_Rewind.assertFailure(791602);
      }

      {
        SimpleDataCallback cbUnknown = new SimpleDataCallback();
        setup.service.compute(KEY1, null, 100, cbUnknown);
        cbUnknown.assertFailure(785491);
      }

      {
        SimpleIntCallback cb_CompactWorks = new SimpleIntCallback();
        setup.service.snapshot(KEY1, new DocumentSnapshot(4, "{\"x\":10,\"y\":10}", 100, 1234L), cb_CompactWorks);
        cb_CompactWorks.assertSuccess(0);
        SimpleDataCallback cb_GetCompactedResults = new SimpleDataCallback();
        setup.service.get(KEY1, cb_GetCompactedResults);
        cb_GetCompactedResults.assertSuccess();
        Assert.assertEquals("{\"x\":10,\"y\":10}", cb_GetCompactedResults.value);
        Assert.assertEquals(5, cb_GetCompactedResults.reads);
      }

      {
        SimpleIntCallback cbCompactFailsNegHistory = new SimpleIntCallback();
        setup.service.snapshot(KEY1, new DocumentSnapshot(1, "{}", -1, 1234L), cbCompactFailsNegHistory);
        cbCompactFailsNegHistory.assertFailure(734263);
      }

      {
        SimpleIntCallback cb_CompactWorks = new SimpleIntCallback();
        setup.service.snapshot(KEY1, new DocumentSnapshot(1, "{\"x\":11,\"y\":11}", 2, 1234L), cb_CompactWorks);
        cb_CompactWorks.assertSuccess(0);
        SimpleDataCallback cb_GetCompactedResults = new SimpleDataCallback();
        setup.service.get(KEY1, cb_GetCompactedResults);
        cb_GetCompactedResults.assertSuccess();
        Assert.assertEquals("{\"x\":4,\"y\":11}", cb_GetCompactedResults.value);
        Assert.assertEquals(5, cb_GetCompactedResults.reads);
      }

      {
        SimpleIntCallback cb_CompactWorks = new SimpleIntCallback();
        setup.service.snapshot(KEY1, new DocumentSnapshot(5, "{\"x\":12,\"y\":12}", 1, 1234L), cb_CompactWorks);
        cb_CompactWorks.assertFailure(729311);
      }

      {
        SimpleIntCallback cb_CompactWorks = new SimpleIntCallback();
        setup.service.snapshot(KEY1, new DocumentSnapshot(4, "{\"x\":12,\"y\":12}", 1, 1234L), cb_CompactWorks);
        cb_CompactWorks.assertSuccess(0);
        SimpleDataCallback cb_GetCompactedResults = new SimpleDataCallback();
        setup.service.get(KEY1, cb_GetCompactedResults);
        cb_GetCompactedResults.assertSuccess();
        Assert.assertEquals("{\"x\":12,\"y\":12}", cb_GetCompactedResults.value);
        Assert.assertEquals(2, cb_GetCompactedResults.reads);
      }

      SimpleMockCallback cb_InitFailAlreadyExists = new SimpleMockCallback();
      setup.service.initialize(KEY1, UPDATE_1, cb_InitFailAlreadyExists);
      cb_InitFailAlreadyExists.assertFailure(667658);

      {
        SimpleMockCallback cb_DeleteWorks = new SimpleMockCallback();
        setup.service.delete(KEY1, DeleteTask.TRIVIAL, cb_DeleteWorks);
        cb_DeleteWorks.assertSuccess();
      }
      {
        SimpleMockCallback cb_DeleteWorks = new SimpleMockCallback();
        setup.service.delete(KEY1, DeleteTask.TRIVIAL, cb_DeleteWorks);
        cb_DeleteWorks.assertSuccess();
      }

      SimpleMockCallback cb_Init2 = new SimpleMockCallback();
      setup.service.initialize(KEY2, UPDATE_1, cb_Init2);
      cb_Init2.assertSuccess();

      {
        SimpleMockCallback cb_Close2 = new SimpleMockCallback();
        setup.service.close(KEY2, cb_Close2);
        cb_Close2.assertSuccess();
      }

      SimpleMockCallback cb_PatchOne2 = new SimpleMockCallback();
      setup.service.patch(KEY2, new RemoteDocumentUpdate[] { UPDATE_2 }, cb_PatchOne2);
      cb_PatchOne2.assertSuccess();

      {
        SimpleMockCallback cb_Close2 = new SimpleMockCallback();
        setup.service.close(KEY2, cb_Close2);
        cb_Close2.assertSuccess();
      }

      SimpleMockCallback cb_PatchTwo2 = new SimpleMockCallback();
      setup.service.patch(KEY2, new RemoteDocumentUpdate[] { UPDATE_3, UPDATE_4 }, cb_PatchTwo2);
      cb_PatchTwo2.assertSuccess();

      {
        SimpleMockCallback cb_Close2 = new SimpleMockCallback();
        setup.service.close(KEY2, cb_Close2);
        cb_Close2.assertSuccess();
      }

      {
        SimpleDataCallback cb_GetIsMergedResults = new SimpleDataCallback();
        setup.service.get(KEY2, cb_GetIsMergedResults);
        cb_GetIsMergedResults.assertSuccess();
        Assert.assertEquals("{\"x\":4,\"y\":4}", cb_GetIsMergedResults.value);
        Assert.assertEquals(4, cb_GetIsMergedResults.reads);
      }
      {
        SimpleMockCallback cb_Close2 = new SimpleMockCallback();
        setup.service.close(KEY2, cb_Close2);
        cb_Close2.assertSuccess();
      }

      {
        SimpleStringCallback cb_Diag = new SimpleStringCallback();
        setup.service.diagnostics(cb_Diag);
        cb_Diag.assertSuccess();
        System.err.println(cb_Diag.value);
      }

      final String archiveKey;
      {
        SimpleBackupCallback cb_Backup = new SimpleBackupCallback();
        setup.service.backup(KEY2, cb_Backup);
        cb_Backup.assertSuccess();
        archiveKey = cb_Backup.value.archiveKey;
      }

      {
        SimpleMockCallback cb_Restore = new SimpleMockCallback();
        setup.service.restore(KEY2, archiveKey, cb_Restore);
        cb_Restore.assertSuccess();
      }

      {
        SimpleDataCallback cb_GetIsMergedResults = new SimpleDataCallback();
        setup.service.get(KEY2, cb_GetIsMergedResults);
        cb_GetIsMergedResults.assertSuccess();
        Assert.assertEquals("{\"x\":4,\"y\":4}", cb_GetIsMergedResults.value);
        Assert.assertEquals(4, cb_GetIsMergedResults.reads);
      }

      {
        CountDownLatch finished = new CountDownLatch(1);
        ArrayList<String> log = new ArrayList<>();
        setup.service.dumpLog(KEY2, new Stream<String>() {
          @Override
          public void next(String ln) {
            log.add(ln);
          }

          @Override
          public void complete() {
            finished.countDown();
          }

          @Override
          public void failure(ErrorCodeException ex) {

          }
        });
        Assert.assertTrue(finished.await(10000, TimeUnit.MILLISECONDS));
        Assert.assertEquals(5, log.size());
        Assert.assertEquals("[1:1-->1] REQUEST:REQUEST ; REDO:{\"x\":1,\"y\":4} ; UNDO={\"x\":0,\"y\":0} (active=false)", log.get(0));
        Assert.assertEquals("[2:2-->2] REQUEST:REQUEST ; REDO:{\"x\":2} ; UNDO={\"x\":1,\"z\":42} (active=true)", log.get(1));
        Assert.assertEquals("BATCH:2", log.get(2));
        Assert.assertEquals("[4:3-->3] REQUEST:REQUEST ; REDO:{\"x\":3} ; UNDO={\"x\":2,\"z\":42} (active=true)", log.get(3));
        Assert.assertEquals("[4:4-->4] REQUEST:REQUEST ; REDO:{\"x\":4} ; UNDO={\"x\":3,\"z\":42} (active=true)", log.get(4));
      }

      {
        SimpleMockCallback cb_Delete = new SimpleMockCallback();
        setup.service.delete(KEY2, DeleteTask.TRIVIAL, cb_Delete);
        cb_Delete.assertSuccess();
      }

      {
        SimpleMockCallback cb_Restore = new SimpleMockCallback();
        setup.service.restore(KEY2, archiveKey, cb_Restore);
        cb_Restore.assertSuccess();
      }

      {
        SimpleMockCallback cb_RestoreFailedInCloud = new SimpleMockCallback();
        setup.service.restore(KEY2, archiveKey + ".notfound", cb_RestoreFailedInCloud);
        cb_RestoreFailedInCloud.assertFailure(-102);
      }

      {
        SimpleMockCallback cb_RestoreFailed2 = new SimpleMockCallback();
        setup.service.restore(KEY2, "notfound", cb_RestoreFailed2);
        cb_RestoreFailed2.assertFailure(739471);
      }

      {
        SimpleDataCallback cb_GetIsMergedResults = new SimpleDataCallback();
        setup.service.get(KEY2, cb_GetIsMergedResults);
        cb_GetIsMergedResults.assertSuccess();
        Assert.assertEquals("{\"x\":4,\"y\":4}", cb_GetIsMergedResults.value);
        Assert.assertEquals(4, cb_GetIsMergedResults.reads);
      }

      {
        SimpleIntCallback cb_CompactWorks = new SimpleIntCallback();
        setup.service.snapshot(KEY2, new DocumentSnapshot(4, "{\"x\":10,\"y\":10}", 1, 1234L), cb_CompactWorks);
        cb_CompactWorks.assertSuccess(0);
        SimpleDataCallback cb_GetCompactedResults = new SimpleDataCallback();
        setup.service.get(KEY2, cb_GetCompactedResults);
        cb_GetCompactedResults.assertSuccess();
        Assert.assertEquals("{\"x\":10,\"y\":10}", cb_GetCompactedResults.value);
        Assert.assertEquals(2, cb_GetCompactedResults.reads);
      }

      {
        SimpleStringCallback cb_Diag = new SimpleStringCallback();
        setup.service.diagnostics(cb_Diag);
        cb_Diag.assertSuccess();
        System.err.println(cb_Diag.value);
      }

      {
        CountDownLatch finished = new CountDownLatch(1);
        ArrayList<String> log = new ArrayList<>();
        setup.service.dumpLog(KEY2, new Stream<String>() {
          @Override
          public void next(String ln) {
            log.add(ln);
          }

          @Override
          public void complete() {
            finished.countDown();
          }

          @Override
          public void failure(ErrorCodeException ex) {

          }
        });
        Assert.assertTrue(finished.await(10000, TimeUnit.MILLISECONDS));
        Assert.assertEquals(4, log.size());
        Assert.assertEquals("BATCH:2", log.get(0));
        Assert.assertEquals("[4:3-->3] REQUEST:REQUEST ; REDO:{\"x\":3} ; UNDO={\"x\":2,\"z\":42} (active=true)", log.get(1));
        Assert.assertEquals("[4:4-->4] REQUEST:REQUEST ; REDO:{\"x\":4} ; UNDO={\"x\":3,\"z\":42} (active=true)", log.get(2));
        Assert.assertEquals("[4] SNAPSHOT:{\"x\":10,\"y\":10} (history=1, assets=1234)", log.get(3));
      }
    });
  }

  @Test
  public void recovery_flow() throws Exception {
    flow((setup) -> {
      SimpleDataCallback cb_GetFailed = new SimpleDataCallback();
      setup.service.get(KEY1, cb_GetFailed);
      cb_GetFailed.assertFailure(625676);

      SimpleMockCallback cb_PatchFailsFNF = new SimpleMockCallback();
      setup.service.patch(KEY1, new RemoteDocumentUpdate[] { UPDATE_2 }, cb_PatchFailsFNF);
      cb_PatchFailsFNF.assertFailure(625676);

      SimpleIntCallback cb_CompactFailsFNF = new SimpleIntCallback();
      setup.service.snapshot(KEY1, new DocumentSnapshot(1, "{}", 1, 1234L), cb_CompactFailsFNF);
      cb_CompactFailsFNF.assertFailure(625676);

      SimpleDataCallback cb_ComputeFailsFNF_Rewind = new SimpleDataCallback();
      setup.service.compute(KEY1, ComputeMethod.Rewind, 1, cb_ComputeFailsFNF_Rewind);
      cb_ComputeFailsFNF_Rewind.assertFailure(625676);

      SimpleDataCallback cb_ComputeFailsFNF_HeadPatch= new SimpleDataCallback();
      setup.service.compute(KEY1, ComputeMethod.HeadPatch, 1, cb_ComputeFailsFNF_HeadPatch);
      cb_ComputeFailsFNF_HeadPatch.assertFailure(625676);

      SimpleMockCallback cb_DeleteSuccessFNF = new SimpleMockCallback();
      setup.service.delete(KEY1, DeleteTask.TRIVIAL, cb_DeleteSuccessFNF);
      cb_DeleteSuccessFNF.assertSuccess();

      {
        SimpleMockCallback cb_Restore = new SimpleMockCallback();
        setup.service.recover(KEY1, new DocumentRestore(100, "{\"z\":123}", NtPrincipal.NO_ONE), cb_Restore);
        cb_Restore.assertFailure(625676);
      }

      SimpleMockCallback cb_InitSuccess = new SimpleMockCallback();
      setup.service.initialize(KEY1, UPDATE_1, cb_InitSuccess);
      cb_InitSuccess.assertSuccess();

      {
        SimpleDataCallback cb_Get = new SimpleDataCallback();
        setup.service.get(KEY1, cb_Get);
        cb_Get.assertSuccess();
        Assert.assertEquals("{\"x\":1,\"y\":4}", cb_Get.value);
      }

      {
        SimpleMockCallback cb_Restore = new SimpleMockCallback();
        setup.service.recover(KEY1, new DocumentRestore(100, "{\"z\":123}", NtPrincipal.NO_ONE), cb_Restore);
        cb_Restore.assertSuccess();

        SimpleDataCallback cb_Get = new SimpleDataCallback();
        setup.service.get(KEY1, cb_Get);
        cb_Get.assertSuccess();
        Assert.assertEquals("{\"z\":123}", cb_Get.value);
      }

      CountDownLatch finished = new CountDownLatch(1);
      ArrayList<String> log = new ArrayList<>();
      setup.service.dumpLog(KEY1, new Stream<String>() {
        @Override
        public void next(String ln) {
          log.add(ln);
        }

        @Override
        public void complete() {
          finished.countDown();
        }

        @Override
        public void failure(ErrorCodeException ex) {

        }
      });
      Assert.assertTrue(finished.await(10000, TimeUnit.MILLISECONDS));
      Assert.assertEquals(2, log.size());
      Assert.assertEquals("[1:1-->1] REQUEST:REQUEST ; REDO:{\"x\":1,\"y\":4} ; UNDO={\"x\":0,\"y\":0} (active=false)", log.get(0));
      Assert.assertEquals("RECOVER:100", log.get(1));
    });
  }

  private RemoteDocumentUpdate wrap(int seq, int redo, int undo) {
    return new RemoteDocumentUpdate(
        seq, seq, NtPrincipal.NO_ONE, "REQUEST", "{\"x\":"+redo + "}", "{\"x\":"+undo + "}", false, 0, 100, UpdateType.AddUserData);

  }

  @Test
  public void bigsnapshot() throws Exception {
    flow((setup) -> {
      SimpleMockCallback cb_InitSuccess = new SimpleMockCallback();
      setup.service.initialize(KEY1, wrap(1, 1, 0), cb_InitSuccess);
      cb_InitSuccess.assertSuccess();
      for (int k = 0; k < 1000; k++) {
        SimpleMockCallback cb_PatchSuccess = new SimpleMockCallback();
        setup.service.patch(KEY1, new RemoteDocumentUpdate[] { wrap(k + 2, k + 2, k + 1) }, cb_PatchSuccess);
        cb_PatchSuccess.assertSuccess();
      }
      SimpleIntCallback cb_SnapshotSuccess = new SimpleIntCallback();
      setup.service.snapshot(KEY1, new DocumentSnapshot(1001, "{\"x\":500000}", 5, 0), cb_SnapshotSuccess);
      cb_SnapshotSuccess.assertSuccess(0);
      AtomicReference<BackupResult> backup = new AtomicReference<>(null);
      CountDownLatch latch = new CountDownLatch(1);
      setup.service.backup(KEY1, new Callback<BackupResult>() {
        @Override
        public void success(BackupResult value) {
          backup.set(value);
          latch.countDown();
        }

        @Override
        public void failure(ErrorCodeException ex) {
          ex.printStackTrace();
        }
      });
      Assert.assertTrue(latch.await(2000, TimeUnit.MILLISECONDS));
      System.err.println(backup.get().archiveKey);
      File archiveFile = new File(new File(setup.cloud.path(), KEY1.space), backup.get().archiveKey);
      ArrayList<byte[]> writes = RestoreLoader.load(archiveFile);
      LocalCache lc = new LocalCache() {
        @Override
        public void finished() throws Exception {

        }
      };
      ArrayList<byte[]> filtered = lc.filter(writes);
      RestoreDebuggerStdErr.print(writes);
      RestoreDebuggerStdErr.print(filtered);
      Assert.assertEquals(writes.size(), filtered.size());
    });
  }
}
