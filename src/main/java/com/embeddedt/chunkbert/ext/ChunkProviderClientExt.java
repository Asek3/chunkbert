package com.embeddedt.chunkbert.ext;

import com.embeddedt.chunkbert.compat.IChunkStatusListener;

public interface ChunkProviderClientExt {
    IChunkStatusListener bobby_getListener();
    void bobby_suppressListener();
    IChunkStatusListener bobby_restoreListener();
}