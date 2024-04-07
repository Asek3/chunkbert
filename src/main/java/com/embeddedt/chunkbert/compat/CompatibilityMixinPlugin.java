package com.embeddedt.chunkbert.compat;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class CompatibilityMixinPlugin implements IMixinConfigPlugin {

    private static boolean vintagiumLoaded;

    static {
        try {
            Class.forName("me.jellysquid.mods.sodium.client.SodiumMixinTweaker");
            vintagiumLoaded = true;
        } catch (NoClassDefFoundError | ClassNotFoundException ignored) {
        }
    }

    @Override
    public void onLoad(String mixinPackage) { }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if(!mixinClassName.contains("vintagium")) {
            return true;
        }

        return vintagiumLoaded;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) { }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }
}