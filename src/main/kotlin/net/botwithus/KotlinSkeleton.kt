package net.botwithus

import net.botwithus.internal.scripts.ScriptDefinition
import net.botwithus.rs3.game.Client
import net.botwithus.rs3.game.actionbar.ActionBar
import net.botwithus.rs3.game.queries.builders.characters.NpcQuery
import net.botwithus.rs3.game.queries.results.ResultSet
import net.botwithus.rs3.game.scene.entities.characters.npc.Npc
import net.botwithus.rs3.game.vars.VarManager
import net.botwithus.rs3.script.Execution
import net.botwithus.rs3.script.LoopingScript
import net.botwithus.rs3.script.config.ScriptConfig
import net.botwithus.rs3.util.Regex
import java.util.*
import java.util.regex.Pattern

class KotlinSkeleton(
    name: String,
    scriptConfig: ScriptConfig,
    scriptDefinition: ScriptDefinition
) : LoopingScript (name, scriptConfig, scriptDefinition) {

    var flicksFlicked: Int = 0
    var debugMode: Boolean = true
    val random: Random = Random()
    var botState: BotState = BotState.FLICKING
    val jadPattern: Pattern = Regex.getPatternForContainsString("Jad")

    enum class BotState {
        IDLE,
        FLICKING,
    }

    override fun initialize(): Boolean {
        super.initialize()
        //Set this to a background script, meaning it can be active alongside others.
        this.isBackgroundScript = true
        //Change loop delay to 600ms from default 100ms
        this.loopDelay = 600
        // Set the script graphics context to our custom one
        this.sgc = KotlinSkeletonGraphicsContext(this, console)
        return true;
    }

    override fun onLoop() {
        //Grab the player from the client
        val player = Client.getLocalPlayer();
        //If the player is null, not logged in, or our bot state is IDLE, wait, do nothing, and return back to onLoop for the next run.
        if (Client.getGameState() != Client.GameState.LOGGED_IN || player == null || botState == BotState.IDLE) {
            Execution.delay(random.nextLong(2500,5500))
            return
        }
        //Query for any Npc using our regex pattern, looking for any Npc containing "Jad"
        val jads: ResultSet<Npc> = NpcQuery.newQuery().name(jadPattern).results()
        var jadNum: Int = 1
        if (jads.isEmpty) {
            if (debugMode)
                println("No jads found.")
            return
        }
        jads.forEach {
            if (debugMode)
                println("Jad number $jadNum anim ID is: ${it.animationId}")
            handlePrayerFlick(it)
            jadNum++;
        }
        return
    }

    private fun handlePrayerFlick(jad: Npc) {
        //Feel free to find the melee attack animID and add support for it.
        if (jad.animationId == 16195) {
            handleMagicPrayerSwitch()
        } else if (jad.animationId == 16202) {
            handleRangePrayerSwitch()
        } else {
            if (debugMode)
                println("No switch required.")
        }
    }

    private fun handleMagicPrayerSwitch() {
        println("Detected magic switch, attempting...")
        if (VarManager.getVarbitValue(16768) == 0) {
            //The varbit is 0, meaning deflect magic is not on, so we should turn it on.
            //Use variable debug to find these varbits, or the scripting-data channel!
            val success: Boolean = ActionBar.usePrayer("Deflect Magic")
            println("Switched to deflect magic:  $success")
            if (success) {
                //Increment the integer tracking our number of flicks for the UI stats tab.
                flicksFlicked++
                //Wait after clicking the prayer so we don't spam it and never turn it on.
                Execution.delay(random.nextLong(1550,2050))
            }
        } else {
            //The varbit is 1, meaning deflect magic is already on.
            //Use variable debug to find these varbits, or the scripting-data channel!
            println("Varbit 16798 was ${VarManager.getVarbitValue(16768)}")
        }
    }

    private fun handleRangePrayerSwitch() {
        println("Detected range switch, attempting...")
        if (VarManager.getVarbitValue(16769) == 0) {
            //The varbit is 0, meaning deflect ranged is not on, so we should turn it on.
            //Use variable debug to find these varbits, or the scripting-data channel!
            val success: Boolean = ActionBar.usePrayer("Deflect Ranged")
            //Log our attempt and true/false result to the script console.
            println("Switched to deflect ranged:  $success")
            if (success) {
                //Increment the integer tracking our number of flicks for the UI stats tab.
                flicksFlicked++
                //Wait after clicking the prayer so we don't spam it and never turn it on.
                Execution.delay(random.nextLong(1550,2050))
            }
        } else {
            //The varbit is 1, meaning deflect ranged is already on.
            //Use variable debug to find these varbits, or the scripting-data channel!
            println("Varbit 16769 was ${VarManager.getVarbitValue(16769)}")
        }
    }

}