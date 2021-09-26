package dev.meyi.noscamspam.commands;

import dev.meyi.noscamspam.NoScamSpam;
import dev.meyi.noscamspam.Utils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class NoScamSpamCommand extends CommandBase {

  @Override
  public List<String> getCommandAliases() {
    return new ArrayList<String>() {
      {
        add("nss");
      }
    };
  }

  @Override
  public String getCommandName() {
    return "noscamspam";
  }

  @Override
  public String getCommandUsage(ICommandSender sender) {
    return "/noscamspam [subcommand]";
  }

  @Override
  public void processCommand(ICommandSender ics, String[] args) throws NumberInvalidException {
    if (ics instanceof EntityPlayer) {
      EntityPlayer player = (EntityPlayer) ics;

      if (args.length >= 1 && args[0].equalsIgnoreCase("check")) {
        if (args.length == 3 && Utils.isInteger(args[2])) {
          int setting = parseInt(args[2]);
          switch (args[1].toLowerCase()) {
            case "skill":
              NoScamSpam.config.skillAverage = setting;
              break;
            case "cata":
              NoScamSpam.config.catacombsLevel = setting;
              break;
            case "network":
              NoScamSpam.config.hypixelLevel = setting;
              NoScamSpam.config.hypixelExperience = Utils.getExperienceFromLevel(setting);
              break;
            default:
              return;
          }
          player.addChatMessage(new ChatComponentText(
              NoScamSpam.PREFIX + EnumChatFormatting.GREEN + "Setting "
                  + EnumChatFormatting.DARK_GREEN + args[1].toLowerCase()
                  + EnumChatFormatting.GREEN + " has been set to " + EnumChatFormatting.DARK_GREEN
                  + args[2] + EnumChatFormatting.GREEN + "."));
          NoScamSpam.cachedAllowed.clear();
          NoScamSpam.blacklist.clear();
        } else if (args.length == 2 && args[1].equalsIgnoreCase("reset")) {
          NoScamSpam.config.reset();
          player.addChatMessage(new ChatComponentText(
              NoScamSpam.PREFIX + EnumChatFormatting.GREEN + "Your boundaries have been reset."));
        } else if (args.length == 1) {
          player.addChatMessage(new ChatComponentText(
              NoScamSpam.PREFIX + EnumChatFormatting.GREEN + "Settings\n"
                  + EnumChatFormatting.DARK_GREEN + EnumChatFormatting.BOLD
                  + " - " + EnumChatFormatting.GREEN + "Skill Average: " + EnumChatFormatting.WHITE
                  + EnumChatFormatting.BOLD
                  + NoScamSpam.config.skillAverage + "\n" + EnumChatFormatting.DARK_GREEN
                  + EnumChatFormatting.BOLD
                  + " - " + EnumChatFormatting.GREEN + "Catacombs Level: "
                  + EnumChatFormatting.WHITE
                  + EnumChatFormatting.BOLD
                  + NoScamSpam.config.catacombsLevel + "\n" + EnumChatFormatting.DARK_GREEN
                  + EnumChatFormatting.BOLD
                  + " - " + EnumChatFormatting.GREEN + "Network Level: " + EnumChatFormatting.WHITE
                  + EnumChatFormatting.BOLD
                  + NoScamSpam.config.hypixelLevel));
        } else {
          player.addChatMessage(getBoundsUsage());
        }
      } else if (args.length >= 1 && args[0].equalsIgnoreCase("whitelist")) {
        if (args.length == 3 && args[1].equalsIgnoreCase("add")) {
          NoScamSpam.blacklist.remove(args[2]);
          if (NoScamSpam.config.whitelist.contains(args[2].toLowerCase())) {
            player.addChatMessage(new ChatComponentText(
                NoScamSpam.PREFIX + EnumChatFormatting.RED + "User already in whitelist."));
          } else {
            NoScamSpam.config.whitelist.add(args[2].toLowerCase());
            player.addChatMessage(new ChatComponentText(
                NoScamSpam.PREFIX + EnumChatFormatting.GREEN + "User added to whitelist."));
          }
        } else if (args.length == 3 && args[1].equalsIgnoreCase("remove")) {
          if (NoScamSpam.config.whitelist.remove(args[2].toLowerCase())) {
            player.addChatMessage(new ChatComponentText(
                NoScamSpam.PREFIX + EnumChatFormatting.GREEN + "User removed from whitelist."));
          } else {
            player.addChatMessage(new ChatComponentText(
                NoScamSpam.PREFIX + EnumChatFormatting.RED + "User not found in whitelist."));
          }
        } else if (args.length == 2 && args[1].equalsIgnoreCase("reset")) {
          NoScamSpam.config.whitelist.clear();
          player.addChatMessage(new ChatComponentText(
              NoScamSpam.PREFIX + EnumChatFormatting.GREEN + "Whitelist is now empty."));
        } else {
          if (NoScamSpam.config.whitelist.size() > 0) {
            StringBuilder out = new StringBuilder();
            for (int i = 0; i < NoScamSpam.config.whitelist.size(); i++) {
              out.append(EnumChatFormatting.DARK_GREEN).append(EnumChatFormatting.BOLD)
                  .append(i + 1)
                  .append(". ").append(EnumChatFormatting.GREEN)
                  .append(NoScamSpam.config.whitelist.get(i)).append("\n");
            }
            player.addChatMessage(new ChatComponentText(
                NoScamSpam.PREFIX + EnumChatFormatting.GREEN + "User Whitelist\n" + out
                    .toString()));
          } else {
            player.addChatMessage(new ChatComponentText(
                NoScamSpam.PREFIX + EnumChatFormatting.GREEN + "Whitelist is empty."));
          }
        }
      } else if (args.length >= 1 && args[0].equalsIgnoreCase("api")) {
        if (args.length == 2) {
          NoScamSpam.config.apiKey = "";
          try {
            if (Utils.validateApiKey(args[1])) {
              player.addChatMessage(new ChatComponentText(
                  NoScamSpam.PREFIX + EnumChatFormatting.RED
                      + "Your api key has been set."));
              NoScamSpam.config.apiKey = args[1];
            } else {
              player.addChatMessage(new ChatComponentText(
                  NoScamSpam.PREFIX + EnumChatFormatting.RED
                      + "Your api key is invalid. Please run /api new to get a fresh api key & use that in /nss api (key)"));
            }
          } catch (IOException e) {
            player.addChatMessage(new ChatComponentText(
                NoScamSpam.PREFIX + EnumChatFormatting.RED
                    + "An error occurred when trying to set your api key. Please re-run the command to try again."));
            e.printStackTrace();
          }
        } else {
          player.addChatMessage(new ChatComponentText(
              NoScamSpam.PREFIX + EnumChatFormatting.RED
                  + "Run /nss api (key) to set your api key. Do /api if you need to get your api key."));
        }
      } else if (args.length == 1 && args[0].equalsIgnoreCase("error")) {
        NoScamSpam.config.showOnError ^= true;
        player.addChatMessage(new ChatComponentText(
            NoScamSpam.PREFIX + (NoScamSpam.config.showOnError ? EnumChatFormatting.GREEN
                : EnumChatFormatting.RED)
                + "On API errors, the party invite will be " +
                (NoScamSpam.config.showOnError ? EnumChatFormatting.DARK_GREEN
                    + "" + EnumChatFormatting.BOLD + "SHOWN"
                    : EnumChatFormatting.DARK_RED + "" + EnumChatFormatting.BOLD + "HIDDEN")
        ));
      } else {
        player.addChatMessage(new ChatComponentText(
            NoScamSpam.PREFIX + EnumChatFormatting.GREEN + "Help\n" + EnumChatFormatting.DARK_GREEN
                + EnumChatFormatting.BOLD
                + " - " + EnumChatFormatting.GREEN + "check (skill|cata|network) (number)" + "\n"
                + EnumChatFormatting.DARK_GREEN + EnumChatFormatting.BOLD
                + " - " + EnumChatFormatting.GREEN + "whitelist (add|remove|reset) (username)"
                + "\n" + EnumChatFormatting.DARK_GREEN + EnumChatFormatting.BOLD
                + " - " + EnumChatFormatting.GREEN + "api (key)"
                + "\n" + EnumChatFormatting.DARK_GREEN + EnumChatFormatting.BOLD
                + " - " + EnumChatFormatting.GREEN + "error"));
      }
    }
  }

  public ChatComponentText getBoundsUsage() {
    return new ChatComponentText(
        NoScamSpam.PREFIX + EnumChatFormatting.RED + "Usage: " + EnumChatFormatting.DARK_RED
            + "/nss check (skill|cata|network) (number)");
  }

  public boolean canCommandSenderUseCommand(final ICommandSender sender) {
    return true;
  }
}


