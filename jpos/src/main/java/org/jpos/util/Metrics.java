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

package org.jpos.util;

import org.HdrHistogram.Histogram;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Metrics implements Loggeable {
    private Histogram template;
    private Map<String,Histogram> metrics = new ConcurrentHashMap<>();
    private double conversion = 1;

    public Metrics(Histogram template) {
        super();
        this.template = template;
        if (template != null && template.getStartTimeStamp() == Long.MAX_VALUE) {
            template.setStartTimeStamp(System.currentTimeMillis());
        }
    }

    public Map<String, Histogram> metrics() {
        return metrics.entrySet()
          .stream()
          .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().copy()));
    }

    public Map<String, Histogram> metrics(String prefix) {
        return metrics.entrySet()
          .stream()
          .filter(e -> e.getKey().startsWith(prefix))
          .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().copy()));
    }

    public void record(String name, long elapsed) {
        Histogram h = getHistogram(name);
        long l = Math.min(elapsed, h.getHighestTrackableValue());
        if (l > 0)
            h.recordValue(l);
    }

    private Histogram getHistogram(String p) {
        Histogram h = metrics.get(p);
        if (h == null) {
            Histogram hist = new Histogram(template);
            hist.setTag(p);
            metrics.putIfAbsent(p, hist);
            h = metrics.get(p);
        }
        return h;
    }

    public void dump(PrintStream ps, String indent) {
        metrics.entrySet()
          .stream()
          .sorted(Map.Entry.comparingByKey())
          .forEach(e -> dumpPercentiles(ps, indent, e.getKey(), e.getValue().copy()));
    }

    private void dumpPercentiles (PrintStream ps, String indent, String key, Histogram h) {
          ps.printf("%s%s min=%.7f, max=%.7f, mean=%.7f stddev=%.7f P50=%.7f, P90=%.7f, P99=%.7f, P99.9=%.7f, P99.99=%.7f tot=%d size=%d%n",
          indent,
          key,
          h.getMinValue()/conversion,
          h.getMaxValue()/conversion,
          h.getMean()/conversion,
          h.getStdDeviation()/conversion,
          h.getValueAtPercentile(50.0)/conversion,                    
          h.getValueAtPercentile(90.0)/conversion,
          h.getValueAtPercentile(99.0)/conversion,
          h.getValueAtPercentile(99.9)/conversion,
          h.getValueAtPercentile(99.99)/conversion,
          h.getTotalCount(),
          h.getEstimatedFootprintInBytes()
        );
    }

    private void dumpHistogram(File dir, String key, Histogram h) {
        try (FileOutputStream fos = new FileOutputStream(new File(dir, key + ".hgrm"))) {
            h.outputPercentileDistribution(new PrintStream(fos), 1.0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void dumpHistograms(File dir, String prefix) {
        metrics.entrySet()
          .stream()
          .sorted(Map.Entry.comparingByKey())
          .forEach(e -> dumpHistogram(dir, prefix + e.getKey(), e.getValue().copy()));
    }

    /**
     * @param conversion
     *            This is used to divide the percentile values while dumping. 
     *            If you are using nano seconds to record and want to display the numbers in millis then conversion can be set to 1000000.
     *            By default conversion is set to 1.
     */
    public void setConversion(double conversion) {
        this.conversion = conversion;
    }
}
