package dev.meyi.noscamspam.json;

import java.util.ArrayList;
import java.util.List;

public class Config {

  public int hypixelExperience;
  public int hypixelLevel;
  public String apiKey;
  public int skillAverage;
  public int catacombsLevel;

  public List<String> whitelist;
  public boolean showOnError = true;

  public Config() {
    apiKey = "";
    reset();
    whitelist = new ArrayList<>();
  }

  public void reset() {
    hypixelExperience = 617500; // Level 20
    hypixelLevel = 20;
    skillAverage = 10;
    catacombsLevel = 5;
  }
}
