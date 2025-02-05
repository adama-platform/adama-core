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
package ape.translator.codegen;

import ape.translator.env.Environment;
import ape.translator.tree.common.StringBuilderWithTabs;
import ape.translator.tree.definitions.DefineAuthorization;
import ape.translator.tree.definitions.DefineAuthorizationPipe;
import ape.translator.tree.definitions.DefineHandler;
import ape.translator.tree.definitions.DefinePassword;

import java.util.ArrayList;

/** authenticate a username/pw pair to a agent under the document */
public class CodeGenAuth {
  public static void writeAuth(final StringBuilderWithTabs sb, Environment raw) {
    sb.append("@Override").writeNewline();
    sb.append("public boolean __open_channel(String name) {").tabUp().writeNewline();
    ArrayList<String> channels = new ArrayList<>();
    for (DefineHandler dh : raw.document.handlers) {
      if (dh.isOpen()) {
        channels.add(dh.channel);
      }
    }
    if (channels.size() == 1) {
      sb.append("return name.equals(\"").append(channels.get(0)).append("\");").tabDown().writeNewline();
    } else if (channels.size() > 1) {
      sb.append("switch (name) {").tabUp().writeNewline();
      int countDown = channels.size();
      for (String channel : channels) {
        sb.append("case \"").append(channel).append("\":");
        countDown--;
        if (countDown <= 0) sb.tabUp();
        sb.writeNewline();
      }
      sb.append("return true;").tabDown().writeNewline();
      sb.append("default:").tabUp().writeNewline();
      sb.append("return false;").tabDown().tabDown().writeNewline();
      sb.append("}").tabDown().writeNewline();
    } else {
      sb.append("return false;").tabDown().writeNewline();
    }
    sb.append("}").writeNewline();

    sb.append("@Override").writeNewline();
    if (raw.document.auths.size() == 1) {
      DefineAuthorization authorization = raw.document.auths.get(0);
      Environment environment = authorization.next(raw);
      sb.append("public String __auth(CoreRequestContext __context, String ").append(authorization.username.text).append(", String ").append(authorization.password.text).append(") {").tabUp().writeNewline();
      sb.append("try {").tabUp().writeNewline();
      sb.append("if (").append(authorization.username.text).append("== null && null == ").append(authorization.password.text).append(") throw new AbortMessageException();").writeNewline();
      authorization.code.specialWriteJava(sb, environment, false, true);
      sb.append("} catch (AbortMessageException ame) {").tabUp().writeNewline();
      sb.append("return null;").tabDown().writeNewline();
      sb.append("}").tabDown().writeNewline();
    } else {
      sb.append("public String __auth(CoreRequestContext __context, String username, String password) {").tabUp().writeNewline();
      sb.append("return null;").tabDown().writeNewline();
    }
    sb.append("}").writeNewline();

    if (raw.document.authPipes.size() == 1) {
      sb.append("public AuthResponse __authpipe(CoreRequestContext __context, String __message) {").tabUp().writeNewline();
      DefineAuthorizationPipe pipe = raw.document.authPipes.get(0);
      sb.append("try {").tabUp().writeNewline();
      sb.append("if (__message == null) throw new AbortMessageException();").writeNewline();
      sb.append("RTx").append(pipe.messageType.text).append(" ").append(pipe.messageValue.text).append(" = new RTx").append(pipe.messageType.text).append("(new JsonStreamReader(__message));").writeNewline();
      pipe.code.specialWriteJava(sb, raw, false, true);
      sb.append("} catch (AbortMessageException ame) {").tabUp().writeNewline();
      sb.append("return null;").tabDown().writeNewline();
      sb.append("}").tabDown().writeNewline();
    } else {
      sb.append("public AuthResponse __authpipe(CoreRequestContext __context, String __message) {").tabUp().writeNewline();
      sb.append("return null;").tabDown().writeNewline();
    }
    sb.append("}").writeNewline();

    sb.append("@Override").writeNewline();
    if (raw.document.passwords.size() == 1) {
      DefinePassword dp = raw.document.passwords.get(0);
      Environment environment = dp.next(raw);
      sb.append("public void __password(CoreRequestContext __context, String ").append(dp.passwordVar.text).append(") {").tabUp().writeNewline();
      sb.append("NtPrincipal __who = __context.who;").writeNewline();
      dp.code.specialWriteJava(sb, environment, false, true);
    } else {
      sb.append("public void __password(CoreRequestContext __context, String __pw) {");
    }
    sb.append("}").writeNewline();
  }
}
