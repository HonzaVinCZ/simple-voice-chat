package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.Entity;
import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.ServerLevel;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.audio.AudioConverter;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.plugins.impl.audio.AudioConverterImpl;
import de.maxhenkel.voicechat.plugins.impl.opus.OpusDecoderImpl;
import de.maxhenkel.voicechat.plugins.impl.opus.OpusEncoderImpl;

import javax.annotation.Nullable;

public class VoicechatApiImpl implements VoicechatApi {

    private static final AudioConverter AUDIO_CONVERTER = new AudioConverterImpl();

    @Nullable
    @Override
    public OpusEncoder createEncoder() {
        return OpusEncoderImpl.create();
    }

    @Nullable
    @Override
    public OpusDecoder createDecoder() {
        return OpusDecoderImpl.create();
    }

    public AudioConverter getAudioConverter() {
        return AUDIO_CONVERTER;
    }

    @Override
    public Entity fromEntity(Object entity) {
        if (entity instanceof net.minecraft.world.entity.Entity e) {
            return new EntityImpl(e);
        } else {
            throw new IllegalArgumentException("entity is not an instance of Entity");
        }
    }

    @Override
    public ServerLevel fromServerLevel(Object serverLevel) {
        if (serverLevel instanceof net.minecraft.server.level.ServerLevel l) {
            return new ServerLevelImpl(l);
        } else {
            throw new IllegalArgumentException("serverLevel is not an instance of ServerLevel");
        }
    }

    @Override
    public Position createPosition(double x, double y, double z) {
        return new PositionImpl(x, y, z);
    }

}
