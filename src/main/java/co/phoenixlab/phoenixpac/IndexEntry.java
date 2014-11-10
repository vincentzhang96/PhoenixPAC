package co.phoenixlab.phoenixpac;

public class IndexEntry {

    protected TypePurposeUniqueId tpuid;
    protected long offset;
    protected int diskSize;
    protected int memorySize;
    protected int compressionId;
    protected byte[] sha256Hash;

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
