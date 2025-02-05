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

import ape.ErrorCodes;
import ape.common.Callback;
import ape.common.ErrorCodeException;
import ape.common.ExceptionLogger;
import ape.common.NamedRunnable;
import ape.runtime.contracts.AdamaStream;
import ape.runtime.json.JsonStreamReader;
import ape.runtime.natives.NtAsset;

/**
 * Represents a stream for the consumer to interact with the document. This simplifies the
 * interaction model such that consumers don't need to think about how threading happens.
 */
public class CoreStream implements AdamaStream {
  private static final ExceptionLogger LOGGER = ExceptionLogger.FOR(CoreStream.class);
  private final CoreRequestContext context;
  private final CoreMetrics metrics;
  private final PredictiveInventory inventory;
  private final DurableLivingDocument document;
  private final StreamHandle handle;
  private final ConnectionMode mode;

  public CoreStream(CoreRequestContext context, CoreMetrics metrics, PredictiveInventory inventory, DurableLivingDocument document, ConnectionMode mode, StreamHandle handle) {
    this.context = context;
    this.metrics = metrics;
    this.inventory = inventory;
    this.document = document;
    this.handle = handle;
    this.mode = mode;
    inventory.message();
    inventory.connect();
    metrics.inflight_streams.up();
  }

  @Override
  public void update(String newViewerState, Callback<Void> callback) {
    if (mode.read) {
      JsonStreamReader patch = new JsonStreamReader(newViewerState);
      document.base.executor.execute(new NamedRunnable("core-stream-update") {
        @Override
        public void execute() throws Exception {
          inventory.message();
          handle.ingestViewUpdate(patch);
          if (document.document().__hasInflightAsyncWork()) {
            // this is, at core, fundamentally expensive
            document.invalidate(Callback.SUCCESS_OR_FAILURE_THROW_AWAY_VALUE(callback));
          } else {
            handle.triggerRefresh();
            callback.success(null);
          }
        }
      });
    } else {
      callback.failure(new ErrorCodeException(ErrorCodes.LIVING_DOCUMENT_WRITE_ONLY_MODE_UNABLE_UPDATE_VIEW));
    }
  }

  /** send a message to the document */
  @Override
  public void send(String channel, String marker, String message, Callback<Integer> callback) {
    if (!mode.write) {
      callback.failure(new ErrorCodeException(ErrorCodes.DOCUMENT_READ_ONLY));
      return;
    }
    if (!document.base.shield.canSendMessageExisting.get()) {
      callback.failure(new ErrorCodeException(ErrorCodes.SHIELD_REJECT_SEND_MESSAGE));
      return;
    }

    document.base.executor.execute(new NamedRunnable("core-stream-send") {
      @Override
      public void execute() throws Exception {
        inventory.message();
        document.send(context, handle.getViewId(), marker, channel, message, callback);
      }
    });
  }

  @Override
  public void password(String password, Callback<Integer> callback) {
    document.base.executor.execute(new NamedRunnable("core-stream-password") {
      @Override
      public void execute() throws Exception {
        inventory.message();
        document.setPassword(context, password, callback);
      }
    });
  }

  @Override
  public void canAttach(Callback<Boolean> callback) {
    if (!document.base.shield.canSendMessageExisting.get()) {
      callback.failure(new ErrorCodeException(ErrorCodes.SHIELD_REJECT_SEND_MESSAGE));
      return;
    }
    document.base.executor.execute(new NamedRunnable("core-stream-can-attach") {
      @Override
      public void execute() throws Exception {
        inventory.message();
        try {
          callback.success(document.canAttach(context));
        } catch (Exception ex) {
          callback.failure(ErrorCodeException.detectOrWrap(ErrorCodes.CORE_STREAM_CAN_ATTACH_UNKNOWN_EXCEPTION, ex, LOGGER));
        }
      }
    });
  }

  @Override
  public void attach(String id, String name, String contentType, long size, String md5, String sha384, Callback<Integer> callback) {
    NtAsset asset = new NtAsset(id, name, contentType, size, md5, sha384);
    document.base.executor.execute(new NamedRunnable("core-stream-attach") {
      @Override
      public void execute() throws Exception {
        inventory.message();
        document.attach(context, asset, callback);
      }
    });
  }

  @Override
  public void close() {
    metrics.inflight_streams.down();
    document.base.executor.execute(new NamedRunnable("core-stream-disconnect") {
      @Override
      public void execute() throws Exception {
        // documents that is silent
        // account for the disconnect message
        inventory.message();
        // disconnect this view
        handle.kill();
        // clean up and keep things tidy
        if (document.garbageCollectPrivateViewsFor(context.who) == 0) {
          // falling edge disconnects the person
          document.disconnect(context, Callback.DONT_CARE_INTEGER);
        } else {
          document.invalidate(Callback.DONT_CARE_INTEGER);
        }
        // tell the client
        handle.disconnect();
      }
    });
  }
}
