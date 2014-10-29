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
import java.nio.ByteBuffer;

public class InMemoryDataEntry extends DataEntry {

    private final boolean readOnly;

    private ByteBuffer buffer;

    public InMemoryDataEntry(long creationTimestamp, long lastModifiedTimestamp) {
        super(creationTimestamp, lastModifiedTimestamp);
        this.readOnly = false;
    }

    public InMemoryDataEntry(long creationTimestamp, long lastModifiedTimestamp, ByteBuffer buffer, boolean readOnly) {
        super(creationTimestamp, lastModifiedTimestamp);
        this.buffer = buffer;
        this.buffer = buffer.asReadOnlyBuffer();
        this.readOnly = readOnly;
    }

    @Override
    public ByteBuffer getDataAsBuffer() throws IOException {
        ByteBuffer ret = buffer.asReadOnlyBuffer();
        ret.rewind();
        return ret;
    }

    @Override
    public byte[] getDataAsBytes() throws IOException {
        ByteBuffer buf = getDataAsBuffer();
        byte[] ret = new byte[buf.limit()];
        buf.get(ret);
        return ret;
    }

    @Override
    public InputStream getDataAsStream() throws IOException {
        if (readOnly) {
            return new ByteBufferInputStream(getDataAsBuffer());
        } else {
            ByteBuffer readOnly = buffer.asReadOnlyBuffer();
            ByteBuffer copy = ByteBuffer.allocateDirect(readOnly.limit());
            copy.put(readOnly);
            copy.flip();
            return new ByteBufferInputStream(copy);
        }
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public void setDataFromBuffer(ByteBuffer buf) {
        if (buffer == null) {
            buffer = ByteBuffer.allocateDirect(buf.limit());
        }
        buffer.rewind();
        buffer.put(buf);
        buffer.flip();
    }

    @Override
    public void setData(byte[] data) {
        if (buffer == null || buffer.capacity() < data.length) {
            buffer = ByteBuffer.wrap(data);
        }
        buffer.rewind();
        buffer.put(data);
        buffer.flip();
    }

    private class ByteBufferInputStream extends InputStream {

        private final ByteBuffer byteBuffer;

        private ByteBufferInputStream(ByteBuffer byteBuffer) {
            this.byteBuffer = byteBuffer;
            this.byteBuffer.rewind();
        }

        @Override
        public int read() throws IOException {
            if (byteBuffer.hasRemaining()) {
                return Byte.toUnsignedInt(byteBuffer.get());
            } else {
                return -1;
            }
        }
    }
}
