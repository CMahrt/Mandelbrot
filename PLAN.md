# Feature-Plan: Mandelbrot-Visualizer

Dieses Dokument sammelt geplante Features. Status: `[ ]` offen, `[x]` fertig, `[~]` in Arbeit.

---

## Farbpaletten & Running Colors

### Palette-Editor
- [ ] Variable Palettengröße (4 bis ~512 Einträge)
- [ ] Farbbalken-Anzeige der gesamten Palette
- [ ] Einzelnen Eintrag anklicken → Farbe ändern (JColorChooser)
- [ ] Palettengröße im Editor einstellbar

### Mapping: Iterationen → Palettenindex
- [ ] `iterationCount % palette.length` (statt fester Index)
- Effekt: kleine Palette → Konturlinien/Ringe; große Palette → weiche Übergänge

### Palette speichern & laden
- [ ] Paletten als Dateien speichern/laden (inkl. Größe)
- [ ] Vordefinierte Paletten mitliefern (Graustufen, Feuer, Regenbogen, ...)

### Running Colors (Palette Cycling)
- [ ] Palette rotiert jeden Frame um 1 Eintrag (kein Neuberechnen des Fraktals)
- [ ] Neuer Eintrag weicht zufällig vom letzten ab
- [ ] Parameter `deviation` (0–255): Stärke der Farbabweichung
- [ ] Animationsgeschwindigkeit steuerbar
- [ ] Start/Stop steuerbar

---

## Render-Parameter

### Bildformat
- [v] Seitenverhältnis frei wählbar (statt fix 16:9), z.B. 1:1, 4:3, 16:9, benutzerdefiniert
- [v] Auflösung (Breite × Höhe) einstellbar

### Iterationstiefe
- [ ] Maximale Iterationszahl konfigurierbar (aktuell fest im Code)
- [ ] Je tiefer der Zoom, desto höhere Tiefe nötig — Hinweis in der UI sinnvoll

### Iterations-zu-Paletten-Kurve (Mapping)
- [ ] Nicht-lineares Mapping von Iterationswert → Palettenindex
- [ ] Kleine Iterationswerte (Randbereich): 1 Iteration = 1 Paletteneintrag (feine Auflösung)
- [ ] Große Iterationswerte (Innenbereich): mehrere Iterationen → 1 Paletteneintrag (komprimiert)
- [ ] Kurvenform einstellbar (z.B. logarithmisch, quadratisch, linear)
- Effekt: visuell interessante Randbereiche bleiben scharf, egal wie groß die max. Iterationstiefe ist

---

## Navigation & Steuerung

### Zoom-Steuerung
- [ ] Zoomfaktor statt roher Breite: Zoom 1 = volle Breite (~3), Zoom 10 = 1/10 davon, etc.
- [ ] Logarithmischer Slider für Zoomfaktor (da Zoom-Bereich viele Größenordnungen überspannt)
- [ ] Ausschnittswahl per Maus (Rechteck aufziehen auf dem Fraktalbild)

### Koordinatenanzeige
- [ ] X/Y-Koordinaten im Steuerungsfenster sind read-only (Anzeige, nicht Eingabe)
- [ ] Mini-Map: kleine Übersicht des gesamten Mandelbrot-Sets mit Fadenkreuz für aktuelle Position
- [ ] Koordinaten evtl. per Mini-Map direkt anklickbar/navigierbar

### Iterationstiefe (Empfehlung)
- [ ] Iterationstiefe bleibt manuell eingebbar
- [ ] Programm macht automatisch einen Vorschlag basierend auf Zoomfaktor
  - Faustformel: empfohlene Tiefe wächst logarithmisch mit dem Zoomfaktor
  - Vorschlag anzeigen, User kann übernehmen oder überschreiben

### Präzise Ausschnittswahl
- [ ] Maus zieht Rechteck grob auf
- [ ] Pfeiltasten: gesamtes Rechteck fein verschieben
- [ ] Shift + Pfeiltasten: nur eine Kante verschieben (oben / unten / links / rechts einzeln)
- [ ] Visuelles D-Pad Widget (4 Pfeile + Umschalter "was wird gesteuert")
  - Desktop: optional als Alternative zu Tastatur
  - Android: einzige Steuerungsmöglichkeit (kein Keyboard)

### Bookmarks
- [ ] Interessante Koordinaten mit Namen abspeichern
- [ ] Gespeicherte Orte per Klick wieder anspringen

---

## Weitere Fraktale

- [ ] **Julia-Mengen** — eng verwandt mit Mandelbrot; für jeden Punkt c gibt es eine eigene Julia-Menge
  - Besonderes Feature: Klick auf einen Punkt im Mandelbrot → zeigt die zugehörige Julia-Menge
  - Gleiche Rendering-Infrastruktur nutzbar, nur die Iterationsformel ändert sich
- [ ] Weitere Fraktale (noch offen — z.B. Burning Ship, Newton-Fraktal, ...)
- [ ] Fraktal-Typ auswählbar in der UI

---

## Performance

- [ ] **Progressives Rendering** (grob → fein) — erst mit niedriger Auflösung rechnen, schrittweise verfeinern; User sieht sofort ein Bild
- [ ] **Multithreading** — Bildbereich auf mehrere CPU-Kerne aufteilen (ForkJoinPool o.ä.)
- [ ] **Abbruch laufender Berechnung** — wenn User Ausschnitt wechselt, alte Berechnung stoppen
- [ ] **Julia-Vorschau mit eigenen Parametern** — feste niedrige Auflösung + Iterationstiefe, unabhängig vom aktuellen Mandelbrot-Zoom

---

## Export & Speichern

### Bilddatei
- [ ] Bild exportieren als JPEG oder PNG
  - PNG verlustfrei (empfehlenswert für scharfe Farbgrenzen), JPEG für kleinere Dateien

### Rohdaten (vollständig)
- [ ] Iterationsmap + Palette + Berechnungsparameter speichern
- [ ] Ermöglicht: Bild wieder laden ohne Neuberechnung, Palette nachträglich ändern

### Parameter-Snapshot (leichtgewichtig)
- [ ] Nur Berechnungsparameter speichern (Koordinaten, Zoom, Iterationstiefe, Palettenreferenz)
- [ ] Kein Bild, keine Map — muss beim Laden neu berechnet werden
- [ ] Entspricht im Wesentlichen einem Bookmark als Datei (teilbar, versionierbar)

---

## Sonstiges
<!-- TODO -->
