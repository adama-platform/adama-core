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
package ape.runtime.sys.web.rxhtml;

import ape.common.Callback;
import ape.common.SimpleExecutor;
import ape.common.TimeSource;
import ape.common.cache.AsyncSharedLRUCache;
import ape.common.cache.SyncCacheLRU;
import ape.rxhtml.routing.Table;

import java.util.concurrent.atomic.AtomicBoolean;

public class CachedRxHtmlFetcher implements RxHtmlFetcher {
  private final SyncCacheLRU<String, Table> storage;
  private final AsyncSharedLRUCache<String, Table> cache;

  public CachedRxHtmlFetcher(TimeSource timeSource, int maxSites, long maxAge, SimpleExecutor executor, RxHtmlFetcher fetcher) {
    this.storage = new SyncCacheLRU<>(timeSource, 0, maxSites, 16 * 1024 * 1024, maxAge, (name, record) -> {
    });
    this.cache = new AsyncSharedLRUCache<>(executor, storage, fetcher::fetch);
  }

  public void startSweeping(AtomicBoolean alive, int periodMinimumMs, int periodMaximumMs) {
    this.cache.startSweeping(alive, periodMinimumMs, periodMaximumMs);
  }

  @Override
  public void fetch(String space, Callback<Table> callback) {
    cache.get(space, callback);
  }
}
