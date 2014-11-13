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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class MemoryPacFile extends PacFile {

    protected final LinkedHashMap<TypePurposeUniqueId, InMemoryAssetHandle> handles;

    {
        handles = new LinkedHashMap<>();
    }

    public MemoryPacFile() {
    }

    public MemoryPacFile(PacFile other) {
        super(other);
    }

    public MemoryPacFile(MemoryPacFile other) {
        super(other);
        handles.clear();
        for (Map.Entry<TypePurposeUniqueId, InMemoryAssetHandle> entry : other.handles.entrySet()) {
            handles.put(entry.getKey(), new InMemoryAssetHandle(entry.getValue()));
        }
    }


    @Override
    public AssetHandle getHandle(TypePurposeUniqueId tpuid) throws IOException {
        AssetHandle handle = handles.get(tpuid);
        if (handle != null) {
            return handle;
        }
        throw new FileNotFoundException(tpuid.toString());
    }
}
