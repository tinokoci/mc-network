package net.exemine.discord.ticket.impl.application.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationData {

    private boolean acceptedTerms;
    private String age;
    private String timeZone;
    private String country;
    private String languages;
    private String availability;
    private String hostOnReddit;
    private String minecraftStaffExperience;
    private String otherStaffExperience;
    private String qualities;
    private String motivation;
}
