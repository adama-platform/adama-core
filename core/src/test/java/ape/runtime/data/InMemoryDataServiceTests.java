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
package ape.runtime.data;

import ape.ErrorCodes;
import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.runtime.contracts.DeleteTask;
import ape.runtime.mocks.MockTime;
import ape.runtime.natives.NtPrincipal;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryDataServiceTests {

  @Test
  public void flow() throws Exception {
    MockTime time = new MockTime();
    InMemoryDataService ds = new InMemoryDataService((t) -> t.run(), time);
    AtomicInteger success = new AtomicInteger(0);
    Key key = new Key("space", "key");
    ds.initialize(key, update(1, "{\"x\":1}", "{\"x\":0,\"y\":0}"), bumpSuccess(success));
    {
      CountDownLatch gotInventory = new CountDownLatch(1);
      ds.inventory(new Callback<Set<Key>>() {
        @Override
        public void success(Set<Key> value) {
          Assert.assertTrue(value.contains(new Key("space", "key")));
          gotInventory.countDown();
        }

        @Override
        public void failure(ErrorCodeException ex) {

        }
      });
      Assert.assertTrue(gotInventory.await(5000, TimeUnit.MILLISECONDS));
    }
    ds.patch(key, new RemoteDocumentUpdate[] {update(2, "{\"x\":2}", "{\"x\":1}"), update(3, "{\"x\":3}", "{\"x\":2}")}, bumpSuccess(success));
    ds.get(
        key,
        new Callback<>() {
          @Override
          public void success(LocalDocumentChange value) {
            success.getAndIncrement();
            Assert.assertEquals("{\"x\":3}", value.patch);
          }

          @Override
          public void failure(ErrorCodeException ex) {
            Assert.fail();
          }
        });
    ds.patch(
        key,
        new RemoteDocumentUpdate[] { update(3, "{\"x\":3}", "{\"x\":2}") },
        new Callback<Void>() {
          @Override
          public void success(Void value) {
            Assert.fail();
          }

          @Override
          public void failure(ErrorCodeException ex) {
            success.getAndIncrement();
            Assert.assertEquals(621580, ex.code);
          }
        });
    ds.compute(
        key,
        ComputeMethod.Rewind,
        1,
        new Callback<LocalDocumentChange>() {
          @Override
          public void success(LocalDocumentChange value) {
            success.getAndIncrement();
            Assert.assertEquals("{\"x\":0,\"y\":0}", value.patch);
          }

          @Override
          public void failure(ErrorCodeException ex) {
            Assert.fail();
          }
        });
    ds.compute(
        key,
        ComputeMethod.HeadPatch,
        1,
        new Callback<LocalDocumentChange>() {
          @Override
          public void success(LocalDocumentChange value) {
            success.getAndIncrement();
            Assert.assertEquals("{\"x\":3}", value.patch);
          }

          @Override
          public void failure(ErrorCodeException ex) {
            Assert.fail();
          }
        });
    ds.compute(
        key,
        ComputeMethod.HeadPatch,
        10,
        new Callback<LocalDocumentChange>() {
          @Override
          public void success(LocalDocumentChange value) {
            Assert.fail();
          }

          @Override
          public void failure(ErrorCodeException ex) {
            success.getAndIncrement();
            Assert.assertEquals(120944, ex.code);
          }
        });
    ds.patch(key, new RemoteDocumentUpdate[] { updateActive(4, "{\"x\":4}", "{\"x\":3}", 42) }, bumpSuccess(success));
    ds.delete(key, DeleteTask.TRIVIAL, bumpSuccess(success));
    Assert.assertEquals(9, success.get());
    ds.shed(key);
  }

  public RemoteDocumentUpdate update(int seq, String redo, String undo) {
    return new RemoteDocumentUpdate(seq, seq, NtPrincipal.NO_ONE, null, redo, undo, false, 0, 0, UpdateType.AddUserData);
  }

  private static Callback<Void> bumpSuccess(AtomicInteger success) {
    return new Callback<Void>() {
      @Override
      public void success(Void value) {
        success.getAndIncrement();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        Assert.fail();
      }
    };
  }

  private static Callback<Integer> bumpSuccessInt(AtomicInteger success) {
    return new Callback<Integer>() {
      @Override
      public void success(Integer value) {
        success.getAndIncrement();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        Assert.fail();
      }
    };
  }

  public RemoteDocumentUpdate updateActive(
      int seq, String redo, String undo, int time) {
    return new RemoteDocumentUpdate(seq, seq, NtPrincipal.NO_ONE, null, redo, undo, true, time, 0, UpdateType.AddUserData);
  }

  @Test
  public void notFound() {
    MockTime time = new MockTime();
    InMemoryDataService ds = new InMemoryDataService((t) -> t.run(), time);
    Key key = new Key("space", "key");
    AtomicInteger failure = new AtomicInteger(0);
    ds.get(key, bumpFailureDoc(failure, 625676));
    ds.patch(key, new RemoteDocumentUpdate[] { update(1, null, null) }, bumpFailure(failure, 144944));
    ds.compute(key, null, 1, bumpFailureDoc(failure, 106546));
    ds.delete(key, DeleteTask.TRIVIAL, bumpFailure(failure, 117816));
  }

  private static Callback<LocalDocumentChange> bumpFailureDoc(
      AtomicInteger failure, int expectedCode) {
    return new Callback<>() {
      @Override
      public void success(LocalDocumentChange value) {
        Assert.fail();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        failure.getAndIncrement();
        Assert.assertEquals(expectedCode, ex.code);
      }
    };
  }

  private static Callback<Void> bumpFailure(AtomicInteger failure, int expectedCode) {
    return new Callback<Void>() {
      @Override
      public void success(Void value) {
        Assert.fail();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        failure.getAndIncrement();
        Assert.assertEquals(expectedCode, ex.code);
      }
    };
  }

  @Test
  public void computeFailures() {
    MockTime time = new MockTime();
    InMemoryDataService ds = new InMemoryDataService((t) -> t.run(), time);
    Key key = new Key("space", "key");
    AtomicInteger failure = new AtomicInteger(0);
    AtomicInteger success = new AtomicInteger(0);
    ds.initialize(key, update(1, "{\"x\":1}", "{\"x\":0,\"y\":0}"), bumpSuccess(success));
    ds.initialize(
        key,
        update(1, "{\"x\":1}", "{\"x\":0,\"y\":0}"),
        bumpFailure(failure, ErrorCodes.UNIVERSAL_INITIALIZE_FAILURE));
    ds.patch(key, new RemoteDocumentUpdate[] { update(2, "{\"x\":2}", "{\"x\":1}") }, bumpSuccess(success));
    ds.patch(key, new RemoteDocumentUpdate[] { update(3, "{\"x\":3}", "{\"x\":2}") }, bumpSuccess(success));
    ds.compute(
        key, null, 1, bumpFailureDoc(failure, ErrorCodes.INMEMORY_DATA_COMPUTE_INVALID_METHOD));
    ds.compute(
        key,
        ComputeMethod.Rewind,
        100,
        bumpFailureDoc(failure, ErrorCodes.INMEMORY_DATA_COMPUTE_REWIND_NOTHING_TODO));
    Assert.assertEquals(3, success.get());
    Assert.assertEquals(3, failure.get());
  }

  @Test
  public void compaction() {
    MockTime time = new MockTime();
    InMemoryDataService ds = new InMemoryDataService((t) -> t.run(), time);
    AtomicInteger success = new AtomicInteger(0);
    Key key = new Key("space", "key");
    ds.initialize(key, update(1, "{\"x\":1}", "{\"x\":0,\"y\":0}"), bumpSuccess(success));
    for (int k = 0; k < 1000; k++) {
      if (k == 250) {
        ds.patch(key, new RemoteDocumentUpdate[] { update(2 + k, "{\"z\":"+(2 + k)+"}", "{\"z\":0}") }, bumpSuccess(success));
      } else {
        ds.patch(key, new RemoteDocumentUpdate[]{update(2 + k, "{\"x\":" + (2 + k) + "}", "{\"x\":" + (1 + k) + "}")}, bumpSuccess(success));
      }
    }
    ds.get(key, new Callback<LocalDocumentChange>() {
      @Override
      public void success(LocalDocumentChange value) {
        Assert.assertEquals("{\"x\":1001,\"z\":252}", value.patch);
        Assert.assertEquals(1001, value.reads);
        success.incrementAndGet();
      }

      @Override
      public void failure(ErrorCodeException ex) {

      }
    });
    ds.compute(key, ComputeMethod.Rewind, 100, new Callback<LocalDocumentChange>() {
      @Override
      public void success(LocalDocumentChange value) {
        Assert.assertEquals("{\"x\":99,\"z\":0}", value.patch);
        Assert.assertEquals(902, value.reads);
        success.incrementAndGet();
      }

      @Override
      public void failure(ErrorCodeException ex) {

      }
    });
    ds.snapshot(key, new DocumentSnapshot(1, "{}", 100, 1234L), new Callback<Integer>() {
      @Override
      public void success(Integer value) {
        Assert.assertEquals(900, (int) value);
        success.incrementAndGet();
      }

      @Override
      public void failure(ErrorCodeException ex) {

      }
    });
    ds.get(key, new Callback<LocalDocumentChange>() {
      @Override
      public void success(LocalDocumentChange value) {
        Assert.assertEquals("{\"x\":1001,\"z\":252}", value.patch);
        Assert.assertEquals(101, value.reads);
        success.incrementAndGet();
      }

      @Override
      public void failure(ErrorCodeException ex) {

      }
    });
    ds.compute(key, ComputeMethod.Rewind, 100, new Callback<LocalDocumentChange>() {
      @Override
      public void success(LocalDocumentChange value) {
        Assert.assertEquals("{\"x\":901}", value.patch);
        Assert.assertEquals(100, value.reads);
        success.incrementAndGet();
      }

      @Override
      public void failure(ErrorCodeException ex) {

      }
    });
    ds.snapshot(key, new DocumentSnapshot(1, "{}", 1000, 1234L), new Callback<Integer>() {
      @Override
      public void success(Integer value) {
        Assert.assertEquals(0, (int) value);
        success.incrementAndGet();
      }

      @Override
      public void failure(ErrorCodeException ex) {

      }
    });

    ds.snapshot(new Key("spaaaaaace", "keeeey"), new DocumentSnapshot(1, "{}", 1000, 1234L), new Callback<Integer>() {
      @Override
      public void success(Integer value) {
      }

      @Override
      public void failure(ErrorCodeException ex) {
        Assert.assertEquals(103060, ex.code);
        success.incrementAndGet();
      }
    });
    ds.recover(key, new DocumentRestore(125, "{\"doc\":1}", NtPrincipal.NO_ONE), new Callback<Void>() {
      @Override
      public void success(Void value) {
        success.incrementAndGet();
      }

      @Override
      public void failure(ErrorCodeException ex) {

      }
    });
    ds.get(key, new Callback<LocalDocumentChange>() {
      @Override
      public void success(LocalDocumentChange value) {
        Assert.assertEquals("{\"doc\":1}", value.patch);
        Assert.assertEquals(1, value.reads);
        Assert.assertEquals(125, value.seq);
        success.incrementAndGet();
      }

      @Override
      public void failure(ErrorCodeException ex) {

      }
    });
    ds.recover(new Key("SPPPPPPAACE", "key"), new DocumentRestore(125, "{\"doc\":1}", NtPrincipal.NO_ONE), new Callback<Void>() {
      @Override
      public void success(Void value) {
      }

      @Override
      public void failure(ErrorCodeException ex) {
        Assert.assertEquals(117817, ex.code);
        success.incrementAndGet();
      }
    });
    ds.close(key, new Callback<Void>() {
      @Override
      public void success(Void value) {
        success.incrementAndGet();
      }

      @Override
      public void failure(ErrorCodeException ex) {

      }
    });
    Assert.assertEquals(1012, success.get());
  }
}
