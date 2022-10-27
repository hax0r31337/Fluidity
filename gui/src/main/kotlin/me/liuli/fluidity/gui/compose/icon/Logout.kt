/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.gui.compose.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

public val Icons.TwoTone.Logout: ImageVector
    get() {
        if (_logout != null) {
            return _logout!!
        }
        _logout = materialIcon(name = "TwoTone.Logout") {
            materialPath {
                moveTo(5.0f, 5.0f)
                horizontalLineToRelative(7.0f)
                verticalLineTo(3.0f)
                horizontalLineTo(5.0f)
                curveTo(3.9f, 3.0f, 3.0f, 3.9f, 3.0f, 5.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(7.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(5.0f)
                close()
                moveTo(21.0f, 12.0f)
                lineToRelative(-4.0f, -4.0f)
                verticalLineToRelative(3.0f)
                horizontalLineTo(9.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(3.0f)
                lineTo(21.0f, 12.0f)
                close()
            }
        }
        return _logout!!
    }

private var _logout: ImageVector? = null