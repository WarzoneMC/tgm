package com.minehut.tgm.team;

import com.minehut.tgm.user.PlayerContext;

/**
 * Determines what team a player should be assigned
 * immediately after joining the server.
 *
 * The default implementation always selects spectators.
 * A tournament plugin will insert its own controller
 * that assigns their team so a staff member doesn't have
 * to force them into a team.
 */
public interface TeamJoinController {
    MatchTeam determineTeam(PlayerContext playerContext);
}
