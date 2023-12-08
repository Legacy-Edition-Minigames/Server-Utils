package net.kyrptonaught.serverutils.mixin.serverTranslator;

import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerPlayerEntity.class)
public interface ServerPlayerEntityLanguageAccessor {

    @Accessor
    String getLanguage();

}
