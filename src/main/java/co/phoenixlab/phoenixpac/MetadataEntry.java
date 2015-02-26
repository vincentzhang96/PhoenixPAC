/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Vincent Zhang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package co.phoenixlab.phoenixpac;

import java.nio.charset.StandardCharsets;

public class MetadataEntry {

    protected int keyLength;
    protected int valLength;
    protected String key;
    protected String val;

    public MetadataEntry() {
    }

    public MetadataEntry(String key, String val) {
        this.key = utf8truncate(key, 255);
        this.val = utf8truncate(val, 255);
        keyLength = key.getBytes(StandardCharsets.UTF_8).length;
        valLength = val.getBytes(StandardCharsets.UTF_8).length;
        if (keyLength > 255) {
            throw new IllegalStateException("Truncated key string is greater than 255 bytes");
        }
        if (valLength > 255) {
            throw new IllegalStateException("Truncated val string is greater than 255 bytes");
        }
    }

    public MetadataEntry(MetadataEntry other) {
        this.keyLength = other.keyLength;
        this.valLength = other.valLength;
        this.key = other.key;
        this.val = other.val;
    }

    public int getKeyLength() {
        return keyLength;
    }

    public int getValLength() {
        return valLength;
    }

    public String getKey() {
        return key;
    }

    public String getVal() {
        return val;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MetadataEntry that = (MetadataEntry) o;

        return key.equals(that.key) && val.equals(that.val);

    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + val.hashCode();
        return result;
    }

    /* https://gist.github.com/lpar/1031951 */
    private static String utf8truncate(String input, int length) {
        StringBuilder result = new StringBuilder(length);
        int resultlen = 0;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            int charlen = 0;
            if (c <= 0x7f) {
                charlen = 1;
            } else if (c <= 0x7ff) {
                charlen = 2;
            } else if (c <= 0xd7ff) {
                charlen = 3;
            } else if (c <= 0xdbff) {
                charlen = 4;
            } else if (c <= 0xdfff) {
                charlen = 0;
            } else if (c <= 0xffff) {
                charlen = 3;
            }
            if (resultlen + charlen > length) {
                break;
            }
            result.append(c);
            resultlen += charlen;
        }
        return result.toString();
    }
}
