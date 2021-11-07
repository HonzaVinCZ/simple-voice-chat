package de.maxhenkel.voicechat.api.packets;

import net.minecraft.world.phys.Vec3;

/**
 * The receiver of this event will hear the sound from the specified location
 * If the location is further away than the maximum voice distance, the receiving player won't actually hear the sound
 */
public interface LocationalSoundPacket extends SoundPacket {

    /**
     * @return the audio location
     */
    Vec3 getPosition();

}
