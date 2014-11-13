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
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;

public class OnDiskAssetHandle implements AssetHandle {

    private final IndexEntry indexEntry;
    private final RandomAccessFile file;

    public OnDiskAssetHandle(IndexEntry indexEntry, RandomAccessFile file) {
        this.indexEntry = indexEntry;
        this.file = file;
    }

    @Override
    public byte[] getRawBytes() throws IOException {
        byte[] ret = new byte[indexEntry.diskSize];
        file.seek(indexEntry.offset);
        file.readFully(ret);
        return ret;
    }

    @Override
    public ByteBuffer getRawByteBuffer() throws IOException {
        ByteBuffer ret = ByteBuffer.allocateDirect(indexEntry.diskSize);
        FileChannel channel = file.getChannel().position(indexEntry.offset);
        int bytesRead = 0;
        while (bytesRead < indexEntry.diskSize) {
            bytesRead += channel.read(ret);
        }
        ret.flip();
        return ret;
    }

    @Override
    public InputStream getRawStream() throws IOException {
        FileChannel channel = file.getChannel().position(indexEntry.offset);
        InputStream stream = Channels.newInputStream(channel);
        return new OnDiskInputStream(stream, indexEntry.diskSize);
    }
}

class OnDiskInputStream extends InputStream {

    private final InputStream stream;
    private final int size;
    private int count;

    public OnDiskInputStream(InputStream stream, int size) {
        this.stream = stream;
        this.size = size;
        this.count = 0;
    }

    @Override
    public int read() throws IOException {
        if (count < size) {
            count++;
            return stream.read();
        }
        return -1;
    }

    @Override
    public int available() throws IOException {
        return size - count;
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }

    @Override
    public boolean markSupported() {
        return false;
    }
}