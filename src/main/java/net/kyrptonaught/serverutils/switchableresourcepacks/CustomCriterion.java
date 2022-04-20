package net.kyrptonaught.serverutils.switchableresourcepacks;

import com.google.gson.JsonObject;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;


public class CustomCriterion implements Criterion<CustomCriterion.Conditions> {
    private final Identifier ID;

    public CustomCriterion(String status) {
        ID = new Identifier(SwitchableResourcepacksMod.MOD_ID, status);
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public final void beginTrackingCondition(PlayerAdvancementTracker playerAdvancementTracker, ConditionsContainer<Conditions> conditionsContainer) {
    }

    @Override
    public void endTrackingCondition(PlayerAdvancementTracker playerAdvancementTracker, ConditionsContainer<Conditions> conditionsContainer) {
    }

    @Override
    public void endTracking(PlayerAdvancementTracker playerAdvancementTracker) {
    }


    @Override
    public Conditions conditionsFromJson(JsonObject jsonObject, AdvancementEntityPredicateDeserializer advancementEntityPredicateDeserializer) {
        return new Conditions(this.getId(), EntityPredicate.Extended.getInJson(jsonObject, "player", advancementEntityPredicateDeserializer));
    }


    public void grant(ServerPlayerEntity serverPlayerEntity) {
        serverPlayerEntity.getServer().getAdvancementLoader().getAdvancements().forEach(advancement -> {
            advancement.getCriteria().forEach((s, advancementCriterion) -> {
                if (advancementCriterion.getConditions() != null && advancementCriterion.getConditions().getId().equals(getId()))
                    serverPlayerEntity.getAdvancementTracker().grantCriterion(advancement, s);
            });
        });
    }

    public void revoke(ServerPlayerEntity serverPlayerEntity) {
        serverPlayerEntity.getServer().getAdvancementLoader().getAdvancements().forEach(advancement -> {
            advancement.getCriteria().forEach((s, advancementCriterion) -> {
                if (advancementCriterion.getConditions() != null && advancementCriterion.getConditions().getId().equals(getId()))
                    serverPlayerEntity.getAdvancementTracker().revokeCriterion(advancement, s);
            });
        });
    }

    static class Conditions extends AbstractCriterionConditions {
        public Conditions(Identifier identifier, EntityPredicate.Extended extended) {
            super(identifier, extended);
        }
    }
}