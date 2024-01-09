package net.kyrptonaught.serverutils.chestTracker;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;

public interface BlockEntityTickerWrapper {

    void server_Utils$wrap(BlockEntityTicker<BlockEntity> ticker);


    void server_Utils$unWrap();
}
