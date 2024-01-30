package net.kyrptonaught.LEMBackend;

import com.google.gson.Gson;
import io.javalin.Javalin;
import net.kyrptonaught.LEMBackend.advancements.AdvancementModule;
import net.kyrptonaught.LEMBackend.advancements.AdvancementRouter;
import net.kyrptonaught.LEMBackend.keyValueStorage.KeyValueModule;
import net.kyrptonaught.LEMBackend.keyValueStorage.KeyValueRouter;
import net.kyrptonaught.LEMBackend.linking.LinkRouter;
import net.kyrptonaught.LEMBackend.linking.LinkingModule;
import net.kyrptonaught.LEMBackend.userConfig.UserConfigModule;
import net.kyrptonaught.LEMBackend.userConfig.UserConfigRouter;
import net.kyrptonaught.LEMBackend.whitelistSync.WhitelistModule;
import net.kyrptonaught.LEMBackend.whitelistSync.WhitelistRouter;
import net.kyrptonaught.serverutils.ConfigManager;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;

public class LEMBackend {
    private static final ConfigManager configManager = new ConfigManager("LEMBackend");
    private static ServerConfig config;
    public static Gson gson = ServerUtilsMod.getGson();
    public static Javalin app;
    public static Mod[] modules;
    public static MinecraftServer minecraftServer;

    public static void start(MinecraftServer minecraftServer) {
        LEMBackend.minecraftServer = minecraftServer;
        config = (ServerConfig) configManager.load("LEMBackendConfig", new ServerConfig());
        configManager.save("LEMBackendConfig", config);

        modules = new Mod[]{
                new Mod(new WhitelistModule(), new WhitelistRouter()),
                new Mod(new UserConfigModule(), new UserConfigRouter()),
                new Mod(new LinkingModule(), new LinkRouter()),
                new Mod(new KeyValueModule(), new KeyValueRouter()),
                new Mod(new AdvancementModule(), new AdvancementRouter()),
        };

        app = Javalin.create((javalinConfig) -> {
                    javalinConfig.showJavalinBanner = false;
                    javalinConfig.jsonMapper(new GsonMapper(gson));
                })
                .start(getConfig().port);

        for (Mod module : modules) {
            module.module.load(gson);
            module.router.addRoutes();
        }

        System.out.println("LEMBackend server started");
    }

    public static void shutdown() {
        System.out.println("LEMBackend saving all...");

        app.stop();

        for (Mod module : modules)
            module.module.save(gson);

        System.out.println("LEMBackend all saved");
    }

    public static ServerConfig getConfig() {
        return config;
    }

    public static Path getBaseConfigPath() {
        return configManager.getDir();
    }

    public static boolean secretsMatch(String secret) {
        return getConfig().secretKey.equals(secret);
    }

    public static class Mod {
        Module module;
        ModuleRouter router;

        public Mod(Module module, ModuleRouter router) {
            this.module = module;
            this.router = router;
            this.router.setModule(module);
        }
    }
}
