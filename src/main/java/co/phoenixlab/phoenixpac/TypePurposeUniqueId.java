package co.phoenixlab.phoenixpac;

public class TypePurposeUniqueId implements Comparable<TypePurposeUniqueId> {

    public static final TypePurposeUniqueId ZERO_TPUID = new TypePurposeUniqueId(0, 0);
    public static final TypePurposeUniqueId T_TPUID = new TypePurposeUniqueId(0xFFFF0000, 0);
    public static final TypePurposeUniqueId P_TPUID = new TypePurposeUniqueId(0x0000FFFF, 0);
    public static final TypePurposeUniqueId U_TPUID = new TypePurposeUniqueId(0, 0xFFFFFFFF);
    public static final TypePurposeUniqueId TP_TPUID = new TypePurposeUniqueId(0xFFFFFFFF, 0);

    private final int typePurposeId;
    private final int uniqueId;

    public TypePurposeUniqueId(int typeId, int purposeId, int uniqueId) {
        this(((typeId << 16) & 0xFFFF0000) | (purposeId & 0x0000FFFF), uniqueId);
    }

    protected TypePurposeUniqueId(int typePurposeId, int uniqueId) {
        this.typePurposeId = typePurposeId;
        this.uniqueId = uniqueId;
    }

    public TypePurposeUniqueId(TypePurposeUniqueId other) {
        this.typePurposeId = other.typePurposeId;
        this.uniqueId = other.uniqueId;
    }

    public int getTypeId() {
        return ((typePurposeId >> 16) & 0xFFFF);
    }

    public int getPurposeId() {
        return typePurposeId & 0xFFFF;
    }

    public int getUniqueId() {
        return uniqueId;
    }

    public int getTypePurposeCombinedId() {
        return typePurposeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TypePurposeUniqueId that = (TypePurposeUniqueId) o;
        return typePurposeId == that.typePurposeId && uniqueId == that.uniqueId;
    }

    @Override
    public int hashCode() {
        int result = typePurposeId;
        result = 31 * result + uniqueId;
        return result;
    }

    @Override
    public String toString() {
        return String.format("TPUID[type=0x%04X purpose=0x%04X unique=0x%08X]", getTypeId(), getPurposeId(), getUniqueId());
    }

    public TypePurposeUniqueId and(TypePurposeUniqueId other) {
        return new TypePurposeUniqueId(this.typePurposeId & other.typePurposeId, this.uniqueId & other.uniqueId);
    }

    public TypePurposeUniqueId or(TypePurposeUniqueId other) {
        return new TypePurposeUniqueId(this.typePurposeId | other.typePurposeId, this.uniqueId | other.uniqueId);
    }

    public TypePurposeUniqueId xor(TypePurposeUniqueId other) {
        return new TypePurposeUniqueId(this.typePurposeId ^ other.typePurposeId, this.uniqueId ^ other.uniqueId);
    }

    public TypePurposeUniqueId not() {
        return new TypePurposeUniqueId(~this.typePurposeId, ~this.uniqueId);
    }

    public TypePurposeUniqueId xnor(TypePurposeUniqueId other) {
        return new TypePurposeUniqueId(~(this.typePurposeId ^ other.typePurposeId), ~(this.uniqueId ^ other.uniqueId));
    }

    public TypePurposeUniqueId add(TypePurposeUniqueId other) {
        return new TypePurposeUniqueId(this.getTypeId() + other.getTypeId(), this.getPurposeId() + other.getPurposeId(), this.uniqueId + other.uniqueId);
    }

    public TypePurposeUniqueId subtract(TypePurposeUniqueId other) {
        return new TypePurposeUniqueId(this.getTypeId() - other.getTypeId(), this.getPurposeId() - other.getPurposeId(), this.uniqueId - other.uniqueId);
    }

    public boolean typeEquals(int typeId) {
        return (this.typePurposeId & 0xFFFF0000) == typeId << 16;
    }

    public boolean purposeEquals(int purposeId) {
        return (this.typePurposeId & 0xFFFF) == (purposeId & 0xFFFF);
    }

    public boolean uniqueEquals(int uniqueId) {
        return this.uniqueId == uniqueId;
    }

    @Override
    public int compareTo(TypePurposeUniqueId o) {
        //  Since the top 2 bytes are the type ID, and we sort in order of type, purpose, unique, we can simply perform unsigned comparison directly without
        //      splitting it into components.
        if(this.typePurposeId == o.typePurposeId) {
            return Integer.compareUnsigned(this.uniqueId, o.uniqueId);
        }
        return Integer.compareUnsigned(this.typePurposeId, o.typePurposeId);
    }
}
