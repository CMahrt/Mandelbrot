package de.cm.mandelproto.io;

import de.cm.mandelproto.math.RenderParameters;

import java.awt.Color;

/**
 * Träger für geladene Fraktal-Rohdaten: Parameter, Iterationsmap (col-major) und Palette.
 */
public record FractalSnapshot(
        RenderParameters params,
        int[][]          iterations,  // [col][row]
        Color[]          palette
) {}
