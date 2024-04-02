package com.embeddedt.chunkbert.compat;

public interface IChunkStatusListener {
    void onChunkAdded(int x, int z);
    void onChunkRemoved(int x, int z);
}