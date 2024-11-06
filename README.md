# Burp API Header Masker

A Burp Suite extension that automatically masks sensitive values in both the Proxy history and message viewer. This is useful when creating documentation, PoC's etc, and prevents having to redact screenshots etc.

- [Burp API Header Masker](#burp-api-header-masker)
  - [Features](#features)
  - [Python Version](#python-version)
    - [Features](#features-1)
    - [Installation](#installation)
    - [Usage](#usage)
  - [Java Version (Under Development)](#java-version-under-development)
    - [Prerequisites](#prerequisites)
    - [Building from Source](#building-from-source)
    - [Installation](#installation-1)
  - [Development](#development)
    - [Python Extension Structure](#python-extension-structure)
    - [Key Components](#key-components)
  - [Author](#author)

## Features

- Detects and masks sensitive values in headers and body content
- Preserves original data while masking display values
- Works in both Proxy history and message viewer
- Useful for documentation and screenshots without manual redaction

## Python Version

### Features
- Automatically masks sensitive values containing:
  - 'token'
  - 'secret'
  - 'api'
  - JWT tokens (starting with 'ey')
- Provides an "Original Message" tab in the message viewer to see unmasked values
- Masks values only in the display, preserving original request/response data
- Works with both requests and responses
- No impact on actual intercepted traffic

### Installation
1. Install Jython standalone JAR (if not already installed):
   - Download from: https://www.jython.org/download
   - In Burp Suite: Extender > Options > Python Environment
   - Select the Jython standalone JAR

2. Load the extension:
   - Extender > Add
   - Set Extension Type to Python
   - Select `apiheadermasker.py`
   - Click Next

### Usage
1. Proxy History:
   - Sensitive values are automatically masked in the display
   - Original traffic is not modified

2. Message Viewer:
   - Double-click any request/response in Proxy history
   - Look for the "Original Message" tab
   - This tab shows the unmasked, original content
   - Switch between masked (in main tabs) and unmasked (in Original Message tab) views

## Java Version (Under Development)

### Prerequisites
- Java Development Kit (JDK) 17 or later
- Gradle (included via wrapper)
- Burp Suite Professional

### Building from Source
1. Clone the repository:
```bash
git clone https://github.com/GangGreenTemperTatum/burp-api-header-masker.git
cd burp-api-header-masker
```

2. Initialize Gradle wrapper:
```bash
gradle wrapper
```

3. Build the extension:
```bash
# Unix-like systems:
./gradlew build

# Windows:
.\gradlew.bat build
```

### Installation
1. Open Burp Suite Professional
2. Go to Extensions tab
3. Click Add
4. Set Extension Type to Java
5. Select the compiled JAR
6. Click Next

## Development

### Python Extension Structure
```
burp-api-header-masker/
├── apiheadermasker.py    # Main extension code
└── README.md
```

### Key Components
- `BurpExtender`: Main extension class
- `processProxyMessage`: Handles masking of values
- `OriginalMessageTab`: Provides access to unmasked values
- Masking patterns:
  ```python
  sensitive_words = ['token', 'secret', 'api', 'ey']
  jwt_pattern = r'(eyJ[a-zA-Z0-9_-]*\.eyJ[a-zA-Z0-9_-]*\.[a-zA-Z0-9_-]*)'
  ```

## Author
@GangGreenTemperTatum