package net.kyrptonaught.serverutils.mixin.personatus;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.response.Response;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.net.URL;

@Mixin(YggdrasilAuthenticationService.class)
public interface YggdrasilInvoker {

    @Invoker
    <T extends Response> T invokeMakeRequest(final URL url, final Object input, final Class<T> classOfT) throws AuthenticationException;
}
