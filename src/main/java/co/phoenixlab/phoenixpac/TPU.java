package co.phoenixlab.phoenixpac;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class TPU {

    public static TypePurposeUniqueId tpu(int type, int group, int unique) {
        return new TypePurposeUniqueId(type, group, unique);
    }

    public static TypePurposeUniqueId valueOf(String s)
            throws NumberFormatException {
        String[] split = s.split(",");
        if (split.length != 3 && split.length != 2) {
            throw new NumberFormatException(s);
        }
        if (split.length == 3) {
            int type = parseIntHex(split[0]);
            int group = parseIntHex(split[1]);
            int unique = parseIntHex(split[2]);
            return tpu(type, group, unique);
        } else {
            int typegroup = parseIntHex(split[0]);
            int unique = parseIntHex(split[1]);
            return tpu((typegroup >> 16) & 0xFFFF, typegroup & 0xFFFF, unique);
        }
    }

    private static int parseIntHex(String s) {
        if (s.toLowerCase().startsWith("0x")) {
            s = s.substring(2);
        }
        return Integer.parseInt(s, 16);
    }


    public static URL asURL(int type, int purpose, int unique) {
        try {
            return new URL(String.format("pac://0x%04x/0x%04x/0x%08x",
                    type & 0xFFFF, purpose & 0xFFFF, unique));
        } catch (MalformedURLException e) {
            //  Shouldn't happen
            throw new RuntimeException(e);
        }
    }

    public static URL asURL(TypePurposeUniqueId tpuid) {
        try {
            return new URL(String.format("pac://0x%04x/0x%04x/0x%08x",
                    tpuid.getTypeId(), tpuid.getPurposeId(), tpuid.getUniqueId()));
        } catch (MalformedURLException e) {
            //  Shouldn't happen
            throw new RuntimeException(e);
        }
    }

    public static Iterable<TypePurposeUniqueId> range(TypePurposeUniqueId start, TypePurposeUniqueId end) {
        return new TPUIDIterable(start, end);
    }

    public static Stream<TypePurposeUniqueId> streamRange(TypePurposeUniqueId start, TypePurposeUniqueId end) {
        return StreamSupport.stream(range(start, end).spliterator(), false);
    }

    static class TPUIDIterable implements Iterable<TypePurposeUniqueId> {

        private final TypePurposeUniqueId start, end;

        public TPUIDIterable(TypePurposeUniqueId start, TypePurposeUniqueId end) {
            this.start = start;
            this.end = end;
            if (start.compareTo(end) > 0) {
                throw new IllegalArgumentException("Start must be less than end");
            }
        }

        @Override
        public Iterator<TypePurposeUniqueId> iterator() {
            return new TPUIDIterator();
        }

        class TPUIDIterator implements Iterator<TypePurposeUniqueId> {

            int type;
            int purpose;
            int unique;

            public TPUIDIterator() {
                type = start.getTypeId();
                purpose = start.getPurposeId();
                unique = start.getUniqueId();
            }

            @Override
            public boolean hasNext() {
                return !(end.typeEquals(type) && end.purposeEquals(purpose) && end.uniqueEquals(unique));
            }

            @Override
            public TypePurposeUniqueId next() {
                TypePurposeUniqueId ret = tpu(type, purpose, unique);
                if (!ret.equals(end)) {
                    if (unique == 0xFFFFFFFF) {
                        unique = 0;
                        if (purpose == 0xFFFF) {
                            purpose = 0;
                            if (type == 0xFFFF) {
                                type = 0;
                            } else {
                                ++type;
                            }
                        } else {
                            ++purpose;
                        }
                    } else {
                        ++unique;
                    }
                }
                return ret;
            }
        }
    }

}
