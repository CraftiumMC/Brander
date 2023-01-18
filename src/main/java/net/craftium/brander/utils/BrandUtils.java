package net.craftium.brander.utils;

import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.ChannelMessageSource;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.packet.PluginMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import static com.velocitypowered.proxy.protocol.util.PluginMessageUtil.readBrandMessage;
import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.SECTION_CHAR;
import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection;

public class BrandUtils
{
    public static void appendBrand(PluginMessageEvent event, ProxyServer server, String customBrand)
    {
        ByteBuf rewrittenBuf = Unpooled.buffer();
        String currentBrand = readBrandMessage(Unpooled.wrappedBuffer(event.getData())) +
                " (" + server.getVersion().getName() + ")";
        String newBrand = format(currentBrand + customBrand);
        ProtocolUtils.writeString(rewrittenBuf, newBrand);

        setBrand(event.getSource(), new PluginMessage(CHANNEL.getId(), rewrittenBuf));
    }

    public static void replaceBrand(PluginMessageEvent event, String customBrand)
    {
        ByteBuf rewrittenBuf = Unpooled.buffer();
        String newBrand = format(customBrand);
        ProtocolUtils.writeString(rewrittenBuf, newBrand);

        setBrand(event.getSource(), new PluginMessage(CHANNEL.getId(), rewrittenBuf));
    }

    private static String format(String brand)
    {
        Component text = MINI_MESSAGE.deserialize(brand);
        return SERIALIZER.serialize(text) + (SECTION_CHAR + "r");
    }

    private static void setBrand(ChannelMessageSource player, PluginMessage brand)
    {
        ConnectedPlayer connectedPlayer = (ConnectedPlayer) player;
        connectedPlayer.getConnection().write(brand);
    }

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer SERIALIZER = legacySection();
    public static final ChannelIdentifier CHANNEL = MinecraftChannelIdentifier.forDefaultNamespace("brand");
}
