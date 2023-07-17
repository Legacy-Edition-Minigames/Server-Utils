package net.kyrptonaught.serverutils.mixin.customMapLoader;

import net.kyrptonaught.serverutils.customMapLoader.RegistryUnfreezer;
import net.minecraft.registry.SimpleRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SimpleRegistry.class)
public class SimpleRegistryMixin implements RegistryUnfreezer {

    @Shadow
    private boolean frozen;

    @Override
    public void unfreeze() {
        this.frozen = false;
    }
}
