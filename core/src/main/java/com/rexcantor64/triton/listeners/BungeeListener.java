package com.rexcantor64.triton.listeners;

import com.rexcantor64.triton.BungeeMLP;
import com.rexcantor64.triton.Triton;
import com.rexcantor64.triton.packetinterceptor.PreLoginBungeeEncoder;
import com.rexcantor64.triton.player.BungeeLanguagePlayer;
import com.rexcantor64.triton.utils.NMSUtils;
import com.rexcantor64.triton.utils.SocketUtils;
import io.netty.channel.Channel;
import lombok.val;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import net.md_5.bungee.netty.PipelineUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class BungeeListener implements Listener {

    @EventHandler
    public void onPlayerJoin(ServerConnectedEvent event) {
        BungeeLanguagePlayer lp = (BungeeLanguagePlayer) Triton.get().getPlayerManager()
                .get(event.getPlayer().getUniqueId());
        Triton.get().getLogger().logTrace("Player %1 connected to a new server", lp);

        Triton.asBungee().getBridgeManager().sendPlayerLanguage(lp, event.getServer());

        if (Triton.get().getConf().isRunLanguageCommandsOnLogin()) {
            lp.executeCommands(event.getServer());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(LoginEvent event) {
        if (event.isCancelled()) return;
        Plugin plugin = Triton.asBungee().getLoader();
        event.registerIntent(plugin);
        Triton.asBungee().getBungeeCord().getScheduler().runAsync(plugin, () -> {
            val lp = new BungeeLanguagePlayer(event.getConnection().getUniqueId(), event.getConnection());
            Triton.get().getPlayerManager().registerPlayer(lp);
            BungeeMLP.asBungee().injectPipeline(lp, event.getConnection());
            event.completeIntent(plugin);
        });
    }

    @EventHandler
    public void onLeave(PlayerDisconnectEvent event) {
        Triton.get().getPlayerManager().unregisterPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = -128)
    public void onPreLogin(PlayerHandshakeEvent event) {
        val ip = SocketUtils.getIpAddress(event.getConnection().getSocketAddress());
        try {
            Object ch = NMSUtils.getDeclaredField(event.getConnection(), "ch");
            Method method = ch.getClass().getDeclaredMethod("getHandle");
            Channel channel = (Channel) method.invoke(ch, new Object[0]);
            channel.pipeline()
                    .addAfter(PipelineUtils.PACKET_ENCODER, "triton-pre-login-encoder",
                            new PreLoginBungeeEncoder(ip));
        } catch (Exception e) {
            Triton.get().getLogger().logError(e, "[PacketInjector] Failed to inject pre-login client connection for %1", ip);
        }
    }

    @EventHandler(priority = 127)
    public void onMotd(ProxyPingEvent event) {
        Plugin plugin = Triton.asBungee().getLoader();

        if (!Triton.get().getConf().isMotd())
            return;

        event.registerIntent(plugin);

        Triton.asBungee().getBungeeCord().getScheduler().runAsync(plugin, () -> {
            val ipAddress = SocketUtils.getIpAddress(event.getConnection().getSocketAddress());
            val lang = Triton.get().getStorage().getLanguageFromIp(ipAddress).getName();
            Triton.get().getLogger().logTrace("Translating MOTD in language '%1' for IP address '%2'", lang, ipAddress);
            val syntax = Triton.get().getConf().getMotdSyntax();

            val players = event.getResponse().getPlayers();
            if (players.getSample() != null) {
                val newSample = new ArrayList<ServerPing.PlayerInfo>();
                for (val playerInfo : players.getSample()) {
                    val translatedName = Triton.get().getLanguageParser()
                            .replaceLanguages(playerInfo.getName(), lang, syntax);
                    if (playerInfo.getName() == null || playerInfo.getName().equals(translatedName)) {
                        newSample.add(playerInfo);
                        continue;
                    }
                    if (translatedName == null) continue; // Disabled line
                    val translatedNameSplit = translatedName.split("\n", -1);
                    if (translatedNameSplit.length > 1) {
                        for (val split : translatedNameSplit) {
                            newSample.add(new ServerPing.PlayerInfo(split, UUID.randomUUID()));
                        }
                    } else {
                        newSample.add(new ServerPing.PlayerInfo(translatedName, playerInfo.getUniqueId()));
                    }
                }
                players.setSample(newSample.toArray(new ServerPing.PlayerInfo[0]));
            }

            val version = event.getResponse().getVersion();
            val translatedVersion = new ServerPing.Protocol(Triton.get().getLanguageParser()
                    .parseString(lang, syntax, version.getName()), version.getProtocol());
            event.getResponse().setVersion(translatedVersion);

            event.getResponse().setDescriptionComponent(componentArrayToSingle(Triton.get().getLanguageParser()
                    .parseComponent(lang, syntax, event.getResponse()
                            .getDescriptionComponent())));
            event.completeIntent(plugin);
        });
    }

    private BaseComponent componentArrayToSingle(BaseComponent... c) {
        if (c.length == 1) return c[0];
        BaseComponent result = new TextComponent("");
        result.setExtra(Arrays.asList(c));
        return result;
    }

}
