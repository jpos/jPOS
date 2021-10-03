/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2010 Alejandro P. Revilla
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

package org.jpos.rc;

import org.jpos.util.Caller;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResultTest {
    @Test
    public void hasFailureTest() {
        Result result = new Result();
        result.fail(CMF.HOST_UNREACHABLE, Caller.info(), "'%s' does not respond", "mymux");
        assertTrue(result.hasFailure(CMF.HOST_UNREACHABLE));
        assertTrue(result.hasIRC(CMF.HOST_UNREACHABLE));
        assertFalse(result.hasFailure(CMF.MISSING_FIELD));
        assertFalse(result.hasWarning(CMF.HOST_UNREACHABLE));
        assertFalse(result.hasInfo(CMF.HOST_UNREACHABLE));
    }
}
