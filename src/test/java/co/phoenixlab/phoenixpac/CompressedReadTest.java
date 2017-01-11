package co.phoenixlab.phoenixpac;

import org.junit.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class CompressedReadTest {

    private Path pacPath;
    private String testString;
    private byte[] rawData;
    private TypePurposeUniqueId typePurposeUniqueId;

    private PacFile pacFile;
    private AssetHandle handle;
    private Inflater inflater;


    @Before
    public void setUp() throws Exception {
        pacPath = Paths.get(".").toAbsolutePath().resolve("test_comp.pac");
        testString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890ëèï▲↔ƒ";
        rawData = testString.getBytes(StandardCharsets.UTF_8);
        typePurposeUniqueId =new TypePurposeUniqueId(0xAABB, 0xCCDD, 0xDEADBEEF);
        MemoryAssetHandle assetHandle = new MemoryAssetHandle(rawData);
        HandledPacFile memoryPacFile = HandlePacBuilder.newBuilder().
            buildHeader().withLatestVersion().setFlag(PacHeader.FLAG_USE_LONG_OFFSETS, true).finishHeader().
            newEntry().
            withTPUID(typePurposeUniqueId).
            withAssetHandle(assetHandle).
            withMemorySize(rawData.length).
            withNoCompression().
            withComputedSha256Hash().
            add().
            finish();
        PacFileWriter writer = new PacFileWriter(pacPath, true);
        writer.writeNew(memoryPacFile);
        writer.close();
        PacFileReader reader = new PacFileReader(pacPath);
        pacFile = reader.read();
        handle = pacFile.getHandle(typePurposeUniqueId);
        inflater = new Inflater();
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
        byte[] bytes = inflate(handle.getRawBytes());
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
        byte[] bytes = inflate(outputStream.toByteArray());

        Assert.assertArrayEquals(rawData, bytes);
        assertString(bytes);
    }

    @Test
    public void handleByteBufferTest() throws Exception {
        ByteBuffer buffer = handle.getRawByteBuffer();
        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);
        byte[] ret = inflate(bytes);
        Assert.assertArrayEquals(rawData, ret);
        assertString(ret);
    }

    private void assertString(byte[] bytes) throws Exception {
        String string = new String(bytes, StandardCharsets.UTF_8);
        Assert.assertEquals(testString, string);
    }

    private byte[] inflate(byte[] in) throws DataFormatException {
        inflater.setInput(in);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int infl;
        while ((infl = inflater.inflate(buf)) != 0) {
            outputStream.write(buf, 0, infl);
        }
        return outputStream.toByteArray();
    }
}
