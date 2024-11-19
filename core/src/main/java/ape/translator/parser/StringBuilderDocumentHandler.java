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
package ape.translator.parser;

import ape.translator.parser.token.Token;

public class StringBuilderDocumentHandler extends TokenDocumentHandler {
  public final StringBuilder builder = new StringBuilder();

  @Override
  public void accept(final Token token) {
    if (token.nonSemanticTokensPrior != null) {
      for (final Token b : token.nonSemanticTokensPrior) {
        accept(b);
      }
    }
    builder.append(token.text);
    if (token.nonSemanticTokensAfter != null) {
      for (final Token a : token.nonSemanticTokensAfter) {
        accept(a);
      }
    }
  }

  @Override
  public String toString() {
    return builder.toString();
  }
}
