package net.kyrptonaught.serverutils.healthcmd;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class CustomDeathMessage extends DamageSource {
    private String deathMessage = "";

    public CustomDeathMessage(String deathMessage) {
        super("custom");
        this.deathMessage = deathMessage;
    }

    @Override
    public Text getDeathMessage(LivingEntity entity) {
        TranslatableText translatableText = (TranslatableText) super.getDeathMessage(entity);
        return new TranslatableText(deathMessage, translatableText.getArgs());
    }
}
