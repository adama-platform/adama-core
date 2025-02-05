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
package ape.runtime.json;

import ape.runtime.contracts.AutoMorphicAccumulator;

import java.util.HashMap;
import java.util.Map;

/** merge and roll-forward operations for JSON */
public class JsonAlgebra {
  /** RFC7396 for merging a patch into a target */
  @SuppressWarnings("unchecked")
  public static Object merge(final Object targetObject, final Object patchObject, boolean keepNulls) {
    if (patchObject instanceof HashMap) {
      HashMap<String, Object> patchMap = (HashMap<String, Object>) patchObject;
      if (targetObject instanceof HashMap) {
        HashMap<String, Object> targetMap = (HashMap<String, Object>) targetObject;
        for (Map.Entry<String, Object> patchEntry : patchMap.entrySet()) {
          String key = patchEntry.getKey();
          if (patchEntry.getValue() == null) {
            targetMap.remove(key);
            if (keepNulls) {
              targetMap.put(key, null);
            }
          } else {
            Object result = merge(targetMap.get(key), patchEntry.getValue(), keepNulls);
            if (result != null) {
              targetMap.put(key, result);
            }
          }
        }
        return targetMap;
      } else {
        return merge(new HashMap<String, Object>(), patchObject, keepNulls);
      }
    }
    return patchObject;
  }

  /** Product an object level delta and write it out; note: this doesn't compare items, and only does object fields present/not */
  @SuppressWarnings("unchecked")
  public static void writeObjectFieldDelta(final Object from, final Object to, JsonStreamWriter writer) {
    if (from instanceof HashMap && to instanceof HashMap) {
      HashMap<String, Object> mapFrom = (HashMap<String, Object>) from;
      HashMap<String, Object> mapTo = (HashMap<String, Object>) to;
      writer.beginObject();
      for (Map.Entry<String, Object> entryTo : mapTo.entrySet()) {
        writer.writeObjectFieldIntro(entryTo.getKey());
        Object objFrom = mapFrom.get(entryTo.getKey());
        if (objFrom != null) {
          writeObjectFieldDelta(objFrom, entryTo.getValue(), writer);
        } else {
          writer.writeTree(entryTo.getValue());
        }
      }
      for (Map.Entry<String, Object> entryFrom : mapFrom.entrySet()) {
        if (!mapTo.containsKey(entryFrom.getKey())) {
          writer.writeObjectFieldIntro(entryFrom.getKey());
          writer.writeNull();
        }
      }
      writer.endObject();
    } else {
      writer.writeTree(to);
    }
  }

  /** an accumulator/fold version of merge */
  public static AutoMorphicAccumulator<String> mergeAccumulator() {
    return mergeAccumulator(true);
  }

  public static AutoMorphicAccumulator<String> mergeAccumulator(boolean keepNulls) {
    return new AutoMorphicAccumulator<>() {
      private Object state = null;

      @Override
      public boolean empty() {
        return state == null;
      }

      @Override
      public void next(String data) {
        JsonStreamReader reader = new JsonStreamReader(data);
        if (state == null) {
          state = reader.readJavaTree();
        } else {
          state = merge(state, reader.readJavaTree(), keepNulls);
        }
      }

      @Override
      public String finish() {
        JsonStreamWriter writer = new JsonStreamWriter();
        writer.writeTree(state);
        return writer.toString();
      }
    };
  }
}
