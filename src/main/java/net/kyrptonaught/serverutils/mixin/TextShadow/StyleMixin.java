package net.kyrptonaught.serverutils.mixin.TextShadow;

import net.kyrptonaught.serverutils.TextShadow.StyleWShadow;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Style.class)
public class StyleMixin implements StyleWShadow {

    private ShadowType shadowType = ShadowType.DEFAULT;


    @Override
    public StyleWShadow.ShadowType hasShadow() {
        return shadowType;
    }

    @Override
    public void setShadow(ShadowType type) {
        this.shadowType = type;
    }
}
