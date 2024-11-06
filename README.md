# Burp API Header Masker

A Burp Suite extension that automatically masks the values of headers containing "API" (case-insensitive) in both the Proxy history and Repeater tabs. This is useful when creating documentation, PoC's etc, and prevents having to redact screenshots etc.

- [Burp API Header Masker](#burp-api-header-masker)
  - [Features](#features)
  - [Prerequisites](#prerequisites)
  - [Building from Source](#building-from-source)
  - [Installation in Burp Suite](#installation-in-burp-suite)
  - [Development Environment Setup](#development-environment-setup)
  - [Building with Different Java Versions](#building-with-different-java-versions)
  - [Troubleshooting](#troubleshooting)

## Features

- Detects headers containing "API" (case-insensitive)
- Masks header values with asterisks (`********`)
- Works in both Proxy history and Repeater tabs
- Preserves original header names
- Processes both requests and responses

## Prerequisites

- Java Development Kit (JDK) 17 or later
- Gradle (included via wrapper)
- Burp Suite Professional

## Building from Source

1. Clone the repository:
```bash
git clone https://github.com/GangGreenTemperTatum/burp-api-header-masker.git
cd burp-api-header-masker
```

2. Initialize the Gradle wrapper:
```bash
gradle wrapper
```

3. Build the extension using Gradle:
```bash
# On Unix-like systems:
./gradlew build

# On Windows:
.\gradlew.bat build
```

The compiled JAR file will be created in `build/libs/burp-api-header-masker-1.0-SNAPSHOT.jar`

## Installation in Burp Suite

1. Open Burp Suite Professional
2. Go to the "Extensions" tab
3. Click the "Add" button
4. Set "Extension type" to "Java"
5. Click "Select file" and choose the compiled JAR file from `build/libs/burp-api-header-masker-1.0-SNAPSHOT.jar`
6. Click "Next" to load the extension

## Development Environment Setup

1. Create the project structure:
```bash
mkdir -p src/main/java/burp
mkdir -p gradle/wrapper
```

2. Copy the source files into the appropriate directories:
   - `APIHeaderMasker.java` → `src/main/java/burp/`
   - `build.gradle` → project root
   - `settings.gradle` → project root

3. Initialize the Gradle wrapper:
```bash
gradle wrapper
```

## Building with Different Java Versions

If you need to use a different Java version, modify the following lines in `build.gradle`:

```gradle
sourceCompatibility = '17'
targetCompatibility = '17'
```

Replace `'17'` with your desired Java version.

## Troubleshooting

If you encounter build errors:

1. Verify Java version:
```bash
java -version
```

2. Ensure Gradle is using the correct Java version:
```bash
./gradlew --version
```

3. Clean and rebuild:
```bash
./gradlew clean build
```
