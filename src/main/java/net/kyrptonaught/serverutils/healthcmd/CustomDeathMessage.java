package net.kyrptonaught.serverutils.healthcmd;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

public class CustomDeathMessage extends DamageSource {
    private String deathMessage = "";

    public CustomDeathMessage(DynamicRegistryManager registryManager, String deathMessage) {
        super(registryManager.get(RegistryKeys.DAMAGE_TYPE).entryOf(DamageTypes.GENERIC));
        this.deathMessage = deathMessage;
    }

    @Override
    public Text getDeathMessage(LivingEntity entity) {
        MutableText translatableText = (MutableText) super.getDeathMessage(entity);
        return Text.translatable(deathMessage, ((TranslatableTextContent) translatableText.getContent()).getArgs());
    }
}
