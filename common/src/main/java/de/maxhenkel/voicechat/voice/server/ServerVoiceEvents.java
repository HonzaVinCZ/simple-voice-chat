package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.net.SecretPacket;
import de.maxhenkel.voicechat.plugins.PluginManager;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ServerVoiceEvents {

    private final Map<UUID, Integer> clientCompatibilities;
    private Server server;

    public ServerVoiceEvents() {
        clientCompatibilities = new ConcurrentHashMap<>();
        PluginManager.instance().init();
        CommonCompatibilityManager.INSTANCE.onServerStarting(this::serverStarting);
        CommonCompatibilityManager.INSTANCE.onPlayerLoggedOut(this::playerLoggedOut);
        CommonCompatibilityManager.INSTANCE.onServerStopping(this::serverStopping);

        CommonCompatibilityManager.INSTANCE.getNetManager().requestSecretChannel.setServerListener((server, player, handler, packet) -> {
            Voicechat.LOGGER.info("Received secret request of {} ({})", player.getDisplayName().getString(), packet.getCompatibilityVersion());
            clientCompatibilities.put(player.getUUID(), packet.getCompatibilityVersion());
            if (packet.getCompatibilityVersion() != Voicechat.COMPATIBILITY_VERSION) {
                Voicechat.LOGGER.warn("Connected client {} has incompatible voice chat version (server={}, client={})", player.getName().getString(), Voicechat.COMPATIBILITY_VERSION, packet.getCompatibilityVersion());
                player.sendMessage(getIncompatibleMessage(packet.getCompatibilityVersion()), Util.NIL_UUID);
            } else {
                initializePlayerConnection(player);
            }
        });
    }

    public Component getIncompatibleMessage(int clientCompatibilityVersion) {
        if (clientCompatibilityVersion <= 6) {
            return new TextComponent("Your voice chat version is not compatible with the servers version.\nPlease install version ")
                    .append(new TextComponent(CommonCompatibilityManager.INSTANCE.getModVersion()).withStyle(ChatFormatting.BOLD))
                    .append(" of ")
                    .append(new TextComponent(CommonCompatibilityManager.INSTANCE.getModName()).withStyle(ChatFormatting.BOLD))
                    .append(".");
        } else {
            return new TranslatableComponent("message.voicechat.incompatible_version",
                    new TextComponent(CommonCompatibilityManager.INSTANCE.getModVersion()).withStyle(ChatFormatting.BOLD),
                    new TextComponent(CommonCompatibilityManager.INSTANCE.getModName()).withStyle(ChatFormatting.BOLD));
        }
    }

    public boolean isCompatible(ServerPlayer player) {
        return clientCompatibilities.getOrDefault(player.getUUID(), -1) == Voicechat.COMPATIBILITY_VERSION;
    }

    public void serverStarting(MinecraftServer mcServer) {
        if (server != null) {
            server.close();
            server = null;
        }

        if (!(mcServer instanceof DedicatedServer) && VoicechatClient.CLIENT_CONFIG != null && !VoicechatClient.CLIENT_CONFIG.runLocalServer.get()) {
            Voicechat.LOGGER.info("Disabling voice chat in singleplayer");
            return;
        }

        try {
            server = new Server(mcServer);
            server.start();
            PluginManager.instance().onServerStarted(mcServer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initializePlayerConnection(ServerPlayer player) {
        if (server == null) {
            return;
        }
        server.getPlayerStateManager().onPlayerCompatibilityCheckSucceeded(player);
        UUID secret = server.getSecret(player.getUUID());
        NetManager.sendToClient(player, new SecretPacket(player, secret, server.getPort(), Voicechat.SERVER_CONFIG));
        Voicechat.LOGGER.info("Sent secret to " + player.getDisplayName().getString());
    }

    public void playerLoggedOut(ServerPlayer player) {
        clientCompatibilities.remove(player.getUUID());
        if (server == null) {
            return;
        }

        server.disconnectClient(player.getUUID());
        Voicechat.LOGGER.info("Disconnecting client " + player.getDisplayName().getString());
    }

    @Nullable
    public Server getServer() {
        return server;
    }

    public void serverStopping(MinecraftServer mcServer) {
        if (server != null) {
            server.close();
            server = null;
        }
    }

}
