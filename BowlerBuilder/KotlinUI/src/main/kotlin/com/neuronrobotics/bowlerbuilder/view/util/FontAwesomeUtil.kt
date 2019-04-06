/*
 * This file is part of BowlerBuilder.
 *
 * BowlerBuilder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BowlerBuilder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with BowlerBuilder.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neuronrobotics.bowlerbuilder.view.util

import com.neuronrobotics.bowlerbuilder.controller.util.BOWLER_ASSET_REPO
import com.neuronrobotics.bowlerbuilder.controller.util.loadBowlerAsset
import javafx.scene.Node
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import org.controlsfx.glyphfont.FontAwesome
import org.controlsfx.glyphfont.Glyph
import org.controlsfx.glyphfont.GlyphFontRegistry

/**
 * Loads an image asset from the [BOWLER_ASSET_REPO].
 *
 * @param filename The name of the file in the repo.
 * @param glyph The fallback [FontAwesome.Glyph] to use if getting the file fails.
 * @return The image asset.
 */
fun loadImageAsset(filename: String, glyph: FontAwesome.Glyph): Node =
    loadBowlerAsset(filename).fold(
        {
            getFontAwesomeGlyph(glyph)
        },
        {
            ImageView(Image(it.toURI().toString()))
        }
    )

fun getFontAwesomeGlyph(glyph: FontAwesome.Glyph): Glyph =
    GlyphFontRegistry.font("FontAwesome").create(glyph)
