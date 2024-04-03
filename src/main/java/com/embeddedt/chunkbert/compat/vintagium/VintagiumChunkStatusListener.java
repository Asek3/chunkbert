package com.embeddedt.chunkbert.compat.vintagium;

import com.embeddedt.chunkbert.compat.IChunkStatusListener;
import me.jellysquid.mods.sodium.client.world.ChunkStatusListener;

public class VintagiumChunkStatusListener implements IChunkStatusListener {
    public final ChunkStatusListener delegate;

    public VintagiumChunkStatusListener(ChunkStatusListener delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onChunkAdded(int x, int z) {
        this.delegate.onChunkAdded(x, z);
    }

    @Override
    public void onChunkRemoved(int x, int z) {
        this.delegate.onChunkRemoved(x, z);
    }
}