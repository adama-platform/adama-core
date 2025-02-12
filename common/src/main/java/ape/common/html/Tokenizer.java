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
package ape.common.html;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.PrimitiveIterator;
import java.util.regex.Pattern;

/** Convert a string into an iterator of tokens */
public class Tokenizer implements Iterator<Token> {
  private final PrimitiveIterator.OfInt codepoints;
  private final LinkedList<Token> buffer;
  private boolean text;
  private boolean quote;
  private final StringBuilder current;
  private int ln;
  private int ch;
  private int p_ln;
  private int p_ch;

  public Tokenizer(PrimitiveIterator.OfInt codepoints) {
    this.codepoints = codepoints;
    this.buffer = new LinkedList<>();
    this.text = true;
    this.quote = false;
    this.current = new StringBuilder();
    this.ln = 0;
    this.ch = 0;
    this.p_ln = 0;
    this.p_ch = 0;
  }

  public static Tokenizer of(String html) {
    return new Tokenizer(html.codePoints().iterator());
  }

  private static final String[] EMBEDDED_TAGS = new String[] { "<script", "<adama", "<style", "<code", "<highlight"};

  private void push() {
    String val = current.toString();
    if (val.length() == 0) {
      return;
    }
    Type type = Type.Text;
    if (val.startsWith("<!")) {
      type = Type.Comment;
    } else if (val.startsWith("</")) {
      type = Type.ElementClose;
    } else if (val.startsWith("<")) {
      type = Type.ElementOpen;
    }
    // TODO: CDATA, DTD
    write(type, val);

    if (type == Type.ElementOpen) {
      String closer = hasEmbeddedContentReturnClosingTag(val);
      if (closer != null) {
        boolean escape = val.contains("escape") || val.contains("<highlight") || val.contains("<adama");
        pushEmbeddedText(closer, escape);
      }
    }
  }

  private void write(Type type, String val) {
    buffer.add(new Token(type, val, p_ln, p_ch, ln, ch));
    current.setLength(0);
    p_ln = ln;
    p_ch = ch;
  }

  private String hasEmbeddedContentReturnClosingTag(String val) {
    for (String emTeg : EMBEDDED_TAGS) {
      if (val.length() >= emTeg.length()) {
        if (val.substring(0, emTeg.length()).equalsIgnoreCase(emTeg)) {
          if (!val.endsWith("/>")) {
            return "</" + emTeg.substring(1);
          }
        }
      }
    }
    return null;
  }

  private String escapeIfNeeded(String value, boolean escape) {
    if (escape) {
      return value.replaceAll(Pattern.quote("<"), "&lt;").replaceAll(Pattern.quote(">"), "&gt;");
    }
    return value;
  }

  private void pushEmbeddedText(String closingTag, boolean escape) {
    StringBuilder body = new StringBuilder();
    StringBuilder tagCheck = new StringBuilder();
    boolean checkTag = false;
    while (codepoints.hasNext()) {
      int cp = codepoints.nextInt();
      if (checkTag) {
        tagCheck.append(Character.toString(cp));
        String toCheck = tagCheck.toString();
        if (closingTag.startsWith(toCheck)) {
          if (toCheck.length() == closingTag.length()) { // DONE
            write(Type.EmbeddedText, escapeIfNeeded(body.toString(), escape));
            ch += toCheck.length();
            while (codepoints.hasNext()) {
              int cp2 = codepoints.nextInt();
              tagCheck.append(Character.toString(cp2));
              ch++;
              if (cp2 == '>') {
                write(Type.ElementClose, tagCheck.toString());
                return;
              }
            }
          }
        } else {
          body.append(toCheck);
          ch += tagCheck.length();
          tagCheck.setLength(0);
          checkTag = false;
        }
      } else {
        if (cp == '<') {
          checkTag = true;
          tagCheck.append('<');
        } else {
          body.append(Character.toString(cp));
          ch++;
        }
      }
      if (cp == '\n') {
        ln++;
        ch = 0;
      }
    }
    // end of stream, flush it out
    write(Type.EmbeddedText, escapeIfNeeded(body.toString(), escape));
    if (checkTag && tagCheck.length() > 0) {
      ch += tagCheck.length();
      write(Type.ElementClose, tagCheck.toString());
    }
  }

  public void ensure(int count) {
    while (buffer.size() < count && codepoints.hasNext()) {
      int cp = codepoints.nextInt();
      if (text) {
        switch (cp) {
          case '<': {
            if (current.length() > 0) {
              push();
            }
            current.append(Character.toString(cp));
            ch++;
            text = false;
            quote = false;
            break;
          }
          default:
            current.append(Character.toString(cp));
            ch++;
        }
      } else {
        current.append(Character.toString(cp));
        ch++;
        switch (cp) {
          case '"': {
            if (quote) {
              quote = false;
            } else {
              quote = true;
            }
          }
          break;
          case '>': {
            if (!quote) {
              push();
              text = true;
            }
          }
          break;
        }
      }
      if (cp == '\n') {
        ln++;
        ch = 0;
      }
    }
    if (!codepoints.hasNext()) {
      push();
    }
  }

  @Override
  public boolean hasNext() {
    ensure(1);
    return buffer.size() > 0;
  }

  @Override
  public Token next() {
    return buffer.removeFirst();
  }
}
