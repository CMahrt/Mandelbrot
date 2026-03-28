# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./gradlew build

# Run the application
./gradlew run

# Run tests
./gradlew test

# Clean
./gradlew clean
```

Java 21 toolchain is configured. Gradle wrapper version is 8.9.

## Architecture

This is a Mandelbrot set fractal visualizer built with Java Swing. The user can explore the fractal by clicking to zoom into regions or inputting custom coordinates.

**Package structure:** `de.cm.mandelproto`
- `App` — entry point; creates `MainFrame`
- `gui/` — Swing UI components
- `math/` — complex number arithmetic and Mandelbrot iteration logic
- `graphics/` — pixel rendering to `BufferedImage`

### Math layer

- `ComplexNumber` — immutable complex arithmetic (add, multiply, abs, conjugate)
- `MandelbrotPoint` — single point; iterates z = z² + c until |z| > 2, tracking escape iteration count
- `IterationMap` (abstract) — holds a 2D grid of points mapped to a region of the complex plane; two iteration strategies:
  - `iterate()` — steps all points once
  - `tileIterate()` — divide-and-conquer; detects fully-interior tiles and skips them for performance
- `MandelbrotPointMap` — concrete `IterationMap` subclass

### GUI layer

- `MainFrame` — main window; hosts menu and `Panel_Mandelbrot` input dialog; spawns rendering threads
- `ImageFrame` — displays the rendered fractal; mouse clicks compute the clicked complex coordinate and re-render centered there at half the current width (zoom in)
- `Panel_Mandelbrot` — input dialog for center coordinates and zoom width
- `PixelCanvas` — custom `JComponent`; owns a `BufferedImage` and maps iteration counts to a grayscale palette; repaints on update

Rendering runs on a separate thread to keep the UI responsive.
