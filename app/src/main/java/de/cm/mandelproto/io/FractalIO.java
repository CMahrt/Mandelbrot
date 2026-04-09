package de.cm.mandelproto.io;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.cm.mandelproto.graphics.Palette;
import de.cm.mandelproto.math.ComplexNumber;
import de.cm.mandelproto.math.IterationMap;
import de.cm.mandelproto.math.RenderParameters;
import lombok.extern.slf4j.Slf4j;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

@Slf4j
public final class FractalIO {

    private static final int CURRENT_VERSION = 1;
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private FractalIO() {}

    public static void save(File file, RenderParameters params, IterationMap iterationMap, Palette palette) throws IOException {
        log.info("Speichere Fraktal nach {}", file);
        ObjectNode node = MAPPER.createObjectNode();
        node.put("version", CURRENT_VERSION);

        // RenderParameters
        ObjectNode rp = node.putObject("renderParameters");
        rp.put("centerReal",    params.center().getReal());
        rp.put("centerImag",    params.center().getImag());
        rp.put("complexWidth",  params.complexWidth());
        rp.put("complexHeight", params.complexHeight());
        rp.put("pixelWidth",    params.pixelWidth());
        rp.put("maxIterations", params.maxIterations());

        // Palette
        ObjectNode paletteNode = node.putObject("palette");
        paletteNode.put("size", palette.size());
        ArrayNode colorsArr = paletteNode.putArray("colors");
        for (int i = 0; i < palette.size(); i++) {
            Color c = new Color(palette.getColor(i));
            colorsArr.addArray().add(c.getRed()).add(c.getGreen()).add(c.getBlue());
        }

        // Iterations: zeilenweise [row][col]
        int cols = iterationMap.getCols();
        int rows = iterationMap.getRows();
        ArrayNode iterArr = node.putArray("iterations");
        for (int row = 0; row < rows; row++) {
            ArrayNode rowArr = iterArr.addArray();
            for (int col = 0; col < cols; col++)
                rowArr.add(iterationMap.getIterationForCoordinate(col, row));
        }

        MAPPER.writeValue(file, node);
        log.info("Fraktal gespeichert ({} × {} Punkte)", cols, rows);
    }

    public static FractalSnapshot  load(File file) throws IOException {
        log.info("Lade Fraktal von {}", file);
        JsonNode node = MAPPER.readTree(file);

        // RenderParameters
        JsonNode rp = node.get("renderParameters");
        RenderParameters params = new RenderParameters(
                new ComplexNumber(rp.get("centerReal").asDouble(), rp.get("centerImag").asDouble()),
                rp.get("complexWidth").asDouble(),
                rp.get("complexHeight").asDouble(),
                rp.get("pixelWidth").asInt(),
                rp.get("maxIterations").asInt()
        );

        // Palette
        JsonNode paletteNode = node.get("palette");
        int paletteSize = paletteNode.get("size").asInt();
        JsonNode colorsNode = paletteNode.get("colors");
        Color[] paletteColors = new Color[paletteSize];
        for (int i = 0; i < paletteSize; i++) {
            JsonNode t = colorsNode.get(i);
            paletteColors[i] = new Color(t.get(0).asInt(), t.get(1).asInt(), t.get(2).asInt());
        }

        // Iterations: [row][col] → int[cols][rows] (col-major intern)
        int cols = params.pixelWidth();
        int rows = params.pixelHeight();
        JsonNode iterNode = node.get("iterations");
        int[][] iterations = new int[cols][rows];
        for (int row = 0; row < rows; row++) {
            JsonNode rowNode = iterNode.get(row);
            for (int col = 0; col < cols; col++)
                iterations[col][row] = rowNode.get(col).asInt();
        }

        log.info("Fraktal geladen ({} × {} Punkte, Palette: {} Einträge)", cols, rows, paletteSize);
        return new FractalSnapshot(params, iterations, paletteColors);
    }
}
