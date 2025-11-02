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
package ape;

import ape.runtime.data.DataService;

/** PLANNED: persistance */
public class SoloDataFactory {
  public DataService persistant() {
            /*
        File corePath = new File(".");
File walRoot = new File(corePath, "wal");
        File dataRoot = new File(corePath, "data");
        File backups = new File(corePath, "backups");
        File cloudPath = new File(corePath, "cloud");
        File storePath = new File(dataRoot, "store");
        walRoot.mkdir();
        dataRoot.mkdir();
        backups.mkdir();

        */
            /*

        SimpleExecutor dataServiceExecutor = SimpleExecutor.create("data-service");
            SimpleExecutor diskExecutor = SimpleExecutor.create("disk");
            CaravanMetrics caravanMetrics = new CaravanMetrics(factory);
            DiskMetrics diskMetrics = new DiskMetrics(factory);

            Cloud cloud = new Cloud() {
                @Override
                public File path() {
                    return null;
                }

                @Override
                public void exists(Key key, String archiveKey, Callback<Void> callback) {

                }

                @Override
                public void restore(Key key, String archiveKey, Callback<File> callback) {

                }

                @Override
                public void backup(Key key, File archiveFile, Callback<Void> callback) {

                }

                @Override
                public void delete(Key key, String archiveKey, Callback<Void> callback) {

                }
            };


            DurableListStore store = new DurableListStore(diskMetrics, storePath, walRoot, 4L * 1024 * 1024 * 1024, 16 * 1024 * 1024, 64 * 1024 * 1024);
            CaravanDataService dataService = new CaravanDataService(caravanMetrics, cloud, store, diskExecutor);
            */
    return null;
  }
}
