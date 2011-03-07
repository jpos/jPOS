package org.jpos.transaction.participant;

import static org.hamcrest.Matchers.is;
import static org.jpos.transaction.TransactionConstants.PREPARED;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import java.io.Serializable;
import java.util.Random;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DelayTest {
    @Mock
    Random random;
    @Mock
    Configuration cfg;
    @Mock
    Serializable context;
    Delay delay;

    @Before
    public void setUp() throws Exception {
        delay = new Delay();
        given(random.nextLong()).willReturn(1L);
        given(cfg.getLong("prepare-delay")).willReturn(12345L);
        given(cfg.getLong("commit-delay")).willReturn(54321L);
        given(cfg.getLong("abort-delay")).willReturn(98765L);
        given(cfg.getBoolean("random")).willReturn(true);
    }

    @Test
    public void testSetConfiguration() throws ConfigurationException {
        delay.setConfiguration(cfg);
        verify(cfg, atLeastOnce()).getLong("prepare-delay");
        verify(cfg, atLeastOnce()).getLong("commit-delay");
        verify(cfg, atLeastOnce()).getLong("abort-delay");
        verify(cfg, atLeastOnce()).getBoolean("random");
    }

    @Test
    public void testSetConfigurationNoRandomOrSetVars() throws ConfigurationException {
        // given
        given(cfg.getLong("prepare-delay")).willReturn(0L);
        given(cfg.getLong("commit-delay")).willReturn(0L);
        given(cfg.getLong("abort-delay")).willReturn(0L);
        given(cfg.getBoolean("random")).willReturn(false);
        // when
        delay.setConfiguration(cfg);
        // then
        verify(cfg, atLeastOnce()).getLong("prepare-delay");
        verify(cfg, atLeastOnce()).getLong("commit-delay");
        verify(cfg, atLeastOnce()).getLong("abort-delay");
        verify(cfg, atLeastOnce()).getBoolean("random");
    }

    @Test
    public void testPrepare() {
        assertThat(delay.prepare(0L, context), is(PREPARED));
    }
}
