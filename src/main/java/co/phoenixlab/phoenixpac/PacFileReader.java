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

import co.phoenixlab.phoenixpac.throwables.InvalidPacFormatException;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class PacFileReader implements AutoCloseable {

    private RandomAccessFile randomAccessFile;

    public PacFileReader(RandomAccessFile randomAccessFile) {
        this.randomAccessFile = randomAccessFile;
    }

    public PacFileReader(File file) throws IOException {
        randomAccessFile = new RandomAccessFile(file, "r");
    }

    public PacFileReader(Path path) throws IOException {
        this(path.toFile());
    }

    public PacFile read() throws IOException {
        PacFile pacFile = new FilesystemPacFile(randomAccessFile);
        readHeader(pacFile);
        readIndex(pacFile);
        readMetadata(pacFile);
        readTrashIndex(pacFile);
        return pacFile;
    }

    private void readHeader(PacFile pacFile) throws IOException {
        final PacHeader header = new PacHeader();
        int magicNumber = randomAccessFile.readInt();
        if (magicNumber != PacHeader.MAGIC_NUMBER) {
            throw new InvalidPacFormatException(String.format("Bad magic: Expected %08X, got %08X",
                                                              PacHeader.MAGIC_NUMBER,
                                                              magicNumber));
        }
        header.majorVersion = randomAccessFile.readUnsignedShort();
        header.minorVersion = randomAccessFile.readUnsignedShort();
        if (header.majorVersion != 4) {
            throw new InvalidPacFormatException("Bad major version: Expected 4, got " + header.majorVersion);
        }
        if (header.minorVersion != 0) {
            throw new InvalidPacFormatException("Bad major version: Expected 0, got " + header.minorVersion);
        }
        header.reservedA = randomAccessFile.readLong();
        header.reservedB = randomAccessFile.readLong();
        header.flags = randomAccessFile.readInt();
        if (header.getFlag(PacHeader.FLAG_USE_LONG_OFFSETS)) {
            header.indexSectionOffset = randomAccessFile.readLong();
            header.metadataSectionOffset = randomAccessFile.readLong();
            header.trashSectionOffset = randomAccessFile.readLong();
        } else {
            header.indexSectionOffset = Integer.toUnsignedLong(randomAccessFile.readInt());
            header.metadataSectionOffset = Integer.toUnsignedLong(randomAccessFile.readInt());
            header.trashSectionOffset = Integer.toUnsignedLong(randomAccessFile.readInt());
        }
        pacFile.header = header;
    }

    private void readIndex(PacFile pacFile) throws IOException {
        randomAccessFile.seek(pacFile.header.indexSectionOffset);
        final int numEntries = randomAccessFile.readInt();
        if (numEntries < 0) {
            throw new InvalidPacFormatException("Cannot have a negative number of Index Entries");
        }
        final Index index = new Index(numEntries);
        final boolean useLong = pacFile.header.getFlag(PacHeader.FLAG_USE_LONG_OFFSETS);
        for (int i = 0; i < numEntries; i++) {
            readIndexEntry(index, useLong);
        }
        pacFile.index = index;
    }

    private void readIndexEntry(final Index index, final boolean useLong) throws IOException {
        final IndexEntry entry  = new IndexEntry();
        entry.tpuid = readTPUID();
        if (useLong) {
            entry.offset = randomAccessFile.readLong();
        } else {
            entry.offset = Integer.toUnsignedLong(randomAccessFile.readInt());
        }
        entry.diskSize = randomAccessFile.readInt();
        entry.memorySize = randomAccessFile.readInt();
        entry.compressionId = randomAccessFile.readInt();
        byte[] shaHash = new byte[16];
        randomAccessFile.readFully(shaHash);
        entry.sha256Hash = shaHash;
        index.entries.put(entry.getTPUID(), entry);
    }

    private void readMetadata(final PacFile pacFile) throws IOException {
        final PacMetadata metadata = new PacMetadata();
        if (pacFile.header.metadataSectionOffset != 0) {
            randomAccessFile.seek(pacFile.header.metadataSectionOffset);
            metadata.size = randomAccessFile.readInt();
            metadata.numMetadataBlocks = randomAccessFile.readInt();
            for (int i = 0; i < metadata.numMetadataBlocks; i++) {
                readMetadataBlock(metadata);
            }
        }
        pacFile.metadata = metadata;
    }

    private void readMetadataBlock(PacMetadata metadata) throws IOException {
        final MetadataBlock block = new MetadataBlock();
        block.tpuid = readTPUID();
        block.numberOfEntries = randomAccessFile.readUnsignedShort();
        block.size = randomAccessFile.readUnsignedShort();
        for (int i = 0; i < block.numberOfEntries; i++) {
            readMetadataEntry(block);
        }
        metadata.metadata.put(block.tpuid, block);
    }

    private void readMetadataEntry(MetadataBlock metadataBlock) throws IOException {
        MetadataEntry entry = new MetadataEntry();
        entry.keyLength = randomAccessFile.readUnsignedByte();
        entry.valLength = randomAccessFile.readUnsignedByte();
        byte[] keyBytes = new byte[entry.keyLength];
        byte[] valBytes = new byte[entry.valLength];
        randomAccessFile.readFully(keyBytes);
        randomAccessFile.readFully(valBytes);
        entry.key = new String(keyBytes, StandardCharsets.UTF_8);
        entry.val = new String(valBytes, StandardCharsets.UTF_8);
        metadataBlock.entries.put(entry.key, entry);
    }

    private void readTrashIndex(PacFile pacFile) throws IOException {
        TrashIndex index = new TrashIndex();
        final boolean useLong = pacFile.header.getFlag(PacHeader.FLAG_USE_LONG_OFFSETS);
        if (pacFile.header.trashSectionOffset != 0) {
            randomAccessFile.seek(pacFile.header.trashSectionOffset);
            index.numTrashEntries = randomAccessFile.readInt();
            for (int i = 0; i < index.numTrashEntries; i++) {
                readTrashIndexEntry(index, useLong);
            }
        }
        pacFile.trashIndex = index;
    }

    private void readTrashIndexEntry(TrashIndex index, boolean useLong) throws IOException {
        TrashIndexEntry entry = new TrashIndexEntry();
        if (useLong) {
            entry.offset = randomAccessFile.readLong();
        } else {
            entry.offset = Integer.toUnsignedLong(randomAccessFile.readInt());
        }
        entry.size = randomAccessFile.readInt();
        index.entries.add(entry);
    }

    private TypePurposeUniqueId readTPUID() throws IOException {
        int typePurpose = randomAccessFile.readInt();
        int unique = randomAccessFile.readInt();
        return new TypePurposeUniqueId(typePurpose, unique);
    }


    @Override
    public void close() throws Exception {
        randomAccessFile.close();
    }
}

