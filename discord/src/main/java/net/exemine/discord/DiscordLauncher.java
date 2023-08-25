package net.exemine.discord;

import net.exemine.api.controller.ApiController;

public class DiscordLauncher {

    public static void main(String[] args) {
        Discord.get().start();
        ApiController.getInstance().setBooted(true);
    }
}
