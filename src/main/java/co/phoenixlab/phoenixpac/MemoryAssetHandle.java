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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class MemoryAssetHandle implements AssetHandle {

    protected byte[] data;
    protected int compressionId;

    public MemoryAssetHandle() {
        this(0);
    }

    public MemoryAssetHandle(int size) {
        data = new byte[size];
    }

    public MemoryAssetHandle(byte[] data) {
        this.data = data;
    }

    public MemoryAssetHandle(MemoryAssetHandle other) {
        this.data = Arrays.copyOf(other.data, other.data.length);
        this.compressionId = other.compressionId;
    }
    public void setRawBytes(byte[] data) {
        this.data = data;
    }

    @Override
    public AssetHandle copy() {
        return new MemoryAssetHandle(this);
    }

    @Override
    public byte[] getRawBytes() throws IOException {
        return data;
    }

    @Override
    public ByteBuffer getRawByteBuffer() throws IOException {
        return ByteBuffer.wrap(data);
    }

    @Override
    public InputStream getRawStream() throws IOException {
        return new ByteArrayInputStream(data);
    }

    @Override
    public int getCompressionId() {
        return compressionId;
    }
}
