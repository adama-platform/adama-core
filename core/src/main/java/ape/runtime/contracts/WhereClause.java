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
package ape.runtime.contracts;

/**
 * Query predicate interface for filtering table rows.
 * Generated from Adama WHERE clauses with three capabilities:
 * 1. getPrimaryKey() - returns the exact primary key if the query is "id == X"
 * 2. scopeByIndicies() - pushes index constraints to narrow the scan set
 * 3. test() - evaluates the full predicate against a candidate row
 * The query planner uses these to optimize table scans via index lookup.
 */
public interface WhereClause<T> {
  /**
   * does the where clause leverage the primary key (i.e. ID == VALUE). If not null, then return
   * VALUE
   */
  Integer getPrimaryKey();

  /**
   * the where clause is able to manipulate the index query set to exploit what it knows about the
   * expression
   */
  void scopeByIndicies(IndexQuerySet __set);

  /** evaluate the where clause precisely */
  boolean test(T item);
}
