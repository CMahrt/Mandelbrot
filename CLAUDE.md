 # CLAUDE.md

Diese Datei enthält Hinweise für Claude Code beim Arbeiten in diesem Repository.

## Befehle

```bash
# Bauen
./gradlew build

# Anwendung starten
./gradlew run

# Tests ausführen
./gradlew test

# Aufräumen
./gradlew clean
```

Java 21 Toolchain ist konfiguriert. Gradle Wrapper Version 8.9.

## Feature-Planung

Alle geplanten Features sind in **`PLAN.md`** im Projektwurzel vermerkt.
Status-Markierungen: `[ ]` offen, `[v]` erledigt, `[~]` in Arbeit.

Vor dem Implementieren eines neuen Features: in `PLAN.md` nachschauen ob es dort steht, und den Status entsprechend setzen. Nach Abschluss `[v]` setzen.

## Architektur

Mandelbrot-Fraktal-Visualisierer auf Basis von Java Swing. Der Benutzer kann das Fraktal durch Klicken (Zoom) oder manuelle Koordinateneingabe erkunden.

**Paketstruktur:** `de.cm.mandelproto`
- `App` — Einstiegspunkt; erzeugt `MainFrame`
- `gui/` — Swing-UI-Komponenten
- `math/` — Komplexe Arithmetik und Mandelbrot-Iterationslogik
- `graphics/` — Pixel-Rendering in `BufferedImage`

### Math-Schicht

- `ComplexNumber` — unveränderliche komplexe Arithmetik (add, multiply, abs, conjugate)
- `MandelbrotPoint` — einzelner Punkt; iteriert z = z² + c bis |z| > 2, zählt Escape-Iterationen
- `IterationMap` (abstrakt) — hält ein 2D-Gitter von Punkten, abgebildet auf einen Bereich der komplexen Ebene; zwei Iterationsstrategien:
  - `iterate()` — schrittweise alle Punkte einmal iterieren
  - `tileIterate()` — Divide-and-Conquer; erkennt vollständig innere Kacheln und überspringt sie
- `MandelbrotPointMap` — konkrete `IterationMap`-Unterklasse

### GUI-Schicht

- `MainFrame` — Hauptfenster; enthält Menü und `Panel_Mandelbrot`; startet Render-Threads
- `ImageFrame` — zeigt das gerenderte Fraktal; Mausklick berechnet die komplexe Koordinate und rendert neu
- `Panel_Mandelbrot` — Eingabedialog für Mittelpunkt (Real/Imag), komplexe Breite/Höhe, Pixelbreite (max. Bildschirmbreite); Pixelhöhe wird automatisch berechnet und angezeigt (read-only)
- `PixelCanvas` — eigene `JComponent`; besitzt ein `BufferedImage` und bildet Iterationszahlen auf eine Graustufen-Palette ab
- `DoubleTextField` / `IntTextField` — Hilfskomponenten: Label + Eingabefeld als Panel

Das Rendering läuft in einem separaten Thread, damit die UI reaktionsfähig bleibt.

### Wichtige Designentscheidungen

- **Seitenverhältnis frei wählbar:** Breite und Höhe (komplex und Pixel) werden überall getrennt geführt — kein hardcodiertes 16:9 mehr. Die Pixelhöhe ergibt sich aus `(height/width) * pixelWidth`.
- **Pixel-Maximum:** Die konfigurierbare Pixelbreite ist auf die Bildschirmbreite begrenzt; die Pixelhöhe darf die Bildschirmhöhe nicht überschreiten (kombinierte Validierung in `Panel_Mandelbrot.updatePixelDimensions()`).
