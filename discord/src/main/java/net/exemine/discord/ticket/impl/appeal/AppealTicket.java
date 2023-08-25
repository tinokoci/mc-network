package net.exemine.discord.ticket.impl.appeal;

import lombok.Getter;
import lombok.Setter;
import net.exemine.discord.ticket.Ticket;
import net.exemine.discord.ticket.impl.appeal.data.AppealData;
import net.exemine.discord.ticket.impl.appeal.level.AppealLevel;
import net.exemine.discord.ticket.impl.appeal.stage.AppealStage;

@Getter
@Setter
public class AppealTicket extends Ticket {

    public final AppealData data = new AppealData();
    private AppealLevel level = AppealLevel.STAFF;
    private AppealStage stage = AppealStage.GUILTY;
}