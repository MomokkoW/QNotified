/*
 * QNotified - An Xposed module for QQ/TIM
 * Copyright (C) 2019-2021 dmca@ioctl.cc
 * https://github.com/ferredoxin/QNotified
 *
 * This software is non-free but opensource software: you can redistribute it
 * and/or modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either
 * version 3 of the License, or any later version and our eula as published
 * by ferredoxin.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * and eula along with this software.  If not, see
 * <https://www.gnu.org/licenses/>
 * <https://github.com/ferredoxin/QNotified/blob/master/LICENSE.md>.
 */
package ltd.nextalone.util

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import me.singleneuron.qn_kernel.data.hostInfo

internal val linearParams = LinearLayout.LayoutParams(0, 0)
internal val relativeParams = RelativeLayout.LayoutParams(0, 0)

internal fun View.hide() {
    this.visibility = View.GONE
    val viewGroup = this.parent as ViewGroup
    if (viewGroup is LinearLayout) {
        this.layoutParams = linearParams
    } else if (viewGroup is RelativeLayout) {
        this.layoutParams = relativeParams
    }
}

internal fun Any.hostId(name: String): Int? {
    return when (this) {
        is View -> this.resources.getIdentifier(name, "id", hostInfo.packageName)
        is Context -> this.resources.getIdentifier(name, "id", hostInfo.packageName)
        else -> null
    }
}

internal fun <T : View?> Any.findHostView(name: String): T? {
    return when (this) {
        is View -> this.hostId(name)?.let { this.findViewById<T>(it) }
        is Activity -> this.hostId(name)?.let { this.findViewById<T>(it) }
        is Dialog -> this.hostId(name)?.let { this.findViewById<T>(it) }
        else -> null
    }
}
