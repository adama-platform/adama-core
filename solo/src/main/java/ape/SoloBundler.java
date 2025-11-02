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

import ape.common.Callback;
import ape.common.Json;
import ape.runtime.deploy.DeploymentFactoryBase;
import ape.runtime.deploy.DeploymentPlan;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class SoloBundler {
  public static String bundle(String spaceName, File mainFile, File includePath, Consumer<String> error) throws Exception {
    ObjectNode plan = Json.newJsonObject();
    plan.put("instrument", true);
    ObjectNode version = plan.putObject("versions").putObject("file");
    String main = Files.readString(mainFile.toPath());
    if (main.trim().equals("")) {
      error.accept("adama|bundled failed due to empty main for '" + spaceName + "'");
      return null;
    }
    version.put("main", main);
    ObjectNode includes = version.putObject("includes");
    if (includePath != null) {
      HashMap<String, String> map = new HashMap<>();
      fillImports(includePath, "", map);
      for (Map.Entry<String, String> entry : map.entrySet()) {
        includes.put(entry.getKey(), entry.getValue());
      }
    }
    plan.put("default", "file");
    plan.putArray("plan");
    return plan.toString();
  }

  private static void fillImports(File imports, String prefix, HashMap<String, String> map) throws Exception {
    if (imports.exists() && imports.isDirectory()) {
      for (File f : imports.listFiles()) {
        if (f.getName().endsWith(".adama")) {
          String name = prefix + f.getName().substring(0, f.getName().length() - 6);
          map.put(name, Files.readString(f.toPath()));
        } else if (f.isDirectory()) {
          fillImports(f, prefix + f.getName() + "/", map);
        }
      }
    }
  }

  private static File firstExist(File base, String... fileIntro) {
    for (String intro : fileIntro) {
      File test = new File(base, intro);
      if (test.exists()) {
        return test;
      }
    }
    return null;
  }

  public static void scan(File root, DeploymentFactoryBase base, Consumer<String> error) throws Exception {
    if (!root.exists()) {
      error.accept("scan root does not exist");
    }
    for (File potentialSpace : root.listFiles()) {
      if (potentialSpace.isDirectory()) {
        File main = firstExist(potentialSpace, "main.adama", "backend.adama");
        if (main == null) {
          error.accept("adama|solo-scan|no main.adama found in " + potentialSpace.getAbsolutePath());
        } else {
          File imports = firstExist(potentialSpace, "imports", "backend");
          String spaceName = potentialSpace.getName();
          System.out.println("found:" + spaceName);
          String plan = bundle(spaceName, main, imports, error);
          CountDownLatch latch = new CountDownLatch(1);
          base.deploy(spaceName, new DeploymentPlan(plan, (t, ec) -> {
            error.accept("adama|solo-deployment-issue[Code-" + ec + "]: " + t.getMessage());
          }), new TreeMap<>(), Callback.FINISHED_LATCH_DONT_CARE_VOID(latch));
          latch.await(60000, TimeUnit.MILLISECONDS);
        }
      }
    }
  }
}
