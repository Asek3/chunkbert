package com.embeddedt.chunkbert.mixin.vintagium;

import com.embeddedt.chunkbert.compat.IChunkStatusListener;
import com.embeddedt.chunkbert.compat.vintagium.SuppressingChunkStatusListener;
import com.embeddedt.chunkbert.compat.vintagium.VintagiumChunkStatusListener;
import com.embeddedt.chunkbert.ext.ChunkProviderClientExt;
import me.jellysquid.mods.sodium.client.world.ChunkStatusListener;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ChunkProviderClient.class, priority = 1010) // higher than our normal one
public abstract class VintagiumChunkProviderMixin implements ChunkProviderClientExt {
    /* Shadows the one in Sodium's Mixin */
    private ChunkStatusListener listener;

    /**
     *  We cannot just replace the original listener with null because we need to make Sodium's Mixin believe that it
     *  did actually succeed, otherwise its internal tracking may unload it at a later point in time (e.g. when the
     *  chunk map center changes and Sodium re-evaluates which chunks are now unloaded).
     */
    private final ChunkStatusListener suppressingListener = new SuppressingChunkStatusListener();

    private VintagiumChunkStatusListener wrappedListener;
    private ChunkStatusListener suppressedListener;

    @Override
    public IChunkStatusListener bobby_getListener() {
        if (listener == null || listener == suppressingListener) {
            return null;
        }
        if (wrappedListener == null || wrappedListener.delegate != listener) {
            wrappedListener = new VintagiumChunkStatusListener(listener);
        }
        return wrappedListener;
    }

    @Override
    public void bobby_suppressListener() {
        suppressedListener = listener;
        listener = suppressingListener;
    }

    @Override
    public IChunkStatusListener bobby_restoreListener() {
        if (suppressedListener != null) {
            listener = suppressedListener;
            suppressedListener = null;
            return bobby_getListener();
        } else {
            return null;
        }
    }
}