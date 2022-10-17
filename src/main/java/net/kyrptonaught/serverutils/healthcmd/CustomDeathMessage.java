package net.kyrptonaught.serverutils.healthcmd;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

public class CustomDeathMessage extends DamageSource {
    private String deathMessage = "";

    public CustomDeathMessage(String deathMessage) {
        super("custom");
        this.deathMessage = deathMessage;
    }

    @Override
    public Text getDeathMessage(LivingEntity entity) {
        MutableText translatableText = (MutableText) super.getDeathMessage(entity);
        return Text.translatable(deathMessage, ((TranslatableTextContent) translatableText.getContent()).getArgs());
    }
}
