package dev.meyi.noscamspam.events;


import dev.meyi.noscamspam.NoScamSpam;
import dev.meyi.noscamspam.json.StatsResponse;
import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NSSChatHandler {

  Pattern party = Pattern.compile(
      "^-----------------------------\\n(\\[.*] )?(.*) has invited you to join their party!\\nYou have 60 seconds to accept. Click here to join!\\n-----------------------------$");
  boolean firstJoin = true;
  private static final CloseableHttpClient httpClient = HttpClientBuilder.create().build();

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onPartyMessage(ClientChatReceivedEvent event) {
    if (!NoScamSpam.config.apiKey.equals("")) {
      Matcher m = party.matcher(event.message.getUnformattedText());
      if (m.find()) {
        String username = m.group(2);
        if (NoScamSpam.config.whitelist.contains(username) || NoScamSpam.cachedAllowed.contains(username))
          return;
        if (NoScamSpam.blacklist.contains(username)) {
          event.setCanceled(true);
          return;
        }

        event.setCanceled(true);
        Thread fetchThread = new Thread(() -> {
          try (CloseableHttpResponse response = httpClient.execute(new HttpGet(
                  "https://api.hypixel.net/player?key=" + NoScamSpam.config.apiKey + "&name=" + username));
               InputStream stream = response.getEntity().getContent()) {
            StatsResponse statsResponse = NoScamSpam.gson
                    .fromJson(IOUtils
                            .toString(stream,
                                    StandardCharsets.UTF_8), StatsResponse.class);

            if (statsResponse.success) {
              if (statsResponse.player.networkExp > NoScamSpam.config.hypixelExperience &&
                      statsResponse.player.achievements.getSkillAverage() > NoScamSpam.config.skillAverage &&
                      statsResponse.player.achievements.skyblock_dungeoneer
                              > NoScamSpam.config.catacombsLevel) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(event.message);
                NoScamSpam.cachedAllowed.add(username);
                ClientChatReceivedEvent newEvent = new ClientChatReceivedEvent((byte) 0, event.message);
                MinecraftForge.EVENT_BUS.post(newEvent);
              } else
                NoScamSpam.blacklist.add(username);
              return;
            }
          } catch (IOException e) {
            e.printStackTrace();
          }

          if (NoScamSpam.config.showOnError) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(event.message);
            ClientChatReceivedEvent newEvent = new ClientChatReceivedEvent((byte) 0, event.message);
            MinecraftForge.EVENT_BUS.post(newEvent);
          }
        });
        fetchThread.start();
      }
    }
  }

  @SubscribeEvent
  public void onPlayerJoinEvent(FMLNetworkEvent.ClientConnectedToServerEvent event) {
    if (firstJoin) {
      firstJoin = false;
      new ScheduledThreadPoolExecutor(1).schedule(() -> {
        try {
          String[] latestTag = ((String) NoScamSpam.gson
              .fromJson(IOUtils.toString(new BufferedReader
                  (new InputStreamReader(
                      HttpClientBuilder.create().build().execute(new HttpGet(
                          "https://api.github.com/repos/symt/NoScamSpam/releases/latest"))
                          .getEntity().getContent()))), Map.class).get("tag_name")).split("\\.");
          String[] currentTag = NoScamSpam.VERSION.split("\\.");

          if (latestTag.length == 3 && currentTag.length == 3) {
            for (int i = 0; i < latestTag.length; i++) {
              int latestCheck = Integer.parseInt(latestTag[i]);
              int currentCheck = Integer.parseInt(currentTag[i]);

              if (latestCheck != currentCheck) {
                if (latestCheck < currentCheck) {
                  Minecraft.getMinecraft().thePlayer.addChatMessage(
                      new ChatComponentText(NoScamSpam.PREFIX + EnumChatFormatting.RED
                          + "This version hasn't been released yet. Please report any bugs that you come across."));
                } else {
                  ChatComponentText updateLink = new ChatComponentText(
                      EnumChatFormatting.DARK_RED + "" + EnumChatFormatting.BOLD
                          + "[UPDATE LINK]");
                  updateLink
                      .setChatStyle(updateLink.getChatStyle().setChatClickEvent(new ClickEvent(
                          Action.OPEN_URL,
                          "https://github.com/symt/NoScamSpam/releases/latest")));
                  Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
                      NoScamSpam.PREFIX + EnumChatFormatting.RED
                          + "The mod version that you're on is outdated. Please update for the best profits: ")
                      .appendSibling(updateLink));
                }
                break;
              }
            }
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }, 3, TimeUnit.SECONDS);
    }
  }
}
