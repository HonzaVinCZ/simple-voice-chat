package de.maxhenkel.voicechat.api.events;

import de.maxhenkel.voicechat.api.VoicechatSocket;

import javax.annotation.Nullable;

public interface VoicechatServerStartingEvent extends ServerEvent {

    /**
     * Sets a custom implementation of the socket used for voicehcat traffic
     *
     * @param socket the custom socket implementation
     */
    void setSocketImplementation(VoicechatSocket socket);

    /**
     * @return the custom socket implementation or <code>null</code> to use voicechats default one
     */
    @Nullable
    VoicechatSocket getSocketImplementation();

}
