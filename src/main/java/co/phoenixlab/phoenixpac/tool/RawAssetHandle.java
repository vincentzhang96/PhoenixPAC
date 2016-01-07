package co.phoenixlab.phoenixpac.tool;

import co.phoenixlab.phoenixpac.AssetHandle;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

class RawAssetHandle implements AssetHandle {

    private final Path path;

    public RawAssetHandle(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public AssetHandle copy() {
        return new RawAssetHandle(path);
    }

    @Override
    public byte[] getRawBytes() throws IOException {
        return Files.readAllBytes(path);
    }

    @Override
    public ByteBuffer getRawByteBuffer() throws IOException {
        try (SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
            while (channel.read(buffer) != -1) {
            }
            buffer.flip();
            return buffer;
        }
    }

    @Override
    public InputStream getRawStream() throws IOException {
        return Files.newInputStream(path, StandardOpenOption.READ);
    }

    @Override
    public int getCompressionId() {
        return 0;
    }
}
