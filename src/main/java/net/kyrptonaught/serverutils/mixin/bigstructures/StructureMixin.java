package net.kyrptonaught.serverutils.mixin.bigstructures;


import net.minecraft.entity.EntityType;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
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
        Optional<EntityType<?>> entityType = Registry.ENTITY_TYPE.getOrEmpty(new Identifier(nextStructureEntityInfo.nbt.getString("id")));
        if (entityType.isPresent() && entityType.get().equals(EntityType.PAINTING)) {
            return (E) new StructureTemplate.StructureEntityInfo(Vec3d.of(nextStructureEntityInfo.blockPos), nextStructureEntityInfo.blockPos, nextStructureEntityInfo.nbt);
        }
        return (E) nextStructureEntityInfo;
    }


    /*
    @Redirect(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/ServerWorldAccess;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
    private boolean forcePlaceNoUpdate(ServerWorldAccess instance, BlockPos blockPos, BlockState blockState, int i) {
        //System.out.println(blockState);
        blockState = Blocks.VINE.getDefaultState();
        return instance.setBlockState(blockPos, blockState, Block.FORCE_STATE);
    }

    @Redirect(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/ServerWorldAccess;updateNeighbors(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;)V"))
    private void forcePlaceNoUpdate(ServerWorldAccess instance, BlockPos blockPos, Block block) {
    }
     */
}
