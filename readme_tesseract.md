# Tesseract OCR Setup for Self-Contained Application

This application embeds Tesseract OCR binaries to provide OCR capabilities without requiring users to install Tesseract separately. This document explains how to obtain and set up the required native binaries.

## Quick Setup

1. Run the setup script:
   ```bash
   chmod +x collect-binaries.sh
   ./collect-binaries.sh
   ```

2. Follow the platform-specific instructions below to obtain binaries

3. Place binaries in the created directories

4. Build the application:
   ```bash
   mvn clean package
   ```

## Directory Structure

After running `collect-binaries.sh`, you'll have this structure:

```
src/main/resources/
├── native/
│   ├── windows-x64/     # Windows binaries go here
│   ├── linux-x64/       # Linux binaries go here
│   └── macos-x64/       # macOS binaries go here
└── tessdata/            # Language data files go here
```

## Platform-Specific Instructions

### Windows (windows-x64)

**Required Files:**
- `tesseract.exe`
- `libleptonica-5.dll`
- `libtesseract-5.dll`

**How to Obtain:**
1. Visit the [UB-Mannheim Tesseract releases](https://github.com/UB-Mannheim/tesseract/wiki)
2. Download the latest Windows installer (e.g., `tesseract-ocr-w64-setup-5.3.3.20231005.exe`)
3. Install Tesseract to a temporary location (e.g., `C:\Program Files\Tesseract-OCR`)
4. Copy the required files:
   ```cmd
   copy "C:\Program Files\Tesseract-OCR\tesseract.exe" src\main\resources\native\windows-x64\
   copy "C:\Program Files\Tesseract-OCR\libleptonica-5.dll" src\main\resources\native\windows-x64\
   copy "C:\Program Files\Tesseract-OCR\libtesseract-5.dll" src\main\resources\native\windows-x64\
   ```

### Linux (linux-x64)

**Required Files:**
- `tesseract`
- `libtesseract.so.5`
- `libleptonica.so.5`

**How to Obtain:**
1. Install Tesseract via package manager:
   ```bash
   # Ubuntu/Debian
   sudo apt-get update
   sudo apt-get install tesseract-ocr libtesseract-dev libleptonica-dev
   
   # CentOS/RHEL/Fedora
   sudo yum install tesseract tesseract-devel leptonica-devel
   # or
   sudo dnf install tesseract tesseract-devel leptonica-devel
   ```

2. Copy the required files:
   ```bash
   # Copy executable
   cp /usr/bin/tesseract src/main/resources/native/linux-x64/
   
   # Copy libraries (paths may vary by distribution)
   cp /usr/lib/x86_64-linux-gnu/libtesseract.so.5 src/main/resources/native/linux-x64/
   cp /usr/lib/x86_64-linux-gnu/libleptonica.so.5 src/main/resources/native/linux-x64/
   
   # Alternative library paths to check:
   # /usr/lib64/libtesseract.so.5
   # /usr/lib64/libleptonica.so.5
   # /lib/x86_64-linux-gnu/libtesseract.so.5
   # /lib/x86_64-linux-gnu/libleptonica.so.5
   ```

3. Find library locations if unsure:
   ```bash
   find /usr -name "libtesseract.so*" 2>/dev/null
   find /usr -name "libleptonica.so*" 2>/dev/null
   ```

### macOS (macos-x64)

**Required Files:**
- `tesseract`
- `libtesseract.5.dylib`
- `libleptonica.5.dylib`

**How to Obtain:**
1. Install Tesseract via Homebrew:
   ```bash
   brew install tesseract
   ```

2. Copy the required files:
   ```bash
   # For Intel Macs
   cp /usr/local/bin/tesseract src/main/resources/native/macos-x64/
   cp /usr/local/lib/libtesseract.5.dylib src/main/resources/native/macos-x64/
   cp /usr/local/lib/libleptonica.5.dylib src/main/resources/native/macos-x64/
   
   # For Apple Silicon Macs (M1/M2)
   cp /opt/homebrew/bin/tesseract src/main/resources/native/macos-x64/
   cp /opt/homebrew/lib/libtesseract.5.dylib src/main/resources/native/macos-x64/
   cp /opt/homebrew/lib/libleptonica.5.dylib src/main/resources/native/macos-x64/
   ```

## Language Data Files (tessdata)

**Required Files:**
- `eng.traineddata` (English - essential)
- `fra.traineddata` (French - optional)
- `deu.traineddata` (German - optional)

**How to Obtain:**
1. Visit the [tessdata repository](https://github.com/tesseract-ocr/tessdata)
2. Download the `.traineddata` files for languages you need
3. Place them in `src/main/resources/tessdata/`

**Quick download commands:**
```bash
# Download English (required)
curl -L https://github.com/tesseract-ocr/tessdata/raw/main/eng.traineddata \
     -o src/main/resources/tessdata/eng.traineddata

# Download French (optional)
curl -L https://github.com/tesseract-ocr/tessdata/raw/main/fra.traineddata \
     -o src/main/resources/tessdata/fra.traineddata

# Download German (optional)
curl -L https://github.com/tesseract-ocr/tessdata/raw/main/deu.traineddata \
     -o src/main/resources/tessdata/deu.traineddata
```

## Verification

After placing all files, verify your structure:

```bash
find src/main/resources/native -type f
find src/main/resources/tessdata -type f
```

Expected output:
```
src/main/resources/native/windows-x64/tesseract.exe
src/main/resources/native/windows-x64/libleptonica-5.dll
src/main/resources/native/windows-x64/libtesseract-5.dll
src/main/resources/native/linux-x64/tesseract
src/main/resources/native/linux-x64/libtesseract.so.5
src/main/resources/native/linux-x64/libleptonica.so.5
src/main/resources/native/macos-x64/tesseract
src/main/resources/native/macos-x64/libtesseract.5.dylib
src/main/resources/native/macos-x64/libleptonica.5.dylib
src/main/resources/tessdata/eng.traineddata
src/main/resources/tessdata/fra.traineddata
src/main/resources/tessdata/deu.traineddata
```

## Important Notes

### File Sizes
- Native binaries: ~20-50MB total per platform
- Language data files: ~10-15MB each
- Final JAR size will be ~150-300MB depending on platforms and languages included

### Licensing
- Tesseract is licensed under Apache License 2.0
- Ensure compliance when distributing your application
- Include appropriate license notices in your documentation

### Platform Support
- You only need binaries for platforms you plan to support
- The application will detect the current platform at runtime
- Missing platform binaries will cause runtime errors on those platforms

### Testing
Test on each target platform after building:
```bash
java -jar target/integrated-rag-system-self-contained-2.0.0.jar
```

Look for this log message:
```
✓ Tesseract native libraries extracted to: /tmp/tesseract-native-xxxxx
```

## Troubleshooting

### Common Issues

**Library not found errors:**
- Ensure all required files are present for your platform
- Check that library versions match (e.g., `libtesseract.so.5` not `libtesseract.so.4`)

**Permission denied errors:**
- On Unix systems, ensure native binaries have execute permissions
- The extractor automatically sets execute permissions

**OCR not working:**
- Verify tessdata files are present and not corrupted  
- Check that at least `eng.traineddata` is available
- Enable debug logging to see detailed error messages

**Large JAR size:**
- Remove unused platform binaries if targeting specific platforms only
- Remove unused language data files
- Consider using compressed/quantized tessdata models if available

### Getting Help

If you encounter issues:
1. Check the application logs for detailed error messages
2. Verify all required files are present and properly placed
3. Test Tesseract binaries independently before embedding
4. Ensure your target platform is supported

### Manual Testing of Binaries

Before embedding, test binaries work correctly:

**Windows:**
```cmd
cd src\main\resources\native\windows-x64
.\tesseract.exe --version
```

**Linux/macOS:**
```bash
cd src/main/resources/native/linux-x64  # or macos-x64
chmod +x tesseract
./tesseract --version
```

The version command should complete without errors and show Tesseract version information.