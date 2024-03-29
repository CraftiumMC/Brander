package net.craftium.brander.hook;

import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.connection.backend.BackendPlaySessionHandler;
import com.velocitypowered.proxy.connection.backend.VelocityServerConnection;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.StateRegistry;
import com.velocitypowered.proxy.protocol.packet.PluginMessage;
import com.velocitypowered.proxy.protocol.util.PluginMessageUtil;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.collection.IntObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class PluginMessageHook extends PluginMessage
{
    private static ProxyServer server;
    private static Field serverConnField;

    public static void init(ProxyServer server)
    {
        try
        {
            PluginMessageHook.server = server;
            serverConnField = BackendPlaySessionHandler.class.getDeclaredField("serverConn");
            serverConnField.setAccessible(true);

            Field versionsField = StateRegistry.PacketRegistry.class.getDeclaredField("versions");
            versionsField.setAccessible(true);

            Field packetIdToSupplierField = StateRegistry.PacketRegistry.ProtocolRegistry.class.getDeclaredField("packetIdToSupplier");
            packetIdToSupplierField.setAccessible(true);

            Field packetClassToIdField = StateRegistry.PacketRegistry.ProtocolRegistry.class.getDeclaredField("packetClassToId");
            packetClassToIdField.setAccessible(true);

            Field clientboundField = StateRegistry.class.getDeclaredField("clientbound");
            clientboundField.setAccessible(true);

            //noinspection unchecked
            var versions = (Map<ProtocolVersion, StateRegistry.PacketRegistry.ProtocolRegistry>)
                    versionsField.get(clientboundField.get(StateRegistry.PLAY));

            BiConsumer<? super ProtocolVersion, ? super StateRegistry.PacketRegistry.ProtocolRegistry>
                    consumer = (version, registry) ->
            {
                try
                {
                    //noinspection unchecked
                    IntObjectMap<Supplier<? extends MinecraftPacket>> packetIdToSupplier =
                            (IntObjectMap<Supplier<? extends MinecraftPacket>>) packetIdToSupplierField.get(registry);

                    //noinspection unchecked
                    Object2IntMap<Class<? extends MinecraftPacket>> packetClassToId =
                            (Object2IntMap<Class<? extends MinecraftPacket>>) packetClassToIdField.get(registry);

                    int id = packetClassToId.getInt(PluginMessage.class);
                    packetClassToId.put(PluginMessageHook.class, id);

                    packetIdToSupplier.remove(id);
                    packetIdToSupplier.put(id, PluginMessageHook::new);
                }
                catch(IllegalAccessException e)
                {
                    e.printStackTrace();
                }
            };

            versions.forEach(consumer);
        }
        catch(NoSuchFieldException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean handle(MinecraftSessionHandler handler)
    {
        if(handler instanceof BackendPlaySessionHandler && PluginMessageUtil.isMcBrand(this))
        {
            try
            {
                VelocityServer server = (VelocityServer) PluginMessageHook.server;
                VelocityServerConnection serverConn = (VelocityServerConnection) serverConnField.get(handler);
                ConnectedPlayer player = serverConn.getPlayer();

                byte[] copy = ByteBufUtil.getBytes(this.content());

                PluginMessageEvent result = server.getEventManager()
                        .fire(new PluginMessageEvent(player, serverConn, this::getChannel, copy))
                        .get();

                if(result.getResult().isAllowed())
                    return super.handle(handler);
                else
                    return true;
            }
            catch(IllegalAccessException | ExecutionException | InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        return super.handle(handler);
    }
}
