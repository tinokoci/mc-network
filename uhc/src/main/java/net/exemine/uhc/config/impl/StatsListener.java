package net.exemine.uhc.config.impl;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.util.Executor;
import net.exemine.uhc.config.ConfigListener;
import net.exemine.uhc.game.event.GameEndEvent;
import net.exemine.uhc.user.info.GameInfo;
import org.bukkit.event.EventHandler;

public class StatsListener extends ConfigListener {

    @EventHandler
    public void onGameEnd(GameEndEvent event) {
        Executor.schedule(() -> {
            plugin.getUserService().values()
                    .stream()
                    .filter(user -> user.getGameInfo().isPlayed())
                    .forEach(user -> {
                        GameInfo info = user.getGameInfo();
                        UHCData data = user.getData();

                        // Combat Stats
                        data.getElo().add(info.getEloGained().getValue());
                        if (info.isWinner()) {
                            data.getWins().increment();
                        }
                        data.getKills().add(info.getKills().getValue());

                        if (info.isDied()) {
                            data.getDeaths().increment();
                        }
                        data.getSwordHits().add(info.getSwordHits().getValue());
                        data.getLandedSwordHits().add(info.getLandedSwordHits().getValue());
                        data.getArrowShots().add(info.getArrowShots().getValue());
                        data.getLandedArrowShots().add(info.getLandedArrowShots().getValue());

                        // Mining Stats
                        data.getMinedDiamonds().add(info.getMinedDiamonds().getValue());
                        data.getMinedGold().add(info.getMinedGold().getValue());
                        data.getMinedIron().add(info.getMinedIron().getValue());
                        data.getMinedRedstone().add(info.getMinedRedstone().getValue());
                        data.getMinedLapis().add(info.getMinedLapis().getValue());
                        data.getMinedCoal().add(info.getMinedCoal().getValue());
                        data.getMinedQuartz().add(info.getMinedQuartz().getValue());

                        // Other Stats
                        if (info.isPlayed()) {
                            data.getGamesPlayed().increment();
                        }
                        if (info.isDiedInTop5()) {
                            data.getTop5s().increment();
                        }
                        if (info.isCarriedToVictory()) {
                            data.getCarriedWins().increment();
                        }
                        data.getLevelsEarned().add(info.getLevelsEarned().getValue());
                        data.getNethersEntered().add(info.getNethersEntered().getValue());

                        user.saveData(false);
                    });
        }).runAsync();

    }
}
