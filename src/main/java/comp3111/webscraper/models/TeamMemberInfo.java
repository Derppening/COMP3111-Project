package comp3111.webscraper.models;

import java.util.Arrays;
import java.util.List;

/**
 * @author Derppening
 *
 * Container class holding team member information.
 */
public class TeamMemberInfo {
    /**
     * List of information for all team members.
     */
    public static final List<TeamMemberInfo> allInfo = Arrays.asList(
            new TeamMemberInfo("MAK Ching Hang", "chmakac", "Derppening"),
            new TeamMemberInfo("WONG Yuk Chun", "ycwongal", "dipsywong98"),
            new TeamMemberInfo("YEUNG Yu Ching", "ycyeungac", "kevinCrylz")
    );

    /**
     * Name of the team member.
     */
    public final String name;
    /**
     * ITSC Account of the team member.
     */
    public final String itsc;
    /**
     * GitHub Account of the team member.
     */
    public final String github;

    /**
     * Private constructor.
     *
     * @param name   Full name.
     * @param itsc   ITSC Account.
     * @param github GitHub account.
     */
    private TeamMemberInfo(String name, String itsc, String github) {
        this.name = name;
        this.itsc = itsc;
        this.github = github;
    }
}
