/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.gui.theme

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import io.material.color.quantize.QuantizerCelebi
import io.material.color.scheme.Scheme
import io.material.color.score.Score
import me.liuli.fluidity.gui.compose.component.AsyncImage
import me.liuli.fluidity.gui.theme.background.CommonBackgroundSource
import org.jetbrains.skia.Image
import org.jetbrains.skiko.SystemTheme
import org.jetbrains.skiko.currentSystemTheme
import java.io.BufferedInputStream

object ThemeManager {

    var source = CommonBackgroundSource()
        set(value) {
            field = value
            refreshScheme()
        }
    val imageStream: BufferedInputStream
        get() = source.get()
    var imageBitmap = Image.makeFromEncoded(imageStream.readBytes()).toComposeImageBitmap()
        private set(value) {
            field.asSkiaBitmap().close() // make sure image was closed in skia, preventing memory leak
            field = value
        }

    var darkMode = currentSystemTheme != SystemTheme.LIGHT // prefer dark than light?
        set(value) {
            field = value
            refreshScheme()
        }
    var scheme = generateScheme(imageBitmap)
        private set

    @Composable
    fun background(modifier: Modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            load = { imageBitmap },
            painterFor = { remember { BitmapPainter(it) } },
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }

    /**
     * @param color ARGB color
     */
    fun generateScheme(color: Int, dark: Boolean = darkMode): ColorScheme {
        // function to construct compose color object
        fun col(color: Int) = Color(color)

        val scheme = if (dark) Scheme.dark(color) else Scheme.light(color)
        return ColorScheme(
            primary = col(scheme.primary),
            onPrimary = col(scheme.onPrimary),
            primaryContainer = col(scheme.primaryContainer),
            onPrimaryContainer = col(scheme.onPrimaryContainer),
            inversePrimary = col(scheme.inversePrimary),
            secondary = col(scheme.secondary),
            onSecondary = col(scheme.onSecondary),
            secondaryContainer = col(scheme.secondaryContainer),
            onSecondaryContainer = col(scheme.onSecondaryContainer),
            tertiary = col(scheme.tertiary),
            onTertiary = col(scheme.onTertiary),
            tertiaryContainer = col(scheme.tertiaryContainer),
            onTertiaryContainer = col(scheme.onTertiaryContainer),
            background = col(scheme.background),
            onBackground = col(scheme.onBackground),
            surface = col(scheme.surface),
            onSurface = col(scheme.onSurface),
            surfaceVariant = col(scheme.surfaceVariant),
            onSurfaceVariant = col(scheme.onSurfaceVariant),
            surfaceTint = col(scheme.surface), // TODO: calc this value
            inverseSurface = col(scheme.inverseSurface),
            inverseOnSurface = col(scheme.inverseOnSurface),
            error = col(scheme.error),
            onError = col(scheme.onError),
            errorContainer = col(scheme.errorContainer),
            onErrorContainer = col(scheme.onErrorContainer),
            outline = col(scheme.outline)
        )
    }

    fun generateScheme(img: ImageBitmap): ColorScheme {
        val colors = QuantizerCelebi.quantize(IntArray(img.width * img.height).also { img.readPixels(it) }, 256)
        return generateScheme(Score.score(colors)[0])
    }

    fun refreshScheme() {
        imageBitmap = Image.makeFromEncoded(imageStream.readBytes()).toComposeImageBitmap()
        scheme = generateScheme(imageBitmap)
    }
}