package net.exemine.hub.provider;

import lombok.RequiredArgsConstructor;
import net.exemine.api.instance.InstanceService;
import net.exemine.api.instance.InstanceType;
import net.exemine.api.punishment.Punishment;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.provider.ScoreboardProvider;
import net.exemine.core.provider.scoreboard.PlayerScoreboard;
import net.exemine.core.util.InstanceUtil;
import net.exemine.hub.user.HubUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@RequiredArgsConstructor
public class HubScoreboardProvider implements ScoreboardProvider<HubUser> {

    private final InstanceService instanceService;
    private final Map<String, SimpleDateFormat> dateFormatMap = new HashMap<>();

    @Override
    public String getTitle() {
        return CC.BOLD_PURPLE + Lang.SERVER_NAME + ' ' + CC.GRAY + '(' + InstanceUtil.getType().getName() + ')';
    }

    @Override
    public void update(HubUser user, PlayerScoreboard<HubUser> board) {
        board.add(CC.RED + CC.STRIKETHROUGH_GRAY + "--------------------");

        if (user.getCoreUser().isPunishedAllowedConnectForLink()) {
            board.add(CC.BOLD_PINK + "Banned");

            Punishment punishment = user.getCoreUser().getPunishmentAllowConnect();
            board.add(Lang.LIST_PREFIX + CC.WHITE + "Reason: " + CC.GOLD + punishment.getAddedReason());
            board.add(Lang.LIST_PREFIX + CC.WHITE + "Duration: " + CC.GOLD + TimeUtil.getNormalDuration(punishment.getDuration()));
        } else {
            board.add(CC.BOLD_PINK + "Info");
            board.add(Lang.LIST_PREFIX + CC.WHITE + "Rank: " + user.getRank().getDisplayName());
            board.add(Lang.LIST_PREFIX + CC.WHITE + "Level: " + CC.GOLD + StringUtil.formatNumber(user.getCoreData().getLevel()));
        }
        board.add();
        board.add(CC.BOLD_PINK + "Servers");
        board.add(Lang.LIST_PREFIX + CC.WHITE + "Global: " + CC.GOLD + instanceService.getOnlinePlayers());
        board.add(Lang.LIST_PREFIX + CC.WHITE + "UHC: " + CC.GOLD + instanceService.getOnlinePlayers(InstanceType.UHC));
        board.add(Lang.LIST_PREFIX + CC.WHITE + "FFA: " + CC.GOLD + instanceService.getOnlinePlayers(InstanceType.FFA));
        board.add();
        board.add(CC.GRAY + getDate(user.getTimeZone()));
        board.add(CC.PURPLE + Lang.WEBSITE);
        board.add(CC.STRIKETHROUGH_GRAY + "--------------------");
        board.update();
    }

    public String getDate(TimeZone timeZone) {
        String timeZoneId = timeZone.getID();
        SimpleDateFormat dateFormat = dateFormatMap.get(timeZoneId);

        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat("dd/MM/yy, hh:mm a") {{
                setTimeZone(timeZone);
            }};
            dateFormatMap.put(timeZoneId, dateFormat);
        }
        return dateFormat.format(new Date());
    }

}
