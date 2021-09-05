package dev.meyi.noscamspam.checks;

import dev.meyi.noscamspam.NoScamSpam;
import dev.meyi.noscamspam.json.StatsResponse;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class UserCheck {

    private static final CloseableHttpClient httpClient = HttpClientBuilder.create().build();

    public static void validateUser(String username, ClientChatReceivedEvent event) {
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
