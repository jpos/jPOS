/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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

package org.jpos.q2.cli;

import org.jpos.q2.CLICommand;
import org.jpos.q2.CLIContext;

import java.io.IOException;
import java.io.InputStream;

public class COPYRIGHT implements CLICommand
{
    public void exec(CLIContext ctx, String[] args) throws IOException
    {
        display(ctx, MAN.class.getResourceAsStream("/COPYRIGHT"));
        ctx.println("");
    }

    private void display(CLIContext ctx, InputStream is) throws IOException
    {
        if (is != null)
        {
            while (is.available() > 0)
            {
                byte[] b = new byte[is.available()];
                is.read(b);
                ctx.print(new String(b, "ISO8859_1"));
            }
        }
    }
}

