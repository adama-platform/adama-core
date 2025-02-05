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
package ape.translator.parser.token;

import ape.translator.parser.exceptions.AdamaLangException;
import ape.translator.tree.common.DocumentPosition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Function;

/** a way of iterating a buffer of tokens; this allows peeking into the future */
public class TokenEngine {
  private final LinkedList<Token> buffer;
  private final Iterator<Integer> codepointIterator;
  private final TokenReaderStateMachine stateMachine;
  private Token currentToken;
  private Token lastTokenIfAvailable = null;
  private ArrayList<Token> nonsemanticForwardingTokens;

  public TokenEngine(final String sourceName, final Iterator<Integer> codepointIterator) {
    this.codepointIterator = codepointIterator;
    buffer = new LinkedList<>();
    stateMachine = new TokenReaderStateMachine(sourceName, this::witness);
    currentToken = null;
    nonsemanticForwardingTokens = null;
  }

  public DocumentPosition position() {
    return stateMachine.position();
  }

  private void witness(final Token token) {
    if (token.majorType.hidden) {
      if (currentToken != null && nonsemanticForwardingTokens == null && (token.minorType == MinorTokenType.CommentEndOfLine || token.minorType == null)) {
        currentToken.addHiddenTokenAfter(token);
      } else {
        forwardToken(token);
      }
    } else {
      if (nonsemanticForwardingTokens != null) {
        token.nonSemanticTokensPrior = nonsemanticForwardingTokens;
        nonsemanticForwardingTokens = null;
      }
      currentToken = token;
      buffer.add(token);
    }
  }

  private void forwardToken(final Token token) {
    if (nonsemanticForwardingTokens == null) {
      nonsemanticForwardingTokens = new ArrayList<>(1);
    }
    nonsemanticForwardingTokens.add(token);
  }

  public Token getLastTokenIfAvailable() {
    return lastTokenIfAvailable;
  }

  public ArrayList<Token> getNonsemanticForwardingTokens() {
    return nonsemanticForwardingTokens;
  }

  public Token popIf(final Function<Token, Boolean> condition) throws AdamaLangException {
    final var candidate = peek();
    if (candidate != null) {
      final var result = condition.apply(candidate);
      if (result != null && result) {
        return pop();
      }
    }
    return null;
  }

  public Token peek() throws AdamaLangException {
    return peek(0);
  }

  public Token pop() throws AdamaLangException {
    var size = buffer.size();
    if (size <= 1) {
      ensureBufferFilled(16);
      size = buffer.size();
    }
    if (size > 0) {
      final var result = buffer.pop();
      lastTokenIfAvailable = result;
      return result;
    }
    return null;
  }

  public Token peek(final int future) throws AdamaLangException {
    ensureBufferFilled(future + 16);
    if (future < buffer.size()) {
      return buffer.get(future);
    } else {
      return null;
    }
  }

  private void ensureBufferFilled(final int size) throws AdamaLangException {
    // we are already filled
    if (buffer.size() > size) {
      return;
    }
    // read the document until the end
    while (codepointIterator.hasNext()) {
      // consume a codepoint
      final int codepoint = codepointIterator.next();
      stateMachine.consume(codepoint);
      // the buffer got filled via magic, coo
      if (buffer.size() > size) {
        return;
      }
    }
    // we have reached the end of the file, if we have any forwarding tokens, append
    // them to the most recent token
    if (nonsemanticForwardingTokens != null && currentToken != null) {
      for (final Token forward : nonsemanticForwardingTokens) {
        currentToken.addHiddenTokenAfter(forward);
      }
      nonsemanticForwardingTokens = null;
    }
    stateMachine.consume(0);
    return;
  }

  public Token popNextAdjSymbolPairIf(final Function<Token, Boolean> condition) throws AdamaLangException {
    final var candidate1 = peek(0);
    final var candidate2 = peek(1);
    if (candidate1 != null && candidate2 != null && candidate1.isSymbol() && candidate2.isSymbol()) {
      // whitespace (or a comment) exists between the tokens, so don't merge them
      if (candidate1.nonSemanticTokensAfter != null || candidate2.nonSemanticTokensPrior != null) {
        return null;
      }
      final var merge = Token.mergeAdjacentTokens(candidate1, candidate2, candidate1.majorType, candidate1.minorType);
      final var result = condition.apply(merge);
      if (result != null && result) {
        pop();
        pop();
        return merge;
      }
    }
    return null;
  }
}
