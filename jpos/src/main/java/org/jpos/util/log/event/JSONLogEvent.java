package org.jpos.util.log.event;

import org.jpos.util.Loggeable;
import org.json.XML;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by erlangga on 2019-10-12.
 */
public class JSONLogEvent implements BaseLogEvent {

    private static final String XML_TAG_PATTERN = "(?s).*(<(\\w+)[^>]*>.*</\\2>|<(\\w+)[^>]*/>).*";
    private static final String STACK_TRACE_TAG_PATTERN = "(?m)^.*?Exception.*(?:\\R+^\\s*at .*)+";
    private static final Pattern STACK_TRACE_REGEX = Pattern.compile(STACK_TRACE_TAG_PATTERN, Pattern.MULTILINE);

    @Override
    public String dumpHeader(PrintStream p, String indent, String realm, Instant dumpedAt, Instant createdAt, boolean noArmor) {
        if (noArmor) {
            p.println();
        } else {
            if (dumpedAt == null)
                dumpedAt = Instant.now();

            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            indent+=indent(indent,'+');
            sb.append(indent+"\"log\": {\n");
            indent = indent(indent,'+');
            sb.append(indent+"\"realm\" : "+ "\""+realm+"\",\n");
            sb.append(indent+"\"at\" : ");
            sb.append("\""+LocalDateTime.ofInstant(dumpedAt, ZoneId.systemDefault())+"\"");

            long elapsed = Duration.between(createdAt, dumpedAt).toMillis();
            if (elapsed > 0) {
                sb.append (",\n"+indent+"\"lifespan\" : \"");
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
            p.print (indent+"\n}\n");
        }
    }

    @Override
    public void dump(PrintStream p, String outer, String realm, Instant dumpedAt, Instant createdAt, List<Object> payLoad, boolean noArmor, String tag) {
        String indent = "";
        try{
            indent = dumpHeader (p, outer, realm,dumpedAt,createdAt, noArmor);
            if (payLoad.isEmpty()) {
                if (tag != null)
                    p.print (", \"" + tag + "\": {}");
            }else {
                if (tag != null) {
                    if (!tag.isEmpty()){
                        p.print (", \n"+indent+"\"" + tag +"\": ");
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
                                stringBuilder = new StringBuilder(new String(baos.toByteArray(), StandardCharsets.UTF_8));
                            }
                        } else if (o instanceof SQLException) {
                            SQLException e = (SQLException) o;
                            p.print("{\n");
                            indent = indent(indent,'+');
                            p.print(indent+"\"exception\" : { \n");

                            indent = indent(indent,'+');
                            p.print(indent+"\"sqlexception\":\"" + e.getMessage()+"\",\n");
                            p.print(indent+"\"sqlstate\":\""+ e.getSQLState()+"\",\n");
                            p.print(indent+"\"vendorerror\":\""+ e.getErrorCode()+"\",\n");
                            p.print(indent+"\"stacktrace\": [");
                            p.print(extrapolateStackTrace((Exception) o,indent)+"]\n");
                            p.print(indent+"}");

                            isClosedBracket = true;
                            isExceptionOccured = true;
                        } else if (o instanceof Throwable) {
                            p.print("{\n");

                            indent = indent(indent,'+');
                            p.print(indent+"\"exception\" : {\n");

                            indent = indent(indent,'+');
                            p.print(indent+"\"name\":\"" + ((Throwable) o).getMessage()+"\",\n");
                            p.print(indent+"\"stacktrace\": [");
                            p.print(extrapolateStackTrace((Exception) o,indent)+"]\n");
                            p.print(indent+"}");

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
                        p.print(",\n"+indent+"\"text\":");
                    }
                    if(stringBuilder!=null && stringBuilder.length()>0){
                        String data = stringBuilder.toString();
                        if(data.matches(XML_TAG_PATTERN)){
                            p.print(convertXmlToJson(data));
                        }else if(STACK_TRACE_REGEX.matcher(data).find()){
                            p.print(data.replaceAll("[\r\n]+", " ")+"\"");
                        }else {
                            p.print(data+"\"");
                        }
                    }
                    if(isClosedBracket){
                        indent = indent(indent,'-');
                        p.print("\n"+indent+"}");
                    }
                }
            }
        } finally {
            p.print("\n"+indent(indent,'-')+"}");
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
        return XML.toJSONObject(xmlString).toString(4);
    }
    
    private String extrapolateStackTrace(Exception ex, String indent) {
        Throwable e = ex;
        StringBuilder stringBuilder = new StringBuilder("\""+e.toString() + "\",\n"+indent);
        for (StackTraceElement e1 : e.getStackTrace()) {
            stringBuilder.append("   "+"\"at " + e1.toString() + "\",\n"+indent);
        }
        while (e.getCause() != null) {
            e = e.getCause();
            stringBuilder.append("\"Cause by: " + e.toString() + "\",\n"+indent);
            for (StackTraceElement e1 : e.getStackTrace()) {
                stringBuilder.append("   "+"\"at " + e1.toString() + "\",\n"+indent);
            }
        }

        String trace = stringBuilder.toString().trim();
        return trace.substring(0,trace.length()-1);
    }

    private String indent(String indent, char symbol) {
        if(symbol=='+'){
            indent = indent+ "  ";
        }else if(symbol=='-'){
            indent = indent.substring(0,2);
        }
        return indent;
    }
}
