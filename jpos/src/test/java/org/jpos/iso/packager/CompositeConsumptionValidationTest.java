/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.iso.packager;

import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOFieldPackager;
import org.jpos.iso.ISOBitMapPackager;
import org.jpos.iso.ISOBasePackager;
import org.jpos.iso.ISOMsg;
import org.junit.jupiter.api.Test;

import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CompositeConsumptionValidationTest {
    @Test
    public void testEuroAllowsAbsentZeroLengthFieldZeroBeforeTaggedFields() throws ISOException {
        EuroSubFieldPackager packager = new EuroSubFieldPackager();
        packager.setFieldPackager(new ISOFieldPackager[] {
            new ConsumptionPackager(0, null),
            new ConsumptionPackager(2, "value")
        });
        ISOMsg m = new ISOMsg();

        assertEquals(2, packager.unpack(m, new byte[] { '0', '1' }));
        assertEquals("value", m.getString(1));
    }

    @Test
    public void testEuroRejectsInvalidTaggedFieldConsumption() {
        for (int consumed : new int[] { -1, 0, 3 }) {
            EuroSubFieldPackager packager = new EuroSubFieldPackager();
            packager.setFieldPackager(new ISOFieldPackager[] {
                null, new ConsumptionPackager(consumed, null)
            });

            ISOException ex = assertThrows(
                ISOException.class,
                () -> packager.unpack(new ISOMsg(), new byte[] { '0', '1' })
            );
            assertEquals(
                "Invalid consumed length " + consumed +
                    " for sub-field 1 with 2 bytes remaining",
                ex.getMessage()
            );
        }
    }

    @Test
    public void testEuroOnlyAllowsZeroConsumptionForAbsentFieldZero() {
        for (int consumed : new int[] { -1, 2 }) {
            EuroSubFieldPackager packager = new EuroSubFieldPackager();
            packager.setFieldPackager(new ISOFieldPackager[] {
                new ConsumptionPackager(consumed, null)
            });

            ISOException ex = assertThrows(
                ISOException.class,
                () -> packager.unpack(new ISOMsg(), new byte[1])
            );
            assertEquals(
                "Invalid consumed length " + consumed +
                    " for sub-field 0 with 1 bytes remaining",
                ex.getMessage()
            );
        }

        EuroSubFieldPackager packager = new EuroSubFieldPackager();
        packager.setFieldPackager(new ISOFieldPackager[] {
            new ConsumptionPackager(0, "unexpected")
        });
        ISOException ex = assertThrows(
            ISOException.class,
            () -> packager.unpack(new ISOMsg(), new byte[1])
        );
        assertEquals(
            "Invalid consumed length 0 for sub-field 0 with 1 bytes remaining",
            ex.getMessage()
        );
    }

    @Test
    public void testCTCSubFieldRejectsInvalidConsumption() {
        for (int consumed : new int[] { -1, 0, 2 }) {
            CTCSubFieldPackager packager = new CTCSubFieldPackager();
            packager.setFieldPackager(new ISOFieldPackager[] {
                new ConsumptionPackager(consumed, null)
            });

            ISOException ex = assertThrows(
                ISOException.class,
                () -> packager.unpack(new ISOMsg(), new byte[1])
            );
            assertEquals(
                "Invalid consumed length " + consumed +
                    " for field 0 with 1 bytes remaining",
                ex.getMessage()
            );
        }
    }

    @Test
    public void testCTCSubElementRejectsInvalidConsumption() {
        for (int consumed : new int[] { -1, 0, 2 }) {
            CTCSubElementPackager packager = new CTCSubElementPackager();
            packager.setFieldPackager(new ISOFieldPackager[] {
                new ConsumptionPackager(consumed, null)
            });

            ISOException ex = assertThrows(
                ISOException.class,
                () -> packager.unpack(new ISOMsg(), new byte[1])
            );
            assertEquals(
                "Invalid consumed length " + consumed +
                    " for field 0 with 1 bytes remaining",
                ex.getMessage()
            );
        }
    }

    @Test
    public void testCTCSubFieldRejectsTrailingInputAfterLastConfiguredField() {
        CTCSubFieldPackager packager = new CTCSubFieldPackager();
        packager.setFieldPackager(new ISOFieldPackager[] {
            new ConsumptionPackager(1, null)
        });

        ISOException ex = assertThrows(
            ISOException.class,
            () -> packager.unpack(new ISOMsg(), new byte[2])
        );
        assertEquals("No field packager for field 1 with input remaining", ex.getMessage());
    }

    @Test
    public void testCTCSubElementRejectsTrailingInputAfterLastConfiguredField() {
        CTCSubElementPackager packager = new CTCSubElementPackager();
        packager.setFieldPackager(new ISOFieldPackager[] {
            new ConsumptionPackager(1, null)
        });

        ISOException ex = assertThrows(
            ISOException.class,
            () -> packager.unpack(new ISOMsg(), new byte[2])
        );
        assertEquals("No field packager for field 1 with input remaining", ex.getMessage());
    }

    @Test
    public void testFieldBoundedPackagersRejectInvalidConsumption() throws ISOException {
        for (int consumed : new int[] { -1, 2 }) {
            GenericSubFieldPackager generic = new GenericSubFieldPackager() {
                @Override
                protected boolean emitBitMap() {
                    return false;
                }
            };
            generic.setFieldPackager(new ISOFieldPackager[] {
                new ConsumptionPackager(consumed, null)
            });
            assertEquals(
                "Invalid consumed length " + consumed + " for field 0",
                assertThrows(ISOException.class, () -> generic.unpack(new ISOMsg(), new byte[1])).getMessage()
            );

            Base1SubFieldPackager base1 = new Base1SubFieldPackager();
            base1.setFieldPackager(new ISOFieldPackager[] {
                new ConsumptionPackager(consumed, null)
            });
            assertEquals(
                "Invalid consumed length " + consumed + " for field 0",
                assertThrows(ISOException.class, () -> base1.unpack(new ISOMsg(), new byte[1])).getMessage()
            );

            ISOBasePackager standard = new ISOBasePackager() { };
            standard.setFieldPackager(new ISOFieldPackager[] {
                new ConsumptionPackager(consumed, null), null
            });
            assertEquals(
                "Invalid consumed length " + consumed + " for field 0",
                assertThrows(ISOException.class, () -> standard.unpack(new ISOMsg(), new byte[1])).getMessage()
            );

            ISOBasePackager standardField = new ISOBasePackager() { };
            standardField.setFieldPackager(new ISOFieldPackager[] {
                null, new ConsumptionPackager(consumed, null)
            });
            assertEquals(
                "Invalid consumed length " + consumed +
                    " for field 1 unpacking field=1, consumed=0",
                assertThrows(
                    ISOException.class,
                    () -> standardField.unpack(new ISOMsg(), new byte[1])
                ).getMessage()
            );
        }
    }

    @Test
    public void testFieldBoundedPackagersContinueToAllowZeroConsumption() throws ISOException {
        GenericSubFieldPackager generic = new GenericSubFieldPackager() {
            @Override
            protected boolean emitBitMap() {
                return false;
            }
        };
        generic.setFieldPackager(new ISOFieldPackager[] {
            new ConsumptionPackager(0, null)
        });
        assertEquals(0, generic.unpack(new ISOMsg(), new byte[1]));

        Base1SubFieldPackager base1 = new Base1SubFieldPackager();
        base1.setFieldPackager(new ISOFieldPackager[] {
            new ConsumptionPackager(0, null)
        });
        assertEquals(0, base1.unpack(new ISOMsg(), new byte[1]));

        ISOBasePackager standard = new ISOBasePackager() { };
        standard.setFieldPackager(new ISOFieldPackager[] {
            new ConsumptionPackager(0, null), null
        });
        assertEquals(0, standard.unpack(new ISOMsg(), new byte[1]));
    }

    @Test
    public void testBitmapPackagersRejectInvalidConsumption() throws ISOException {
        for (int consumed : new int[] { -1, 2 }) {
            GenericSubFieldPackager generic = new GenericSubFieldPackager();
            generic.setFieldPackager(new ISOFieldPackager[] {
                null, new BitmapConsumptionPackager(consumed, new BitSet())
            });
            assertEquals(
                "Invalid consumed length " + consumed + " for bitmap",
                assertThrows(ISOException.class, () -> generic.unpack(new ISOMsg(), new byte[1])).getMessage()
            );

            Base1SubFieldPackager base1 = new Base1SubFieldPackager();
            base1.setFieldPackager(new ISOFieldPackager[] {
                new BitmapConsumptionPackager(consumed, new BitSet())
            });
            assertEquals(
                "Invalid consumed length " + consumed + " for bitmap",
                assertThrows(ISOException.class, () -> base1.unpack(new ISOMsg(), new byte[1])).getMessage()
            );

            ISOBasePackager standard = new ISOBasePackager() { };
            standard.setFieldPackager(new ISOFieldPackager[] {
                null, new BitmapConsumptionPackager(consumed, new BitSet())
            });
            assertEquals(
                "Invalid consumed length " + consumed + " for bitmap",
                assertThrows(ISOException.class, () -> standard.unpack(new ISOMsg(), new byte[1])).getMessage()
            );
        }
    }

    @Test
    public void testBase1RejectsBitmapFieldBeyondConfiguredPackagers() throws ISOException {
        BitSet bitmap = new BitSet();
        bitmap.set(3);
        Base1SubFieldPackager packager = new Base1SubFieldPackager();
        packager.setFieldPackager(new ISOFieldPackager[] {
            new BitmapConsumptionPackager(1, bitmap)
        });

        ISOException ex = assertThrows(
            ISOException.class,
            () -> packager.unpack(new ISOMsg(), new byte[2])
        );
        assertEquals("field packager '3' is null", ex.getMessage());
    }

    private static final class ConsumptionPackager extends ISOFieldPackager {
        private final int consumed;
        private final Object value;

        private ConsumptionPackager(int consumed, Object value) {
            this.consumed = consumed;
            this.value = value;
        }

        @Override
        public int getMaxPackedLength() {
            return 0;
        }

        @Override
        public byte[] pack(ISOComponent c) {
            return new byte[0];
        }

        @Override
        public int unpack(ISOComponent c, byte[] b, int offset) throws ISOException {
            if (value != null)
                c.setValue(value);
            return consumed;
        }
    }

    private static final class BitmapConsumptionPackager extends ISOBitMapPackager {
        private final int consumed;
        private final BitSet value;

        private BitmapConsumptionPackager(int consumed, BitSet value) {
            this.consumed = consumed;
            this.value = value;
        }

        @Override
        public int getMaxPackedLength() {
            return 0;
        }

        @Override
        public byte[] pack(ISOComponent c) {
            return new byte[0];
        }

        @Override
        public int unpack(ISOComponent c, byte[] b, int offset) throws ISOException {
            c.setValue(value);
            return consumed;
        }
    }
}
