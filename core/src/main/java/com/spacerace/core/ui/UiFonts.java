package com.spacerace.core.ui;

import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

/** Shared FreeType settings for UI text (incl. Polish diacritics). */
public final class UiFonts {

    public static final String CHARSET = FreeTypeFontGenerator.DEFAULT_CHARS
            + "ąćęłńóśźżĄĆĘŁŃÓŚŹŻ";

    private UiFonts() {}

    public static void applyCharset(FreeTypeFontGenerator.FreeTypeFontParameter params) {
        params.characters = CHARSET;
    }
}
