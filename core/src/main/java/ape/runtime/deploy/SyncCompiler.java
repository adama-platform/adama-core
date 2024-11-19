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
package ape.runtime.deploy;

import ape.ErrorCodes;
import ape.common.ErrorCodeException;
import ape.translator.jvm.ByteArrayJavaFileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

/** the sync compiler */
public class SyncCompiler {
  private static final Logger LOGGER = LoggerFactory.getLogger(SyncCompiler.class);

  public static CachedByteCode compile(final String spaceName, final String className, final String javaSource, String reflection) throws ErrorCodeException {
    final var compiler = ToolProvider.getSystemJavaCompiler();
    final var diagnostics = new DiagnosticCollector<JavaFileObject>();
    final var fileManager = new ByteArrayJavaFileManager(compiler.getStandardFileManager(null, null, null));
    final var task = compiler.getTask(null, fileManager, diagnostics, null, null, ByteArrayJavaFileManager.turnIntoCompUnits(className + ".java", javaSource));
    if (task.call() == false) {
      StringBuilder report = new StringBuilder();
      for (final Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
        report.append(diagnostic.toString() + "\n");
      }
      ErrorCodeException ex = new ErrorCodeException(ErrorCodes.FACTORY_CANT_COMPILE_JAVA_CODE, report.toString());
      LOGGER.error("failed-java-compile", ex);
      throw ex;
    }
    try {
      final var classBytes = fileManager.getClasses();
      fileManager.close();
      return new CachedByteCode(spaceName, className, reflection, classBytes);
    } catch (final Exception ex) {
      throw new ErrorCodeException(ErrorCodes.FACTORY_CANT_BIND_JAVA_CODE, ex);
    }
  }
}
