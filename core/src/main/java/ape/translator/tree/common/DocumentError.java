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
package ape.translator.tree.common;

import ape.runtime.json.JsonStreamWriter;

/**
 * Defines an error within the document that can also be tied to a specific position within the
 * document
 */
public class DocumentError {
  public final String file;
  public final String message;
  public final DocumentPosition position;

  /**
   * construct the error
   * @param position where within the file the error happened
   * @param message what is the message for the error
   */
  public DocumentError(final DocumentPosition position, final String message) {
    if (position == null || message == null) {
      throw new NullPointerException();
    }
    this.file = position.getSource();
    this.message = message;
    this.position = position;
  }

  /** write the error out into the given ObjectNode using the LSP format */
  public String json() {
    JsonStreamWriter writer = new JsonStreamWriter();
    writer.beginObject();
    writer.writeObjectFieldIntro("range");
    position.dump(writer);

    writer.writeObjectFieldIntro("severity");
    writer.writeInteger(1);

    writer.writeObjectFieldIntro("source");
    writer.writeString("error");

    writer.writeObjectFieldIntro("message");
    writer.writeString(message);

    if (file != null) {
      writer.writeObjectFieldIntro("file");
      writer.writeString(file);
    }

    writer.endObject();
    return writer.toString();
  }
}
