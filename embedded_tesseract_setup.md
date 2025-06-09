# Embedding Tesseract Binaries in Your Application

## Project Structure with Embedded Binaries

```
your-project/
├── src/
│   └── main/
│       ├── java/
│       └── resources/
│           ├── native/
│           │   ├── windows-x64/
│           │   │   ├── tesseract.exe
│           │   │   ├── libleptonica-5.dll
│           │   │   └── libtesseract-5.dll
│           │   ├── linux-x64/
│           │   │   ├── tesseract
│           │   │   ├── libtesseract.so.5
│           │   │   └── libleptonica.so.5
│           │   └── macos-x64/
│           │       ├── tesseract
│           │       ├── libtesseract.5.dylib
│           │       └── libleptonica.5.dylib
│           └── tessdata/
│               ├── eng.traineddata
│               ├── fra.traineddata
│               └── deu.traineddata
├── pom.xml
└── README.md
```

## Updated Maven Configuration

The key is to ensure Maven includes these resources in your JAR and that you can extract them at runtime.

```xml
<build>
    <plugins>
        <!-- Ensure native resources are included -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-resources-plugin</artifactId>
            <version>3.3.1</version>
            <configuration>
                <includeEmptyDirs>true</includeEmptyDirs>
            </configuration>
        </plugin>
        
        <!-- Rest of your existing plugins -->
    </plugins>
</build>
```

## Native Library Extractor Utility

Create a utility to extract and set up the native binaries at runtime:

```java
package com.notebooklm.util;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TesseractNativeExtractor {
    private static final String TEMP_DIR_PREFIX = "tesseract-native-";
    private static Path extractedLibsPath;
    private static Path extractedTessDataPath;
    
    public static synchronized void extractNativeLibraries() throws IOException {
        if (extractedLibsPath != null) {
            return; // Already extracted
        }
        
        String platform = detectPlatform();
        Path tempDir = Files.createTempDirectory(TEMP_DIR_PREFIX);
        
        // Extract native binaries
        extractedLibsPath = tempDir.resolve("bin");
        Files.createDirectories(extractedLibsPath);
        extractPlatformBinaries(platform, extractedLibsPath);
        
        // Extract tessdata
        extractedTessDataPath = tempDir.resolve("tessdata");
        Files.createDirectories(extractedTessDataPath);
        extractTessData(extractedTessDataPath);
        
        // Set up cleanup on shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                deleteRecursively(tempDir);
            } catch (IOException e) {
                System.err.println("Failed to cleanup temp directory: " + e.getMessage());
            }
        }));
        
        System.out.println("✓ Tesseract native libraries extracted to: " + tempDir);
    }
    
    private static String detectPlatform() {
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();
        
        if (os.contains("win")) {
            return "windows-x64";
        } else if (os.contains("linux")) {
            return "linux-x64";
        } else if (os.contains("mac")) {
            return "macos-x64";
        } else {
            throw new UnsupportedOperationException("Unsupported platform: " + os + " " + arch);
        }
    }
    
    private static void extractPlatformBinaries(String platform, Path targetDir) throws IOException {
        String resourcePath = "/native/" + platform + "/";
        
        // List of files to extract for each platform
        List<String> filesToExtract = getFilesForPlatform(platform);
        
        for (String fileName : filesToExtract) {
            extractResource(resourcePath + fileName, targetDir.resolve(fileName));
        }
        
        // Make files executable on Unix systems
        if (!platform.startsWith("windows")) {
            makeExecutable(targetDir);
        }
    }
    
    private static List<String> getFilesForPlatform(String platform) {
        switch (platform) {
            case "windows-x64":
                return Arrays.asList("tesseract.exe", "libleptonica-5.dll", "libtesseract-5.dll");
            case "linux-x64":
                return Arrays.asList("tesseract", "libtesseract.so.5", "libleptonica.so.5");
            case "macos-x64":
                return Arrays.asList("tesseract", "libtesseract.5.dylib", "libleptonica.5.dylib");
            default:
                throw new UnsupportedOperationException("Unknown platform: " + platform);
        }
    }
    
    private static void extractTessData(Path targetDir) throws IOException {
        String[] languages = {"eng", "fra", "deu"}; // Add more as needed
        
        for (String lang : languages) {
            String resourcePath = "/tessdata/" + lang + ".traineddata";
            try (InputStream is = TesseractNativeExtractor.class.getResourceAsStream(resourcePath)) {
                if (is != null) {
                    Files.copy(is, targetDir.resolve(lang + ".traineddata"), 
                              StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("✓ Extracted tessdata for language: " + lang);
                }
            }
        }
    }
    
    private static void extractResource(String resourcePath, Path targetPath) throws IOException {
        try (InputStream is = TesseractNativeExtractor.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }
    
    private static void makeExecutable(Path directory) throws IOException {
        Files.walk(directory)
             .filter(Files::isRegularFile)
             .forEach(path -> path.toFile().setExecutable(true));
    }
    
    private static void deleteRecursively(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                 .sorted(Comparator.reverseOrder())
                 .map(Path::toFile)
                 .forEach(File::delete);
        }
    }
    
    public static String getTesseractExecutablePath() {
        if (extractedLibsPath == null) {
            throw new IllegalStateException("Native libraries not extracted yet");
        }
        
        String executableName = System.getProperty("os.name").toLowerCase().contains("win") 
                               ? "tesseract.exe" : "tesseract";
        return extractedLibsPath.resolve(executableName).toString();
    }
    
    public static String getTessDataPath() {
        if (extractedTessDataPath == null) {
            throw new IllegalStateException("Tessdata not extracted yet");
        }
        return extractedTessDataPath.toString();
    }
    
    public static String getNativeLibraryPath() {
        if (extractedLibsPath == null) {
            throw new IllegalStateException("Native libraries not extracted yet");
        }
        return extractedLibsPath.toString();
    }
}
```

## Updated Document Processor

Now update your document processor to use the extracted binaries:

```java
package com.notebooklm;

import com.notebooklm.util.TesseractNativeExtractor;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;

import java.io.*;
import java.util.List;

public class EnhancedDocumentProcessor {
    private final TikaConfig tikaConfig;
    private final Tesseract tesseract;
    
    public EnhancedDocumentProcessor() {
        try {
            // Extract native libraries first
            TesseractNativeExtractor.extractNativeLibraries();
            
            // Initialize Tesseract with extracted binaries
            this.tesseract = initializeTesseract();
            
            // Configure Tika
            this.tikaConfig = new TikaConfig(getClass().getResourceAsStream("/tika-config.xml"));
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize document processor", e);
        }
        System.out.println("✓ Enhanced Document Processor with embedded Tesseract ready.");
    }
    
    private Tesseract initializeTesseract() {
        Tesseract tesseract = new Tesseract();
        
        // Set paths to extracted binaries and data
        tesseract.setDatapath(TesseractNativeExtractor.getTessDataPath());
        tesseract.setLanguage("eng");
        tesseract.setOcrEngineMode(1);
        tesseract.setPageSegMode(1);
        
        // Set the tesseract executable path
        tesseract.setTessVariable("user_defined_dpi", "300");
        
        return tesseract;
    }
    
    // Rest of your existing methods...
}
```

## Application Startup Integration

In your main application class, ensure extraction happens early:

```java
package com.notebooklm;

import com.notebooklm.util.TesseractNativeExtractor;

public class IntegratedRAGSystem {
    
    public static void main(String[] args) {
        try {
            System.out.println("🚀 Starting Self-Contained RAG System...");
            
            // Extract native libraries first
            TesseractNativeExtractor.extractNativeLibraries();
            
            // Continue with your existing initialization...
            
        } catch (Exception e) {
            System.err.println("Failed to start application: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
```

## Where to Get the Binaries

### For Windows:
- Download from: https://github.com/UB-Mannheim/tesseract/wiki
- Extract the DLLs from the installation directory

### For Linux:
- Install tesseract: `sudo apt-get install tesseract-ocr libtesseract-dev`
- Copy from: `/usr/bin/tesseract`, `/usr/lib/x86_64-linux-gnu/`

### For macOS:
- Install via Homebrew: `brew install tesseract`
- Copy from: `/usr/local/bin/tesseract`, `/usr/local/lib/`

### For tessdata files:
- Download from: https://github.com/tesseract-ocr/tessdata
- Choose the languages you need (each file is 10-15MB)

## Build Script to Gather Binaries

Create a helper script to collect binaries:

```bash
#!/bin/bash
# collect-binaries.sh

RESOURCES_DIR="src/main/resources"

# Create directories
mkdir -p "$RESOURCES_DIR/native/windows-x64"
mkdir -p "$RESOURCES_DIR/native/linux-x64"
mkdir -p "$RESOURCES_DIR/native/macos-x64"
mkdir -p "$RESOURCES_DIR/tessdata"

echo "Place your platform-specific binaries in the following directories:"
echo "Windows: $RESOURCES_DIR/native/windows-x64/"
echo "Linux: $RESOURCES_DIR/native/linux-x64/"
echo "macOS: $RESOURCES_DIR/native/macos-x64/"
echo "Tessdata: $RESOURCES_DIR/tessdata/"
```

## Important Notes:

1. **JAR Size**: Your JAR will be much larger (100-200MB) due to binaries
2. **Licensing**: Ensure you comply with Tesseract's Apache 2.0 license
3. **Platform Support**: You'll need separate binaries for each platform you support
4. **Testing**: Test on each target platform to ensure extraction works correctly
5. **Performance**: First-run extraction adds ~2-5 seconds to startup time

This approach gives you a truly self-contained application that doesn't require users to install Tesseract separately!