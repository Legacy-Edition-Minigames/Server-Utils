package net.kyrptonaught.serverutils.customMapLoader.voting.pages;

import net.kyrptonaught.serverutils.customMapLoader.addons.BattleMapAddon;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public record DynamicData(ServerPlayerEntity player, List<BattleMapAddon> addons, String arg) {
}
