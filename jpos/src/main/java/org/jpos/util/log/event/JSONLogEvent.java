package org.jpos.util.log.event;

import org.jdom2.Element;
import org.jpos.util.Loggeable;
import org.json.JSONObject;
import org.json.XML;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by erlangga on 2019-10-12.
 */
public class JSONLogEvent implements BaseLogEvent {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    @Override
    public String dumpHeader(PrintStream p, String indent, String realm, Instant dumpedAt, Instant createdAt, boolean noArmor) {
        if (noArmor) {
            p.println();
        } else {
            if (dumpedAt == null)
                dumpedAt = Instant.now();

            StringBuilder sb = new StringBuilder();
            sb.append("{"+"\"log\": {");
            sb.append("\"realm\" : "+ "\""+realm+"\",");
            sb.append("\"at\" : ");
            sb.append("\""+LocalDateTime.ofInstant(dumpedAt, ZoneId.systemDefault())+"\"");

            long elapsed = Duration.between(createdAt, dumpedAt).toMillis();
            if (elapsed > 0) {
                sb.append (",\"lifespan\" : \"");
                sb.append (elapsed);
                sb.append (" ms\"");
            }

            p.print(sb.toString());
        }
        return indent;
    }

    @Override
    public void dumpTrailer(PrintStream p, String indent, boolean noArmor) {
        if (!noArmor) {
            p.print ("}"+"}");
            p.println();
        }
    }

    @Override
    public void dump(PrintStream p, String outer, String realm, Instant dumpedAt, Instant createdAt, List<Object> payLoad, boolean noArmor, String tag) {
        try{
            dumpHeader (p, outer, realm,dumpedAt,createdAt, noArmor);
            if (payLoad.isEmpty()) {
                if (tag != null)
                    p.print (", \"" + tag + "\":{}");
            }else {
                if (tag != null) {
                    if (!tag.isEmpty()){
                        p.print (", \"" + tag +"\":");
                    }
                }
                boolean isOpenQuote = false;
                boolean isClosedBracket = false;
                boolean isExceptionOccured = false;

                synchronized (payLoad) {
                    StringBuilder stringBuilder = null;
                    stringBuilder = new StringBuilder();
                    for (Object o : payLoad) {
                        if (o instanceof Loggeable) {
                            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            try (PrintStream ps = new PrintStream(baos, true, "UTF-8")) {
                                ((Loggeable) o).dump(ps, "");
                                String json = convertXmlToJson(new String(baos.toByteArray(), StandardCharsets.UTF_8));
                                if(json!=null){
                                    p.print(json);
                                }
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        } else if (o instanceof SQLException) {
                            p.print("SQLException");
                        } else if (o instanceof Throwable) {
                            p.print("{ \"exception\" : { \"name\":\"" + ((Throwable) o).getMessage()+"\",");
                            p.print("\"stackTrace\":\"");
                            p.print(Arrays.toString(((Throwable)o).getStackTrace()));
                            p.print("\"}");

                            isClosedBracket = true;
                            isExceptionOccured = true;
                        } else if (o instanceof Object[]) {
                            p.print("Object[]");
                        } else if (o instanceof Element) {
                            p.print("Element");
                        } else if (o != null) {
                            if(stringBuilder.length()==0){
                                isOpenQuote = true;
                                stringBuilder.append("\"");
                            }
                            stringBuilder.append(o.toString());
                        }
                    }
                    if(isExceptionOccured && stringBuilder.length()>0){
                        p.print(",\"text\":");
                    }
                    p.print(stringBuilder.toString());
                    if(!payLoad.isEmpty() && isOpenQuote){
                        p.print("\"");
                    }
                    if(isClosedBracket){
                        p.print("}");
                    }
                }
            }
        } finally {
            dumpTrailer(p,outer,noArmor);
        }
    }

    @Override
    public String addMessage(String tagname, String message) {
        String json = "{" +
                "\"" + tagname + "\":" +
                "\"" + message + "\"" +
                "}";
        return json;
    }

    private String convertXmlToJson(String xmlString) {
        JSONObject jsonObject = XML.toJSONObject(xmlString);
        Iterator<String> keys = jsonObject.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            if(jsonObject.get(key) instanceof String){
                String str = jsonObject.getString(key);
                if(str.isEmpty()){
                    return null;
                }
            }
        }

        String json = jsonObject.toString();
        return json;
    }
}
