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
    requires org.bouncycastle.lts.prov;
    // requires org.apache.sshd.common;
    // requires org.apache.sshd.server;
    // requires org.apache.sshd.core;

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
    // exports org.jpos.q2.ssh;
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

    uses org.jpos.core.EnvironmentProvider;
}
