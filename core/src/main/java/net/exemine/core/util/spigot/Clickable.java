package net.exemine.core.util.spigot;

import lombok.NoArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
public class Clickable {

    private final List<BaseComponent> components = new ArrayList<>();

    public Clickable(String message, String hover, String clickString) {
        add(message, hover, clickString);
    }

    public Clickable(String message, String hover) {
        add(message, hover);
    }

    public Clickable(String message) {
        add(message);
    }

    public Clickable add(String message, String hover, String clickString) {
        List<BaseComponent> createdComponents = List.of(TextComponent.fromLegacyText(message));

        if (hover != null) {
            createdComponents.forEach(component -> component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create())));
        }
        if (clickString != null) {
            createdComponents.forEach(component -> component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, clickString)));
        }
        components.addAll(createdComponents);
        return this;
    }

    public Clickable add(String message, String clickString) {
        add(message, null, clickString);
        return this;
    }

    public Clickable add(String message) {
        add(message, null, null);
        return this;
    }

    public void send(UUID uuid) {
        if (uuid != null) {
            send(Bukkit.getPlayer(uuid));
        }
    }

    public void send(Player player) {
        player.sendMessage(asComponents());
    }

    public void broadcast() {
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(asComponents()));
    }

    public BaseComponent[] asComponents() {
        return components.toArray(new BaseComponent[0]);
    }
}