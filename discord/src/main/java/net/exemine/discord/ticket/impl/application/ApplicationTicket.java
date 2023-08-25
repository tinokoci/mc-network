package net.exemine.discord.ticket.impl.application;

import lombok.Getter;
import lombok.Setter;
import net.exemine.discord.ticket.Ticket;
import net.exemine.discord.ticket.impl.application.data.ApplicationData;
import net.exemine.discord.ticket.impl.application.stage.ApplicationStage;

@Getter
@Setter
public class ApplicationTicket extends Ticket {

    private final ApplicationData data = new ApplicationData();
    private ApplicationStage stage = ApplicationStage.TERMS;
}
