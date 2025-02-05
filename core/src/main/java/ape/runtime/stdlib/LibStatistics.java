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
package ape.runtime.stdlib;

import ape.runtime.natives.NtList;
import ape.runtime.natives.NtMaybe;
import ape.translator.reflect.Extension;
import ape.translator.reflect.HiddenType;
import ape.translator.reflect.UseName;

import java.util.Arrays;

/** very simple statistics */
public class LibStatistics {
  @UseName(name = "average")
  @Extension
  public static @HiddenType(clazz = Double.class) NtMaybe<Double> avgDoubles(@HiddenType(clazz = Double.class) final NtList<Double> list) {
    if (list.size() > 0) {
      var sum = 0D;
      for (final Double x : list) {
        sum += x;
      }
      return new NtMaybe<>(sum / list.size());
    }
    return new NtMaybe<>();
  }

  @UseName(name = "average")
  @Extension
  public static @HiddenType(clazz = Double.class) NtMaybe<Double> avgInts(@HiddenType(clazz = Integer.class) final NtList<Integer> list) {
    if (list.size() > 0) {
      double sum = 0.0;
      for (final Integer x : list) {
        sum += x;
      }
      return new NtMaybe<>(sum / list.size());
    }
    return new NtMaybe<>();
  }

  @UseName(name = "average")
  @Extension
  public static @HiddenType(clazz = Double.class) NtMaybe<Double> avgLongs(@HiddenType(clazz = Long.class) final NtList<Long> list) {
    if (list.size() > 0) {
      double sum = 0.0;
      for (final Long x : list) {
        sum += x;
      }
      return new NtMaybe<>(sum / list.size());
    }
    return new NtMaybe<>();
  }

  @UseName(name = "sum")
  @Extension
  public static @HiddenType(clazz = Double.class) NtMaybe<Double> sumDoubles(@HiddenType(clazz = Double.class) final NtList<Double> list) {
    if (list.size() > 0) {
      var sum = 0D;
      for (final Double x : list) {
        sum += x;
      }
      return new NtMaybe<>(sum);
    }
    return new NtMaybe<>();
  }

  @UseName(name = "sum")
  @Extension
  public static @HiddenType(clazz = Integer.class) NtMaybe<Integer> sumInts(@HiddenType(clazz = Integer.class) final NtList<Integer> list) {
    if (list.size() > 0) {
      var sum = 0;
      for (final Integer x : list) {
        sum += x;
      }
      return new NtMaybe<>(sum);
    }
    return new NtMaybe<>();
  }

  @UseName(name = "sum")
  @Extension
  public static @HiddenType(clazz = Long.class) NtMaybe<Long> sumLongs(@HiddenType(clazz = Long.class) final NtList<Long> list) {
    if (list.size() > 0) {
      var sum = 0L;
      for (final Long x : list) {
        sum += x;
      }
      return new NtMaybe<>(sum);
    }
    return new NtMaybe<>();
  }

  @UseName(name = "maximum")
  @Extension
  public static @HiddenType(clazz = Double.class) NtMaybe<Double> maxDoubles(@HiddenType(clazz = Double.class) final NtList<Double> list) {
    if (list.size() > 0) {
      double val = list.lookup(0).get();
      for (final Double x : list) {
        if (x > val) {
          val = x;
        }
      }
      return new NtMaybe<>(val);
    }
    return new NtMaybe<>();
  }

  @UseName(name = "maximum")
  @Extension
  public static @HiddenType(clazz = Integer.class) NtMaybe<Integer> maxInts(@HiddenType(clazz = Integer.class) final NtList<Integer> list) {
    if (list.size() > 0) {
      int val = list.lookup(0).get();
      for (final Integer x : list) {
        if (x > val) {
          val = x;
        }
      }
      return new NtMaybe<>(val);
    }
    return new NtMaybe<>();
  }

  @UseName(name = "maximum")
  @Extension
  public static @HiddenType(clazz = Long.class) NtMaybe<Long> maxLongs(@HiddenType(clazz = Long.class) final NtList<Long> list) {
    if (list.size() > 0) {
      long val = list.lookup(0).get();
      for (final Long x : list) {
        if (x > val) {
          val = x;
        }
      }
      return new NtMaybe<>(val);
    }
    return new NtMaybe<>();
  }

  @UseName(name = "minimum")
  @Extension
  public static @HiddenType(clazz = Double.class) NtMaybe<Double> minDoubles(@HiddenType(clazz = Double.class) final NtList<Double> list) {
    if (list.size() > 0) {
      double val = list.lookup(0).get();
      for (final Double x : list) {
        if (x < val) {
          val = x;
        }
      }
      return new NtMaybe<>(val);
    }
    return new NtMaybe<>();
  }

  @UseName(name = "minimum")
  @Extension
  public static @HiddenType(clazz = Integer.class) NtMaybe<Integer> minInts(@HiddenType(clazz = Integer.class) final NtList<Integer> list) {
    if (list.size() > 0) {
      int val = list.lookup(0).get();
      for (final Integer x : list) {
        if (x < val) {
          val = x;
        }
      }
      return new NtMaybe<>(val);
    } else {
      return new NtMaybe<>();
    }
  }

  @UseName(name = "minimum")
  @Extension
  public static @HiddenType(clazz = Long.class) NtMaybe<Long> minLongs(@HiddenType(clazz = Long.class) final NtList<Long> list) {
    if (list.size() > 0) {
      long val = list.lookup(0).get();
      for (final Long x : list) {
        if (x < val) {
          val = x;
        }
      }
      return new NtMaybe<>(val);
    }
    return new NtMaybe<>();
  }

  @UseName(name = "median")
  @Extension
  public static @HiddenType(clazz = Double.class) NtMaybe<Double> medianDoubles(@HiddenType(clazz = Double.class) final NtList<Double> list) {
    // TODO: I HATE this because we can do it better, but need to import a library
    if (list.size() > 0) {
      Double[] values = list.toArray((n) -> new Double[n]);
      Arrays.sort(values);
      int index = values.length / 2;
      if (values.length % 2 == 0) {
        return new NtMaybe<>((values[index] + values[index - 1]) / 2.0);
      } else {
        return new NtMaybe<>(values[index]);
      }
    }
    return new NtMaybe<>();
  }

  @UseName(name = "median")
  @Extension
  public static @HiddenType(clazz = Integer.class) NtMaybe<Integer> medianInts(@HiddenType(clazz = Integer.class) final NtList<Integer> list) {
    // TODO: I HATE this because we can do it better, but need to import a library
    if (list.size() > 0) {
      Integer[] values = list.toArray((n) -> new Integer[n]);
      Arrays.sort(values);
      int index = values.length / 2;
      if (values.length % 2 == 0) {
        return new NtMaybe<>((values[index] + values[index - 1]) / 2);
      } else {
        return new NtMaybe<>(values[index]);
      }
    }
    return new NtMaybe<>();
  }

  @UseName(name = "median")
  @Extension
  public static @HiddenType(clazz = Long.class) NtMaybe<Long> medianLongs(@HiddenType(clazz = Long.class) final NtList<Long> list) {
    // TODO: I HATE this because we can do it better, but need to import a library
    if (list.size() > 0) {
      Long[] values = list.toArray((n) -> new Long[n]);
      Arrays.sort(values);
      int index = values.length / 2;
      if (values.length % 2 == 0) {
        return new NtMaybe<>((values[index] + values[index - 1]) / 2);
      } else {
        return new NtMaybe<>(values[index]);
      }
    }
    return new NtMaybe<>();
  }

  @UseName(name = "percentile")
  @Extension
  public static @HiddenType(clazz = Double.class) NtMaybe<Double> percentileDoubles(@HiddenType(clazz = Double.class) final NtList<Double> list, double percent) {
    // TODO: I HATE this because we can do it better, but need to import a library
    if (list.size() > 0 && percent >= 0.0 && percent <= 1.0) {
      Double[] values = list.toArray((n) -> new Double[n]);
      Arrays.sort(values);
      return new NtMaybe<>(values[(int) (values.length * percent)]);
    }
    return new NtMaybe<>();
  }

  @UseName(name = "percentile")
  @Extension
  public static @HiddenType(clazz = Integer.class) NtMaybe<Integer> percentileInts(@HiddenType(clazz = Integer.class) final NtList<Integer> list, double percent) {
    // TODO: I HATE this because we can do it better, but need to import a library
    if (list.size() > 0 && percent >= 0.0 && percent <= 1.0) {
      Integer[] values = list.toArray((n) -> new Integer[n]);
      Arrays.sort(values);
      return new NtMaybe<>(values[(int) (values.length * percent)]);
    }
    return new NtMaybe<>();
  }

  @UseName(name = "percentile")
  @Extension
  public static @HiddenType(clazz = Long.class) NtMaybe<Long> percentileLongs(@HiddenType(clazz = Long.class) final NtList<Long> list, double percent) {
    // TODO: I HATE this because we can do it better, but need to import a library
    if (list.size() > 0 && percent >= 0.0 && percent <= 1.0) {
      Long[] values = list.toArray((n) -> new Long[n]);
      Arrays.sort(values);
      return new NtMaybe<>(values[(int) (values.length * percent)]);
    }
    return new NtMaybe<>();
  }
}
