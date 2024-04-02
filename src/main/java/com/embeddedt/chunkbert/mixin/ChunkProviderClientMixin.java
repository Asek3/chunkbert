package com.embeddedt.chunkbert.mixin;

import com.embeddedt.chunkbert.ChunkbertConfig;
import com.embeddedt.chunkbert.FakeChunkManager;
import com.embeddedt.chunkbert.FakeChunkStorage;
import com.embeddedt.chunkbert.compat.IChunkStatusListener;
import com.embeddedt.chunkbert.ext.ChunkProviderClientExt;
import com.embeddedt.chunkbert.ext.IChunkProviderClient;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(ChunkProviderClient.class)
public abstract class ChunkProviderClientMixin implements IChunkProviderClient, ChunkProviderClientExt {
    @Shadow public abstract Chunk getLoadedChunk(int x, int z);
    @Shadow @Final private Chunk blankChunk;
    @Shadow @Final private World world;
    @Nullable
    protected FakeChunkManager bobbyChunkManager = null;
    // Cache of chunk which was just unloaded so we can immediately
    // load it again without having to wait for the storage io worker.
    protected @Nullable NBTTagCompound bobbyChunkReplacement;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void bobbyInit(World worldIn, CallbackInfo ci) {
        if(ChunkbertConfig.enabled)
            bobbyChunkManager = new FakeChunkManager((WorldClient)worldIn, (ChunkProviderClient) (Object) this);
    }

    @Nullable
    @Override
    public FakeChunkManager getBobbyChunkManager() {
        return bobbyChunkManager;
    }

    @Override
    public IChunkStatusListener bobby_getListener() {
        return null;
    }

    @Override
    public void bobby_suppressListener() {
    }

    @Override
    public IChunkStatusListener bobby_restoreListener() {
        return null;
    }

    @Inject(method = "provideChunk", at = @At("RETURN"), cancellable = true)
    private void bobbyGetChunk(int x, int z, CallbackInfoReturnable<Chunk> ci) {
        // Did we find a live chunk?
        if (ci.getReturnValue() != blankChunk) {
            return;
        }

        if (bobbyChunkManager == null) {
            return;
        }

        // Otherwise, see if we've got one
        Chunk chunk = bobbyChunkManager.getChunk(x, z);
        if (chunk != null) {
            ci.setReturnValue(chunk);
        }
    }

    @Inject(method = "loadChunk", at = @At("HEAD"))
    private void bobbyUnloadFakeChunk(int x, int z, CallbackInfoReturnable<Chunk> cir) {
        if (bobbyChunkManager == null) {
            return;
        }

        if (bobbyChunkManager.getChunk(x, z) != null) {
            // We'll be replacing a fake chunk with a real one.
            // Suppress the chunk status listener so the chunk mesh does
            // not get removed before it is re-rendered.
            bobby_suppressListener();
        }

        // This needs to be called unconditionally because even if there is no chunk loaded at the moment,
        // we might already have one queued which we need to cancel as otherwise it will overwrite the real one later.
        bobbyChunkManager.unload(x, z, true);
    }

    @Inject(method = "loadChunk", at = @At("RETURN"))
    private void bobbyFakeChunkReplaced(int x, int z, CallbackInfoReturnable<Chunk> cir) {
        IChunkStatusListener listener = bobby_restoreListener();
        if (listener != null) {
            // However, if we failed to load the chunk from the packet for whatever reason,
            // we need to notify the listener that the chunk has indeed been unloaded.
            if (getLoadedChunk(x, z) == null) {
                listener.onChunkRemoved(x, z);
            }
        }
    }

    @Inject(method = "unloadChunk", at = @At("HEAD"))
    private void bobbySaveChunk(int chunkX, int chunkZ, CallbackInfo ci) {
        if (bobbyChunkManager == null) {
            return;
        }

        Chunk chunk = world.getChunkProvider().getLoadedChunk(chunkX, chunkZ);
        if (chunk == null) {
            return;
        }

        // We'll be replacing a fake chunk with a real one.
        // Suppress the chunk status listener so the chunk mesh does
        // not get removed before it is re-rendered.
        bobby_suppressListener();

        FakeChunkStorage storage = bobbyChunkManager.getStorage();
        NBTTagCompound tag = storage.serialize(chunk);
        storage.save(chunk.getPos(), tag);
        bobbyChunkReplacement = tag;

        bobby_restoreListener();
    }

    @Inject(method = "unloadChunk", at = @At("RETURN"))
    private void bobbyReplaceChunk(int chunkX, int chunkZ, CallbackInfo ci) {
        if (bobbyChunkManager == null) {
            return;
        }

        NBTTagCompound tag = bobbyChunkReplacement;
        bobbyChunkReplacement = null;
        if (tag == null) {
            return;
        }
        bobbyChunkManager.load(chunkX, chunkZ, tag, bobbyChunkManager.getStorage());
    }

    @Inject(method = "makeString", at = @At("RETURN"), cancellable = true)
    private void bobbyDebugString(CallbackInfoReturnable<String> cir) {
        if (bobbyChunkManager == null) {
            return;
        }

        cir.setReturnValue(cir.getReturnValue() + " " + bobbyChunkManager.getDebugString());
    }
}
