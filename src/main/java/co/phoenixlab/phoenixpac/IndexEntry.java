package co.phoenixlab.phoenixpac;

import java.util.Arrays;

public class IndexEntry {

    protected TypePurposeUniqueId tpuid;
    protected long offset;
    protected int diskSize;
    protected int memorySize;
    protected int compressionId;
    protected byte[] sha256Hash;

    public IndexEntry() {
    }

    public IndexEntry(IndexEntry other) {
        this.tpuid = other.tpuid;
        this.offset = other.offset;
        this.diskSize = other.diskSize;
        this.memorySize = other.memorySize;
        this.compressionId = other.compressionId;
        this.sha256Hash = Arrays.copyOf(other.sha256Hash, other.sha256Hash.length);
    }


    public TypePurposeUniqueId getTPUID() {
        return tpuid;
    }

    public int getDiskSize() {
        return diskSize;
    }

    public int getMemorySize() {
        return memorySize;
    }

    public long getOffset() {
        return offset;
    }

    public int getCompressionId() {
        return compressionId;
    }

    public byte[] getSha256Hash() {
        return sha256Hash;
    }
}
