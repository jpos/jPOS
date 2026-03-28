package org.jpos.metrics.iso;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.jpos.iso.ISOMsg;
import org.jpos.metrics.MeterInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Interface for ISO message metrics tracking. */
public interface ISOMsgMetrics {
    /** Default channel metric name. */
    String DEFAULT_CHANNEL_METRIC_NAME = MeterInfo.ISOMSG_IN.id();

    /** Environment variable name for channel tags configuration. */
    String ENV_CHANNEL_TAGS =   "metrics.channel.tags";
    /** Default tags for channel metrics. */
    String DEFAULT_TAGS =       "name, type, direction";

    /** Environment variable name for channel fields configuration. */
    String ENV_CHANNEL_FIELDS =     "metrics.channel.fields";
    /** Default channel fields for metrics. */
        String DEFAULT_CHANNEL_FIELDS = "mti";

    /** Implemented by components that expose an {@link ISOMsgMetrics} instance. */
    interface Source{
            /**
         * Sets the ISOMsgMetrics instance.
         * @param metrics the ISOMsgMetrics to attach
         */
        void setISOMsgMetrics(ISOMsgMetrics metrics);
        /**
         * Returns the attached ISOMsgMetrics.
         * @return the ISOMsgMetrics, or null
         */
        ISOMsgMetrics getISOMsgMetrics();
    }

    /** Sets the metric name.
     * @param metricName the metric name
     */
    void setMetricName(String metricName);
    /** Returns the metric name.
     * @return the metric name
     */
    String getMetricName();

    /** Returns the metric description.
     * @return the metric description
     */
    String getMetricDescription();
    /** Sets the metric description.
     * @param metricDescription the metric description
     */
    void setMetricDescription(String metricDescription);

    /** Adds metric tags to the given Tags object.
     * @param tags the Tags to add to
     * @return the updated Tags
     */
    Tags addTags(Tags tags);
    /** Adds metric tags from an array of strings.
     * @param tags the tags to add
     * @return the updated Tags
     */
        default Tags addTags(String ...tags) { return addTags(Tags.of(tags)); }
    /** Returns all metric tags.
     * @return the metric tags
     */
    default Tags getTags() { return addTags(Tags.empty()); }

    /**
     * Records an {@link ISOMsg} in the meter registry.<br>
     * The metric name and tags will be taken strictly from this object's
     * configuration. <br>
     * If this object hasn't been successfully registered, it throws an
     * {@link IllegalStateException}.
     *
     * @param m the {@link ISOMsg} to record.
     * @throws IllegalStateException when this object hasn't been registered
     */
    void recordMessage(ISOMsg m) throws IllegalStateException;

    /**
     * Records an {@link ISOMsg} in the meter registry.<br>
     * Similar to {@link #recordMessage(ISOMsg)} but using the metric name, description and maybe some
     * tags taken from the {@link MeterInfo} argument.
     * <p>
     * If the metric for that combination of {@link MeterInfo} values and local values fails to register
     * in the global {@link MeterRegistry} (or any underlying one like the Prometheus registry), the method
     * may throw an {@link IllegalStateException}.  This also happens if this object hasn't been successfully
     * registered by The metric name and tags will be taken from
     * what has been configured. If this object hasn't been successfully registered, it throws an
     * {@link IllegalStateException}.
     *
     * @param m the {@link ISOMsg} to record.
     * @throws IllegalStateException when this object hasn't been registered
     * @param m          the ISO message
     * @param meterInfo  the meter info to record
     * @throws IllegalStateException if recording fails
     */
    void recordMessage(ISOMsg m, MeterInfo meterInfo) throws IllegalStateException;


    /**
     * Register this object to work with a given {@link MeterRegistry}.<br>
     *
     * This method may serve more than one purpose in the object's lifecycle:
     * <ul>
     *  <li>Assign a {@link MeterRegistry} to be used for the created meters.
     *  (The registry can be obtained by calling {@link #getRegistry()})</li>
     *  <li>Before this object has been registered, it can be configured by setting tags, etc.,
     *      but attempting to record a message (e.g. through  {@link #recordMessage(ISOMsg)})
     *      will throw an {@link IllegalStateException}.</li>
     *  <li>After it has been registered, it's ready to record messages.
     *      However, it <b>can't be configured</b> any longer, or it will throw an {@link IllegalStateException}.
     *      The object's configuration can be considered "frozen".</li>
     *  <li>In some (future) implementation, it may make use of the {@link #getMetricSignature()} to
     *      do some caching to ensure that every metric name has only one set of tag keys, thus avoiding
     *      metrics name+keyset collision in {@code PrometheusMeterRegistry}.</li>
     * </ul>
     *
     *  The {@link #unregister()} method should be called when done using this object.
     *
     * @param registry the meter registry to register with
     * @return true if successful, false if there was an error
     */
    boolean register(MeterRegistry registry);

    /**
     * It calls {@link #removeMeters()} and clears its internal reference to its {@link MeterRegistry}.<br>
     *
     * It will also "unfreeze" the object, making it available for reconfiguration.
     */
    void unregister();

/** Returns the meter registry.
     * @return the MeterRegistry
     */
        MeterRegistry getRegistry();

    void removeMeters();


    /**
     * A unique meter signature, concatenating the meter name, vertical pipe, comma-separated sorted list of all tag keys
     * that this object produces.<br>
     * Default implementation uses the values from {@link #getMetricName()} and {@link #getTags()}.
     * A concrete implementation must make sure of gathering all the appropriate tag keys from internal config state
     * which may include more than what's returned by {@link #getTags()}.
     * @return  The unique metric signature.
     */
    default String getMetricSignature() {
        List<String> keys = new ArrayList<>();
        getTags().forEach(t -> keys.add(t.getKey()));
        return getMetricName()+"|"+
                keys.stream().sorted().collect(Collectors.joining(","));
    }
}
