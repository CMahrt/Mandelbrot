# Feature-Plan: Mandelbrot-Visualizer

Dieses Dokument sammelt geplante Features. Status: `[ ]` offen, `[x]` fertig, `[~]` in Arbeit.

---

## Farbpaletten & Running Colors

### Palette-Editor
- [ ] Farbbalken-Anzeige der gesamten Palette
- [ ] Palettengröße im Editor einstellbar
- [ ] **Anker-basierte Bearbeitung** — statt alle Einträge einzeln zu setzen, definiert der User einige Ankerfarben an frei wählbaren Positionen; die Zwischenfarben werden automatisch interpoliert (lineare RGB-Interpolation, wie `PaletteLibrary.interpolate()` es bereits intern tut). Anker verschieben, hinzufügen, löschen — Palette aktualisiert sich live.
- [ ] Einzelnen Eintrag anklicken → Farbe ändern (JColorChooser) — überschreibt lokal ohne die Anker zu verändern

### Mapping: Iterationen → Palettenindex
- [v] `iterationCount % palette.length` (statt fester Index)
- Effekt: kleine Palette → Konturlinien/Ringe; große Palette → weiche Übergänge

### Palette-Größe ändern ohne Reset
- [ ] Wenn die Palettengröße im Inspector geändert wird, soll die **aktuell aktive** Palette resampled werden — nicht neu aus der Library geladen
- [ ] Verkleinern: Downsampling — gleichmäßig verteilte Einträge aus der alten Palette übernehmen (jede N-te Farbe)
- [ ] Vergrößern: Interpolation — Farbwerte zwischen bestehenden Einträgen linear interpolieren (wie `PaletteLibrary.interpolate()`)
- [ ] Letzter Eintrag (Innen-Farbe, Index `size-1`) bleibt in beiden Fällen schwarz/fest
- [ ] Auslöser: nur die Size-ComboBox; Palette-ComboBox lädt weiterhin neu aus der Library

### Palette speichern & laden
- [ ] Paletten als Dateien speichern/laden (inkl. Größe)
- [v] Vordefinierte Paletten mitliefern (Graustufen, Feuer, Regenbogen, Ozean)

### Running Colors (Palette Cycling)
- [v] Palette rotiert jeden Frame um 1 Eintrag (kein Neuberechnen des Fraktals)
- [v] Neuer Eintrag weicht zufällig vom letzten ab
- [v] Parameter `deviation` (0–255): Stärke der Farbabweichung
- [v] Animationsgeschwindigkeit steuerbar
- [v] Start/Stop steuerbar
- [v] **"Running Colors" als Palette-Eintrag** — `PaletteLibrary.NAMES` um einen Eintrag "Running Colors" erweitern; Konfiguration in eigenem modalen Dialog; ComboBox zeigt Herkunft der Palette
- [v] **Innen-Bereich ausnehmen** — Checkbox im Running-Colors-Dialog: letzter Paletteneintrag (Index `size-1`, reserviert für Innen-Punkte) wird von der Rotation ausgenommen
- [v] **Deterministischer Bounce-Modus** — jeder RGB-Kanal bewegt sich mit zufälliger Schrittweite in fester Richtung; dreht bei 0/255 um (Dreieckswelle mit organischem Jitter)

---

## Render-Parameter

### Bildformat
- [v] Seitenverhältnis frei wählbar (statt fix 16:9), z.B. 1:1, 4:3, 16:9, benutzerdefiniert
- [v] Auflösung (Breite × Höhe) einstellbar

### Iterationstiefe
- [v] Maximale Iterationszahl konfigurierbar (Eingabefeld in Panel_Mandelbrot)
- [v] Je tiefer der Zoom, desto höhere Tiefe nötig — automatischer Vorschlag in der UI

### Nachschärfen (Incremental Refine)
- [v] Button „Nachschärfen" im Inspector — öffnet Dialog zur Eingabe einer neuen (höheren) Iterationstiefe
- [v] Nur Punkte, die bisher `maxIterations` erreicht haben, werden neu berechnet — sie gelten als „noch unentschieden" und könnten mit mehr Iterationen entkommen
- [v] Punkte, die bereits entkommen sind (Iterationszahl < altes Maximum), bleiben unverändert — kein Neuberechnen nötig
- [v] `IterationMap` muss dazu den alten `maxIterations`-Wert kennen, um die Kandidaten-Punkte identifizieren zu können
- [v] Nach dem Nachschärfen: `maxIterations` des Frames wird auf den neuen Wert aktualisiert
- Effekt: Man kann ein bereits gerendertes Bild mit wenig Aufwand verfeinern, ohne alles neu zu rechnen

### Iterations-zu-Paletten-Kurve (Mapping)
- [v] Nicht-lineares Mapping von Iterationswert → Palettenindex
- [v] Kleine Iterationswerte (Randbereich): 1 Iteration = 1 Paletteneintrag (feine Auflösung)
- [v] Große Iterationswerte (Innenbereich): mehrere Iterationen → 1 Paletteneintrag (komprimiert)
- [v] Kurvenform einstellbar (z.B. logarithmisch, quadratisch, linear) — LINEAR, SQRT, LOG in `PaletteMapper`
- Effekt: visuell interessante Randbereiche bleiben scharf, egal wie groß die max. Iterationstiefe ist

### Palette-Offset: minimale Iteration als Untergrenze
- [ ] `IterationMap` ermittelt die kleinste tatsächlich vorkommende Escape-Iteration (nur Außen-Punkte, d.h. `iteration < maxIterations`) — `getMinIteration()` mit Lazy-Cache (wird nach `tileIterate()` und `refine()` invalidiert)
- [ ] `PaletteMapper.configure()` erhält zusätzlich `minIterations`; `map()` normiert auf `[minIterations, maxIterations]` statt `[0, maxIterations]`
- [ ] `PixelCanvas.drawImage()` übergibt `iterationMap.getMinIteration()` an `configure()`
- Effekt: Bei tiefen Zooms (z.B. alle Pixel zwischen Iter. 75–800) wird die volle Palette auf den tatsächlichen Wertebereich gestreckt statt die untere Hälfte zu verschwenden

---

## Navigation & Steuerung

### Zoom-Steuerung
- [ ] Zoomfaktor statt roher Breite: Zoom 1 = volle Breite (~3), Zoom 10 = 1/10 davon, etc.
- [ ] Logarithmischer Slider für Zoomfaktor (da Zoom-Bereich viele Größenordnungen überspannt)
- [v] Ausschnittswahl per Maus (Rechteck aufziehen auf dem Fraktalbild) — siehe "Präzise Ausschnittswahl" unten

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
- [v] Hält Liste aller offenen `ImageFrame`s (statt nur `currentImageFrame`)
- [v] Menü zum direkten Springen/Fokussieren eines bestimmten Frames

**Inspector-Fenster — 1:1 pro `ImageFrame`**
- [v] Zeigt Parameter des zugehörigen Bildes read-only: Mittelpunkt, Zoomfaktor, Pixelgröße
- [v] Palette-ComboBox und Running-Colors-Steuerung (wirken live auf dieses Bild)
- [ ] Name/Label für diesen Frame vergeben
- [v] Button "Neuen Ausschnitt wählen" — versetzt das zugehörige `ImageFrame` in den Auswahlmodus

**Create-New-Workflow (modal)**
- [ ] "Create New" öffnet Typ-Dialog: Mandelbrot-Ausschnitt, Julia-Menge, ...
- [v] Rechteck aufziehen → Parameter-Dialog für Auflösung, Iterationen (vorausgefüllt aus Selektion)
- [v] Ergebnis: neues `ImageFrame` + eigener Inspector

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
- [v] **Berechnung und Rendering trennen** — `tileIterate()` im SwingWorker-Hintergrund; `ImageFrame` + `drawImage()` auf dem EDT
- [v] **App-Start auf EDT** — `App.main()` verwendet `SwingUtilities.invokeLater(MainFrame::new)`

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

### RenderParameters-Hash
- [ ] `RenderParameters` bekommt eine `hash()`-Methode (oder `hashCode()`-Override) — kompakter Fingerabdruck aus center, complexWidth, complexHeight, pixelWidth, maxIterations
- [ ] Mögliche Verwendungen: Fenstername/Dateivorschlag, Duplikaterkennung beim Laden, Abgleich Parent-Kind-Kette, Cache-Key für vorberechnete Bilder
- [ ] Implementierung: `Objects.hash(...)` für schnellen int-Hash; alternativ SHA-1/MD5 der Felder für kollisionsarmen String-Hash (z.B. `"mfrac-a3f7c2"`)

### Rohdaten (vollständig)
- [v] Iterationsmap + Palette + Berechnungsparameter speichern — `.mfrac` JSON-Format
- [v] Ermöglicht: Bild wieder laden ohne Neuberechnung, Palette nachträglich ändern

### Vorschau im Lade-Dialog
- [x] `JFileChooser.setAccessory()` — kleines Preview-Panel neben dem Datei-Browser
- [x] Panel hört auf `PropertyChangeListener` des Choosers (`SELECTED_FILE_CHANGED_PROPERTY`)
- [x] Bei Auswahl einer `.mfrac`-Datei: Iterationsmap + Palette laden, in feste Größe (z.B. 128×128 px) skalieren, als `BufferedImage` anzeigen
- [x] `FractalIO.load()` im SwingWorker-Hintergrund; Generations-Counter verhindert veraltete Ergebnisse
- [x] Bei ungültiger Datei oder Ladefehler: leeres Panel oder Platzhaltertext
- Effekt: Dateien mit nichtssagenden Namen sind trotzdem erkennbar

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

### Parent-Map-Referenz (Zoom-Kette)
- [ ] Jedes `ImageFrame` kennt den `RenderParameters`-Snapshot seines Eltern-Frames — die Koordinaten, aus denen hereingezoomt wurde
- [ ] Beim Erstellen eines neuen Ausschnitts (`openImage()`) übergibt `MainFrame` oder `ImageFrame` die eigenen Parameter als `parentParams` an das neue Frame
- [ ] Die Kette kaskadiert: A → B → C ergibt eine vollständige Zoom-Historie
- [ ] Voraussetzung und Grundlage für die Zoom-Animation (Fly-Through) weiter unten
- Effekt: Jedes Bild weiß, woher es stammt; Navigation zurück zum Parent möglich; Zoom-Pfade rekonstruierbar

### Zoom-Animation (Fly-Through)
- [ ] Benutzer definiert Start- und Zielkoordinate (Mittelpunkt + Zoom) — z.B. per Bookmark **oder über die Parent-Map-Kette**
- [ ] Programm berechnet N Zwischenframes (interpoliert Mittelpunkt und Zoomfaktor logarithmisch)
- [ ] Frames werden vorab gerendert und im Speicher (oder auf Disk) gehalten
- [ ] Abspielansicht zeigt die Frames als flüssiges Filmchen (einstellbare FPS)
- [ ] Optionale Rotation entlang der Sequenz mitanimierbar (kombiniert mit Drehbarer-Blickwinkel-Feature)
- [ ] Export der Framesequenz als Videodatei (z.B. MP4 via FFmpeg) oder animiertes GIF

---

## Mathematische Grundlagen / Offene Fragen

### Präzisionsgrenze bei tiefen Zooms
- [ ] **Klären: ab welchem Zoomfaktor werden `double`-Koordinaten sinnlos?**
  - `double` (64-bit IEEE 754) hat ~15–16 signifikante Dezimalstellen
  - Ausgangsbereich der komplexen Ebene: Breite ≈ 3,5 (Größenordnung 10⁰)
  - Pixelschritt = `complexWidth / pixelWidth`; bei 1000 px und `complexWidth = 1e-13` wäre Pixelschritt ≈ 1e-16 — unterhalb der `double`-Auflösung
  - Faustformel: sinnvolle Grenze liegt bei `complexWidth` ≈ 1e-13 bis 1e-14
  - **Maßnahmen:** UI-Warnung wenn Grenze unterschritten wird; langfristig: Umstieg auf `BigDecimal` oder Perturbationstheorie (Referenzorbit) für beliebig tiefe Zooms
  - Perturbationstheorie erlaubt extrem tiefe Zooms mit `double`-Arithmetik durch geschickte Umformung um einen Referenzpunkt — Standardansatz in modernen Mandelbrot-Renderern

---

## Sonstiges

### Internationalisierung (i18n)
- [v] Alle UI-Texte (Menüeinträge, Labels, Buttons, Dialoge) über `ResourceBundle` externalisieren
- [v] Sprachdateien als `.properties`-Dateien (`uiStrings.properties`, `uiStrings_de.properties`)
- [v] Sprache einstellbar (Settings-Menü; Systemsprache als Default)
- [v] Von Anfang an konsequent — keine hartcodierten Strings in Swing-Komponenten
