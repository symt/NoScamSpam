package dev.meyi.noscamspam;

import com.google.gson.Gson;
import dev.meyi.noscamspam.commands.NoScamSpamCommand;
import dev.meyi.noscamspam.events.NSSChatHandler;
import dev.meyi.noscamspam.json.Config;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = NoScamSpam.MODID, version = NoScamSpam.VERSION)
public class NoScamSpam {

  public static final String MODID = "NoScamSpam";
  public static final String VERSION = "0.1.1";
  public static final Gson gson = new Gson();
  public static final String PREFIX =
      EnumChatFormatting.BLACK + "[" + EnumChatFormatting.RED + "NSS"
          + EnumChatFormatting.BLACK + "]" + EnumChatFormatting.RESET + " ";

  public static Config config;
  public static boolean firstTime = false;
  public static List<String> blacklist = new ArrayList<>();
  private File configFile;

  @Mod.EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    configFile = event.getSuggestedConfigurationFile();
    String configString = null;

    try {
      if (configFile.isFile()) {
        configString = new String(Files.readAllBytes(Paths.get(configFile.getPath())));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (configString != null && Utils.isValidJSONObject(gson, configString)) {
      config = gson.fromJson(configString, Config.class);
    } else {
      config = new Config();
      firstTime = true;
    }
  }

  @EventHandler
  public void init(FMLInitializationEvent event) {
    ClientCommandHandler.instance.registerCommand(new NoScamSpamCommand());
    MinecraftForge.EVENT_BUS.register(new NSSChatHandler());

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> Utils.saveConfigFile(configFile, gson.toJson(config))));
  }
}
