package com.notebooklm;

import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
// ... other imports

public class EnhancedDocumentProcessor {
    // ...
    private final TikaConfig tikaConfig; // NEW: Tika configuration

    public EnhancedDocumentProcessor() {
        try {
            // Explicitly configure Tika to use the Tesseract OCR parser
            this.tikaConfig = new TikaConfig(getClass().getResourceAsStream("/tika-config.xml"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Tika configuration for Tesseract", e);
        }
        System.out.println("✓ Enhanced Document Processor with Tika/Tesseract ready.");
    }

    public List<DocumentChunk> processFile(String filePath) throws Exception {
        File file = new File(filePath);
        Metadata metadata = new Metadata();

        // Use a BodyContentHandler that doesn't limit text size
        BodyContentHandler handler = new BodyContentHandler(-1);

        AutoDetectParser parser = new AutoDetectParser(this.tikaConfig);
        ParseContext context = new ParseContext();

        try (InputStream stream = new FileInputStream(file)) {
            parser.parse(stream, handler, metadata, context);
            // The handler now contains text extracted by Tika, including OCR from Tesseract
            String content = handler.toString();

            ProcessedDocument document = new ProcessedDocument(
                filePath, file.getName(), content, parseMetadata(metadata), "tika_tesseract"
            );
            return chunkDocument(document);
        }
    }
    // ... (rest of the class remains mostly the same)
}
