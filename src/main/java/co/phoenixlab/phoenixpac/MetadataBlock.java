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

public class MetadataBlock {

    protected TypePurposeUniqueId tpuid;
    protected int numberOfEntries;
    protected int size;
    protected final LinkedHashMap<String, MetadataEntry> entries;

    public MetadataBlock() {
        entries = new LinkedHashMap<>();
    }

    public MetadataBlock(MetadataBlock other) {
        this.tpuid = other.tpuid;
        this.numberOfEntries = other.numberOfEntries;
        this.size = other.size;
        this.entries = new LinkedHashMap<>(other.entries.size());
        for (Map.Entry<String, MetadataEntry> entry : other.entries.entrySet()) {
            entries.put(entry.getKey(), new MetadataEntry(entry.getValue()));
        }
    }


    public TypePurposeUniqueId getTpuid() {
        return tpuid;
    }

    public int getNumberOfEntries() {
        return numberOfEntries;
    }

    public int getSize() {
        return size;
    }

    public LinkedHashMap<String, MetadataEntry> getEntries() {
        return entries;
    }
}
