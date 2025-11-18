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
package ape.runtime.stdlib.bonus;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.BiConsumer;

/** This is a very basic and dumb way to generate a dungeon via a bunch of random rooms connected together */
public class BasicRoomMazeGenerator {
  private static enum Decoration {
    Empty,
    Pillars,
    JustPassageWay
  }

  private static class Room {
    public final Decoration decoration;
    public final int min_x;
    public final int max_x;
    public final int min_y;
    public final int max_y;

    public Room(Decoration decoration, int min_x, int max_x, int min_y, int max_y) {
      this.decoration = decoration;
      this.min_x = min_x;
      this.max_x = max_x;
      this.min_y = min_y;
      this.max_y = max_y;
    }

    public void plot(BiConsumer<Integer, Integer> plotter) {
      for (int x = min_x; x <= max_x; x++) {
        for (int y = min_y; y <= max_y; y++) {
          boolean interior = (min_x + 2 < x && x < max_x - 2) && (min_y + 2 < y && y < max_y - 2);
          switch (decoration) {
            case JustPassageWay:
              if (!interior) {
                plotter.accept(x, y);
              }
              break;
            case Pillars:
              if (!(interior && (x / 4) % 2 == 0 && (y / 4) % 2 == 0)) {
                plotter.accept(x, y);
              }
              break;
            case Empty:
            default:
              plotter.accept(x, y);
          }
        }
      }
    }
  }

  private static boolean intervalsAreTooClose(int a, int b, int c, int d, int margin) {
    // Case 1: The intervals overlap or touch each other
    // If they overlap (including touching at endpoints), distance = 0
    // Example: [1,5] and [5,10] → they touch at 5 → distance 0
    if (Math.max(a, c) <= Math.min(b, d)) {
      return true; // distance is 0, which is always <= margin (assuming margin >= 0)
    }

    // At this point, the intervals do not overlap.
    // There are exactly two possible gaps:
    //   1) [a,b] is completely to the left of [c,d] → gap = c - b
    //   2) [c,d] is completely to the left of [a,b] → gap = a - d

    int gap;
    if (b < c) {
      // First interval ends before second starts
      gap = c - b;           // distance between b and c
    } else {
      // Second interval ends before first starts (a >= d)
      gap = a - d;           // distance between d and a
    }

    // The intervals are "too close" if the gap is less than or equal to the margin
    return gap <= margin;
  }

  private static boolean touch(Room a, Room b, int margin) {
    if (intervalsAreTooClose(a.min_x, a.max_x, b.min_x, b.max_x, margin)) {
      return intervalsAreTooClose(a.min_y, a.max_y, b.min_y, b.max_y, margin);
    }
    return false;
  }

  private static boolean is_b_east_a(Room a, Room b) {
    if (b.min_x > a.max_x) {
      if (intervalsAreTooClose(a.min_y, a.max_y, b.min_y, b.max_y, 0)) {
        return true;
      }
    }
    return false;
  }

  private static int eastern_midpoint(Room a, Room b) {
    int l = Math.max(a.min_y, b.min_y);
    int r = Math.min(a.max_y, b.max_y);
    return (l + r) / 2;
  }

  private static int southern_midpoint(Room a, Room b) {
    int l = Math.max(a.min_x, b.min_x);
    int r = Math.min(a.max_x, b.max_x);
    return (l + r) / 2;
  }

  private static boolean is_b_south_a(Room a, Room b) {
    if (b.min_y > a.max_y) {
      if (intervalsAreTooClose(a.min_x, a.max_x, b.min_x, b.max_x, 0)) {
        return true;
      }
    }
    return false;
  }


  private static class RoomSet {
    public ArrayList<Room> rooms = new ArrayList<Room>();

    public boolean addIfDoesntTouch(Room r, int margin) {
      for (Room room : rooms) {
        if (touch(room, r, margin)) {
          return false;
        }
      }
      rooms.add(r);
      return true;
    }

    public Room[] toArray() {
      return rooms.toArray(new Room[rooms.size()]);
    }
  }

  private static Room genRoom(Random rng, int w, int h, int min_w, int min_h, int max_w, int max_h) {
    int rx = rng.nextInt(w - max_w);
    int ry = rng.nextInt(h - max_h);
    int rw = Math.max(min_w, Math.min(max_w, 1 + rng.nextInt(w - rx - 1)));
    int rh = Math.max(min_h, Math.min(max_h, 1 + rng.nextInt(h - ry - 1)));
    if (rx  + rw > w) {
      rx = w - rw - 1;
    }
    if (ry  + rh > h) {
      ry = h - rh - 1;
    }
    int pick = rng.nextInt(10);
    Decoration decoration = Decoration.Empty;
    if (pick < 2) {
      decoration = Decoration.Pillars;
    } else if (pick < 5) {
      decoration = Decoration.JustPassageWay;
    }
    return new Room(decoration, rx, rx + rw, ry, ry + rh);
  }

  public static void generate(Random rng, int w, int h, int generations, int min, int max, int margin, BiConsumer<Integer, Integer> plotter) {
    RoomSet set = new RoomSet();
    for (int k = 0; k < generations; k++) {
      Room candidate = genRoom(rng, w, h, min, min, max, max);
      if (set.addIfDoesntTouch(candidate, margin)) {
        candidate.plot(plotter);
      }
    }

    Room[] rooms = set.toArray();
    for (int i = 0; i < rooms.length; i++) {
      Room toEast = null;
      Room toSouth = null;
      for (int j = 0; j < rooms.length; j++) {
        if (i != j) {
          if (is_b_east_a(rooms[i], rooms[j])) {
            if (toEast == null) {
              toEast = rooms[j];
            } else if (toEast.min_x > rooms[j].min_x) {
              toEast = rooms[j];
            }
          }
          if (is_b_south_a(rooms[i], rooms[j])) {
            if (toSouth == null) {
              toSouth = rooms[j];
            } else if (toSouth.min_y > rooms[j].min_y) {
              toSouth = rooms[j];
            }
          }
        }
      }
      if (toEast != null) {
        int y = eastern_midpoint(rooms[i], toEast);
        for (int x = rooms[i].max_x; x <= toEast.min_x; x++) {
          plotter.accept(x, y);
        }
      }
      if (toSouth != null) {
        int x= southern_midpoint(rooms[i], toSouth);
        for (int y = rooms[i].max_y; y <= toSouth.min_y; y++) {
          plotter.accept(x, y);
        }
      }
    }
  }
}
