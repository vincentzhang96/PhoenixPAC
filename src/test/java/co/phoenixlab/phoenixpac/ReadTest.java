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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ReadTest {

    private Path pacPath;
    private String testString;
    private byte[] rawData;
    private TypePurposeUniqueId typePurposeUniqueId;

    private PacFile pacFile;
    private AssetHandle handle;


    @Before
    public void setUp() throws Exception {
        pacPath = Paths.get(".").toAbsolutePath().resolve("test.pac");
        testString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890ëèï▲↔ƒ";
        rawData = testString.getBytes(StandardCharsets.UTF_8);
        typePurposeUniqueId =new TypePurposeUniqueId(0xAABB, 0xCCDD, 0xDEADBEEF);
        MemoryAssetHandle assetHandle = new MemoryAssetHandle(rawData);
        HandledPacFile memoryPacFile = HandlePacBuilder.newBuilder().
                buildHeader().withLatestVersion().setFlag(PacHeader.FLAG_USE_LONG_OFFSETS, true).finishHeader().
                newEntry().
                withTPUID(typePurposeUniqueId).
                withMemoryAssetHandle(assetHandle).
                withMemorySize(rawData.length).
                withNoCompression().
                withComputedSha256Hash().
                add().
                finish();
        PacFileWriter writer = new PacFileWriter(pacPath);
        writer.writeNew(memoryPacFile);
        writer.close();
        PacFileReader reader = new PacFileReader(pacPath);
        pacFile = reader.read();
        handle = pacFile.getHandle(typePurposeUniqueId);
    }

    @Test
    public void headerTest() throws Exception {
        PacHeader header = pacFile.getHeader();
        Assert.assertNotNull(header);
        Assert.assertEquals(PacFile.MAJOR_VERSION, header.getMajorVersion());
        Assert.assertEquals(PacFile.MINOR_VERSION, header.getMinorVersion());
        Assert.assertEquals(0L, header.getReservedA());
        Assert.assertEquals(0L, header.getReservedB());
        Assert.assertTrue(header.getFlag(PacHeader.FLAG_USE_LONG_OFFSETS));
        Assert.assertTrue(header.getIndexSectionOffset() > PacHeader.SIZE_OF);
        long metaOff = header.getMetadataSectionOffset();
        Assert.assertTrue(metaOff > PacHeader.SIZE_OF || metaOff == 0);
        long trashOff = header.getTrashSectionOffset();
        Assert.assertTrue(trashOff > PacHeader.SIZE_OF || trashOff == 0);
    }

    @Test
    public void indexTest() throws Exception {
        Index index = pacFile.getIndex();
        Assert.assertNotNull(index);
        Assert.assertEquals(1, index.getNumIndexEntries());
        Assert.assertEquals(index.getNumIndexEntries(), index.getEntries().size());
        Assert.assertTrue(index.getEntries().containsKey(typePurposeUniqueId));
    }

    @Test
    public void handleTest() throws Exception {
        Assert.assertNotNull(handle);
        Assert.assertEquals(0, handle.getCompressionId());
    }

    @Test
    public void handleByteArrayTest() throws Exception {
        byte[] bytes = handle.getRawBytes();
        Assert.assertArrayEquals(rawData, bytes);
        assertString(bytes);
    }

    @Test
    public void handleInputStreamTest() throws Exception {
        InputStream inputStream = handle.getRawStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buf = new byte[16];
        int len = 0;
        while ((len = inputStream.read(buf)) != -1) {
            outputStream.write(buf, 0, len);
        }
        byte[] bytes = outputStream.toByteArray();
        Assert.assertArrayEquals(rawData, bytes);
        assertString(bytes);
    }

    @Test
    public void handleByteBufferTest() throws Exception {
        ByteBuffer buffer = handle.getRawByteBuffer();
        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);
        Assert.assertArrayEquals(rawData, bytes);
        assertString(bytes);
    }

    private void assertString(byte[] bytes) throws Exception {
        String string = new String(bytes, StandardCharsets.UTF_8);
        Assert.assertEquals(testString, string);
    }
}
