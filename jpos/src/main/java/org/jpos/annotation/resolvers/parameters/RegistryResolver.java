package org.jpos.annotation.resolvers.parameters;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jpos.annotation.Registry;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOUtil;
import org.jpos.transaction.Context;
import org.jpos.transaction.TransactionParticipant;
import org.jpos.util.NameRegistrar;


public class RegistryResolver implements ResolverServiceProvider {
    private static final class RegistryResolverImpl implements Resolver {
        String registryKey;

        @Override
        public void configure(Parameter f) throws ConfigurationException {
            Registry annotation = f.getAnnotation(Registry.class);
            registryKey = findKey(annotation.value(), f.getType(), NameRegistrar.getAsMap());
            if (ISOUtil.isEmpty(registryKey)) {
                throw new ConfigurationException("Could not find Registry entry for " + f.getName());
            }
        }

        @Override
        public <T> T getValue(TransactionParticipant participant, Context ctx) {
            return NameRegistrar.getIfExists(registryKey);
        }

        String findKey(String key, Class<?> type, Map<?, ?> entries) throws ConfigurationException {
            if (entries.containsKey(key)) {
                return key;
            }
            List<String> typeMatches = new ArrayList<>();
            List<String> keyMatches = new ArrayList<>();
            findPotentialMatches(key, type, entries, typeMatches, keyMatches);
            return getMatch(key, typeMatches, keyMatches);
        }

        protected String getMatch(String key, List<String> typeMatches, List<String> keyMatches)
                throws ConfigurationException {
            if (!ISOUtil.isEmpty(key)) {
                return getMatch(key, keyMatches);
            } else {
                return getMatch(key, typeMatches);
            }
        }

        protected void findPotentialMatches(String key, Class<?> type, Map<?, ?> entries, List<String> typeMatches,
                List<String> keyMatches) {
            for (Entry<?, ?> entry: entries.entrySet()) {
                String mKey = String.valueOf(entry.getKey());
                if (mKey.equalsIgnoreCase(key)) {
                    keyMatches.add(mKey);
                }
                if (type.isAssignableFrom(entry.getValue().getClass())) {
                    typeMatches.add(mKey);
                }
            }
        }

        protected String getMatch(String key, List<String> keyMatches) throws ConfigurationException {
            switch(keyMatches.size()) {
            case 0: return null;
            case 1: return keyMatches.get(0);
            default : throw new ConfigurationException("Found multiple matches for key " + key);      
            }
        }
    }

    @Override
    public boolean isMatch(Parameter p) {
        return p.isAnnotationPresent(org.jpos.annotation.Registry.class);
    }

    @Override
    public Resolver resolve(Parameter p) {
        return new RegistryResolverImpl();
    }
}