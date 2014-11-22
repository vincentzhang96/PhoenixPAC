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

public class MemoryPacBuilder {

    private MemoryPacFile pacFile;

    private MemoryPacBuilder() {
        pacFile = new MemoryPacFile();
        pacFile.index = new Index();
        pacFile.metadata = new PacMetadata();
    }

    public HeaderBuilder buildHeader() {
        return new HeaderBuilder(this);
    }

    public EntryBuilder newEntry() {
        return new EntryBuilder(this);
    }

    public MemoryPacBuilder addEntry(IndexEntry entry, MemoryAssetHandle assetHandle) {
        EntryBuilder builder = new EntryBuilder(this);
        builder.entry = entry;
        builder.assetHandle = assetHandle;
        builder.add();
        return this;
    }

    public MemoryPacFile finish() {
        if (pacFile.header == null) {
            throw new IllegalStateException("Header not constructed");
        }
        pacFile.index.syncCount();
        return pacFile;
    }


    public static MemoryPacBuilder newBuilder() {
        return new MemoryPacBuilder();
    }

    public class HeaderBuilder {

        private PacHeader header;
        private MemoryPacBuilder builder;

        HeaderBuilder(MemoryPacBuilder builder) {
            this.builder = builder;
            header = new PacHeader();
        }

        public HeaderBuilder withMajorVersion(int majorVersion) {
            header.majorVersion = majorVersion;
            return this;
        }

        public HeaderBuilder withMinorVersion(int minorVersion) {
            header.minorVersion = minorVersion;
            return this;
        }

        public HeaderBuilder withLatestVersion() {
            return withMajorVersion(PacFile.MAJOR_VERSION).withMinorVersion(PacFile.MINOR_VERSION);
        }

        public HeaderBuilder setFlag(int flag, boolean value) {
            header.flags = (header.flags & ~flag) | ((value ? 0xFFFFFFFF : 0) & flag);
            return this;
        }

        public MemoryPacBuilder finishHeader() {
            pacFile.header = header;
            return builder;
        }

    }

    public class EntryBuilder {
        private IndexEntry entry;
        private MemoryAssetHandle assetHandle;
        private MemoryPacBuilder builder;

        EntryBuilder(MemoryPacBuilder builder) {
            this.builder = builder;
            entry = new IndexEntry();
        }

        public EntryBuilder withTPUID(int type, int purpose, int unique) {
            return withTPUID(new TypePurposeUniqueId(type, purpose, unique));
        }

        public EntryBuilder withTPUID(TypePurposeUniqueId tpuid) {
            entry.tpuid = tpuid;
            return this;
        }

        public EntryBuilder withMemorySize(int memorySize) {
            entry.memorySize = memorySize;
            return this;
        }

        public EntryBuilder withMemoryAssetHandle(MemoryAssetHandle memoryAssetHandle) {
            assetHandle = memoryAssetHandle;
            return this;
        }

        public EntryBuilder withComputedSha256Hash() {
            if (assetHandle == null) {
                throw new IllegalStateException("Must call withMemoryAssetHandle() first");
            }
            entry.sha256Hash = IndexEntry.computeSha256Hash(assetHandle.data);
            return this;
        }

        public EntryBuilder withIgnoredSha256Hash() {
            if (assetHandle == null) {
                throw new IllegalStateException("Must call withMemoryAssetHandle() first");
            }
            entry.sha256Hash = new byte[IndexEntry.SHA_256_HASH_BYTE_LEN];
            return this;
        }

        public EntryBuilder withNoCompression() {
            entry.compressionId = 0;
            return this;
        }

        public EntryBuilder withCompression(int compressionId) {
            entry.compressionId = compressionId;
            return this;
        }

        public MemoryPacBuilder add() {
            if (entry.tpuid == null) {
                throw new IllegalStateException("Must call withTPUID() first");
            }
            if (assetHandle == null) {
                throw new IllegalStateException("Must call withMemoryAssetHandle() first");
            }
            if (entry.sha256Hash == null) {
                throw new IllegalStateException("Must call either withComputedSha256Hash() or withIgnoredSha256Hash() first");
            }
            pacFile.index.entries.put(entry.tpuid, entry);
            pacFile.handles.put(entry.tpuid, assetHandle);
            return builder;
        }

    }
}
