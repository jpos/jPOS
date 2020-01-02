/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
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

package org.jpos.q2.ssh;

import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.jpos.q2.Q2;
import org.jpos.util.Log;

import java.security.PublicKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

public abstract class AbstractPKA  implements PublickeyAuthenticator
{
    public boolean authenticate(String username, PublicKey key, ServerSession session)
    {
        boolean res = false;
        List<PublicKey> authorizedKeys=null;
        try
        {
            authorizedKeys = parseAuthorizedKeys();
        }
        catch (Exception e)
        {
            Log.getLog(Q2.LOGGER_NAME, "sshd").error(e);
        }

        if(authorizedKeys!=null)
        {
            for (PublicKey authorizedKey : authorizedKeys)
            {
                res = compare(authorizedKey, key);
                if (res)
                {
                    break;
                }
            }
        }
        return res && username!=null && username.equals(getUsername());
    }

    protected abstract List<PublicKey> parseAuthorizedKeys() throws Exception;
    protected abstract String getUsername();

    private boolean compare(Object o1, Object o2)
    {
        if (o1 instanceof RSAPublicKey)
        {
            return compare((RSAPublicKey) o1, o2);
        }
        else if (o1 instanceof DSAPublicKey)
        {
            return compare((DSAPublicKey) o1, o2);
        }
        return false;
    }

    private boolean compare(RSAPublicKey k, Object o)
    {
        if (o == k)
        {
            return true;
        }

        if (!(o instanceof RSAPublicKey))
        {
            return false;
        }

        RSAPublicKey other = (RSAPublicKey) o;

        return k.getModulus().equals(other.getModulus())
               && k.getPublicExponent().equals(other.getPublicExponent());
    }

    private boolean compare(DSAPublicKey k, Object o)
    {
        if (o == k)
        {
            return true;
        }

        if (!(o instanceof DSAPublicKey))
        {
            return false;
        }

        DSAPublicKey other = (DSAPublicKey) o;

        return k.getY().equals(other.getY())
               && k.getParams().getG().equals(other.getParams().getG())
               && k.getParams().getP().equals(other.getParams().getP())
               && k.getParams().getQ().equals(other.getParams().getQ());
    }
}
