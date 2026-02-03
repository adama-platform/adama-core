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
package ape.web.assets;

import ape.common.Callback;
import ape.runtime.data.Key;
import ape.runtime.natives.NtAsset;
import ape.web.io.ConnectionContext;

/**
 * Storage interface for document-attached files from the web tier.
 * Provides four core operations:
 * - request(): Stream asset content to client (by AssetRequest or Key+NtAsset)
 * - attach(): Associate uploaded asset with a document, optionally sending a channel message
 * - upload(): Store asset bytes to the underlying storage system
 */
public interface AssetSystem {
  /** stream an asset from underlying medium */
  void request(AssetRequest request, AssetStream stream);

  /** stream out an asset */
  void request(Key key, NtAsset asset, AssetStream stream);

  /** attach the asset to the given document under the given principal */
  void attach(String identity, ConnectionContext context, Key key, NtAsset asset, String channel, String message, Callback<Integer> callback);

  /** upload an asset at the given key */
  void upload(Key key, NtAsset asset, AssetUploadBody body, Callback<Void> callback);
}
