/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2024 jPOS Software SRL
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

@JsonPropertyOrder({
  "osName", "osVersion", "javaVersion", "javaVendor", "aes", "host", "userName", "cwd", "watch-service", "environment",
  "args", "encoding", "zone-info", "processName", "freeSpace", "usableSpace", "version", "revision", "instance",
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
