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
package ape.rxhtml;

import ape.rxhtml.preprocess.Mobilify;
import ape.rxhtml.preprocess.ExpandStaticObjects;
import ape.rxhtml.preprocess.Pagify;
import ape.rxhtml.template.config.Feedback;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Parses and preprocesses RxHTML forest documents.
 * Applies a pipeline of transformations including mobile optimization,
 * page templating, and static object expansion to prepare the document
 * for code generation.
 */
public class Loader {
  public static Document parseForest(String forest, Feedback feedback, ProductionMode mode) {
    Document document = Jsoup.parse(forest);
    Mobilify.go(document, mode);
    // TODO: enabling this requires a LOT of work
    // MarkStaticContent.mark(document);
    Pagify.pagify(document);
    ExpandStaticObjects.expand(document, feedback);
    return document;
  }
}
