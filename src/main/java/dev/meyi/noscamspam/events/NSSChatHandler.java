package dev.meyi.noscamspam.events;


import dev.meyi.noscamspam.NoScamSpam;
import dev.meyi.noscamspam.checks.UserCheck;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NSSChatHandler {

  Pattern party = Pattern.compile(
      "^-----------------------------\\n(\\[.*] )?(.*) has invited you to join their party!\\nYou have 60 seconds to accept. Click here to join!\\n-----------------------------$");

  @SubscribeEvent
  public void onPartyMessage(ClientChatReceivedEvent event) {
    if (!NoScamSpam.config.apiKey.equals("")) {
      Matcher m = party.matcher(event.message.getUnformattedText());
      if (m.find()) {
        if (UserCheck.shouldBlockUser(m.group(2).toLowerCase())) {
          event.setCanceled(true);
        }
      }
    }
  }
}
