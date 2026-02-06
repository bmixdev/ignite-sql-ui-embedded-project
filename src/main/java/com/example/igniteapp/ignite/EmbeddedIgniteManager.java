package com.example.igniteapp.ignite;

import com.example.igniteapp.config.IgniteProperties;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.ClientConnectorConfiguration;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

/**
 * Embedded Ignite for local testing.
 * Enable via: --app.ignite.embedded.enabled=true
 */
@Component
public class EmbeddedIgniteManager implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(EmbeddedIgniteManager.class);

    private final IgniteProperties props;

    private volatile boolean running = false;
    private Ignite ignite;

    public EmbeddedIgniteManager(IgniteProperties props) {
        this.props = props;
    }

    @Override
    public void start() {
        if (!props.getEmbedded().isEnabled()) return;
        if (running) return;

        IgniteConfiguration cfg = new IgniteConfiguration();
        cfg.setIgniteInstanceName(props.getEmbedded().getInstanceName());

        // Make workDir absolute if needed
        String wd = props.getEmbedded().getWorkDir();
        Path p = Paths.get(wd);
        if (!p.isAbsolute()) {
            wd = Paths.get(System.getProperty("user.dir")).resolve(wd).toAbsolutePath().toString();
        }
        cfg.setWorkDirectory(wd);

        // Local discovery (single-node testing)
        TcpDiscoverySpi disco = new TcpDiscoverySpi();
        TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();
        ipFinder.setAddresses(Collections.singletonList("127.0.0.1:47500..47509"));
        disco.setIpFinder(ipFinder);
        cfg.setDiscoverySpi(disco);

        // Thin client connector (JDBC thin uses this)
        ClientConnectorConfiguration ccc = new ClientConnectorConfiguration();
        ccc.setPort(props.getEmbedded().getThinPort());
        cfg.setClientConnectorConfiguration(ccc);

        // Storage (persistence off by default)
        DataStorageConfiguration storage = new DataStorageConfiguration();
        DataRegionConfiguration region = new DataRegionConfiguration();
        region.setName("default");
        region.setPersistenceEnabled(props.getEmbedded().isPersistenceEnabled());
        storage.setDefaultDataRegionConfiguration(region);
        cfg.setDataStorageConfiguration(storage);

        ignite = Ignition.start(cfg);
        running = true;

        log.info("Embedded Ignite started. thinPort={}, workDir={}", props.getEmbedded().getThinPort(), wd);
    }

    @Override
    public void stop() {
        if (!running) return;
        try {
            Ignition.stop(props.getEmbedded().getInstanceName(), true);
        } finally {
            running = false;
            ignite = null;
        }
        log.info("Embedded Ignite stopped.");
    }

    @Override public boolean isRunning() { return running; }
    @Override public boolean isAutoStartup() { return true; }
    @Override public int getPhase() { return Integer.MIN_VALUE + 1000; }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }
}
