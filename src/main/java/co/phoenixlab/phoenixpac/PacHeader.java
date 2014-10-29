package co.phoenixlab.phoenixpac;

public class PacHeader {

    public static final int FLAG_WIDE_SIZE = 1,
            FLAG_WIDE_POSITION = 2,
            FLAG_GIGA = 4;

    public static final int MAGIC_NUMBER = 0x50504143;

    protected int majorVersion;
    protected int minorVersion;
    protected long fileCreatedTimestamp;
    protected long fileModifiedTimestamp;
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

    public long getFileCreatedTimestamp() {
        return fileCreatedTimestamp;
    }

    public long getFileModifiedTimestamp() {
        return fileModifiedTimestamp;
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

    public boolean getWideSizeFlag() {
        return (flags & FLAG_WIDE_SIZE) != 0;
    }

    public boolean getWidePositionFlag() {
        return (flags & FLAG_WIDE_POSITION) != 0;
    }

    public boolean getGigaFlag() {
        return (flags & FLAG_GIGA) != 0;
    }


}
