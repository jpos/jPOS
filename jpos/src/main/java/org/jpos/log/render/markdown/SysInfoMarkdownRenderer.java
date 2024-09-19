package org.jpos.log.render.markdown;

import org.jpos.log.LogRenderer;
import org.jpos.log.evt.KV;
import org.jpos.log.evt.ProcessOutput;
import org.jpos.log.evt.SysInfo;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public final class SysInfoMarkdownRenderer implements LogRenderer<SysInfo> {

    @Override
    public void render(SysInfo sysinfo, PrintStream ps, String indent) {
        Map<String,Object> entries = extractEntriesToMap(sysinfo);
        int width = maxLength(entries);
        final String fmt = "| %-" + width + "s | %s |%n";
        ps.println ("### SystemMonitor");
        ps.print (row(fmt, "id", "value"));
        ps.print (row(fmt, "-".repeat(width), "-----"));
        entries.forEach((k, v) -> {
            switch (k) {
                case "nameRegistrarEntries":
                case "threads":
                case "scripts":
                    break;
                default:
                    ps.print(
                      row(fmt, k, v.toString())
                    );
            }
        });
        renderNameRegistrar(sysinfo.nameRegistrarEntries(), ps, fmt, width);
        renderThreads(sysinfo.threads(), ps, fmt, width);
        renderScripts(sysinfo.scripts(), ps);
    }
    public Class<?> clazz() {
        return SysInfo.class;
    }
    public Type type() {
        return Type.MARKDOWN;
    }

    private void renderNameRegistrar(List<KV> entries, PrintStream ps, String fmt, int width) {
        ps.println ("#### NameRegistrar");
        ps.print (row(fmt, "id", "component"));
        ps.print (row(fmt, "-".repeat(width), "---------"));
        entries.forEach(kv -> {
            ps.print(
              row(fmt, kv.key(), kv.value()));
        });
    }

    private void renderThreads(List<KV> entries, PrintStream ps, String fmt, int width) {
        ps.println ("#### Threads");
        ps.print (row(fmt, "thread", "info"));
        ps.print (row(fmt, "-".repeat(width), "----"));
        entries.forEach(kv -> {
            ps.print(
              row(fmt, kv.key(), kv.value()));
        });
    }

    private void renderScripts(List<ProcessOutput> entries, PrintStream ps) {
        entries.forEach (processOutput -> {
            ps.printf ("#### %s%n", processOutput.name());
            if (!processOutput.stdout().isEmpty()) {
                ps.println ("```");
                ps.println (processOutput.stdout());
                ps.println ("```");
            }
            if (processOutput.stderr() != null) {
                ps.println ("```");
                ps.println (processOutput.stderr());
                ps.println ("```");
            }
        });
    }


    private String row (String fmt, String c1, String c2) {
        return fmt.formatted(c1, c2);
    }

    private int maxLength(Map<String, Object> map) {
        return map.keySet().stream()
          .map(String::length)
          .max(Integer::compareTo).orElse(0);
    }

    private Map<String, Object> extractEntriesToMap(SysInfo record) {
        return Stream.of(record.getClass().getRecordComponents())
          .collect(Collectors.toMap(
            RecordComponent::getName,
            component -> {
                try {
                    Method accessor = component.getAccessor();
                    return accessor.invoke(record);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    return "%s:%s".formatted(e.getClass().getSimpleName(), e.getMessage());
                }
            }
          ));
    }
}
