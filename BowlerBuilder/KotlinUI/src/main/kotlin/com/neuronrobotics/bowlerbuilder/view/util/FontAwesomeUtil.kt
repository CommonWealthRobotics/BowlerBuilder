/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
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
