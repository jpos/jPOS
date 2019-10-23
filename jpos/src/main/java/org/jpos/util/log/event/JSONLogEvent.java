package org.jpos.util.log.event;

import org.jpos.util.Loggeable;
import org.json.JSONObject;
import org.json.XML;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by erlangga on 2019-10-12.
 */
public class JSONLogEvent implements BaseLogEvent {

    private static final String XML_TAG_PATTERN = "(?s).*(<(\\w+)[^>]*>.*</\\2>|<(\\w+)[^>]*/>).*";
    private static final String STACK_TRACE_TAG_PATTERN = "(^\\d+\\) .+)|(^.+Exception: .+)|(^\\s+at .+)|(^\\s+... \\d+ more)|(^\\s*Caused by:.+)";

    private static final Pattern STACK_TRACE_REGEX = Pattern.compile(STACK_TRACE_TAG_PATTERN, Pattern.MULTILINE);

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

                boolean isClosedBracket = false;
                boolean isExceptionOccured = false;

                synchronized (payLoad) {
                    StringBuilder stringBuilder = null;
                    for (Object o : payLoad) {
                        if (o instanceof Loggeable) {
                            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            try (PrintStream ps = new PrintStream(baos, true)) {
                                ((Loggeable) o).dump(ps, "");
                                String json = convertXmlToJson(new String(baos.toByteArray(), StandardCharsets.UTF_8));
                                if(json!=null){
                                    p.print(json);
                                }
                            }
                        } else if (o instanceof SQLException) {
                            SQLException e = (SQLException) o;
                            p.print("{ \"exception\" : { \"SQLException\":\"" + e.getMessage()+"\",");
                            p.print("\"SQLState\":\""+ e.getSQLState()+"\",");
                            p.print("\"VendorError\":\""+ e.getErrorCode()+"\",");
                            p.print("\"stackTrace\":");
                            p.print(getCurrentStackTraceString(((Throwable)o).getStackTrace()));
                            p.print("}");

                            isClosedBracket = true;
                            isExceptionOccured = true;
                        } else if (o instanceof Throwable) {
                            p.print("{ \"exception\" : { \"name\":\"" + ((Throwable) o).getMessage()+"\",");
                            p.print("\"stackTrace\":");
                            p.print(getCurrentStackTraceString(((Throwable)o).getStackTrace()));
                            p.print("}");

                            isClosedBracket = true;
                            isExceptionOccured = true;
                        } else if (o instanceof Object[]) {
                            Object[] oa = (Object[]) o;
                            p.print("[");
                            for (int j = 0; j < oa.length; j++) {
                                if (j > 0)
                                    p.print(",");
                                p.print("\""+oa[j].toString()+"\"");
                            }
                            p.print("]");
                        } else if (o != null) {
                            String data = o.toString();

                            if(stringBuilder==null){
                                stringBuilder = new StringBuilder();
                                stringBuilder.append("\"");
                            }

                            stringBuilder.append(data);
                        }
                    }
                    if(isExceptionOccured && stringBuilder!=null && stringBuilder.length()>0){
                        p.print(",\"text\":");
                    }
                    if(stringBuilder!=null && stringBuilder.length()>0){
                        String data = stringBuilder.toString();
                        if(data.matches(XML_TAG_PATTERN)){
                            p.print(convertXmlToJson(stringBuilder.toString()));
                        }else if(STACK_TRACE_REGEX.matcher(data).find()){
                            p.print(data.replaceAll("[\r\n]+", " ")+"\"");
                        }else {
                            p.print(data+"\"");
                        }
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

    private String getCurrentStackTraceString(StackTraceElement[] stackTrace){
        return Stream.of(stackTrace).map((a) -> "\"" + a.toString() + "\"").collect(Collectors.joining(",\n","[", "]"));
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
