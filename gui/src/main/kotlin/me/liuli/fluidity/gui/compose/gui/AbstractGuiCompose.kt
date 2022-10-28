/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.gui.compose.gui

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.pointer.PointerEventType
import me.liuli.fluidity.gui.compose.ComposeManager
import net.minecraft.client.gui.GuiScreen
import net.minecraft.util.ChatAllowedCharacters
import org.jetbrains.skia.Color
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11
import java.awt.Component
import java.awt.event.InputEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import java.awt.event.KeyEvent as AwtKeyEvent

abstract class AbstractGuiCompose(private val backgroundColor: Int = Color.WHITE, private val repeatKeys: Boolean = true, private val waitComposeLoad: Boolean = false) : GuiScreen() {

    lateinit var composeManager: ComposeManager
    protected var hasCompose = false

    // we need to store char data for key release event
    private val pressedKeyMap = mutableMapOf<Int, Char>()

    open fun initCompose(content: @Composable () -> Unit) {
        composeManager = ComposeManager(Display.getWidth(), Display.getHeight(), content, backgroundColor)
        hasCompose = true
    }

    open fun closeCompose() {
        composeManager.finalize()
        hasCompose = false
        pressedKeyMap.clear()
    }

    override fun initGui() {
        if (repeatKeys) {
            Keyboard.enableRepeatEvents(true)
        }
    }

    override fun onGuiClosed() {
        if (repeatKeys) {
            Keyboard.enableRepeatEvents(false)
        }
    }

    protected open fun drawComposeLoadingScreen() {
        if (waitComposeLoad) {
            val width = Display.getWidth()
            val height = Display.getHeight()
            while (!composeManager.hasSuccessRender) {
                composeManager.updateCanvas(width, height)
                Thread.sleep(5L)
            }
            drawComposeCanvas()
        } else {
            drawBackground(0)
        }
    }

    protected open fun drawComposeCanvas() {
        GL11.glEnable(GL11.GL_ALPHA_TEST)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glColor4f(1f, 1f, 1f, 1f)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, composeManager.texId)

        GL11.glBegin(GL11.GL_QUADS)
        GL11.glTexCoord2f(0f, 0f)
        GL11.glVertex2i(0, 0)
        GL11.glTexCoord2f(0f, 1f)
        GL11.glVertex2i(0, height)
        GL11.glTexCoord2f(1f, 1f)
        GL11.glVertex2i(width, height)
        GL11.glTexCoord2f(1f, 0f)
        GL11.glVertex2i(width, 0)
        GL11.glEnd()

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)
        GL11.glDisable(GL11.GL_BLEND)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (hasCompose) {
            composeManager.updateCanvas(Display.getWidth(), Display.getHeight())

            if (!composeManager.hasSuccessRender) {
                this.drawComposeLoadingScreen()
                return
            }

            this.drawComposeCanvas()

            composeManager.sendPointerEvent(
                position = getMousePos(),
                eventType = PointerEventType.Move,
                nativeEvent = MouseEvent(getAwtMods())
            )

            if (Mouse.hasWheel()) {
                val wheel = Mouse.getDWheel()
                if (wheel != 0) {
                    composeManager.sendPointerEvent(
                        eventType = PointerEventType.Scroll,
                        position = getMousePos(),
                        scrollDelta = Offset(0f, -(wheel / 60f)),
                        nativeEvent =  MouseWheelEvent(getAwtMods())
                    )
                }
            }

            // key up
            pressedKeyMap.map { it }.forEach { (key, char) ->
                if (!Keyboard.isKeyDown(key)) {
                    composeManager.sendKeyEvent(KeyEvent(AwtKeyEvent.KEY_RELEASED, System.nanoTime() / 1_000_000, getAwtMods(), remapKeycode(key, char), 0.toChar(), AwtKeyEvent.KEY_LOCATION_STANDARD))
                    pressedKeyMap.remove(key)
                }
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, key: Int) {
        if (hasCompose) {
            composeManager.sendPointerEvent(
                position = getMousePos(),
                eventType = PointerEventType.Press,
                nativeEvent = MouseEvent(getAwtMods())
            )
        }
        super.mouseClicked(mouseX, mouseY, key)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, key: Int) {
        if (hasCompose) {
            composeManager.sendPointerEvent(
                position = getMousePos(),
                eventType = PointerEventType.Release,
                nativeEvent = MouseEvent(getAwtMods())
            )
        }
        super.mouseClicked(mouseX, mouseY, key)
    }
//
    override fun handleKeyboardInput() {
        if (Keyboard.getEventKeyState()) {
            val char = Keyboard.getEventCharacter()
            val key = Keyboard.getEventKey()
            if (hasCompose) {
                val kmod = getAwtMods()
                val time = System.nanoTime() / 1_000_000
                composeManager.sendKeyEvent(KeyEvent(AwtKeyEvent.KEY_PRESSED, time, kmod, remapKeycode(key, char), 0.toChar(), AwtKeyEvent.KEY_LOCATION_STANDARD))
                pressedKeyMap[key] = char
                if (ChatAllowedCharacters.isAllowedCharacter(char)) {
                    composeManager.sendKeyEvent(KeyEvent(AwtKeyEvent.KEY_TYPED, time, kmod, 0, char, AwtKeyEvent.KEY_LOCATION_UNKNOWN))
                }
            }
            keyTyped(char, key) // this need to be handled to make window closeable
        }

        mc.dispatchKeypresses()
    }

    companion object {
        private val _dummy = object : Component() {}

        private fun getMousePos() = Offset(Mouse.getX().toFloat(), Display.getHeight() - Mouse.getY().toFloat())

        private fun getAwtMods(): Int {
            var n = 0
            if (Mouse.isButtonDown(0)) {
                n = n or InputEvent.BUTTON1_DOWN_MASK
            }
            if (Mouse.isButtonDown(2)) {
                n = n or InputEvent.BUTTON2_DOWN_MASK
            }
            if (Mouse.isButtonDown(1)) {
                n = n or InputEvent.BUTTON3_DOWN_MASK
            }
            if (isCtrlKeyDown()) {
                n = n or InputEvent.CTRL_DOWN_MASK
            }
            if (isShiftKeyDown()) {
                n = n or InputEvent.SHIFT_DOWN_MASK
            }
            if (isAltKeyDown()) {
                n = n or InputEvent.ALT_DOWN_MASK
            }
            return n
        }

        private fun remapKeycode(kc: Int, c: Char): Int {
            return when (kc) {
                Keyboard.KEY_ESCAPE -> AwtKeyEvent.VK_ESCAPE
                Keyboard.KEY_1 -> AwtKeyEvent.VK_1
                Keyboard.KEY_2 -> AwtKeyEvent.VK_2
                Keyboard.KEY_3 -> AwtKeyEvent.VK_3
                Keyboard.KEY_4 -> AwtKeyEvent.VK_4
                Keyboard.KEY_5 -> AwtKeyEvent.VK_5
                Keyboard.KEY_6 -> AwtKeyEvent.VK_6
                Keyboard.KEY_7 -> AwtKeyEvent.VK_7
                Keyboard.KEY_8 -> AwtKeyEvent.VK_8
                Keyboard.KEY_9 -> AwtKeyEvent.VK_9
                Keyboard.KEY_0 -> AwtKeyEvent.VK_0
                Keyboard.KEY_MINUS -> AwtKeyEvent.VK_MINUS
                Keyboard.KEY_EQUALS, Keyboard.KEY_NUMPADEQUALS -> AwtKeyEvent.VK_EQUALS
                Keyboard.KEY_BACK -> AwtKeyEvent.VK_BACK_SPACE
                Keyboard.KEY_TAB -> AwtKeyEvent.VK_TAB
                Keyboard.KEY_Q -> AwtKeyEvent.VK_Q
                Keyboard.KEY_W -> AwtKeyEvent.VK_W
                Keyboard.KEY_E -> AwtKeyEvent.VK_E
                Keyboard.KEY_R -> AwtKeyEvent.VK_R
                Keyboard.KEY_T -> AwtKeyEvent.VK_T
                Keyboard.KEY_Y -> AwtKeyEvent.VK_Y
                Keyboard.KEY_U -> AwtKeyEvent.VK_U
                Keyboard.KEY_I -> AwtKeyEvent.VK_I
                Keyboard.KEY_O -> AwtKeyEvent.VK_O
                Keyboard.KEY_P -> AwtKeyEvent.VK_P
                Keyboard.KEY_LBRACKET -> AwtKeyEvent.VK_CLOSE_BRACKET
                Keyboard.KEY_RBRACKET -> AwtKeyEvent.VK_OPEN_BRACKET
                Keyboard.KEY_RETURN, Keyboard.KEY_NUMPADENTER -> AwtKeyEvent.VK_ENTER
                Keyboard.KEY_LCONTROL, Keyboard.KEY_RCONTROL -> AwtKeyEvent.VK_CONTROL
                Keyboard.KEY_A -> AwtKeyEvent.VK_A
                Keyboard.KEY_S -> AwtKeyEvent.VK_S
                Keyboard.KEY_D -> AwtKeyEvent.VK_D
                Keyboard.KEY_F -> AwtKeyEvent.VK_F
                Keyboard.KEY_G -> AwtKeyEvent.VK_G
                Keyboard.KEY_H -> AwtKeyEvent.VK_H
                Keyboard.KEY_J -> AwtKeyEvent.VK_J
                Keyboard.KEY_K -> AwtKeyEvent.VK_K
                Keyboard.KEY_L -> AwtKeyEvent.VK_L
                Keyboard.KEY_SEMICOLON -> AwtKeyEvent.VK_SEMICOLON
                Keyboard.KEY_GRAVE -> AwtKeyEvent.VK_DEAD_GRAVE
                Keyboard.KEY_LSHIFT, Keyboard.KEY_RSHIFT -> AwtKeyEvent.VK_SHIFT
                Keyboard.KEY_BACKSLASH -> AwtKeyEvent.VK_BACK_SLASH
                Keyboard.KEY_Z -> AwtKeyEvent.VK_Z
                Keyboard.KEY_X -> AwtKeyEvent.VK_X
                Keyboard.KEY_C -> AwtKeyEvent.VK_C
                Keyboard.KEY_V -> AwtKeyEvent.VK_V
                Keyboard.KEY_B -> AwtKeyEvent.VK_B
                Keyboard.KEY_N -> AwtKeyEvent.VK_N
                Keyboard.KEY_M -> AwtKeyEvent.VK_M
                Keyboard.KEY_COMMA, Keyboard.KEY_NUMPADCOMMA -> AwtKeyEvent.VK_COMMA
                Keyboard.KEY_PERIOD -> AwtKeyEvent.VK_PERIOD
                Keyboard.KEY_SLASH -> AwtKeyEvent.VK_SLASH
                Keyboard.KEY_MULTIPLY -> AwtKeyEvent.VK_MULTIPLY
                Keyboard.KEY_LMENU, Keyboard.KEY_RMENU -> AwtKeyEvent.VK_ALT
                Keyboard.KEY_SPACE -> AwtKeyEvent.VK_SPACE
                Keyboard.KEY_CAPITAL -> AwtKeyEvent.VK_CAPS_LOCK
                Keyboard.KEY_F1 -> AwtKeyEvent.VK_F1
                Keyboard.KEY_F2 -> AwtKeyEvent.VK_F2
                Keyboard.KEY_F3 -> AwtKeyEvent.VK_F3
                Keyboard.KEY_F4 -> AwtKeyEvent.VK_F4
                Keyboard.KEY_F5 -> AwtKeyEvent.VK_F5
                Keyboard.KEY_F6 -> AwtKeyEvent.VK_F6
                Keyboard.KEY_F7 -> AwtKeyEvent.VK_F7
                Keyboard.KEY_F8 -> AwtKeyEvent.VK_F8
                Keyboard.KEY_F9 -> AwtKeyEvent.VK_F9
                Keyboard.KEY_F10 -> AwtKeyEvent.VK_F10
                Keyboard.KEY_NUMLOCK -> AwtKeyEvent.VK_NUM_LOCK
                Keyboard.KEY_SCROLL -> AwtKeyEvent.VK_SCROLL_LOCK
                Keyboard.KEY_NUMPAD7 -> AwtKeyEvent.VK_NUMPAD7
                Keyboard.KEY_NUMPAD8 -> AwtKeyEvent.VK_NUMPAD8
                Keyboard.KEY_NUMPAD9 -> AwtKeyEvent.VK_NUMPAD9
                Keyboard.KEY_SUBTRACT -> AwtKeyEvent.VK_SUBTRACT
                Keyboard.KEY_NUMPAD4 -> AwtKeyEvent.VK_NUMPAD4
                Keyboard.KEY_NUMPAD5 -> AwtKeyEvent.VK_NUMPAD5
                Keyboard.KEY_NUMPAD6 -> AwtKeyEvent.VK_NUMPAD6
                Keyboard.KEY_ADD -> AwtKeyEvent.VK_ADD
                Keyboard.KEY_NUMPAD1 -> AwtKeyEvent.VK_NUMPAD1
                Keyboard.KEY_NUMPAD2 -> AwtKeyEvent.VK_NUMPAD2
                Keyboard.KEY_NUMPAD3 -> AwtKeyEvent.VK_NUMPAD3
                Keyboard.KEY_NUMPAD0 -> AwtKeyEvent.VK_NUMPAD0
                Keyboard.KEY_DECIMAL -> AwtKeyEvent.VK_DECIMAL
                Keyboard.KEY_F11 -> AwtKeyEvent.VK_F11
                Keyboard.KEY_F12 -> AwtKeyEvent.VK_F12
                Keyboard.KEY_F13 -> AwtKeyEvent.VK_F13
                Keyboard.KEY_F14 -> AwtKeyEvent.VK_F14
                Keyboard.KEY_F15 -> AwtKeyEvent.VK_F15
                Keyboard.KEY_F16 -> AwtKeyEvent.VK_F16
                Keyboard.KEY_F17 -> AwtKeyEvent.VK_F17
                Keyboard.KEY_F18 -> AwtKeyEvent.VK_F18
                Keyboard.KEY_KANA -> AwtKeyEvent.VK_KANA
                Keyboard.KEY_F19 -> AwtKeyEvent.VK_F19
                Keyboard.KEY_CONVERT -> AwtKeyEvent.VK_CONVERT
                Keyboard.KEY_NOCONVERT -> AwtKeyEvent.VK_NONCONVERT
                Keyboard.KEY_YEN -> AwtKeyEvent.VK_DOLLAR
                Keyboard.KEY_CIRCUMFLEX -> AwtKeyEvent.VK_CIRCUMFLEX
                Keyboard.KEY_AT -> AwtKeyEvent.VK_AT
                Keyboard.KEY_COLON -> AwtKeyEvent.VK_COLON
                Keyboard.KEY_UNDERLINE -> AwtKeyEvent.VK_UNDERSCORE
                Keyboard.KEY_KANJI -> AwtKeyEvent.VK_KANJI
                Keyboard.KEY_STOP -> AwtKeyEvent.VK_STOP
                Keyboard.KEY_DIVIDE -> AwtKeyEvent.VK_DIVIDE
                Keyboard.KEY_PAUSE -> AwtKeyEvent.VK_PAUSE
                Keyboard.KEY_HOME -> AwtKeyEvent.VK_HOME
                Keyboard.KEY_UP -> AwtKeyEvent.VK_UP
                Keyboard.KEY_LEFT -> AwtKeyEvent.VK_LEFT
                Keyboard.KEY_RIGHT -> AwtKeyEvent.VK_RIGHT
                Keyboard.KEY_END -> AwtKeyEvent.VK_END
                Keyboard.KEY_DOWN -> AwtKeyEvent.VK_DOWN
                Keyboard.KEY_INSERT -> AwtKeyEvent.VK_INSERT
                Keyboard.KEY_DELETE -> AwtKeyEvent.VK_DELETE
                Keyboard.KEY_CLEAR -> AwtKeyEvent.VK_CLEAR
                else -> c.code
            }
        }

        private fun KeyEvent(awtId: Int, time: Long, awtMods: Int, key: Int, char: Char, location: Int) = KeyEvent(
            AwtKeyEvent(_dummy, awtId, time, awtMods, key, char, location)
        )

        private fun MouseEvent(awtMods: Int) = MouseEvent(_dummy, 0, 0, awtMods, 0, 0, 1, false)

        private fun MouseWheelEvent(awtMods: Int) = MouseWheelEvent(_dummy, 0, 0, awtMods, 0, 0, 1, false, MouseWheelEvent.WHEEL_UNIT_SCROLL, 3, 1)
    }
}