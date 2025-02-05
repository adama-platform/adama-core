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

import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.SimpleExecutor;
import ape.runtime.data.mocks.MockFinderService;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class BoundLocalFinderServiceTests {
  @Test
  public void flow() throws Exception {
    MockFinderService mock = new MockFinderService("the-machine");
    mock.bindLocal(new Key("space", "key"));
    BoundLocalFinderService finder = new BoundLocalFinderService(SimpleExecutor.NOW, mock, "the-region", "the-machine");
    CountDownLatch latch = new CountDownLatch(4);
    finder.find(new Key("space", "key"), new Callback<DocumentLocation>() {
      @Override
      public void success(DocumentLocation value) {
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        ex.printStackTrace();
      }
    });
    finder.bind(new Key("space", "bound"), new Callback<Void>() {
      @Override
      public void success(Void value) {
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {

      }
    });
    finder.find(new Key("space", "bound"), new Callback<DocumentLocation>() {
      @Override
      public void success(DocumentLocation value) {
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        ex.printStackTrace();
      }
    });
    finder.backup(new Key("space", "bound"), new BackupResult("new-archive", 1, 100, 200), new Callback<Void>() {
      @Override
      public void success(Void value) {
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {

      }
    });
    Assert.assertTrue(latch.await(1000, TimeUnit.MILLISECONDS));
    CountDownLatch latchAgain = new CountDownLatch(1);
    finder.find(new Key("space", "bound"), new Callback<DocumentLocation>() {
      @Override
      public void success(DocumentLocation value) {
        Assert.assertEquals("new-archive", value.archiveKey);
        latchAgain.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {
        ex.printStackTrace();
      }
    });
    Assert.assertTrue(latchAgain.await(1000, TimeUnit.MILLISECONDS));
    CountDownLatch latchFree = new CountDownLatch(1);
    finder.free(new Key("space", "bound"), new Callback<Void>() {
      @Override
      public void success(Void value) {
        latchFree.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {

      }
    });
    Assert.assertTrue(latchFree.await(1000, TimeUnit.MILLISECONDS));
    CountDownLatch latchLast = new CountDownLatch(3);
    finder.list(new Callback<List<Key>>() {
      @Override
      public void success(List<Key> value) {
        latchLast.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {

      }
    });
    finder.listDeleted(new Callback<List<Key>>() {
      @Override
      public void success(List<Key> value) {
        latchLast.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {

      }
    });
    finder.markDelete(new Key("space", "key"), new Callback<Void>() {
      @Override
      public void success(Void value) {
        finder.commitDelete(new Key("space", "key"), new Callback<Void>() {
          @Override
          public void success(Void value) {
            latch.countDown();
          }

          @Override
          public void failure(ErrorCodeException ex) {

          }
        });
      }

      @Override
      public void failure(ErrorCodeException ex) {

      }
    });
    Assert.assertTrue(latchFree.await(1000, TimeUnit.MILLISECONDS));
  }

  @Test
  public void success_bind_even_after_find_failure() throws Exception {
    MockFinderService mock = new MockFinderService("the-machine");
    mock.bindLocal(new Key("space", "key"));
    BoundLocalFinderService finder = new BoundLocalFinderService(SimpleExecutor.NOW, mock, "the-region", "the-machine");
    CountDownLatch latch = new CountDownLatch(2);
    finder.bind(new Key("space", "cant-find"), new Callback<Void>() {
      @Override
      public void success(Void value) {
        latch.countDown();
      }

      @Override
      public void failure(ErrorCodeException ex) {

      }
    });
    finder.find(new Key("space", "cant-find"), new Callback<DocumentLocation>() {
      @Override
      public void success(DocumentLocation value) {

      }

      @Override
      public void failure(ErrorCodeException ex) {
        latch.countDown();
      }
    });
    Assert.assertTrue(latch.await(10000, TimeUnit.MILLISECONDS));
  }
}
