package net.minecraft.server;

import java.net.InetSocketAddress;

// CraftBukkit start
import java.util.ArrayList;
import org.bukkit.craftbukkit.util.CraftIconCache;
import org.bukkit.entity.Player;
import net.minecraft.util.com.mojang.authlib.GameProfile;
// CraftBukkit end

import net.minecraft.util.io.netty.util.concurrent.GenericFutureListener;

public class PacketStatusListener implements PacketStatusInListener {

    private final MinecraftServer minecraftServer;
    private final NetworkManager networkManager;

    public PacketStatusListener(MinecraftServer minecraftserver, NetworkManager networkmanager) {
        this.minecraftServer = minecraftserver;
        this.networkManager = networkmanager;
    }

    public void a(IChatBaseComponent ichatbasecomponent) {}

    public void a(EnumProtocol enumprotocol, EnumProtocol enumprotocol1) {
        if (enumprotocol1 != EnumProtocol.STATUS) {
            throw new UnsupportedOperationException("Unexpected change in protocol to " + enumprotocol1);
        }
    }

    public void a() {}

    public void a(PacketStatusInStart packetstatusinstart) {
        // CraftBukkit start - fire ping event
        final ArrayList<Player> playerSampleList = new ArrayList<Player>();
        for (GameProfile i : minecraftServer.at().b().c()) {
            playerSampleList.add(minecraftServer.server.getPlayerExact(i.getName()));
        }
        class ServerListPingEvent extends org.bukkit.event.server.ServerListPingEvent {
            CraftIconCache icon = minecraftServer.server.getServerIcon();

            ServerListPingEvent() {
                super(((InetSocketAddress) networkManager.getSocketAddress()).getAddress(), minecraftServer.getMotd(), minecraftServer.getPlayerList().getPlayerCount(), minecraftServer.getPlayerList().getMaxPlayers(), playerSampleList);
            }

            @Override
            public void setServerIcon(org.bukkit.util.CachedServerIcon icon) {
                if (!(icon instanceof CraftIconCache)) {
                    throw new IllegalArgumentException(icon + " was not created by " + org.bukkit.craftbukkit.CraftServer.class);
                }
                this.icon = (CraftIconCache) icon;
            }
        }

        ServerListPingEvent event = new ServerListPingEvent();
        this.minecraftServer.server.getPluginManager().callEvent(event);
        ServerPing ping = new ServerPing();
        ping.setFavicon(event.icon.value);
        ping.setMOTD(new ChatComponentText(event.getMotd()));
        ServerPingPlayerSample samplePing = new ServerPingPlayerSample(event.getMaxPlayers(), minecraftServer.getPlayerList().getPlayerCount());
        GameProfile[] playerSample = new GameProfile[event.getPlayerSample().size()];
        for (int i=0; i<event.getPlayerSample().size(); i++) {
            Player p = event.getPlayerSample().get(i);
            playerSample[i] = new GameProfile(p.getUniqueId().toString().replaceAll("-", ""), p.getPlayerListName());
        }
        samplePing.a(playerSample);
        ping.setPlayerSample(samplePing);
        ping.setServerInfo(new ServerPingServerData(minecraftServer.server.getVersion(), 4)); // MAGICAL VALUE FROM MinecraftServer

        this.networkManager.handle(new PacketStatusOutServerInfo(ping), new GenericFutureListener[0]);
        // CraftBukkit end
    }

    public void a(PacketStatusInPing packetstatusinping) {
        this.networkManager.handle(new PacketStatusOutPong(packetstatusinping.c()), new GenericFutureListener[0]);
    }
}
