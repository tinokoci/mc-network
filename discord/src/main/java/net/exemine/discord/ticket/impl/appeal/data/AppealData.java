package net.exemine.discord.ticket.impl.appeal.data;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.exemine.api.punishment.PunishmentType;

@Getter
@Setter
@ToString
public class AppealData {

    private boolean guilty;
    private String reasonForPardon;
    private String punishmentReason;
    private PunishmentType punishmentType;
    private String punishmentId;
}
