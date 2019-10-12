package org.jpos.util;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOMsg;

import java.util.Map;
import java.util.TreeMap;

public class ISOLogUtil {
    public static final String MTI = "MTI";

    public static Map<String, Object> getMap(ISOMsg isoMsg) throws ISOException
    {
        Map<String, Object> mapz = new TreeMap<>();
        Map<Integer, ISOField> fields = isoMsg.getChildren();

        mapz.put(MTI, isoMsg.getMTI());
        for (Map.Entry<Integer, ISOField> entry : fields.entrySet())
        {
            if (entry.getKey() >= 1)
            {
                mapz.put("de" + entry.getKey(), isoMsg.getString(entry.getKey()));
            }

        }

        return mapz;
    }

}
