package dev.meyi.noscamspam.checks;

import dev.meyi.noscamspam.NoScamSpam;
import dev.meyi.noscamspam.json.StatsResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class UserCheck {

  private static final CloseableHttpClient httpClient = HttpClientBuilder.create().build();

  public static boolean shouldBlockUser(String username) {
    if (NoScamSpam.config.whitelist.contains(username)) {
      return false;
    } else if (NoScamSpam.blacklist.contains(username)) {
      return true;
    }

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
          return false;
        } else {
          NoScamSpam.blacklist.add(username);
          return true;
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return false;
  }
}
