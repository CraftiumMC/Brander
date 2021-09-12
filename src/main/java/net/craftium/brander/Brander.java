package net.craftium.brander;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.craftium.brander.hook.PluginMessageHook;
import org.slf4j.Logger;
import space.arim.dazzleconf.ConfigurationFactory;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.ext.snakeyaml.SnakeYamlConfigurationFactory;
import space.arim.dazzleconf.helper.ConfigurationHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Brander
{
    private final Logger logger;
    private final Path dataFolder;
    private final ProxyServer server;

    private Config config;

    @Inject
    public Brander(Logger logger, @DataDirectory Path dataFolder, ProxyServer server) throws IOException
    {
        this.logger = logger;
        this.dataFolder = dataFolder;
        this.server = server;

        Files.createDirectories(dataFolder);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event)
    {
        this.config = loadConfig();
        if(config == null)
            return;

        PluginMessageHook.init(server);
        server.getEventManager().register(this, new Listener(this));
        server.getCommandManager().register("brander", new Command(this));
    }

    public boolean reload()
    {
        this.config = loadConfig();
        return !(config == null);
    }

    public Config getConfig()
    {
        return config;
    }

    public ProxyServer getServer()
    {
        return server;
    }

    private Config loadConfig()
    {
        ConfigurationFactory<Config> factory = SnakeYamlConfigurationFactory
                .create(Config.class, ConfigurationOptions.defaults());

        ConfigurationHelper<Config> configHelper = new ConfigurationHelper<>(dataFolder, "config.yml", factory);

        try
        {
            return configHelper.reloadConfigData();
        }
        catch(IOException e)
        {
            logger.error("Failed to read config file", e);
        }
        catch(InvalidConfigException e)
        {
            logger.error("Failed to parse config file", e);
        }

        return null;
    }
}
