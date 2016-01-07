package co.phoenixlab.phoenixpac;

public class PacHeader {

    public static final int FLAG_USE_LONG_OFFSETS = 1;
    public static final int FLAG_JAVA_ARRAY_COMPAT = 1 << 1;

    public static final int MAGIC_NUMBER = 0x50504143;

    public static final int SIZE_OF = 4 + 4 + 8 + 8 + 4 + 8 + 8 + 8;

    protected int majorVersion;
    protected int minorVersion;
    protected long reservedA;
    protected long reservedB;
    protected int flags;
    protected long indexSectionOffset;
    protected long metadataSectionOffset;
    protected long trashSectionOffset;

    public PacHeader() {
    }

    public PacHeader(PacHeader other) {
        this.majorVersion = other.majorVersion;
        this.minorVersion = other.minorVersion;
        this.reservedA = other.reservedA;
        this.reservedB = other.reservedB;
        this.flags = other.flags;
        this.indexSectionOffset = other.indexSectionOffset;
        this.metadataSectionOffset = other.metadataSectionOffset;
        this.trashSectionOffset = other.trashSectionOffset;
    }

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

    @Override
    public String toString() {
        return String.format("Header{maj=%s min=%s reserved=%016X %016X flags=%08X indxOff=%08X metaOff=%08X trshOff=%08X}",
                             majorVersion, minorVersion, reservedA, reservedB, flags, indexSectionOffset, metadataSectionOffset, trashSectionOffset);
    }

}
