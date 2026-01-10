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

package org.jpos.core;

import org.jpos.iso.ISOUtil;
import org.jpos.util.Loggeable;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.scanner.ScannerException;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Manages environment-specific configuration for jPOS applications.
 *
 * <p>Environment provides property resolution with support for:
 * <ul>
 *     <li>YAML ({@code .yml}) and properties ({@code .cfg}) configuration files</li>
 *     <li>Property expressions: {@code ${property.name}}</li>
 *     <li>Default values: {@code ${property.name:default}}</li>
 *     <li>Equality tests: {@code ${property.name=expected}}</li>
 *     <li>Boolean negation: {@code ${!property.name}}</li>
 *     <li>Prefix-specific lookups:
 *         <ul>
 *             <li>{@code $env{VAR}} - OS environment variable only</li>
 *             <li>{@code $sys{prop}} - Java system property only</li>
 *             <li>{@code $cfg{prop}} - Configuration file only</li>
 *             <li>{@code $verb{text}} - Verbatim (no expansion)</li>
 *         </ul>
 *     </li>
 *     <li>Nested expressions: {@code ${outer:${inner:default}}}</li>
 * </ul>
 *
 * <p>The default property resolution order (for {@code ${prop}}) is:
 * <ol>
 *     <li>OS environment variable ({@code prop})</li>
 *     <li>OS environment variable ({@code PROP} with dots replaced by underscores)</li>
 *     <li>Java system property</li>
 *     <li>Configuration file property</li>
 * </ol>
 *
 * <p>Configuration is loaded from the directory specified by {@code jpos.envdir}
 * (default: "cfg") with the filename from {@code jpos.env} (default: "default").
 *
 * @see EnvironmentProvider
 */
public class Environment implements Loggeable {
    private static final String DEFAULT_ENVDIR = "cfg";         // default dir for the env file (relative to cwd), overridable with sys prop "jpos.envdir"

    private static final String CFG_PREFIX = "cfg";
    private static final String SYSTEM_PREFIX = "sys";
    private static final String ENVIRONMENT_PREFIX = "env";

    private static Environment INSTANCE;

    private String name;
    private String envDir;
    private AtomicReference<Properties> propRef = new AtomicReference<>(new Properties());
    private static String SP_PREFIX = "system.property.";
    private static int SP_PREFIX_LENGTH = SP_PREFIX.length();
    private String errorString;
    private ServiceLoader<EnvironmentProvider> serviceLoader;
    // Sentinel used to protect verbatim '$' so it is not expanded in later passes.
    private static final String VERB_DOLLAR_SENTINEL = "\u0000$VERB_DOLLAR$\u0000";


    static {
        try {
            INSTANCE = new Environment();
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    protected static Map<String,String> notMap = new HashMap<>();
    static {
        notMap.put("false", "true");
        notMap.put("true",  "false");
        notMap.put("yes",   "no");
        notMap.put("no",    "yes");
    }

    private Environment() throws IOException {
        name = System.getProperty ("jpos.env");
        name = name == null ? "default" : name;
        envDir = System.getProperty("jpos.envdir", DEFAULT_ENVDIR);
        serviceLoader = ServiceLoader.load(EnvironmentProvider.class);
        readConfig ();
    }

    /**
     * Returns the name of the current environment.
     * Determined by the {@code jpos.env} system property, defaults to "default".
     *
     * @return the environment name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the directory where environment configuration files are located.
     * Determined by the {@code jpos.envdir} system property, defaults to "cfg".
     *
     * @return the environment directory path
     */
    public String getEnvDir() {
        return envDir;
    }

    /**
     * Reloads the environment configuration from disk.
     * Reads the {@code jpos.env} and {@code jpos.envdir} system properties
     * and reloads the corresponding configuration files.
     *
     * @return the newly loaded Environment instance
     * @throws IOException if an error occurs reading configuration files
     */
    public static Environment reload() throws IOException {
        return (INSTANCE = new Environment());
    }

    /**
     * Returns the singleton Environment instance.
     *
     * @return the current Environment
     */
    public static Environment getEnvironment() {
        return INSTANCE;
    }

    /**
     * Resolves a property expression using the singleton Environment.
     * If the property cannot be resolved, returns the original expression.
     *
     * @param p the property expression to resolve (e.g., "${my.property}")
     * @return the resolved value, or the original expression if unresolved
     * @see #getProperty(String)
     */
    public static String get (String p) {
        return getEnvironment().getProperty(p, p);
    }

    /**
     * Resolves a property expression using the singleton Environment.
     * If the property cannot be resolved, returns the specified default.
     *
     * @param p the property expression to resolve
     * @param def the default value to return if the property is unresolved
     * @return the resolved value, or {@code def} if unresolved
     * @see #getProperty(String, String)
     */
    public static String get (String p, String def) {
        return getEnvironment().getProperty(p, def);
    }

    /**
     * Resolves a property expression with a default fallback.
     *
     * @param p the property expression to resolve
     * @param def the default value to return if the property resolves to null
     * @return the resolved value, or {@code def} if null
     * @see #getProperty(String)
     */
    public String getProperty (String p, String def) {
        String s = getProperty (p);
        return s != null ? s : def;
    }

    /**
     * Returns any error message from the last configuration load attempt.
     * Typically set when YAML parsing fails.
     *
     * @return the error message, or null if no error occurred
     */
    public String getErrorString() {
        return errorString;
    }

    /**
     * If property name has the pattern <code>${propname}</code>, this method will
     *
     * <ul>
     *     <li>Attempt to get it from an operating system environment variable called 'propname'</li>
     *     <li>If not present, it will try to pick it from the Java system.property</li>
     *     <li>If not present either, it will try the target environment (either <code>.yml</code> or <code>.cfg</code></li>
     *     <li>Otherwise it returns null</li>
     * </ul>
     *
     * The special pattern <code>$env{propname}</code> would just try to pick it from the OS environment.
     * <code>$sys{propname}</code> will just try to get it from a System.property and
     * <code>$verb{propname}</code> will return a verbatim copy of the value.
     *
     * @param s property name
     * @return property value
     */
    public String getProperty (String s) {
        if (s == null)
            return null;

        // Fast-path: no possible expressions.
        if (s.indexOf('$') < 0)
            return s;

        if (s.startsWith("$verb{")) {
            int closeIdx = s.indexOf('}', 6); // first '}' after "$verb{"
            if (closeIdx == s.length() - 1 && s.length() > "$verb{}".length()) {
                return s.substring(6, closeIdx);
            }
        }
        String r = s;

        // Bounded expansion + cycle detection
        final int MAX_EXPANSION_STEPS = 256;
        final int MAX_SEEN_STATES = 2048;

        final Set<String> seen = new HashSet<>();
        seen.add(r);

        for (int step = 0; step < MAX_EXPANSION_STEPS; step++) {
            String next = expandOnce(r);
            if (Objects.equals(next, r))
                break;
            if (!seen.add(next))
                break;
            if (seen.size() > MAX_SEEN_STATES)
                break;
            r = next;
        }
        return unescapeVerbatimDollars(r);
    }

    /**
     * Expands all occurrences of $...{...} in the input string in a single linear pass.
     * This method is deliberately regex-free to avoid backtracking / stack overflow.
     */
    private String expandOnce(String in) {
        StringBuilder out = new StringBuilder(in.length());
        int i = 0;
        boolean changed = false;

        while (i < in.length()) {
            char ch = in.charAt(i);
            if (ch != '$') {
                out.append(ch);
                i++;
                continue;
            }

            // Inline $verb{...}: verbatim payload, no expansion even across passes, Terminate at the first '}'
            if (in.startsWith("$verb{", i)) {
                int closeIdx = in.indexOf('}', i + 6);
                if (closeIdx != -1) {
                    String payload = in.substring(i + 6, closeIdx);
                    out.append(escapeVerbatimDollars(payload));
                    i = closeIdx + 1;
                    changed = true;
                    continue;
                }
                // If no closing brace found, fall through and treat '$' as literal (via parseToken failure path).
            }
            Token t = parseToken(in, i);
            if (t == null) {
                // Not a valid token; treat '$' as literal.
                out.append('$');
                i++;
                continue;
            }

            String replacement = evaluateToken(t, in.substring(t.start(), t.endExclusive()));
            if (replacement == null) {
                return in;
            }

            out.append(replacement);
            i = t.endExclusive();
            changed = true;
        }

        return changed ? out.toString() : in;
    }

    private boolean isKnownPrefix(String prefix) {
        return prefix.isEmpty()
           || CFG_PREFIX.equals(prefix)
           || SYSTEM_PREFIX.equals(prefix)
           || ENVIRONMENT_PREFIX.equals(prefix);
    }

    private String evaluateToken(Token t, String originalTokenText) {
        if (!isKnownPrefix(t.prefix())) {
            return null; // expandOnce() will return the original input unchanged.
        }
        boolean negated = t.negated();
        String defValueLiteral = null;

        String resolved = resolveByPrefix(t.prefix(), t.property());

        if (t.op() == '=') {
            String rhsResolved = t.rhs() == null ? "" : getProperty(t.rhs());
            resolved = (resolved != null && resolved.equals(rhsResolved)) ? "true" : "false";
        } else if (t.op() == ':') {
            // Default case. Keep literal default as-is; outer expansion passes will dereference it.
            if (resolved == null) {
                defValueLiteral = t.rhs(); // may be null or empty, both are meaningful
                resolved = defValueLiteral;
            }
        } else {
            // No op, resolved stays as-is (may be null).
        }

        if (resolved != null) {
            resolved = applyProviderTransformations(resolved);
            resolved = applyNegation(resolved, negated, defValueLiteral == null);
            return resolved;
        }
        // Undefined/unresolved and no default.
        // If negated and unresolved => "true", otherwise token removal is NOT desired
        if (negated) {
            return "true";
        }
        // prefixed lookups resolve to empty when missing.
        return t.prefix().isEmpty() ? originalTokenText : "";
    }

    private String resolveByPrefix(String prefix, String prop) {
        return switch (prefix) {
            case CFG_PREFIX -> propRef.get().getProperty(prop, null);
            case SYSTEM_PREFIX -> System.getProperty(prop);
            case ENVIRONMENT_PREFIX -> System.getenv(prop);
            default -> prefix.isEmpty() ? resolveWithPriority(prop) : null;
        };
    }

    /**
     * Resolves a property using the default priority: ENV > System property > cfg file.
     */
    private String resolveWithPriority(String prop) {
        String r = System.getenv(prop);
        if (r == null) r = System.getenv(prop.replace('.', '_').toUpperCase());
        if (r == null) r = System.getProperty(prop);
        if (r == null) r = propRef.get().getProperty(prop);
        return r;
    }

    /**
     * Applies EnvironmentProvider transformations. Some providers may return a value
     * that still begins with the same provider prefix (e.g., obf(obf(x))).
     * In that case, transformations are applied repeatedly until no provider matches
     * or a safety limit is reached.
     */
    private String applyProviderTransformations(String value) {
        String v = value;
        if (v == null)
            return null;

        final int MAX_PROVIDER_STEPS = 32; // safety against misbehaving providers

        for (int step = 0; step < MAX_PROVIDER_STEPS; step++) {
            boolean changed = false;

            for (EnvironmentProvider p : serviceLoader) {
                String prefix = p.prefix();
                int prefixLen = prefix.length();
                if (v.length() > prefixLen && v.startsWith(prefix)) {
                    String next = p.get(v.substring(prefixLen));
                    if (next == null) {
                        // Be conservative: if provider returns null, stop transforming and return current.
                        return v;
                    }
                    if (!Objects.equals(next, v)) {
                        v = next;
                        changed = true;
                    } else {
                        // No progress; avoid tight loops.
                        return v;
                    }
                    break; // restart from first provider on the new value
                }
            }

            if (!changed)
                return v;
        }

        // Safety stop: return the last value we reached.
        return v;
    }

    /**
     * Applies boolean negation if the token was negated and it's not a default literal.
     */
    private String applyNegation(String value, boolean negated, boolean canNegate) {
        if (negated && canNegate) {
            String normalized = value.trim().toLowerCase();
            return notMap.getOrDefault(normalized, value);
        }
        return value;
    }

    /**
     * Token parsed from $...{...}.
     * @param start position of '$' in the input string
     * @param endExclusive index just after the closing '}'
     * @param prefix "", "cfg", "sys", "env", or unknown (unknown handled earlier)
     * @param negated true if property name started with '!'
     * @param property the property name (without '!' prefix)
     * @param op operator: 0 (none), ':', or '='
     * @param rhs default value or equals RHS (may be null/empty)
     */
    private record Token(int start, int endExclusive, String prefix, boolean negated, String property, char op, String rhs) {}

    private Token parseToken(String s, int dollarPos) {
        final int n = s.length();
        int i = dollarPos;

        if (i >= n || s.charAt(i) != '$')
            return null;
        i++; // skip '$'

        // prefix: [\w]* (may be empty) until '{'
        int prefixStart = i;
        while (i < n && isWordChar(s.charAt(i))) {
            i++;
        }
        if (i >= n || s.charAt(i) != '{') {
            return null;
        }
        String prefix = s.substring(prefixStart, i);
        i++; // skip '{'

        // property name: [-!\w.]+ (but stop on ':' '=' or '}' )
        if (i >= n)
            return null;

        boolean negated = false;
        int propStart = i;

        // read property characters
        while (i < n) {
            char c = s.charAt(i);
            if (c == ':' || c == '=' || c == '}')
                break;
            if (!isPropChar(c))
                return null;
            i++;
        }

        if (i == propStart) // empty property name is not valid
            return null;

        String prop = s.substring(propStart, i);
        if (prop.startsWith("!")) {
            negated = true;
            prop = prop.substring(1);
            if (prop.isEmpty())
                return null;
        }

        // operator?
        if (i >= n)
            return null;

        char op = 0;
        String rhs = null;

        char c = s.charAt(i);
        if (c == '}' ) {
            // simple ${prop}
            return new Token(dollarPos, i + 1, prefix, negated, prop, op, rhs);
        } else if (c == ':' || c == '=') {
            op = c;
            i++; // skip op
            int rhsStart = i;

            // find matching '}' for this token, supporting nested $...{...} in RHS
            int end = findTokenEnd(s, dollarPos);
            if (end < 0)
                return null;

            // RHS is content between op and final '}' of this token.
            rhs = s.substring(rhsStart, end);

            return new Token(dollarPos, end + 1, prefix, negated, prop, op, rhs);
        } else {
            // unexpected character
            return null;
        }
    }

    /**
     * Returns the index of the '}' that closes the token beginning at dollarPos, or -1 if not found.
     * Supports nested tokens inside defaults/RHS by counting nested "$...{" starts.
     */
    private int findTokenEnd(String s, int dollarPos) {
        final int n = s.length();
        int i = dollarPos;

        // We know s[dollarPos] == '$'. Find the first '{' that starts this token.
        i++; // after '$'
        while (i < n && isWordChar(s.charAt(i))) i++;
        if (i >= n || s.charAt(i) != '{') return -1;
        i++; // after the '{' of the outer token

        int depth = 0; // nested token depth within RHS
        while (i < n) {
            char ch = s.charAt(i);

            if (ch == '$' && looksLikeTokenStart(s, i)) {
                // consume "$" + prefix + "{", and count as nested
                depth++;
                i++; // after '$'
                while (i < n && isWordChar(s.charAt(i))) i++;
                if (i < n && s.charAt(i) == '{') {
                    i++; // after '{'
                    continue;
                } else {
                    // Should not happen because looksLikeTokenStart checked it, but be defensive.
                    continue;
                }
            }

            if (ch == '}') {
                if (depth == 0) {
                    return i;
                }
                depth--;
                i++;
                continue;
            }

            i++;
        }
        return -1;
    }

    private boolean looksLikeTokenStart(String s, int pos) {
        final int n = s.length();
        if (pos < 0 || pos >= n || s.charAt(pos) != '$')
            return false;

        int i = pos + 1;

        // prefix: [\w]* (may be empty)
        while (i < n && isWordChar(s.charAt(i))) i++;

        // must have '{'
        if (i >= n || s.charAt(i) != '{')
            return false;

        int j = i + 1; // first char inside '{'
        if (j >= n)
            return false;

        char first = s.charAt(j);

        // Reject empty property name: "${}"
        // Reject operator immediately: "${:...}" or "${=...}"
        // In general, require at least one property-name char.
        if (first == '}' || first == ':' || first == '=')
            return false;

        // And it must be a valid property-name char
        return isPropChar(first);
    }
    
    private static boolean isWordChar(char c) {
        return (c == '_' ||
          (c >= '0' && c <= '9') ||
          (c >= 'A' && c <= 'Z') ||
          (c >= 'a' && c <= 'z'));
    }

    private static boolean isPropChar(char c) {
        return isWordChar(c) || c == '.' || c == '-' || c == '!';
    }

    @SuppressWarnings("unchecked")
    private void readConfig () throws IOException {
        if (name != null) {
            Properties properties = new Properties();
            String[] names = ISOUtil.commaDecode(name);
            for (String n: names) {
                if (!readYAML(n, properties))
                    readCfg(n, properties);
            }
            extractSystemProperties();
            propRef.get().put ("jpos.env", name);
            propRef.get().put ("jpos.envdir", envDir);
        }
    }

    private void extractSystemProperties() {
        Properties properties = propRef.get();
        properties
          .stringPropertyNames()
          .stream()
          .filter(e -> e.startsWith(SP_PREFIX))
          .forEach(prop -> System.setProperty(
            prop.substring(SP_PREFIX_LENGTH), getProperty ((String) properties.get(prop)))
          );
    }

    private boolean readYAML (String n, Properties properties) throws IOException {
        errorString = null;
        File f = new File(envDir + "/" + n + ".yml");
        if (f.exists() && f.canRead()) {
            try (InputStream fis = new FileInputStream(f)) {
                Yaml yaml = new Yaml();
                Iterable<Object> document = yaml.loadAll(fis);
                document.forEach(d -> {
                    flat(properties, null, (Map<String, Object>) d, false);
                });
                propRef.set(properties);
                return true;
            } catch (ScannerException e) {
                errorString = "Environment (" + getName() + ") error " + e.getMessage();
            }
        }
        return false;
    }

    private boolean readCfg (String n, Properties properties) throws IOException {
        File f = new File(envDir + "/" + n + ".cfg");
        if (f.exists() && f.canRead()) {
            try (InputStream fis = new FileInputStream(f)) {
                properties.load(new BufferedInputStream(fis));
                propRef.set(properties);
                return true;
            }
        }
        return false;
    }

    /**
     * Flattens a nested Map structure into a flat Properties object using dot notation.
     * For example, a nested structure like {@code {server: {port: 8080}}} becomes
     * the property {@code server.port=8080}.
     *
     * <p>List values are comma-encoded using {@link ISOUtil#commaEncode(String[])}.
     *
     * @param properties the Properties object to populate
     * @param prefix the current key prefix (null for root level)
     * @param c the Map to flatten
     * @param dereference if true, resolve property expressions in string values
     */
    @SuppressWarnings("unchecked")
    public static void flat (Properties properties, String prefix, Map<String,Object> c, boolean dereference) {
        for (Map.Entry<String,Object> entry : c.entrySet()) {
            String p = prefix == null ? entry.getKey() : (prefix + "." + entry.getKey());
            if (entry.getValue() instanceof Map) {
                flat(properties, p, (Map<String, Object>) entry.getValue(), dereference);
            } else if (entry.getValue() instanceof List<?> listParams) {
                List<String> list = listParams.stream()
                  .map(Object::toString)
                  .map(str -> dereference ? Environment.get(str) : str)
                  .collect(Collectors.toList());
                properties.put (p, ISOUtil.commaEncode(list.toArray(new String[0])));
            } else {
                Object obj = entry.getValue();
                properties.put (p, (dereference && obj instanceof String ?
                                    Environment.get((String) obj) :
                                    entry.getValue().toString()));
            }
        }
    }

    @Override
    public void dump(final PrintStream p, String indent) {
        p.printf ("%s<environment name='%s' envdir='%s'>%n", indent, name, envDir);
        Properties properties = propRef.get();
        properties.stringPropertyNames().stream().
          forEachOrdered(prop -> p.printf ("%s  %s=%s%n", indent, prop, properties.getProperty(prop)) );
        p.printf ("%s</environment>%n", indent);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (name != null) {
            sb.append(String.format("[%s]%n", name));
            Properties properties = propRef.get();
            properties.stringPropertyNames().stream().
              forEachOrdered(prop -> {
                  String s = properties.getProperty(prop);
                  String ds = Environment.get(String.format("${%s}", prop)); // de-referenced string
                  boolean differ = !s.equals(ds);
                  sb.append(String.format ("  %s=%s%s%n",
                    prop,
                    s,
                    differ ? " (*)" : ""
                  )
              );
            });
            if (serviceLoader.iterator().hasNext()) {
                sb.append ("  providers:");
                sb.append (System.lineSeparator());
                for (EnvironmentProvider provider : serviceLoader) {
                    sb.append(String.format("    %s%n", provider.getClass().getCanonicalName()));
                }
            }
        }
        return sb.toString();
    }

    private static String escapeVerbatimDollars(String s) {
        return s == null ? null : s.replace("$", VERB_DOLLAR_SENTINEL);
    }

    private static String unescapeVerbatimDollars(String s) {
        return s == null ? null : s.replace(VERB_DOLLAR_SENTINEL, "$");
    }
}
