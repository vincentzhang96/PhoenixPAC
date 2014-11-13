/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Vincent Zhang
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
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package co.phoenixlab.phoenixpac;

import java.util.LinkedHashMap;
import java.util.Map;

public class Index {

    protected int numIndexEntries;
    protected final LinkedHashMap<TypePurposeUniqueId, IndexEntry> entries;

    public Index(int numIndexEntries) {
        this.numIndexEntries = numIndexEntries;
        entries = new LinkedHashMap<>(numIndexEntries);
    }



    public Index() {
        this(0);
    }

    public Index(Index other) {
        this.numIndexEntries = other.numIndexEntries;
        this.entries = new LinkedHashMap<>(other.entries.size());
        for (Map.Entry<TypePurposeUniqueId, IndexEntry> entry : other.entries.entrySet()) {
            entries.put(entry.getKey(), new IndexEntry(entry.getValue()));
        }
    }

    public int getNumIndexEntries() {
        return numIndexEntries;
    }

    public LinkedHashMap<TypePurposeUniqueId, IndexEntry> getEntries() {
        return entries;
    }
}
