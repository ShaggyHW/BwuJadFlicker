package net.botwithus

import net.botwithus.api.game.hud.inventories.Backpack
import net.botwithus.internal.scripts.ScriptDefinition
import net.botwithus.rs3.game.Client
import net.botwithus.rs3.game.actionbar.ActionBar
import net.botwithus.rs3.game.queries.builders.characters.NpcQuery
import net.botwithus.rs3.game.queries.results.ResultSet
import net.botwithus.rs3.game.scene.entities.characters.npc.Npc
import net.botwithus.rs3.game.skills.Skills
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
) : LoopingScript(name, scriptConfig, scriptDefinition) {

	var flicksFlicked: Int = 0
	var debugMode: Boolean = true
	val random: Random = Random()
	var botState: BotState = BotState.FLICKING
	val jadPattern: Pattern = Regex.getPatternForContainsString("Jad")

	var prayerRestorePattern: Pattern = Regex.getPatternForContainsString("restore")
	var prayerPotPattern: Pattern = Regex.getPatternForContainsString("prayer")
	var drinkCD = 0

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
			Execution.delay(random.nextLong(2500, 5500))
			return
		}

		if (player.getPrayerPoints() < Skills.PRAYER.actualLevel * 10 * .5) {
			if (drinkCD <= 0) {
				println("Low prayer, drinking");
				if(!Backpack.interact(prayerRestorePattern, "Drink")){
					Backpack.interact(prayerPotPattern, "Drink")
				}
				drinkCD = 3;
				Execution.delay(random.nextLong(10L, 25L));
				return
			}
		}
		drinkCD--

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
		if (VarManager.getVarbitValue(16768) == 0 && VarManager.getVarbitValue(16745) == 0) {
			val success: Boolean = ActionBar.usePrayer("Deflect Magic") || ActionBar.usePrayer("Protect from Magic")
			println("Switched to magic prayer: $success")
			if (success) {
				flicksFlicked++
				Execution.delay(random.nextLong(1550, 2050))
			}
		} else {
			println("Magic prayer already active")
		}
	}

	private fun handleRangePrayerSwitch() {
		println("Detected range switch, attempting...")
		if (VarManager.getVarbitValue(16769) == 0 && VarManager.getVarbitValue(16746) == 0) {
			val success: Boolean = ActionBar.usePrayer("Deflect Ranged") || ActionBar.usePrayer("Protect from Ranged")
			println("Switched to ranged prayer: $success")
			if (success) {
				flicksFlicked++
				Execution.delay(random.nextLong(1550, 2050))
			}
		} else {
			println("Ranged prayer already active")
		}
	}

}