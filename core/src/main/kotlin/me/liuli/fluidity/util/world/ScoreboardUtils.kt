package me.liuli.fluidity.util.world

import com.google.common.collect.Iterables
import com.google.common.collect.Lists
import me.liuli.fluidity.util.mc
import net.minecraft.scoreboard.ScoreObjective
import net.minecraft.scoreboard.ScorePlayerTeam


fun getScoreboardCollection(): List<String> {
    val worldScoreboard = mc.theWorld.scoreboard
    var currObjective: ScoreObjective? = null
    val playerTeam = worldScoreboard.getPlayersTeam(mc.thePlayer.name)

    if (playerTeam != null) {
        val colorIndex = playerTeam.chatFormat.colorIndex

        if (colorIndex >= 0)
            currObjective = worldScoreboard.getObjectiveInDisplaySlot(3 + colorIndex)
    }

    val objective = currObjective ?: worldScoreboard.getObjectiveInDisplaySlot(1) ?: return emptyList()

    val scoreboard = objective.scoreboard
    var scoreCollection = scoreboard.getSortedScores(objective)
    val scores = Lists.newArrayList(Iterables.filter(scoreCollection) { input ->
        input?.playerName != null && !input.playerName.startsWith("#")
    })

    return if (scores.size > 15) {
        Lists.newArrayList(Iterables.skip(scores, scoreCollection.size - 15))
    } else {
        scores
    }.map { score ->
        val team = scoreboard.getPlayersTeam(score.playerName)

        ScorePlayerTeam.formatPlayerName(team, score.playerName)
    }
}