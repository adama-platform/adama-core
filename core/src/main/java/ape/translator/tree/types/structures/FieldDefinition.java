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
package ape.translator.tree.types.structures;

import ape.translator.env.ComputeContext;
import ape.translator.env.Environment;
import ape.translator.parser.token.Token;
import ape.translator.tree.common.DocumentPosition;
import ape.translator.parser.Formatter;
import ape.translator.tree.types.reactive.*;
import ape.translator.tree.watcher.WatchSet;
import ape.translator.tree.expressions.Expression;
import ape.translator.tree.privacy.Policy;
import ape.translator.tree.privacy.PublicPolicy;
import ape.translator.tree.types.TyType;
import ape.translator.tree.types.checking.properties.StorageTweak;
import ape.translator.tree.types.natives.TyNativeList;
import ape.translator.tree.types.natives.TyNativeMaybe;
import ape.translator.tree.types.reactive.*;
import ape.translator.tree.types.traits.DetailNeverPublic;

import java.util.function.Consumer;

/** the definition for a field */
public class FieldDefinition extends StructureComponent {
  public final Expression computeExpression;
  public final Expression defaultValueOverride;
  public final Token equalsToken;
  public final Token introToken;
  public final String name;
  public final Token nameToken;
  public final Policy policy;
  public final Token semicolonToken;
  public final WatchSet watching;
  public final boolean readonly;

  public Token lossyOrRequiredToken;
  public Token uniqueToken;
  public TyType type;
  private Formatter.FirstAndLastToken fal;
  private CachePolicy cachePolicy;

  public final Token invalidationRuleToken;
  public final InvalidationRule invalidationRule;

  public final Token silent;

  public FieldDefinition(final Policy policy, final Token introToken, final TyType type, final Token nameToken, Token invalidationRuleToken, final Token equalsToken, final Expression computeExpression, final Expression defaultValueOverride, final Token lossyOrRequiredToken, final Token uniqueToken, Token silent, final Token semicolonToken) {
    this.policy = policy;
    this.introToken = introToken;
    this.type = type;
    this.nameToken = nameToken;
    name = nameToken.text;
    this.computeExpression = computeExpression;
    this.equalsToken = equalsToken;
    this.defaultValueOverride = defaultValueOverride;
    this.lossyOrRequiredToken = lossyOrRequiredToken;
    this.uniqueToken = uniqueToken;
    this.silent = silent;
    this.semicolonToken = semicolonToken;
    if (policy != null) {
      ingest(policy);
    }
    this.invalidationRuleToken = invalidationRuleToken;
    if (invalidationRuleToken != null) {
      switch (invalidationRuleToken.text) {
        case "@updated":
          invalidationRule = InvalidationRule.DateTimeUpdated;
          break;
        case "@bump":
          invalidationRule = InvalidationRule.IntegerBump;
          break;
        default:
          this.invalidationRule = null;
      }
    } else {
      this.invalidationRule = null;
    }
    boolean _readonly = false;
    if (introToken != null) {
      ingest(introToken);
      _readonly = "readonly".equals(introToken.text);
    }
    this.readonly = _readonly;
    if (type != null) {
      ingest(type);
    }
    if (computeExpression != null) {
      ingest(computeExpression);
    }
    if (defaultValueOverride != null) {
      ingest(defaultValueOverride);
    }
    ingest(silent);
    if (semicolonToken != null) {
      ingest(semicolonToken);
    }
    watching = new WatchSet();
  }

  public boolean isLossy() {
    if (lossyOrRequiredToken != null) {
      return "lossy".equals(lossyOrRequiredToken.text);
    }
    return false;
  }

  public void enableCache(CachePolicy cachePolicy) {
    this.cachePolicy = cachePolicy;
  }

  public boolean hasCachePolicy() {
    return cachePolicy != null;
  }

  public CachePolicy getCachePolicy() {
    return this.cachePolicy;
  }

  public boolean isRequired() {
    if (lossyOrRequiredToken != null) {
      return "required".equals(lossyOrRequiredToken.text);
    }
    return false;
  }

  public static FieldDefinition invent(final TyType type, final String name) {
    PublicPolicy policy = null;
    if (type != null) {
      policy = new PublicPolicy(null);
      policy.ingest(type);
    }
    FieldDefinition fd = new FieldDefinition(policy, null, type, Token.WRAP(name), null, null, null, null, null, null, null, null);
    fd.ingest(type);
    return fd;
  }

  public static FieldDefinition inventId(DocumentPosition dp) {
    FieldDefinition fd = new FieldDefinition(null, null, new TyReactiveInteger(false, null).withPosition(dp), Token.WRAP("id"), null, null, null, null, null, null, null, null);
    fd.ingest(dp);
    return fd;
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    if (policy != null) {
      policy.emit(yielder);
    }
    if (introToken != null) {
      yielder.accept(introToken);
    }
    if (cachePolicy != null) {
      cachePolicy.emit(yielder);
    }
    if (type != null) {
      type.emit(yielder);
    }
    yielder.accept(nameToken);
    if (invalidationRuleToken != null) {
      yielder.accept(invalidationRuleToken);
    }
    if (equalsToken != null) {
      yielder.accept(equalsToken);
      if (computeExpression != null) {
        computeExpression.emit(yielder);
      }
      if (defaultValueOverride != null) {
        defaultValueOverride.emit(yielder);
      }
    }
    if (lossyOrRequiredToken != null) {
      yielder.accept(lossyOrRequiredToken);
    }
    if (uniqueToken != null) {
      yielder.accept(uniqueToken);
    }
    if (silent != null) {
      yielder.accept(silent);
    }
    yielder.accept(semicolonToken);
  }

  public boolean isSilent() {
    return silent != null;
  }

  @Override
  public void format(Formatter formatter) {
    if (policy != null) {
      policy.format(formatter);
    }
    if (type != null) {
      type.format(formatter);
    }
    if (cachePolicy != null) {
      cachePolicy.format(formatter);
    }
    if (equalsToken != null) {
      if (computeExpression != null) {
        computeExpression.format(formatter);
      }
      if (defaultValueOverride != null) {
        defaultValueOverride.format(formatter);
      }
    }
    Formatter.FirstAndLastToken fal = new Formatter.FirstAndLastToken();
    emit(fal);
    if (fal.first != null) {
      formatter.startLine(fal.first);
      formatter.endLine(fal.last);
    }
  }

  @Override
  public boolean equals(final Object otherRaw) {
    if (otherRaw instanceof FieldDefinition) {
      final var other = (FieldDefinition) otherRaw;
      if (name.equals(other.name)) {
        if (type != null && other.type != null) {
          return type.getAdamaType().equals(other.type.getAdamaType());
        }
      }
    }
    return false;
  }

  private Environment envOf(Environment priorEnv, final StructureStorage owningStructureStorage) {
    Environment env = priorEnv.scopeWithComputeContext(ComputeContext.Computation);
    if (owningStructureStorage.specialization == StorageSpecialization.Record) {
      env = env.scopeRecord(owningStructureStorage.name.text);
    }
    return env;
  }

  public void typing(final Environment priorEnv, final StructureStorage owningStructureStorage) {
    final var environment = envOf(priorEnv, owningStructureStorage);
    if (policy != null) {
      policy.typing(environment, owningStructureStorage);
    }
    if (invalidationRule != null) {
      if (owningStructureStorage.root) {
        environment.document.createError(this, String.format("The field '%s' has an invalidation event which is not valid for the root document", name));
      }
    }
    if (type != null) {
      type = environment.rules.Resolve(type, false);
    }
    if (type == null && computeExpression != null) {
      type = computeExpression.typing(environment.scopeReactiveExpression().scopeWithCache("__c" + name).scopeWithComputeContext(ComputeContext.Computation), null /* no suggestion makes sense */);
      if (type != null) {
        type = new TyReactiveLazy(type, hasCachePolicy()).withPosition(type);
      }
    }
    if (type != null) {
      type.typing(environment);
      if (defaultValueOverride != null) {
        final var defType = defaultValueOverride.typing(environment, null /* no suggestion makes sense */);
        if (defType != null) {
          environment.rules.CanTypeAStoreTypeB(type, defType, StorageTweak.None, false);
        }
      }
    }
    if (type instanceof DetailNeverPublic) {
      environment.document.createError(this, String.format("Field has a type that is not allowed: %s", type.getAdamaType()));
    }
    if (type == null) {
      environment.document.createError(this, String.format("The field '%s' has no type", name));
    }
  }

  private static String cycleType(TyType type) {
    if (type instanceof TyReactiveRef) {
      return ((TyReactiveRef) type).ref;
    } else if (type instanceof TyReactiveRecord) {
      return ((TyReactiveRecord) type).name;
    } else if (type instanceof TyReactiveMaybe) {
      return cycleType(((TyReactiveMaybe) type).tokenizedElementType.item);
    } else if (type instanceof TyNativeList) {
      return cycleType(((TyNativeList) type).elementType);
    } else if (type instanceof TyNativeMaybe) {
      return cycleType(((TyNativeMaybe) type).tokenElementType.item);
    } else if (type instanceof TyReactiveTable) {
      return ((TyReactiveTable) type).recordName;
    } else if (type instanceof TyReactiveLazy) {
      return cycleType(((TyReactiveLazy) type).computedType);
    }
    return null;
  }

  public String getCycleType() {
    return cycleType(type);
  }
}
