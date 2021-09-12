package net.craftium.brander;

import space.arim.dazzleconf.annote.ConfComments;
import space.arim.dazzleconf.annote.ConfDefault.DefaultString;

public interface Config
{
    @DefaultString(" | Playing on <dark_aqua>Craftium.net</dark_aqua>")
    String brand();

    @DefaultString("APPEND")
    @ConfComments("APPEND or REPLACE")
    Mode mode();

    enum Mode
    {
        APPEND, REPLACE
    }
}
