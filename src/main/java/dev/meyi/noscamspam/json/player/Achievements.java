package dev.meyi.noscamspam.json.player;

public class Achievements {

  public int skyblock_excavator; // mining level
  public int skyblock_harvester; // farming level
  public int skyblock_combat; // combat level
  public int skyblock_gatherer; // foraging level
  public int skyblock_angler; // fishing level
  public int skyblock_concoctor; // alchemy level
  public int skyblock_augmentation; // enchanting level
  public int skyblock_domesticator; // taming level

  public int skyblock_dungeoneer; // dungeon level

  public int getSkillAverage() {
    return Math
        .floorDiv(skyblock_excavator + skyblock_harvester + skyblock_combat + skyblock_gatherer
                + skyblock_angler + skyblock_concoctor + skyblock_augmentation + skyblock_domesticator,
            8);
  }
}
