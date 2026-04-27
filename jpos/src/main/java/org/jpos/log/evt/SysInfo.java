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

package org.jpos.log.evt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.jpos.log.AuditLogEvent;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Audit event capturing a snapshot of the running JVM and host: OS / JDK
 * identity, runtime resources (memory, threads, GC), classpath fingerprint,
 * the process configuration, and selected runtime tables (name registrar,
 * threads, deployed scripts).
 *
 * @param osName             value of {@code os.name}
 * @param osVersion          value of {@code os.version}
 * @param javaVersion        running JDK version
 * @param javaVendor         running JDK vendor
 * @param aes                maximum supported AES key size (e.g. {@code "256"})
 *                           or a diagnostic string when probing fails
 * @param host               host name reported by the OS
 * @param userName           value of {@code user.name}
 * @param cwd                current working directory
 * @param watchService       fully-qualified class name of the active
 *                           {@code WatchService}, or a marker when polling is in use
 * @param environment        active jPOS environment name
 * @param args               original command-line arguments, joined into a single string
 * @param encoding           default platform {@link Charset}
 * @param zoneInfo           default time-zone identifier
 * @param processName        JVM process name (typically {@code pid@host})
 * @param freeSpace          deploy-directory free space, formatted human-readable
 * @param usableSpace        deploy-directory usable space, formatted human-readable
 * @param version            jPOS version string
 * @param revision           jPOS source revision (branch / commit)
 * @param classPath          effective classpath, expanded against the launcher
 *                           manifest when applicable
 * @param classPathHash      hex SHA-1 of {@code classPath} for change-tracking
 * @param instance           per-process random instance identifier
 * @param uptime             elapsed time since Q2 startup
 * @param loadAverage        OS load average over the last minute
 * @param processors         number of available logical processors
 * @param drift              measured wall-clock drift in milliseconds
 * @param maxMemory          {@code Runtime.maxMemory()} in bytes
 * @param totalMemory        {@code Runtime.totalMemory()} in bytes
 * @param freeMemory         {@code Runtime.freeMemory()} in bytes
 * @param inUseMemory        {@code totalMemory - freeMemory} in bytes
 * @param gcTotalCnt         total GC collection count across all collectors
 * @param gcTotalTime        total GC time in milliseconds across all collectors
 * @param threadCount        live thread count
 * @param threadPeak         peak live thread count since JVM start
 * @param nameRegistrarEntries snapshot of the {@code NameRegistrar}, key/value pairs
 * @param threads            snapshot of running threads, key/value pairs
 * @param scripts            output of deployed monitoring scripts
 */
@JsonPropertyOrder({
  "osName", "osVersion", "javaVersion", "javaVendor", "aes", "host", "userName", "cwd", "watch-service", "environment",
  "args", "encoding", "zone-info", "processName", "freeSpace", "usableSpace", "version", "revision", "class-path", "class-path-hash", "instance",
  "uptime", "loadAverage", "processors", "drift", "maxMemory", "totalMemory", "freeMemory", "inUseMemory",
  "gcTotalCnt", "gcTotalTime", "threadCount", "threadPeak", "nameRegistrar"
})
public record SysInfo (
  @JsonProperty("os-name") String osName,
  @JsonProperty("os-version") String osVersion,
  @JsonProperty("java-version") String javaVersion,
  @JsonProperty("java-vendor") String javaVendor,
  @JsonProperty("AES") String aes,
  String host,
  @JsonProperty("user-name") String userName,
  @JsonProperty("cwd") String cwd,
  @JsonProperty("watch-service") String watchService,
  String environment,
  String args,
  Charset encoding,
  @JsonProperty("zone-info") String zoneInfo,
  @JsonProperty("process-name") String processName,
  @JsonProperty("free-space") String freeSpace,
  @JsonProperty("usable-space") String usableSpace,
  String version,
  String revision,
  @JsonProperty("class-path") String classPath,
  @JsonProperty("class-path-hash") String classPathHash,
  UUID instance,
  Duration uptime,
  @JsonProperty("load-average") double loadAverage,
  int processors,
  long drift,
  @JsonProperty("max-memory") long maxMemory,
  @JsonProperty("total-memory") long totalMemory,
  @JsonProperty("free-memory") long freeMemory,
  @JsonProperty("in-use-memory") long inUseMemory,
  @JsonProperty("gc-total-cnt") long gcTotalCnt,
  @JsonProperty("gc-total-time") long gcTotalTime,
  @JsonProperty("thread-count") int threadCount,
  @JsonProperty("thread-peak") int threadPeak,
  @JsonProperty("name-registrar") @JacksonXmlProperty(localName = "name-registrar") List<KV> nameRegistrarEntries,
  @JsonProperty("threads") @JacksonXmlProperty(localName = "threads") List<KV> threads,
  @JsonProperty("scripts") @JacksonXmlProperty(localName = "scripts") List<ProcessOutput> scripts
) implements AuditLogEvent { }
