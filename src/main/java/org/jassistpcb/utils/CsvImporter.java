package org.jassistpcb.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CsvImporter {

    public static List<PcbPart> processCsv(String filePath, int headerSize) throws IOException {
        List<PcbPart> pcbParts = new ArrayList<>();
        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            System.err.println("Arquivo não encontrado: " + filePath);
            return pcbParts;
        }

        try (FileReader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader().withSkipHeaderRecord().withQuote('"'))) {

            for (CSVRecord record : csvParser) {
                String designator = record.get("Designator");
                String comment = record.get("Comment");
                String layer = record.get("Layer");
                String footprint = record.get("Footprint");
                String centerX = record.get("Center-X(mm)");
                String centerY = record.get("Center-Y(mm)");
                String rotation = record.get("Rotation");
                String description = record.get("Description");

                PcbPart pcbPart = new PcbPart(designator, layer, centerX, centerY, rotation);
                pcbPart.setComment(comment);
                pcbPart.setFootprint(footprint);
                pcbPart.setDescription(description);

                pcbParts.add(pcbPart);
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo CSV: " + e.getMessage());
            throw e;
        }

        return pcbParts;
    }
}
