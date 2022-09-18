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
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import java.awt.event.KeyEvent as AwtKeyEvent

abstract class AbstractGuiCompose(private val backgroundColor: Int = Color.WHITE, private val repeatKeys: Boolean = true) : GuiScreen() {

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

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (hasCompose) {
            composeManager.updateCanvas(Display.getWidth(), Display.getHeight())

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

            composeManager.sendPointerEvent(
                position = getMousePos(),
                eventType = PointerEventType.Move,
                nativeEvent = MouseEvent(mouseModifiers())
            )

            if (Mouse.hasWheel()) {
                val wheel = Mouse.getDWheel()
                if (wheel != 0) {
                    composeManager.sendPointerEvent(
                        eventType = PointerEventType.Scroll,
                        position = getMousePos(),
                        scrollDelta = Offset(0f, -(wheel / 60f)),
                        nativeEvent =  MouseWheelEvent(mouseModifiers())
                    )
                }
            }

            // key up
            pressedKeyMap.map { it }.forEach { (key, char) ->
                if (!Keyboard.isKeyDown(key)) {
                    composeManager.sendKeyEvent(KeyEvent(AwtKeyEvent.KEY_RELEASED, System.nanoTime() / 1_000_000, keyModifiers(), remapKeycode(key, char), 0.toChar(), AwtKeyEvent.KEY_LOCATION_STANDARD))
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
                nativeEvent = MouseEvent(mouseModifiers())
            )
        }
        super.mouseClicked(mouseX, mouseY, key)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, key: Int) {
        if (hasCompose) {
            composeManager.sendPointerEvent(
                position = getMousePos(),
                eventType = PointerEventType.Release,
                nativeEvent = MouseEvent(mouseModifiers())
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
                val kmod = keyModifiers()
                val time = System.nanoTime() / 1_000_000

                // Note that we don't distinguish between Left/Right Shift, Del from numpad or not, etc.
                // To distinguish we should change `location` parameter
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

        private fun keyModifiers(mod: Int = 0): Int {
            var n = mod
            if (isCtrlKeyDown()) {
                n = n or 0x80
            }
            if (isShiftKeyDown()) {
                n = n or 0x40
            }
            if (isAltKeyDown()) {
                n = n or 0x200
            }
            return n
        }

        private fun mouseModifiers(mod: Int = 0): Int {
            var n = mod
            if (Mouse.isButtonDown(0)) {
                n = n or 0x400
            }
            if (Mouse.isButtonDown(2)) {
                n = n or 0x800
            }
            if (Mouse.isButtonDown(1)) {
                n = n or 0x1000
            }
            return n
        }

        /**
         * fill the gap between LWJGL and AWT key codes
         * https://stackoverflow.com/questions/15313469/java-keyboard-keycodes-list/31637206
         */
        private fun remapKeycode(kc: Int, c: Char): Int {
            return when (kc) {
                Keyboard.KEY_BACK -> 8
                Keyboard.KEY_DELETE -> 127
                Keyboard.KEY_RETURN -> 10
                Keyboard.KEY_ESCAPE -> 27
                Keyboard.KEY_LEFT -> 37
                Keyboard.KEY_UP -> 38
                Keyboard.KEY_RIGHT -> 39
                Keyboard.KEY_DOWN -> 40
                Keyboard.KEY_TAB -> 9
                Keyboard.KEY_END -> 35
                Keyboard.KEY_HOME -> 36
                Keyboard.KEY_LSHIFT, Keyboard.KEY_RSHIFT -> 16
                Keyboard.KEY_LCONTROL, Keyboard.KEY_RCONTROL -> 17
                Keyboard.KEY_LMENU, Keyboard.KEY_RMENU -> 18
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