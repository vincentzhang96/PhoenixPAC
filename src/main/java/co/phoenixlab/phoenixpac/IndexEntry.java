package co.phoenixlab.phoenixpac;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class IndexEntry {

    public static final int SHA_256_HASH_BYTE_LEN = 32;

    protected TypePurposeUniqueId tpuid;
    protected long offset;
    protected int diskSize;
    protected int memorySize;
    protected int compressionId;
    protected byte[] sha256Hash;

    {
        sha256Hash = new byte[SHA_256_HASH_BYTE_LEN];
    }

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

    public static byte[] computeSha256Hash(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            return digest.digest(input);
        } catch (NoSuchAlgorithmException n) {
            throw new RuntimeException("SHA-256 must be available on this system to use the digest feature.", n);
        }
    }
}
