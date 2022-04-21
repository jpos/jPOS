module org.jpos.jpos {
    requires java.se;
    requires org.jdom2;
    requires org.bouncycastle.pg;
    requires org.bouncycastle.provider;
    requires org.apache.commons.cli;
    requires org.osgi.core;
    requires org.javatuples;
    requires org.yaml.snakeyaml;
    requires org.hdrhistogram.HdrHistogram;

    exports org.jpos.iso.packager;
    exports org.jpos.iso.validator;
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
    exports org.jpos.core.handlers.exception;
    exports org.jpos.rc;
}

