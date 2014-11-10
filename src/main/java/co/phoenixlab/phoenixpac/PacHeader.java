package co.phoenixlab.phoenixpac;

public class PacHeader {

    public static final int FLAG_USE_LONG_OFFSETS = 1;

    public static final int MAGIC_NUMBER = 0x50504143;

    protected int majorVersion;
    protected int minorVersion;
    protected long reservedA;
    protected long reservedB;
    protected int flags;
    protected long indexSectionOffset;
    protected long metadataSectionOffset;
    protected long trashSectionOffset;

    public int getMajorVersion() {
        return majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public long getReservedA() {
        return reservedA;
    }

    public long getReservedB() {
        return reservedB;
    }

    public int getFlags() {
        return flags;
    }

    public long getIndexSectionOffset() {
        return indexSectionOffset;
    }

    public long getMetadataSectionOffset() {
        return metadataSectionOffset;
    }

    public long getTrashSectionOffset() {
        return trashSectionOffset;
    }

    public boolean getFlag(int flagConst) {
        return (flags & flagConst) != 0;
    }


}
