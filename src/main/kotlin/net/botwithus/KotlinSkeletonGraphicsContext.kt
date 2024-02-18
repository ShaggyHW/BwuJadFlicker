package net.botwithus

import net.botwithus.rs3.imgui.ImGui
import net.botwithus.rs3.imgui.ImGuiWindowFlag
import net.botwithus.rs3.script.ScriptConsole
import net.botwithus.rs3.script.ScriptGraphicsContext

class KotlinSkeletonGraphicsContext(
    private val script: KotlinSkeleton,
    console: ScriptConsole
) : ScriptGraphicsContext (console) {

    override fun drawSettings() {
        super.drawSettings()
        if (ImGui.Begin("Jad Flicker", ImGuiWindowFlag.None.value)) {
            if (ImGui.BeginTabBar("Jad Flicker", ImGuiWindowFlag.None.value)) {
                if (ImGui.BeginTabItem("Settings", ImGuiWindowFlag.None.value)) {
                    script.debugMode = ImGui.Checkbox("Debug mode", script.debugMode)
                    ImGui.EndTabItem()
                }
                if (ImGui.BeginTabItem("Stats", ImGuiWindowFlag.None.value)) {
                    ImGui.Text("Flicks flicked ${script.flicksFlicked}")
                    ImGui.EndTabItem()
                }
                ImGui.EndTabBar()
            }
            ImGui.End()
        }
    }

    override fun drawOverlay() {
        super.drawOverlay()
    }

}