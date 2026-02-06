package org.jpos.metrics.iso;

import io.micrometer.core.instrument.*;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.Environment;
import org.jpos.iso.ISOMsg;
import org.jpos.metrics.MeterFactory;
import org.jpos.metrics.MeterInfo;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ISOMsgCounter implements ISOMsgMetrics, LogSource, Configurable {
    private final Map<String, Function<ISOMsg,String>> aliases= Map.of(
            "mti",    (m) -> m.getString(0),
            "rc",     (m) -> m.getString(39),
            "scheme", (m) -> m.getString("113.66"),
            "isemv",  (m) -> Boolean.toString(m.hasField(55)),
            "ttype",  this::getTtype,
            "itc",    this::getITC
    );

    private MeterRegistry registry;

    // Store my meters for safe removal later on. Meters are uniquely identified by their Meter.Id.
    private final Set<Meter> meters = ConcurrentHashMap.newKeySet();
    private boolean frozen = false;

    // custom properties
    private String metricName;
    private String metricDescription;
    private Tags tags = Tags.empty();
    private final Map<String,String> fieldSet = new HashMap<>();

    private Logger logger;
    private String realm;

    public ISOMsgCounter() throws ConfigurationException {
        // Configure initial **default global** tags and fieldsets from env vars.

        // Custom tags are added to the Meter.
        // Syntax: comma/space separated entries of the form "tag:value" or just "tag" .
        var envTags = Environment.get("${"+ENV_CHANNEL_TAGS+"}", DEFAULT_TAGS);
        var tagMap = parseTagPairs(envTags, false);
        tagMap.forEach((k,v) -> tags = tags.and(k,v));

        // Fieldsets are tags and values taken from the ISOMsg
        // Syntax: comma/space separated entries of the form "alias, tag:alias, tag:isofield".
        var envFields = Environment.get("${"+ENV_CHANNEL_FIELDS+"}", DEFAULT_CHANNEL_FIELDS);
        var fieldsMap = parseTagPairs(envFields, true);
        validateFieldSetMap(fieldsMap);
        fieldSet.putAll(fieldsMap);
    }


    /**
     * @return This overrides the default implementation, also including the keys from the internal field set.
     */
    @Override
    public String getMetricSignature() {
        List<String> keys = new ArrayList<>(fieldSet.size()*2);
        tags.forEach(t -> keys.add(t.getKey()));
        fieldSet.forEach((k,_) -> keys.add(k));
        return getMetricName()+"|"+
                keys.stream().sorted().collect(Collectors.joining(","));
    }


    @Override
    public String getMetricName() {
        return metricName != null ? metricName : DEFAULT_CHANNEL_METRIC_NAME;
    }
    @Override
    public void setMetricName(String metricName) {
        throwIfFrozen(true);
        Objects.requireNonNull(metricName, "Metric name can't be null");
        this.metricName = metricName;
    }


    public String getMetricDescription() {
        return metricDescription != null ? metricDescription : "";
    }
    public void setMetricDescription(String metricDescription) {
        this.metricDescription = metricDescription;
    }

    @Override
    public Tags addTags(Tags tags) {
        throwIfFrozen(true);
        if (!meters.isEmpty()) {
            String name = tags.stream()
                              .filter(t->"name".equals(t.getKey()))
                              .map(Tag::getValue).findAny()
                              .orElse(getMetricName());
            throw new IllegalStateException("ISOMsgCounter "+name+" can't add tags after started");
        }
        return (this.tags= this.tags.and(tags));
    }
    public Tags getTags() {
        return Tags.of(tags);
    }

    @Override
    public void recordMessage(ISOMsg m) {
        if (registry != null && m != null)  {
            throwIfFrozen(false);
            Tags ft = resolveFieldTags(m, fieldSet);
            Counter c = MeterFactory.updateCounter(registry,
                        getMetricName(),
                        tags.and(ft),
                        getMetricDescription());
            meters.add(c);
            c.increment();
        }
    }

    @Override
    public void recordMessage(ISOMsg m, MeterInfo meterInfo) {
        if (registry != null && m != null)  {
            throwIfFrozen(false);
            Tags ft = resolveFieldTags(m, fieldSet);
            String myName = getMetricName();

            Counter c;
            if (!DEFAULT_CHANNEL_METRIC_NAME.equals(myName))
                c = MeterFactory.updateCounter(registry,
                        getMetricName(),
                        meterInfo.add(tags).and(ft),    // allow our tags to override meterInfo's
                        getMetricDescription());
            else
                c = MeterFactory.updateCounter(registry, meterInfo, tags.and(ft));
            meters.add(c);
            c.increment();
        }
    }


    @Override
    public boolean register(MeterRegistry registry) {
        Objects.requireNonNull(registry, "Null registry passed to register() method.");
        this.registry = registry;
        frozen = true;
        return true;
    }

    @Override
    public void unregister() {
        removeMeters();
        registry = null;
    }


    @Override
    public MeterRegistry getRegistry() {
        return registry;
    }


    @Override
    public void removeMeters()  {
        if (registry != null) {
            LogEvent evt = logger != null ? new LogEvent(this, "info", "Removing meters: ") : null;

            // flag will make new recordMessage calls fail, in a NON-thread safe way
            // but this is normally called after the channel is being stopped anyway
            frozen = false;
            meters.forEach(m -> {
                if (evt != null) evt.addMessage(m.getId());
                registry.remove(m);
            });
            meters.clear();
            if (evt != null)
                Logger.log(evt);
        }
    }

    // ============= configuration =============

    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        String name = cfg.get("name", null);
         if (name != null)
            setMetricName(name);
        boolean customName = !DEFAULT_CHANNEL_METRIC_NAME.equals(getMetricName());

        setMetricDescription(cfg.get("description", null));

        // Process custom tag overrides (global defaults were handled in constructor).
        // Custom config overrides can only override the values of the pre-existing global env tags.
        // New tags can't be added, global tags can't be removed.
        //
        // Exception: If this class has a custom metric name, then it can define its own tag set.
        boolean hasTags = cfg.get("tags", null) != null;
        if (customName && hasTags)
            tags = Tags.empty();                                                // start afresh if custom metric has tags

        var currTags = getTagsAsMap();
        var ovrMap = parseTagPairs(cfg.get("tags", ""), false);
        for (var ent : ovrMap.entrySet()) {
            if (currTags.containsKey(ent.getKey()) || customName)               // if known tag, or custom name
                currTags.put(ent.getKey(), ent.getValue());                     // then allow override!
            else
                throw new ConfigurationException("Attempt to add unknown metric tag: '"+ent.getKey()+"'");
        }
        currTags.forEach((k,v) -> tags = tags.and(k,v));                        // add/override all custom tags to our tags


        // Process custom isofield overrides (global defaults were handled in constructor).
        // Custom overrides can only override pre-existing env tags, unless this class has a custom metric name.
        boolean hasFields = cfg.get("fields", null) != null;
        if (customName && hasFields)
            fieldSet.clear();                                                   // start afresh if custom metric has fields

        var fieldsOvrMap = parseTagPairs(cfg.get("fields", ""), true);
        validateFieldSetMap(fieldsOvrMap);
        for (var ent : fieldsOvrMap.entrySet()) {
            if (fieldSet.containsKey(ent.getKey()) || customName)               // known tag, or custom metric
                fieldSet.put(ent.getKey(), ent.getValue());                     // allow, override!
            else
                throw new ConfigurationException(
                    "Attempt to add unknown metric isofield tag: '"+ent.getKey()+"'");
        }
    }



    // copySingleTag: copy the tag name as value if no colon syntax; else, set value as empty string
    protected Map<String,String> parseTagPairs(String tp, boolean copySingleTag) {
        Map<String,String> ret = new HashMap<>();
        String[] tagPairs = tp.trim().split("[, ]+");
        for (String pair : tagPairs) {
            if (pair.isEmpty()) continue;               // avoids possible commas at beginning of tp

            String[] tv = pair.trim().split(":");
            ret.put(tv[0],
                    tv.length >= 2 ? tv[1] :
                        copySingleTag ? tv[0] : "");
        }
        return ret;
    }

    protected void validateFieldSetMap(Map<String,String> fieldsMap) throws ConfigurationException {
        for (var valexpr : fieldsMap.values()) {
            boolean isField = valexpr.matches("^[0-9]+(\\.[0-9]+)*$");          // is isomsg field path dot-syntax?
            if (!isField && aliases.get(valexpr) == null)
                throw new ConfigurationException("Unknown metric tag alias for fieldset: '"+valexpr+"'");
        }
    }


    // ============= some helper methods =============

    private void throwIfFrozen(boolean frozenCondition) {
        if (frozen == frozenCondition)
            throw new IllegalStateException(frozen ?
                        "Can't modify this ISOMsgCounter after registration ("+getMetricSignature()+")" :
                        "Can't use this ISOMsgCounter before registration ("+getMetricSignature()+")"
            );
    }


    /** returns a clone set, which may not be up to date next time you use it */
    protected Set<Meter> getMeters() {
        return new HashSet<>(meters);
    }

    // useful for tests and debugging
    protected Map<String,String> getFieldSet() {
        return Map.copyOf(fieldSet);
    }


    // Make Tags easy to navigate; do not abuse of this one unless you need repeated querying
    public Map<String,String> getTagsAsMap() {
        Map<String,String> tm= new HashMap<>();                             // make Tags easy to navigate
        getTags().forEach(t->tm.put(t.getKey(),t.getValue()));
        return tm;
    }

    private String getTtype(ISOMsg m) {
        return m.hasField(3) ? m.getString(3).substring(0,2) : "";
    }

    private String getITC(ISOMsg m) {
        String mti = m.getString(0);
        if (mti == null || mti.trim().isEmpty()) return "";
        // some common fields to make an ITC from
        var fields = Arrays.asList(mti, getTtype(m), m.getString(24), m.getString(25), m.getString(70));
        return fields.stream().filter(s -> s != null && !s.isEmpty()).collect(Collectors.joining("."));
    }


    /**
    * Hook for subclasses to resolve, against an ISOMsg, the valexpr part of a tag:valexpr in a fieldset.
    * <br/>
    * A subclass may add or override its own aliases, or have a special way to convert "valexpr"
    * to a String taken from the given ISOMsg.
    * <br/>
    * If the subclass can't resolve the alias/valexpr, it may call super (i.e. this method)
    * as a fallback.
    */
    protected String resolveValExpr(ISOMsg m, String val) {
        var fun = aliases.get(val);   // check if valexpr is a registered alias
        val = fun != null ? fun.apply(m) : m.getString(val);
        return val != null ? val : "";
    }

    /**
     * Returns a micrometer {@link Tags}, with keys and values resolved from a given fieldset against
     * a given ISOMsg.<br/>
     * Some of the valexprs in the fieldset may be aliases that need to be resolved to an ISOMsg field,
     * and the field path is used to get the value from the ISOMsg.<br/>
     * This method relies on the protected {@link #resolveValExpr(ISOMsg, String)} to resolve each valexpr.
     *
     * @param m the ISOMsg
     * @return a micrometer {@link Tags} with all the tags from fieldset and the resolved values from the message
     */
    private Tags resolveFieldTags(ISOMsg m, Map<String,String> fieldset) {
        Tags tt = Tags.empty();
        // each entry is {tag,valexpr}, where valexpr may be an alias or an isofield path
        for (var ent : fieldset.entrySet()) {
            String val = resolveValExpr(m, ent.getValue());
            tt = tt.and(ent.getKey(), val);
        }
        return tt;
    }


    @Override
    public void setLogger(Logger logger, String realm) {
        this.logger = logger;
        this.realm = realm;
    }
    @Override
    public String getRealm() {
        return realm;
    }
    @Override
    public Logger getLogger() {
        return logger;
    }
}
