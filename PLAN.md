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
- [v] `iterationCount % palette.length` (statt fester Index)
- Effekt: kleine Palette → Konturlinien/Ringe; große Palette → weiche Übergänge

### Palette speichern & laden
- [ ] Paletten als Dateien speichern/laden (inkl. Größe)
- [v] Vordefinierte Paletten mitliefern (Graustufen, Feuer, Regenbogen, Ozean)

### Running Colors (Palette Cycling)
- [v] Palette rotiert jeden Frame um 1 Eintrag (kein Neuberechnen des Fraktals)
- [v] Neuer Eintrag weicht zufällig vom letzten ab
- [v] Parameter `deviation` (0–255): Stärke der Farbabweichung
- [v] Animationsgeschwindigkeit steuerbar
- [v] Start/Stop steuerbar

---

## Render-Parameter

### Bildformat
- [v] Seitenverhältnis frei wählbar (statt fix 16:9), z.B. 1:1, 4:3, 16:9, benutzerdefiniert
- [v] Auflösung (Breite × Höhe) einstellbar

### Iterationstiefe
- [v] Maximale Iterationszahl konfigurierbar (Eingabefeld in Panel_Mandelbrot)
- [v] Je tiefer der Zoom, desto höhere Tiefe nötig — automatischer Vorschlag in der UI

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

### Visueller Schnellzoom per Mausrad
- [ ] Mausrad auf dem Fraktalbild skaliert das vorhandene BufferedImage (kein Neuberechnen)
- [ ] Zoom zentriert auf die aktuelle Mausposition
- [ ] Nur visuelles Feedback — Pixel werden unschärfer je weiter man reinzoomt
- [ ] Steuerungsfenster aktualisiert Koordinaten und complexWidth passend zum Skalierungsfaktor
- [ ] Erst ein expliziter Render-Aufruf (Ok-Button) berechnet das Bild in voller Qualität neu

### Koordinatenanzeige
- [ ] X/Y-Koordinaten im Steuerungsfenster sind read-only (Anzeige, nicht Eingabe)
- [ ] Mini-Map: kleine Übersicht des gesamten Mandelbrot-Sets mit Fadenkreuz für aktuelle Position
- [ ] Koordinaten evtl. per Mini-Map direkt anklickbar/navigierbar

### Iterationstiefe (Empfehlung)
- [v] Iterationstiefe bleibt manuell eingebbar
- [v] Programm macht automatisch einen Vorschlag basierend auf Zoomfaktor
  - Faustformel: empfohlene Tiefe wächst logarithmisch mit dem Zoomfaktor
  - Vorschlag anzeigen, User kann übernehmen oder überschreiben

### Präzise Ausschnittswahl
- [v] Maus zieht Rechteck grob auf
- [v] Pfeiltasten: gesamtes Rechteck fein verschieben
- [v] Shift + Pfeiltasten: Breite/Höhe des Rechtecks ändern (Links/Rechts = Breite, Oben/Unten = Höhe)
- [ ] Visuelles D-Pad Widget (4 Pfeile + Umschalter "was wird gesteuert")
  - Desktop: optional als Alternative zu Tastatur
  - Android: einzige Steuerungsmöglichkeit (kein Keyboard)

### UI-Architektur: Inspector-Konzept (geplantes Refactoring)

Ablösung von `Panel_Mandelbrot` durch ein neues, klar getrenntes UI-Modell:

**`MainFrame` — Fenster-Manager**
- [ ] Hält Liste aller offenen `ImageFrame`s (statt nur `currentImageFrame`)
- [ ] Menü zum direkten Springen/Fokussieren eines bestimmten Frames

**Inspector-Fenster — 1:1 pro `ImageFrame`**
- [ ] Zeigt Parameter des zugehörigen Bildes read-only: Mittelpunkt, Zoomfaktor, Pixelgröße
- [ ] Palette-ComboBox und Running-Colors-Steuerung (wirken live auf dieses Bild)
- [ ] Name/Label für diesen Frame vergeben
- [ ] Button "Create New" — versetzt das zugehörige `ImageFrame` in den Auswahlmodus

**Create-New-Workflow (modal)**
- [ ] "Create New" öffnet Typ-Dialog: Mandelbrot-Ausschnitt, Julia-Menge, ...
- [ ] Je nach Typ wird das `ImageFrame` interaktiv: Rechteck aufziehen (Mandelbrot) oder Punkt anklicken (Julia)
- [ ] Danach: Parameter-Dialog für Auflösung, Iterationen (vorausgefüllt aus Selektion)
- [ ] Ergebnis: neues `ImageFrame` + eigener Inspector

---

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

### Swing-Threading-Architektur (bekanntes Problem)
- [ ] **Berechnung und Rendering trennen** — `PixelCanvas.draw()` mischt aktuell beides in einem Background-Thread:
  - `tileIterate()` = schwere Berechnung → gehört in den Background-Thread
  - `drawImage()` + `setVisible()` = UI-Operationen → müssen auf dem EDT laufen
  - Aktuell: `ImageFrame` und `setVisible()` werden vom Background-Thread aufgerufen (Swing-Verletzung)
  - Saubere Lösung: Background-Thread nur für `tileIterate()`, danach `SwingUtilities.invokeLater` für Rendering
- [ ] **App-Start auf EDT** — `App.main()` sollte `SwingUtilities.invokeLater(MainFrame::new)` verwenden (Swing-Standard)

### Weitere Performance-Features
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

## Darstellung & Fenster

### Drehbarer Blickwinkel (Rotation)
- [ ] Winkelparameter einführen (0°–360°, default 0°)
- [ ] Pixel → komplexe Koordinate: Verschiebungsvektor vom Mittelpunkt wird vor der Umrechnung um den Winkel rotiert (2D-Rotationsmatrix)
- [ ] Das Fraktal selbst bleibt unverändert — nur die Abtastrichtung dreht sich
- [ ] Winkel einstellbar per Eingabefeld oder Slider in `Panel_Mandelbrot`
- [ ] Bei 0° identisches Ergebnis wie bisher (achsenparallel)

---

### Scrollbares Fraktalbild
- [ ] `PixelCanvas` in einen `JScrollPane` einbetten
- [ ] Bild bleibt in voller Pixelgröße gerendert — kein Skalieren/Strecken
- [ ] Scrollbalken erscheinen automatisch, wenn das Bild größer als das Fenster ist
- [ ] Fenster darf kleiner als das Bild sein (kein erzwungenes Verkleinern des Bildes)

---

## Animation & Video

### Zoom-Animation (Fly-Through)
- [ ] Benutzer definiert Start- und Zielkoordinate (Mittelpunkt + Zoom) — z.B. per Bookmark
- [ ] Programm berechnet N Zwischenframes (interpoliert Mittelpunkt und Zoomfaktor logarithmisch)
- [ ] Frames werden vorab gerendert und im Speicher (oder auf Disk) gehalten
- [ ] Abspielansicht zeigt die Frames als flüssiges Filmchen (einstellbare FPS)
- [ ] Optionale Rotation entlang der Sequenz mitanimierbar (kombiniert mit Drehbarer-Blickwinkel-Feature)
- [ ] Export der Framesequenz als Videodatei (z.B. MP4 via FFmpeg) oder animiertes GIF

---

## Sonstiges

### Internationalisierung (i18n)
- [v] Alle UI-Texte (Menüeinträge, Labels, Buttons, Dialoge) über `ResourceBundle` externalisieren
- [v] Sprachdateien als `.properties`-Dateien (`uiStrings.properties`, `uiStrings_de.properties`)
- [v] Sprache einstellbar (Settings-Menü; Systemsprache als Default)
- [v] Von Anfang an konsequent — keine hartcodierten Strings in Swing-Komponenten
