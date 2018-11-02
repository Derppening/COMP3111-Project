package comp3111.webscraper.models;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class TeamMemberInfoTest {
    private static final List<String> NAMES = Arrays.asList(
            "Mak Ching Hang",
            "Wong Yuk Chun",
            "Yeung Yu Ching"
    );

    private static final List<String> ITSC = Arrays.asList(
            "chmakac",
            "ycwongal",
            "ycyeungac"
    );

    private static final List<String> GITHUB = Arrays.asList(
            "Derppening",
            "dipsywong98",
            "kevinCrylz"
    );

    @Test
    public void testTeamMemberInformation() {
        // check size of team member information
        assertSame(3, TeamMemberInfo.allInfo.size());

        // check names are present
        for (String name : NAMES) {
            assertSame(1L, TeamMemberInfo.allInfo
                    .stream()
                    .filter(i -> i.name.equalsIgnoreCase(name))
                    .count());
        }

        // check ITSCs are present
        for (String itsc : ITSC) {
            assertSame(1L, TeamMemberInfo.allInfo
                    .stream()
                    .filter(i -> i.itsc.equals(itsc))
                    .count());
        }
        // check all ITSCs are lower case (i'm not aware of ITSCs with upper cases)
        assertTrue(TeamMemberInfo.allInfo.stream().allMatch(i -> i.itsc.equals(i.itsc.toLowerCase())));

        // check Githubs are present
        for (String github : GITHUB) {
            assertSame(1L, TeamMemberInfo.allInfo
                    .stream()
                    .filter(i -> i.github.equals(github))
                    .count());
        }
    }
}
