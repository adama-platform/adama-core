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
package ape.translator.tree.types.natives;

import ape.runtime.json.JsonStreamWriter;
import ape.translator.env.Environment;
import ape.translator.parser.Formatter;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.DocumentPosition;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.types.ReflectionSource;
import ape.translator.tree.types.TySimpleNative;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.TypeBehavior;
import ape.translator.tree.types.traits.*;
import ape.translator.tree.types.traits.assign.AssignmentViaNative;
import ape.translator.tree.types.traits.details.DetailHasDeltaType;
import ape.translator.tree.types.traits.details.DetailTypeHasMethods;

import java.util.function.Consumer;

public abstract class TyNativeProxyString extends TySimpleNative implements //
        IsNativeValue, //
        DetailHasDeltaType, //
        DetailTypeHasMethods, //
        DetailCanExtractForUnique, //
        IsCSVCompatible, //
        AssignmentViaNative //
{
    public final Token readonlyToken;
    public final Token token;
    public final String adamaType;

    public TyNativeProxyString(TypeBehavior behavior, Token readonlyToken, Token token, String javaType, String adamaType) {
        super(behavior, javaType, javaType, -1);
        this.readonlyToken = readonlyToken;
        this.token = token;
        this.adamaType = adamaType;
        ingest(token);
    }

    @Override
    public void format(Formatter formatter) {
    }

    @Override
    public void emitInternal(Consumer<Token> yielder) {
        if (readonlyToken != null) {
            yielder.accept(readonlyToken);
        }
        yielder.accept(token);
    }

    @Override
    public String getAdamaType() {
        return adamaType;
    }

    @Override
    public void writeTypeReflectionJson(JsonStreamWriter writer, ReflectionSource source) {
        writer.beginObject();
        writer.writeObjectFieldIntro("nature");
        writer.writeString("native_value");
        writeAnnotations(writer);
        writer.writeObjectFieldIntro("type");
        writer.writeString(adamaType);
        writer.endObject();
    }

    @Override
    public String getDeltaType(Environment environment) {
        return "DStringProxy";
    }
}
