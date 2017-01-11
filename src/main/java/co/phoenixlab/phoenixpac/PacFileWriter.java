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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.DeflaterInputStream;

public class PacFileWriter implements AutoCloseable {

    private static final byte[] EMPTY_RESERVED = new byte[16];

    private RandomAccessFile randomAccessFile;
    private final boolean compressFiles;

    public PacFileWriter(RandomAccessFile randomAccessFile) {
        this(randomAccessFile, false);
    }

    public PacFileWriter(RandomAccessFile randomAccessFile, boolean compressFiles) {
        this.randomAccessFile = randomAccessFile;
        this.compressFiles = compressFiles;
    }

    public PacFileWriter(File file) throws IOException {
        this(file, false);
    }

    public PacFileWriter(File file, boolean compressFiles) throws IOException {
        randomAccessFile = new RandomAccessFile(file, "rw");
        this.compressFiles = compressFiles;
    }

    public PacFileWriter(Path path) throws IOException {
        this(path.toFile());
    }

    public PacFileWriter(Path path, boolean compressFiles) throws IOException {
        this(path.toFile(), compressFiles);
    }

    public void writeNew(HandledPacFile<AssetHandle> pacFile) throws IOException {
        HandledPacFile<AssetHandle> pac = new HandledPacFile<>(pacFile);
        randomAccessFile.seek(0);
        randomAccessFile.setLength(0);
        //  The first time we write the header we don't care about the offsets - set them to 0
        pac.header.indexSectionOffset = 0L;
        pac.header.metadataSectionOffset = 0L;
        pac.header.trashSectionOffset = 0L;
        boolean wide = pac.header.getFlag(PacHeader.FLAG_USE_LONG_OFFSETS);
        writeHeader(pac.header, wide);

        //  Write files (and update the index entries)
        writeFiles(pac);

        //  Write the index
        pac.header.indexSectionOffset = writeIndex(pac.index, wide);

        //  Write metadata
        pac.header.metadataSectionOffset = writeMetadata(pac.getMetadata());

        //  No trash

        //  Rewrite header
        randomAccessFile.seek(0);
        writeHeader(pac.header, wide);
    }

    private void writeHeader(PacHeader header, boolean wide) throws IOException {
        randomAccessFile.writeInt(PacHeader.MAGIC_NUMBER);
        randomAccessFile.writeShort(header.majorVersion);
        randomAccessFile.writeShort(header.minorVersion);
        randomAccessFile.write(EMPTY_RESERVED);
        //  Later on we should smartly detect if we need long offsets or not.
        //  For now keep what we have.
        randomAccessFile.writeInt(header.flags);
        if (wide) {
            randomAccessFile.writeLong(header.indexSectionOffset);
            randomAccessFile.writeLong(header.metadataSectionOffset);
            randomAccessFile.writeLong(header.trashSectionOffset);
        } else {
            randomAccessFile.writeInt((int) (header.indexSectionOffset & 0xFFFFFFFFL));
            randomAccessFile.writeInt((int) (header.metadataSectionOffset & 0xFFFFFFFFL));
            randomAccessFile.writeInt((int) (header.trashSectionOffset & 0xFFFFFFFFL));
        }
    }

    private void writeFiles(HandledPacFile<AssetHandle> pacFile) throws IOException {
        for (Map.Entry<TypePurposeUniqueId, AssetHandle> entry : pacFile.getHandles().entrySet()) {
            IndexEntry indx = pacFile.getIndex().getEntry(entry.getKey());
            AssetHandle handle = entry.getValue();
            indx.offset = randomAccessFile.getFilePointer();
            indx.compressionId = PacFile.COMPRESSION_DEFLATE;
            int srcCompId = handle.getCompressionId();
            if (srcCompId == 0 && compressFiles) {
                //  Compress the file
                indx.compressionId = PacFile.COMPRESSION_DEFLATE;
                DeflaterInputStream srcIn = new DeflaterInputStream(handle.getRawStream(),
                    new Deflater(Deflater.BEST_COMPRESSION));
                ReadableByteChannel srcCh = Channels.newChannel(srcIn);
                long written = 0;
                long writ;
                long pos = randomAccessFile.getFilePointer();
                while ((writ = randomAccessFile.getChannel().transferFrom(srcCh, pos + written, 8192)) != 0) {
                    written += writ;
                }
                int diskSize = (int)(Math.min(written, Integer.MAX_VALUE));
                randomAccessFile.skipBytes(Math.max((int) written, 0));
                indx.diskSize = diskSize;
            } else {
                //  Carry compression over
                indx.compressionId = srcCompId;
                randomAccessFile.write(handle.getRawBytes());
                indx.diskSize = (int)(randomAccessFile.getFilePointer() - indx.offset);
            }
        }
    }

    private long writeIndex(Index index, boolean wide) throws IOException {
        long startPos = randomAccessFile.getFilePointer();
        randomAccessFile.writeInt(index.numIndexEntries);
        for (IndexEntry entry : index.entries.values()) {
            randomAccessFile.writeInt(entry.tpuid.getTypePurposeCombinedId());
            randomAccessFile.writeInt(entry.tpuid.getUniqueId());
            if (wide) {
                randomAccessFile.writeLong(entry.offset);
            } else {
                randomAccessFile.writeInt((int) entry.offset);
            }
            randomAccessFile.writeInt(entry.diskSize);
            randomAccessFile.writeInt(entry.memorySize);
            randomAccessFile.writeInt(0x00FFFFFF & (entry.compressionId << 24));
            randomAccessFile.write(entry.sha256Hash);
        }
        randomAccessFile.writeInt(Index.GUARD_BYTES);
        return startPos;
    }

    private long writeMetadata(PacMetadata metadata) throws IOException {
        if (metadata == null || metadata.metadata.isEmpty()) {
            return 0;
        }
        metadata.calculateSize();
        long startPos = randomAccessFile.getFilePointer();
        randomAccessFile.writeInt(metadata.size);
        randomAccessFile.writeInt(metadata.numMetadataBlocks);
        for (MetadataBlock block : metadata.metadata.values()) {
            randomAccessFile.writeInt(block.tpuid.getTypePurposeCombinedId());
            randomAccessFile.writeInt(block.tpuid.getUniqueId());
            randomAccessFile.writeShort(block.numberOfEntries);
            randomAccessFile.writeShort(block.size);
            for (MetadataEntry entry : block.entries.values()) {
                randomAccessFile.writeByte(entry.keyLength);
                randomAccessFile.writeByte(entry.valLength);
                randomAccessFile.write(entry.key.getBytes(StandardCharsets.UTF_8));
                randomAccessFile.write(entry.val.getBytes(StandardCharsets.UTF_8));
            }
        }
        randomAccessFile.writeInt(PacMetadata.GUARD_BYTES);
        return startPos;
    }

    @Override
    public void close() throws IOException {
        randomAccessFile.close();
    }
}
