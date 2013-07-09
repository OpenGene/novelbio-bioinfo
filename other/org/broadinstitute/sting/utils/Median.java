/*
* Copyright (c) 2012 The Broad Institute
* 
* Permission is hereby granted, free of charge, to any person
* obtaining a copy of this software and associated documentation
* files (the "Software"), to deal in the Software without
* restriction, including without limitation the rights to use,
* copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the
* Software is furnished to do so, subject to the following
* conditions:
* 
* The above copyright notice and this permission notice shall be
* included in all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
* OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
* NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
* HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
* WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
* THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package org.broadinstitute.sting.utils;

import java.util.*;

/**
 * Utility class for calculating median from a data set, potentially limiting the size of data to a
 * fixed amount
 *
 * @author Your Name
 * @since Date created
 */
public class Median<T extends Comparable> {
    final List<T> values;
    final int maxValuesToKeep;
    boolean sorted = false;

    public Median() {
        this(Integer.MAX_VALUE);
    }

    public Median(final int maxValuesToKeep) {
        this.maxValuesToKeep = maxValuesToKeep;
        this.values = new ArrayList<T>();
    }

    public boolean isFull() {
        return values.size() >= maxValuesToKeep;
    }

    public int size() {
        return values.size();
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public T getMedian() {
        if ( isEmpty() )
            throw new IllegalStateException("Cannot get median value from empty array");
        return getMedian(null);  // note that value null will never be used
    }

    /**
     * Returns the floor(n + 1 / 2) item from the list of values if the list
     * has values, or defaultValue is the list is empty.
     */
    public T getMedian(final T defaultValue) {
        if ( isEmpty() )
            return defaultValue;

        if ( ! sorted ) {
            sorted = true;
            Collections.sort(values);
        }

        final int offset = (int)Math.floor((values.size() + 1) * 0.5) - 1;
        return values.get(offset);
    }

    public boolean add(final T value) {
        if ( ! isFull() ) {
            sorted = false;
            return values.add(value);
        }
        else
            return false;
    }
}
