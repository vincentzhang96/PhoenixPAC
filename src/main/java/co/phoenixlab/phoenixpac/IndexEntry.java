package co.phoenixlab.phoenixpac;

public class IndexEntry {

    protected TypePurposeUniqueId tpuid;
    protected long physicalSize;
    protected long location;
    protected int compressionId;
    protected long memorySize;
    protected int adler32;

    public TypePurposeUniqueId getTpuid() {
        return tpuid;
    }

    public long getPhysicalSize() {
        return physicalSize;
    }

    public long getLocation() {
        return location;
    }

    public int getCompressionId() {
        return compressionId;
    }

    public long getMemorySize() {
        return memorySize;
    }

    public int getAdler32() {
        return adler32;
    }
}
