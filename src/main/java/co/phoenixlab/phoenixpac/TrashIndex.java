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

import java.util.LinkedHashSet;
import java.util.stream.Collectors;

public class TrashIndex {

    public static int GUARD_BYTES = 0x54525348;

    protected int numTrashEntries;
    protected final LinkedHashSet<TrashIndexEntry> entries;

    public TrashIndex(int numTrashEntries) {
        this.numTrashEntries = numTrashEntries;
        entries = new LinkedHashSet<>(numTrashEntries);
    }



    public TrashIndex() {
        this(0);
    }

    public TrashIndex(TrashIndex other) {
        this.numTrashEntries = other.numTrashEntries;
        this.entries = new LinkedHashSet<>(other.entries.size());
        entries.addAll(entries.stream().
                map(TrashIndexEntry::new).
                collect(Collectors.toList()));
    }

    public int getNumTrashEntries() {
        return numTrashEntries;
    }

    public LinkedHashSet<TrashIndexEntry> getEntries() {
        return entries;
    }
}
