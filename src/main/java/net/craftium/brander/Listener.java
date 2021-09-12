package net.craftium.brander;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import net.craftium.brander.utils.BrandUtils;

public class Listener
{
    private final Brander plugin;

    public Listener(Brander plugin)
    {
        this.plugin = plugin;
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event)
    {
        ChannelIdentifier identifier = event.getIdentifier();

        if(!(identifier.getId().equals(BrandUtils.CHANNEL.getId())))
            return;
        if(!(event.getTarget() instanceof ServerConnection))
            return;
        if(!(event.getSource() instanceof Player))
            return;

        switch(plugin.getConfig().mode())
        {
            case APPEND:
                BrandUtils.appendBrand(event, plugin.getServer(), plugin.getConfig().brand());
                break;
            case REPLACE:
                BrandUtils.replaceBrand(event, plugin.getConfig().brand());
                break;
        }
    }
}
