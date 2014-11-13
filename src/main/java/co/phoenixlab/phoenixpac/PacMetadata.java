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

public class PacMetadata {

    protected int size;
    protected int numMetadataBlocks;
    protected final LinkedHashMap<TypePurposeUniqueId, MetadataBlock> metadata;

    public PacMetadata() {
        metadata = new LinkedHashMap<>();
    }

    public PacMetadata(PacMetadata other) {
        this.size = other.size;
        this.numMetadataBlocks = other.numMetadataBlocks;
        this.metadata = new LinkedHashMap<>(other.metadata.size());
        for (Map.Entry<TypePurposeUniqueId, MetadataBlock> entry : other.metadata.entrySet()) {
            metadata.put(entry.getKey(), new MetadataBlock(entry.getValue()));
        }
    }

    public int getNumMetadataBlocks() {
        return numMetadataBlocks;
    }

    public int getSize() {
        return size;
    }

    public LinkedHashMap<TypePurposeUniqueId, MetadataBlock> getMetadata() {
        return metadata;
    }
}
