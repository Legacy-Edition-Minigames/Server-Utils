package net.kyrptonaught.serverutils.mixin.bigstructures;

import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Iterator;
import java.util.Optional;

@Mixin(StructureTemplate.class)
public class StructureMixin {

    @Redirect(method = "spawnEntities", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;next()Ljava/lang/Object;"))
    public <E> E fixPaintings(Iterator<StructureTemplate.StructureEntityInfo> instance) {
        StructureTemplate.StructureEntityInfo nextStructureEntityInfo = instance.next();
        Optional<EntityType<?>> entityType = Registries.ENTITY_TYPE.getOrEmpty(new Identifier(nextStructureEntityInfo.nbt.getString("id")));
        if (entityType.isPresent() && entityType.get().equals(EntityType.PAINTING)) {
            return (E) new StructureTemplate.StructureEntityInfo(Vec3d.of(nextStructureEntityInfo.blockPos), nextStructureEntityInfo.blockPos, nextStructureEntityInfo.nbt);
        }
        return (E) nextStructureEntityInfo;
    }
}
