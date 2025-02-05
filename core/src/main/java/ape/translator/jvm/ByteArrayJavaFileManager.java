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
package ape.translator.jvm;

import javax.tools.*;
import javax.tools.JavaFileObject.Kind;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/** responsible for capturing results from the java compiler */
@SuppressWarnings("unchecked")
public class ByteArrayJavaFileManager extends ForwardingJavaFileManager {
  private Map<String, byte[]> classes;

  public ByteArrayJavaFileManager(final JavaFileManager fileManager) {
    super(fileManager);
    classes = new TreeMap<>();
  }

  public static ArrayList<JavaFileObject> turnIntoCompUnits(final String fileName, final String code) {
    final var compUnits = new ArrayList<JavaFileObject>();
    compUnits.add(new SingleSourceJavaObject(fileName, code));
    return compUnits;
  }

  public Map<String, byte[]> getClasses() {
    return classes;
  }

  @Override
  public JavaFileObject getJavaFileForOutput(final JavaFileManager.Location location, final String className, final Kind kind, final FileObject sibling) throws IOException {
    return new ClassByteArrayOutputBuffer(className);
  }

  @Override
  public void flush() throws IOException {
  }

  @Override
  public void close() throws IOException {
    classes = null;
  }

  private static class SingleSourceJavaObject extends SimpleJavaFileObject {
    final String source;

    SingleSourceJavaObject(final String fileName, final String code) {
      super(URI.create(new StringBuilder().append("code:///").append(fileName).toString()), Kind.SOURCE);
      source = code;
    }

    @Override
    public CharBuffer getCharContent(final boolean ignoreEncodingErrors) {
      return CharBuffer.wrap(source);
    }
  }

  private class ClassByteArrayOutputBuffer extends SimpleJavaFileObject {
    private final String name;

    ClassByteArrayOutputBuffer(final String name) {
      super(URI.create(new StringBuilder().append("code:///").append(name).toString()), Kind.CLASS);
      this.name = name;
    }

    @Override
    public OutputStream openOutputStream() {
      return new FilterOutputStream(new ByteArrayOutputStream()) {
        @Override
        public void close() throws IOException {
          out.close();
          final var bos = (ByteArrayOutputStream) out;
          classes.put(name, bos.toByteArray());
        }
      };
    }
  }
}
