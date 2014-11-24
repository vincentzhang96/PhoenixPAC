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

import org.junit.Assert;
import org.junit.Test;

public class TPUIDTest {

    @Test
    public void constructor1() {
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(1, 2, 3);
        Assert.assertEquals(1, t1.getTypeId());
        Assert.assertEquals(2, t1.getPurposeId());
        Assert.assertEquals(3, t1.getUniqueId());
    }

    @Test
    public void constructor2() {
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(0x00010002, 3);
        Assert.assertEquals(1, t1.getTypeId());
        Assert.assertEquals(2, t1.getPurposeId());
        Assert.assertEquals(3, t1.getUniqueId());
    }

    @Test
    public void copyConstructor() {
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(1, 2, 3);
        TypePurposeUniqueId t2 = new TypePurposeUniqueId(t1);
        Assert.assertNotSame(t2, t1);
        Assert.assertEquals(t1, t2);
        Assert.assertEquals(1, t2.getTypeId());
        Assert.assertEquals(2, t2.getPurposeId());
        Assert.assertEquals(3, t2.getUniqueId());
    }

    @Test
    public void constructorTypeOverflow() {
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(0xAAAA1234, 0, 0);
        Assert.assertEquals(0x1234, t1.getTypeId());
        Assert.assertEquals(0, t1.getPurposeId());
    }

    @Test
    public void constructorPurposeOverflow() {
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(0, 0xAAAA1234, 0);
        Assert.assertEquals(0x1234, t1.getPurposeId());
        Assert.assertEquals(0, t1.getTypeId());
    }

    @Test
    public void equals() {
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(1, 2, 3);
        TypePurposeUniqueId t2 = new TypePurposeUniqueId(1, 2, 3);
        Assert.assertEquals(t1, t2);
    }

    @Test
    public void equalsSelf() {
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(1, 2, 3);
        Assert.assertEquals(t1, t1);
    }

    @Test
    public void notEqualT() {
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(1, 2, 3);
        TypePurposeUniqueId t2 = new TypePurposeUniqueId(2, 2, 3);
        Assert.assertNotEquals(t1, t2);
    }

    @Test
    public void notEqualP() {
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(1, 2, 3);
        TypePurposeUniqueId t2 = new TypePurposeUniqueId(1, 3, 3);
        Assert.assertNotEquals(t1, t2);
    }

    @Test
    public void notEqualU() {
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(1, 2, 3);
        TypePurposeUniqueId t2 = new TypePurposeUniqueId(1, 2, 4);
        Assert.assertNotEquals(t1, t2);
    }

    @Test
    public void notEqualNull() {
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(1, 2, 3);
        Assert.assertNotEquals(t1, null);
    }

    @Test
    public void hashCodeEquals() {
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(1, 2, 3);
        TypePurposeUniqueId t2 = new TypePurposeUniqueId(1, 2, 3);
        Assert.assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    public void getType() {
        int type = 1;
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(type, 0, 0);
        Assert.assertEquals(type, t1.getTypeId());
    }

    @Test
    public void getPurpose() {
        int purpose = 1;
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(0, purpose, 0);
        Assert.assertEquals(purpose, t1.getPurposeId());
    }

    @Test
    public void getUnique() {
        int unique = 1;
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(0, 0, unique);
        Assert.assertEquals(unique, t1.getUniqueId());
    }

    @Test
    public void getCombined() {
        int combined = 0xFEFE8080;
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(combined, 0);
        Assert.assertEquals(combined, t1.getTypePurposeCombinedId());
    }

    @Test
    public void toStringTest() {
        String s = "TPUID[type=0xDEAD purpose=0xBEEF unique=0xCAFEBABE]";
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(0xDEAD, 0xBEEF, 0xCAFEBABE);
        Assert.assertEquals(s, t1.toString());
    }

    @Test
    public void and() {
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(0x1234, 0x5678, 0x12345678);
        TypePurposeUniqueId mask = new TypePurposeUniqueId(0xFF00, 0xFF10, 0xFED0FF02);
        TypePurposeUniqueId result = t1.and(mask);
        Assert.assertNotSame(result, t1);
        Assert.assertNotSame(result, mask);
        Assert.assertEquals(0x1234 & 0xFF00, result.getTypeId());
        Assert.assertEquals(0x5678 & 0xFF10, result.getPurposeId());
        Assert.assertEquals(0x12345678 & 0xFED0FF02, result.getUniqueId());
    }

    @Test
    public void or() {
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(0x1234, 0x5678, 0x12345678);
        TypePurposeUniqueId mask = new TypePurposeUniqueId(0xFF00, 0xFF10, 0xFED0FF02);
        TypePurposeUniqueId result = t1.or(mask);
        Assert.assertNotSame(result, t1);
        Assert.assertNotSame(result, mask);
        Assert.assertEquals(0x1234 | 0xFF00, result.getTypeId());
        Assert.assertEquals(0x5678 | 0xFF10, result.getPurposeId());
        Assert.assertEquals(0x12345678 | 0xFED0FF02, result.getUniqueId());
    }

    @Test
    public void xor() {
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(0x1234, 0x5678, 0x12345678);
        TypePurposeUniqueId mask = new TypePurposeUniqueId(0xFF00, 0xFF10, 0xFED0FF02);
        TypePurposeUniqueId result = t1.xor(mask);
        Assert.assertNotSame(result, t1);
        Assert.assertNotSame(result, mask);
        Assert.assertEquals(0x1234 ^ 0xFF00, result.getTypeId());
        Assert.assertEquals(0x5678 ^ 0xFF10, result.getPurposeId());
        Assert.assertEquals(0x12345678 ^ 0xFED0FF02, result.getUniqueId());
    }

    @Test
    public void not() {
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(0xFFFF, 0x0000, 0xF0F0F0F0);
        TypePurposeUniqueId result = t1.not();
        Assert.assertNotSame(result, t1);
        Assert.assertEquals(0x0000, result.getTypeId());
        Assert.assertEquals(0xFFFF, result.getPurposeId());
        Assert.assertEquals(0x0F0F0F0F, result.getUniqueId());
    }

    @Test
    public void xnor() {
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(0x1234, 0x5678, 0x12345678);
        TypePurposeUniqueId t2 = new TypePurposeUniqueId(0xFF00, 0xFF10, 0xFED0FF02);
        TypePurposeUniqueId result = t1.xnor(t2);
        Assert.assertNotSame(result, t1);
        Assert.assertNotSame(result, t2);
        Assert.assertEquals(~(0x1234 ^ 0xFF00) & 0xFFFF, result.getTypeId());
        Assert.assertEquals(~(0x5678 ^ 0xFF10) & 0xFFFF, result.getPurposeId());
        Assert.assertEquals(~(0x12345678 ^ 0xFED0FF02), result.getUniqueId());
    }

    @Test
    public void add() {
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(1, 2, 3);
        TypePurposeUniqueId t2 = new TypePurposeUniqueId(1, 2, 3);
        TypePurposeUniqueId result = t1.add(t2);
        Assert.assertNotSame(result, t1);
        Assert.assertNotSame(result, t2);
        Assert.assertEquals(1 + 1, result.getTypeId());
        Assert.assertEquals(2 + 2, result.getPurposeId());
        Assert.assertEquals(3 + 3, result.getUniqueId());
    }

    @Test
    public void addTypeWrap() {
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(0xFFFF, 0, 0);
        TypePurposeUniqueId t2 = new TypePurposeUniqueId(0x0002, 0, 0);
        TypePurposeUniqueId result = t1.add(t2);
        Assert.assertEquals(1, result.getTypeId());
    }

    @Test
    public void addPurposeWrap() {
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(0, 0xFFFF, 0);
        TypePurposeUniqueId t2 = new TypePurposeUniqueId(0, 0x0003, 0);
        TypePurposeUniqueId result = t1.add(t2);
        Assert.assertEquals(2, result.getPurposeId());
    }

    @Test
    public void addUniqueWrap() {
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(0, 0, 0xFFFFFFFF);
        TypePurposeUniqueId t2 = new TypePurposeUniqueId(0, 0, 0x00000004);
        TypePurposeUniqueId result = t1.add(t2);
        Assert.assertEquals(3, result.getUniqueId());
    }

    @Test
    public void subtract() {
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(5, 6, 7);
        TypePurposeUniqueId t2 = new TypePurposeUniqueId(1, 2, 3);
        TypePurposeUniqueId result = t1.subtract(t2);
        Assert.assertNotSame(result, t1);
        Assert.assertNotSame(result, t2);
        Assert.assertEquals(4, result.getTypeId());
        Assert.assertEquals(4, result.getPurposeId());
        Assert.assertEquals(4, result.getUniqueId());
    }

    @Test
    public void subtractTypeWrap() {
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(0x0000, 0, 0);
        TypePurposeUniqueId t2 = new TypePurposeUniqueId(0x0002, 0, 0);
        TypePurposeUniqueId result = t1.subtract(t2);
        Assert.assertEquals(0xFFFE, result.getTypeId());
    }

    @Test
         public void subtractPurposeWrap() {
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(0, 0x0000, 0);
        TypePurposeUniqueId t2 = new TypePurposeUniqueId(0, 0x0003, 0);
        TypePurposeUniqueId result = t1.subtract(t2);
        Assert.assertEquals(0xFFFD, result.getPurposeId());
    }

    @Test
    public void subtractUniqueWrap() {
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(0, 0, 0x00000000);
        TypePurposeUniqueId t2 = new TypePurposeUniqueId(0, 0, 0x00000004);
        TypePurposeUniqueId result = t1.subtract(t2);
        Assert.assertEquals(0xFFFFFFFC, result.getUniqueId());
    }

    @Test
    public void typeEquals() {
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(0x1234, 0, 0);
        Assert.assertTrue(t1.typeEquals(0x1234));
    }

    @Test
    public void purposeEquals() {
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(0, 0x5678, 0);
        Assert.assertTrue(t1.purposeEquals(0x5678));
    }

    @Test
    public void uniqueEuals() {
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(0, 0, 0xDEADBEEF);
        Assert.assertTrue(t1.uniqueEquals(0xDEADBEEF));
    }

    @Test
    public void compareToSame() {
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(0, 0, 0);
        TypePurposeUniqueId t2 = new TypePurposeUniqueId(0, 0, 0);
        Assert.assertEquals("Test is bad", t1, t2);
        Assert.assertEquals(0, t1.compareTo(t2));
        Assert.assertEquals(0, t2.compareTo(t1));
    }

    @Test
    public void compareToUnequalType() {
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(0, 0, 0);
        TypePurposeUniqueId t2 = new TypePurposeUniqueId(1, 0, 0);
        Assert.assertNotEquals("Test is bad", t1, t2);
        Assert.assertTrue(t1.compareTo(t2) < 0);
        Assert.assertTrue(t2.compareTo(t1) > 0);
    }

    @Test
    public void compareToUnequalPurpose() {
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(0, 0, 0);
        TypePurposeUniqueId t2 = new TypePurposeUniqueId(0, 1, 0);
        Assert.assertNotEquals("Test is bad", t1, t2);
        Assert.assertTrue(t1.compareTo(t2) < 0);
        Assert.assertTrue(t2.compareTo(t1) > 0);
    }

    @Test
    public void compareToUnequalTypePurpose() {
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(0, 1, 0);
        TypePurposeUniqueId t2 = new TypePurposeUniqueId(1, 0, 0);
        Assert.assertNotEquals("Test is bad", t1, t2);
        Assert.assertTrue(t1.compareTo(t2) < 0);
        Assert.assertTrue(t2.compareTo(t1) > 0);
    }

    @Test
    public void compareToUnequalUnique() {
        TypePurposeUniqueId t1 = new TypePurposeUniqueId(0, 0, 0);
        TypePurposeUniqueId t2 = new TypePurposeUniqueId(0, 0, 1);
        Assert.assertNotEquals("Test is bad", t1, t2);
        Assert.assertTrue(t1.compareTo(t2) < 0);
        Assert.assertTrue(t2.compareTo(t1) > 0);
    }

}
