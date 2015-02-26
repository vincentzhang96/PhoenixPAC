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

public class HandlePacBuilder {

    private HandledPacFile<AssetHandle> pacFile;

    private HandlePacBuilder() {
        pacFile = new HandledPacFile<>();
        pacFile.index = new Index();
    }

    public HeaderBuilder buildHeader() {
        return new HeaderBuilder(this);
    }

    public EntryBuilder newEntry() {
        return new EntryBuilder(this);
    }

    public HandlePacBuilder addEntry(IndexEntry entry, AssetHandle assetHandle) {
        EntryBuilder builder = new EntryBuilder(this);
        builder.entry = entry;
        builder.assetHandle = assetHandle;
        builder.add();
        return this;
    }

    public MetadataBuilder buildMetadata() {
        return new MetadataBuilder(this);
    }

    public HandledPacFile<AssetHandle> finish() {
        if (pacFile.header == null) {
            throw new IllegalStateException("Header not constructed");
        }
        pacFile.index.syncCount();
        return pacFile;
    }


    public static HandlePacBuilder newBuilder() {
        return new HandlePacBuilder();
    }

    public class HeaderBuilder {

        private PacHeader header;
        private HandlePacBuilder builder;

        HeaderBuilder(HandlePacBuilder builder) {
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

        public HandlePacBuilder finishHeader() {
            pacFile.header = header;
            return builder;
        }

    }

    public class EntryBuilder {
        private IndexEntry entry;
        private AssetHandle assetHandle;
        private HandlePacBuilder builder;

        EntryBuilder(HandlePacBuilder builder) {
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

        public EntryBuilder withDiskSize(int diskSize) {
            entry.diskSize = diskSize;
            return this;
        }

        public EntryBuilder withSize(int size) {
            entry.diskSize = entry.memorySize = size;
            return this;
        }

        public EntryBuilder withAssetHandle(AssetHandle assetHandle) {
            this.assetHandle = assetHandle;
            return this;
        }

        public EntryBuilder withComputedSha256Hash() throws IOException {
            if (assetHandle == null) {
                throw new IllegalStateException("Must call withMemoryAssetHandle() first");
            }
            entry.sha256Hash = IndexEntry.computeSha256Hash(assetHandle.getRawBytes());
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

        public HandlePacBuilder add() {
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

    public class MetadataBuilder {
        private PacMetadata metadata;
        private HandlePacBuilder builder;

        public MetadataBuilder(HandlePacBuilder builder) {
            this.builder = builder;
            metadata = builder.pacFile.metadata;
            if (metadata == null) {
                metadata = new PacMetadata();
            }
        }

        public MetadataBlockBuilder editBlock(TypePurposeUniqueId tpuid) {
            return new MetadataBlockBuilder(this, tpuid, metadata.getMetadata().get(tpuid));
        }

        public HandlePacBuilder finish() {
            builder.pacFile.metadata = metadata;
            return builder;
        }

        public class MetadataBlockBuilder {
            private TypePurposeUniqueId tpuid;
            private MetadataBlock block;
            private MetadataBuilder builder;

            public MetadataBlockBuilder(MetadataBuilder builder, TypePurposeUniqueId tpuid, MetadataBlock block) {
                this.builder = builder;
                this.tpuid = tpuid;
                this.block = block == null ? new MetadataBlock() : block;
                this.block.tpuid = tpuid;
            }

            public MetadataBlockBuilder addEntry(String key, String val) {
                MetadataEntry entry = new MetadataEntry(key, val);
                block.entries.put(entry.getKey(), entry);
                return this;
            }

            public MetadataBuilder add() {
                builder.metadata.metadata.put(tpuid, block);
                return builder;
            }
        }
    }
}
