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

import java.io.IOException;

public abstract class PacFile {


    public static final int MAJOR_VERSION = 4;
    public static final int MINOR_VERSION = 0;

    protected PacHeader header;
    protected Index index;
    protected PacMetadata metadata;
    protected TrashIndex trashIndex;

    public PacFile() {
    }

    public PacFile(PacFile other) {
        this.header = new PacHeader(other.header);
        this.index = new Index(other.index);
        if (other.metadata != null) {
            this.metadata = new PacMetadata(other.metadata);
        } else {
            this.metadata = new PacMetadata();
        }
        if (other.trashIndex != null) {
            this.trashIndex = new TrashIndex(other.trashIndex);
        } else {
            this.trashIndex = new TrashIndex();
        }
    }

    public PacFile(int majorVers, int minorVers) {
        this.header = new PacHeader();
        this.header.majorVersion = majorVers;
        this.header.minorVersion = minorVers;
        this.index = new Index();
        this.metadata = new PacMetadata();
        this.trashIndex = new TrashIndex();
    }

    public PacHeader getHeader() {
        return header;
    }

    public Index getIndex() {
        return index;
    }

    public PacMetadata getMetadata() {
        return metadata;
    }

    public TrashIndex getTrashIndex() {
        return trashIndex;
    }

    public abstract AssetHandle getHandle(TypePurposeUniqueId tpuid) throws IOException;

}
