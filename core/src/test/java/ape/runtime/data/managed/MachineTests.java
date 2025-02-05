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
package ape.runtime.data.managed;

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.runtime.data.Key;
import ape.runtime.data.LocalDocumentChange;
import ape.runtime.data.RemoteDocumentUpdate;
import ape.runtime.data.UpdateType;
import ape.runtime.data.*;
import ape.runtime.data.mocks.MockArchiveDataSource;
import ape.runtime.data.mocks.MockFinderService;
import ape.runtime.natives.NtPrincipal;
import ape.runtime.sys.mocks.MockInstantDataService;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class MachineTests {
  public RemoteDocumentUpdate update(int seq, String redo, String undo) {
    return new RemoteDocumentUpdate(seq, seq, NtPrincipal.NO_ONE, null, redo, undo, false, 0, 0, UpdateType.AddUserData);
  }

  private static final Key KEY = new Key("space", "key");

  @Test
  public void happy() throws Exception {
    MockInstantDataService data = new MockInstantDataService();
    MockArchiveDataSource archive = new MockArchiveDataSource(data);

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<String> val = new AtomicReference<>();
    Callback<LocalDocumentChange> got = new Callback<LocalDocumentChange>() {
      @Override
      public void success(LocalDocumentChange value) {
        val.set(value.patch);
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        ex.printStackTrace();
        latch.countDown();
      }
    };

    Callback<Void> debug = new Callback<Void>() {
      @Override
      public void success(Void value) {
      }

      @Override
      public void failure(ErrorCodeException ex) {
        ex.printStackTrace();
      }
    };

    BaseTests.flow((base) -> {
      base.on(KEY, (machine) -> {
        machine.write(new Action(() -> archive.initialize(KEY, update(1, "{\"x\":1}", "{\"x\":0}"), debug), debug));
        machine.write(new Action(() -> archive.patch(KEY, new RemoteDocumentUpdate[] { update(2, "{\"x\":2}", "{\"x\":1}") }, debug), debug));
        machine.read(new Action(() -> archive.get(KEY, got), got));
      });
      Assert.assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
      Assert.assertEquals("{\"x\":2}", val.get());
    }, archive);
  }

  @Test
  public void shed() throws Exception {
    MockInstantDataService data = new MockInstantDataService();
    MockArchiveDataSource archive = new MockArchiveDataSource(data);

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<String> val = new AtomicReference<>();
    Callback<LocalDocumentChange> got = new Callback<LocalDocumentChange>() {
      @Override
      public void success(LocalDocumentChange value) {
        val.set(value.patch);
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        ex.printStackTrace();
        latch.countDown();
      }
    };

    Callback<Void> debug = new Callback<Void>() {
      @Override
      public void success(Void value) {
      }

      @Override
      public void failure(ErrorCodeException ex) {
        ex.printStackTrace();
      }
    };

    Runnable gotIt = archive.latchLogAt(1);
    BaseTests.flow((base) -> {
      base.on(KEY, (machine) -> {
        machine.write(new Action(() -> archive.initialize(KEY, update(1, "{\"x\":1}", "{\"x\":0}"), debug), debug));
        machine.write(new Action(() -> archive.patch(KEY, new RemoteDocumentUpdate[] { update(2, "{\"x\":2}", "{\"x\":1}") }, debug), debug));
        machine.read(new Action(() -> archive.get(KEY, got), got));
      });
      Assert.assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
      Assert.assertEquals("{\"x\":2}", val.get());
      CountDownLatch sentShed = new CountDownLatch(1);
      base.on(KEY, (machine) -> {
        machine.write(new Action(() -> archive.patch(KEY, new RemoteDocumentUpdate[] { update(3, "{\"x\":3}", "{\"x\":2}") }, debug), debug));
        machine.shed();
        sentShed.countDown();
      });
      Assert.assertTrue(sentShed.await(1000, TimeUnit.MILLISECONDS));
      gotIt.run();
      archive.assertLogAt(0, "BACKUP:space/key");
    }, archive);
  }

  @Test
  public void closing() throws Exception {
    MockInstantDataService data = new MockInstantDataService();
    MockArchiveDataSource archive = new MockArchiveDataSource(data);
    BaseTests.flow((base) -> {
      CountDownLatch latchFailure = new CountDownLatch(2);
      base.on(KEY, (machine) -> {
        Callback<Void> cb = new Callback<Void>() {
          @Override
          public void success(Void value) {

          }

          @Override
          public void failure(ErrorCodeException ex) {
            latchFailure.countDown();
          }
        };
        machine.close();
        machine.read(new Action(() -> {}, cb));
        machine.close();
        machine.write(new Action(() -> {}, cb));
      });
      Assert.assertTrue(latchFailure.await(1000, TimeUnit.MILLISECONDS));
    }, archive);
  }

  @Test
  public void deleting() throws Exception {
    MockInstantDataService data = new MockInstantDataService();
    MockArchiveDataSource archive = new MockArchiveDataSource(data);
    BaseTests.flow((base) -> {
      CountDownLatch latchFailure = new CountDownLatch(2);
      base.on(KEY, (machine) -> {
        Callback<Void> cb = new Callback<Void>() {
          @Override
          public void success(Void value) {

          }

          @Override
          public void failure(ErrorCodeException ex) {
            latchFailure.countDown();
          }
        };
        machine.delete();
        machine.read(new Action(() -> {}, cb));
        machine.delete();
        machine.write(new Action(() -> {}, cb));
      });
      Assert.assertTrue(latchFailure.await(1000, TimeUnit.MILLISECONDS));
    }, archive);
  }

  @Test
  public void badstate() throws Exception {
    MockInstantDataService data = new MockInstantDataService();
    MockArchiveDataSource archive = new MockArchiveDataSource(data);
    BaseTests.flow((base) -> {
      MockFinderService finder = (MockFinderService) base.finder;
      finder.bindArchive(KEY, "");
      CountDownLatch latchFailure = new CountDownLatch(1);
      base.on(KEY, (machine) -> {
        Callback<String> callback = new Callback<String>() {
          @Override
          public void success(String value) {

          }

          @Override
          public void failure(ErrorCodeException ex) {
            ex.printStackTrace();
            latchFailure.countDown();
          }
        };
        machine.read(new Action(() -> {
          callback.success("hi");
        }, callback));
      });
      Assert.assertTrue(latchFailure.await(1000, TimeUnit.MILLISECONDS));
    }, archive);
  }
}
