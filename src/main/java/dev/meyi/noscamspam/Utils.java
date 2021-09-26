package dev.meyi.noscamspam;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dev.meyi.noscamspam.json.StatsResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

public class Utils {

  @SuppressWarnings("ResultOfMethodCallIgnored")
  public static void saveConfigFile(File configFile, String toSave) {
    try {
      if (!configFile.isFile()) {
        configFile.createNewFile();
      }
      Files.write(Paths.get(configFile.getAbsolutePath()),
          toSave.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static boolean isValidJSONObject(Gson gson, String json) {
    try {
      gson.fromJson(json, Object.class);
    } catch (JsonSyntaxException e) {
      return false;
    }
    return true;
  }

  public static boolean isInteger(String s) {
    return isInteger(s, 10);
  }

  public static boolean isInteger(String s, int radix) {
    if (s.isEmpty()) {
      return false;
    }
    for (int i = 0; i < s.length(); i++) {
      if (i == 0 && s.charAt(i) == '-') {
        if (s.length() == 1) {
          return false;
        } else {
          continue;
        }
      }
      if (Character.digit(s.charAt(i), radix) < 0) {
        return false;
      }
    }
    return true;
  }

  public static int getExperienceFromLevel(int level) {
    return (1250 * (level - 2) + 10000) * (level - 1);
  }

  public static boolean validateApiKey(String key) throws IOException {
    return NoScamSpam.gson.fromJson(new BufferedReader
        (new InputStreamReader(
            HttpClientBuilder.create().build().execute(new HttpGet(
                "https://api.hypixel.net/key?key=" + key)).getEntity()
                .getContent())), StatsResponse.class).success;
  }
}
