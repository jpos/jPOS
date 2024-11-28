module org.jpos.jpos {
    requires java.se;
    requires jdk.jfr;
    requires org.jdom2;
    requires org.apache.commons.cli;
    requires org.javatuples;
    requires org.yaml.snakeyaml;
    requires org.hdrhistogram.HdrHistogram;
    requires org.jline;
    requires bsh;
    requires org.slf4j;
    requires com.sleepycat.je;
    requires org.jdbm;
    requires org.bouncycastle.pg;
    requires jdk.httpserver;
    requires micrometer.core;
    requires micrometer.registry.prometheus;
    requires org.apache.sshd.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.dataformat.xml;
    requires org.bouncycastle.provider;
    //    requires net.i2p.crypto.eddsa;

    exports org.jpos.iso.packager;
    exports org.jpos.iso.validator;
    exports org.jpos.iso;
    exports org.jpos.q2;
    exports org.jpos.q2.cli;
    exports org.jpos.q2.install;
    exports org.jpos.q2.iso;
    exports org.jpos.q2.qbean;
    exports org.jpos.q2.security;
    exports org.jpos.q2.ui;
    exports org.jpos.security;
    exports org.jpos.security.jceadapter;
    exports org.jpos.space;
    exports org.jpos.tlv;
    exports org.jpos.transaction;
    exports org.jpos.transaction.gui;
    exports org.jpos.transaction.participant;
    exports org.jpos.ui;
    exports org.jpos.ui.action;
    exports org.jpos.ui.factory;
    exports org.jpos.util;
    exports org.jpos.util.function;
    exports org.jpos.emv;
    exports org.jpos.core;
    exports org.jpos.core.handlers.exception;
    exports org.jpos.rc;
    exports org.jpos.log;
    exports org.jpos.log.render.xml;
    exports org.jpos.log.render.json;
    exports org.jpos.log.render.markdown;
    exports org.jpos.log.evt;
    exports org.jpos.core.annotation;

    uses org.jpos.core.EnvironmentProvider;
    uses org.jpos.log.LogRenderer;
}