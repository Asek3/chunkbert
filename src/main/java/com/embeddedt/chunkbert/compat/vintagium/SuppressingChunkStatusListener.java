package com.embeddedt.chunkbert.compat.vintagium;

import me.jellysquid.mods.sodium.client.world.ChunkStatusListener;

public class SuppressingChunkStatusListener implements ChunkStatusListener {
    @Override
    public void onChunkAdded(int x, int z) {
    }

    @Override
    public void onChunkRemoved(int x, int z) {
    }
}