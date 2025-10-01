# Clipboard Manager

A Kotlin Multiplatform library for monitoring system clipboard changes across all major platforms.

## Overview

Clipboard Manager provides a unified interface for tracking clipboard changes in real-time. The
library automatically selects the optimal implementation based on your target platform, using native
clipboard APIs for efficient monitoring.

## Supported Platforms

- Android (API 21+)
- JVM Desktop (Windows, macOS, Linux)
- iOS (iosArm64, iosX64, iosSimulatorArm64)
- JavaScript (Browser)
- WebAssembly JavaScript

## Installation

Add the dependency to your project:

```kotlin
dependencies {
    implementation("io.github.niyajali:clipboard-manager:1.0.1")
}
```

## Quick Start

### Basic Usage

```kotlin
val listener = object : ClipboardListener {
    override fun onClipboardChange(content: ClipboardContent) {
        println("Clipboard changed: ${content.text}")
    }
}

// Simple usage (backward compatible)
val monitor = ClipboardMonitorFactory.create(listener)
monitor.start()

// Or using new Builder API (recommended)
val monitor = ClipboardMonitor.Builder()
    .setListener(listener)
    .build()
monitor.start()

// When finished
monitor.stop()
```

### Configurable Usage

```kotlin
val monitor = ClipboardMonitor.Builder()
    .setListener(myListener)
    .setDebounceDelay(100)              // Android debouncing (0-1000ms)
    .setPollingInterval(250)            // Polling for macOS/Linux/iOS/JS (50-5000ms)
    .enableDuplicateFiltering(true)     // Filter duplicate clipboard content
    .setErrorHandler { error ->         // Custom error handling
        logger.error("Clipboard error", error)
    }
    .build()

monitor.start()
```

### Preset Configurations

```kotlin
// High-frequency monitoring (clipboard history apps)
val monitor = ClipboardMonitor.Builder()
    .setListener(listener)
    .useHighFrequencyPreset()  // 0ms debounce, 100ms polling
    .build()

// Low-power monitoring (mobile/battery-conscious apps)
val monitor = ClipboardMonitor.Builder()
    .setListener(listener)
    .useLowPowerPreset()       // 200ms debounce, 1000ms polling
    .build()

// Balanced monitoring (default settings)
val monitor = ClipboardMonitor.Builder()
    .setListener(listener)
    .useBalancedPreset()       // 50ms debounce, 200ms polling
    .build()
```

### Android Initialization

The library initializes automatically via AndroidX Startup. No manual setup required.

For manual initialization (if AndroidX Startup is disabled):

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ClipboardMonitorFactory.init(this)
    }
}
```

### Kotlin Flow Integration

```kotlin
// Monitor all clipboard changes
ClipboardMonitorFactory.create(listener)
    .asFlow()
    .collect { content ->
        println("Content: ${content.text}")
    }

// Monitor only text changes
ClipboardMonitorFactory.create(listener)
    .textFlow()
    .collect { text ->
        println("Text: $text")
    }

// Monitor file changes
ClipboardMonitorFactory.create(listener)
    .filesFlow()
    .collect { files ->
        println("Files: ${files.joinToString()}")
    }

// Monitor image availability
ClipboardMonitorFactory.create(listener)
    .imageAvailableFlow()
    .collect { hasImage ->
        println("Image available: $hasImage")
    }
```

## Core Components

### ClipboardMonitor

Main interface for monitoring clipboard changes. Provides lifecycle methods for starting and
stopping monitoring.

**Methods:**

- `start()` - Begins monitoring clipboard changes
- `stop()` - Stops monitoring and releases resources
- `isRunning()` - Returns monitoring status
- `getCurrentContent()` - Retrieves current clipboard content synchronously

### ClipboardListener

Callback interface for receiving clipboard change notifications.

**Method:**

- `onClipboardChange(content: ClipboardContent)` - Called when clipboard content changes

### ClipboardContent

Data class representing clipboard content with support for multiple formats.

**Properties:**

- `text: String?` - Plain text content
- `html: String?` - HTML formatted content
- `rtf: String?` - Rich Text Format content
- `files: List<String>?` - List of file paths or URIs
- `imageAvailable: Boolean` - Indicates if image is present
- `timestamp: Long` - Unix timestamp in milliseconds

**Helper Methods:**

- `isEmpty()` - Checks if clipboard is empty
- `isNotEmpty()` - Checks if clipboard has content
- `toString()` - Returns concise content summary

### ClipboardMonitorBuilder

Fluent builder for creating configured clipboard monitors.

**Configuration Methods:**

- `setListener(listener)` - Set clipboard change listener (required)
- `setDebounceDelay(ms)` - Set debounce delay for Android (0-1000ms, default: 50ms)
- `setPollingInterval(ms)` - Set polling interval for platforms that use polling (50-5000ms,
  default: 200ms)
- `enableDuplicateFiltering(enable)` - Enable/disable duplicate content filtering (default: true)
- `setErrorHandler(handler)` - Set custom error handler (optional)

**Preset Methods:**

- `useHighFrequencyPreset()` - Optimized for responsiveness (0ms debounce, 100ms polling)
- `useLowPowerPreset()` - Optimized for battery life (200ms debounce, 1000ms polling)
- `useBalancedPreset()` - Balanced settings (50ms debounce, 200ms polling)

**Build Methods:**

- `buildConfig()` - Build configuration only (returns ClipboardConfig)
- `build()` - Build and create monitor (returns ClipboardMonitor)

### ClipboardConfig

Immutable configuration data class for clipboard monitors.

**Properties:**

- `listener: ClipboardListener` - Clipboard change listener
- `debounceDelayMs: Long` - Debounce delay in milliseconds (default: 50ms)
- `pollingIntervalMs: Long` - Polling interval in milliseconds (default: 200ms)
- `enableDuplicateFiltering: Boolean` - Enable duplicate filtering (default: true)
- `errorHandler: ((Throwable) -> Unit)?` - Optional error handler

**Factory Methods:**

- `default(listener)` - Create config with default settings
- `highFrequency(listener)` - Create high-frequency config
- `lowPower(listener)` - Create low-power config

### ClipboardMonitorFactory

Factory object for creating platform-specific clipboard monitors using Strategy Pattern.

**Methods:**

- `create(config: ClipboardConfig)` - Create monitor with configuration
- `create(listener: ClipboardListener)` - Create monitor with default configuration (backward
  compatible)

**Android-specific:**

- `init(context: Context)` - Manual initialization (optional)
- `isInitialized()` - Check initialization status

## Configuration Options

| Option                     | Default | Range     | Applicable Platforms  |
|----------------------------|---------|-----------|-----------------------|
| `debounceDelayMs`          | 50ms    | 0-1000ms  | Android, Windows      |
| `pollingIntervalMs`        | 200ms   | 50-5000ms | macOS, Linux, iOS, JS |
| `enableDuplicateFiltering` | true    | boolean   | All platforms         |
| `errorHandler`             | null    | function  | All platforms         |

## Platform Implementations

### Android

Uses Android ClipboardManager with OnPrimaryClipChangedListener.

**Features:**

- Event-driven monitoring (no polling)
- Configurable debouncing (default: 50ms)
- Duplicate detection via content signatures
- Main thread callbacks
- Supports text, HTML, URIs, and image detection

**Limitations:**

- RTF format not supported (always null)

**Permissions:**
No special permissions required.

### JVM Desktop

#### Windows

Uses native Win32 clipboard notifications via message-only window.

**Features:**

- Native clipboard change events
- Minimal CPU usage
- Configurable duplicate filtering
- Supports text, HTML, RTF, files, and images
- Background thread callbacks

**Implementation:**

- `AddClipboardFormatListener` Win32 API
- JNA (Java Native Access) for native interop
- Message-only window for event handling

#### macOS and Linux

Uses AWT Toolkit clipboard with periodic polling.

**Features:**

- Configurable polling interval (default: 200ms)
- Supports text, HTML, RTF, files, and images
- Background thread callbacks
- Automatic resource cleanup

**Implementation:**

- `Toolkit.getDefaultToolkit().systemClipboard`
- `ScheduledExecutorService` for polling

### iOS

Uses UIPasteboard with change count polling.

**Features:**

- Configurable polling interval (default: 500ms)
- Main thread callbacks
- Supports text, HTML, URLs, and image detection
- RTF support depends on pasteboard content

**Permissions:**
iOS 14+ shows paste notification banner on first access. No explicit permission required.

### JavaScript and WebAssembly

Uses browser Clipboard API with periodic polling.

**Features:**

- Configurable polling interval (default: 500ms)
- Supports text content reliably
- HTML and file support varies by browser

**Requirements:**

- Secure context (HTTPS or localhost)
- Modern browser with Clipboard API support
- User permission for clipboard access

**Limitations:**

- No native change events
- RTF format not supported
- Limited file access due to browser security
- Permission prompt on first access

**Browser Compatibility:**

- Chrome/Edge 88+: Full support
- Firefox 90+: Full support
- Safari: Partial support (requires user interaction)

## Extension Functions

### Flow Extensions

```kotlin
// All clipboard changes as Flow
fun ClipboardMonitor.asFlow(): Flow<ClipboardContent>

// Only text changes
fun ClipboardMonitor.textFlow(includeEmpty: Boolean = false): Flow<String?>

// Only file changes
fun ClipboardMonitor.filesFlow(): Flow<List<String>>

// Image availability changes
fun ClipboardMonitor.imageAvailableFlow(): Flow<Boolean>
```

### Result Extensions

```kotlin
// Transform successful results
fun <T, R> ClipboardResult<T>.map(transform: (T) -> R): ClipboardResult<R>

// Chain result operations
fun <T, R> ClipboardResult<T>.flatMap(transform: (T) -> ClipboardResult<R>): ClipboardResult<R>

// Extract values
fun <T> ClipboardResult<T>.getOrNull(): T?
fun <T> ClipboardResult<T>.getOrDefault(default: T): T
fun <T> ClipboardResult<T>.getOrThrow(): T

// Callbacks for different result types
fun <T> ClipboardResult<T>.onSuccess(block: (T) -> Unit): ClipboardResult<T>
fun <T> ClipboardResult<T>.onEmpty(block: () -> Unit): ClipboardResult<T>
fun <T> ClipboardResult<T>.onPermissionDenied(block: (String?) -> Unit): ClipboardResult<T>
fun <T> ClipboardResult<T>.onError(block: (String, Throwable?) -> Unit): ClipboardResult<T>

// Status checks
fun <T> ClipboardResult<T>.isSuccess(): Boolean
fun <T> ClipboardResult<T>.isFailure(): Boolean
fun <T> ClipboardResult<T>.isEmpty(): Boolean
```

## Thread Safety

All monitor implementations are thread-safe and can be called from any thread.

**Callback Threading:**

- **Android**: Main thread (UI thread)
- **Windows**: Dedicated message loop thread
- **macOS/Linux**: Scheduled executor thread
- **iOS**: Main thread
- **JS/WasmJS**: Event loop (main thread)

UI updates are safe from callbacks on Android and iOS. Other platforms require proper thread
handling.

## Resource Management

Always call `stop()` when finished monitoring to release resources:

```kotlin
val monitor = ClipboardMonitor.Builder()
    .setListener(listener)
    .build()

try {
    monitor.start()
    // ... use monitor ...
} finally {
    monitor.stop()
}
```

Or use Kotlin coroutines with automatic cleanup:

```kotlin
coroutineScope {
    ClipboardMonitor.Builder()
        .setListener(listener)
        .build()
        .asFlow()
        .collect { content ->
            // Process content
        }
    // Automatically cleaned up when scope exits
}
```

## Content Type Support

| Content Type | Android | Windows | macOS | Linux | iOS  | JS/WasmJS |
|--------------|---------|---------|-------|-------|------|-----------|
| Plain Text   | Yes     | Yes     | Yes   | Yes   | Yes  | Yes       |
| HTML         | Yes     | Yes     | Yes   | Yes   | Yes  | Limited   |
| RTF          | No      | Yes     | Yes   | Yes   | Yes  | No        |
| Files        | Yes¹    | Yes     | Yes   | Yes   | Yes² | Limited   |
| Images       | Yes³    | Yes³    | Yes³  | Yes³  | Yes³ | Limited   |

¹ Android provides content URIs (`content://...`)  
² iOS provides file paths via URLs  
³ Detection only; actual image data requires platform-specific APIs

## Error Handling

The library handles errors gracefully with configurable error handling:

```kotlin
val monitor = ClipboardMonitor.Builder()
    .setListener(myListener)
    .setErrorHandler { error ->
        when (error) {
            is SecurityException -> Log.e("Clipboard", "Permission denied")
            is IllegalStateException -> Log.e("Clipboard", "Invalid state")
            else -> Log.e("Clipboard", "Error: ${error.message}", error)
        }
    }
    .build()
```

Listener exceptions are caught internally to prevent monitoring interruption.

## Performance Characteristics

### Android

- Minimal overhead (event-driven)
- No polling
- Configurable debouncing prevents excessive callbacks

### Windows

- Minimal CPU usage (event-driven)
- Native OS notifications
- No polling

### macOS and Linux

- Configurable polling overhead (default: 200ms)
- Lightweight clipboard reads

### iOS

- Configurable polling overhead (default: 500ms)
- Efficient change count checks
- Full clipboard read only on changes

### JavaScript and WebAssembly

- Configurable polling overhead (default: 500ms)
- Permission prompt may delay first read
- Limited to text content in most cases

## Example Applications

### Clipboard History Manager

```kotlin
class ClipboardHistory {
    private val history = mutableListOf<ClipboardContent>()
    private val monitor = ClipboardMonitor.Builder()
        .setListener(object : ClipboardListener {
            override fun onClipboardChange(content: ClipboardContent) {
                if (content.isNotEmpty()) {
                    history.add(content)
                    if (history.size > 100) {
                        history.removeAt(0)
                    }
                }
            }
        })
        .useHighFrequencyPreset()  // Immediate updates
        .build()

    fun start() = monitor.start()
    fun stop() = monitor.stop()
    fun getHistory(): List<ClipboardContent> = history.toList()
}
```

### Clipboard Synchronization

```kotlin
class ClipboardSync(private val remoteSync: suspend (String) -> Unit) {
    private val monitor = ClipboardMonitor.Builder()
        .setListener(object : ClipboardListener {
            override fun onClipboardChange(content: ClipboardContent) {
                content.text?.let { text ->
                    coroutineScope.launch {
                        remoteSync(text)
                    }
                }
            }
        })
        .setDebounceDelay(100)  // Debounce before syncing
        .setErrorHandler { error ->
            logger.error("Sync failed", error)
        }
        .build()

    fun start() = monitor.start()
    fun stop() = monitor.stop()
}
```

### Mobile Battery-Efficient Monitoring

```kotlin
// Android or iOS
val monitor = ClipboardMonitor.Builder()
    .setListener(myListener)
    .useLowPowerPreset()  // Battery-conscious settings
    .setErrorHandler { error ->
        Timber.e(error, "Clipboard error")
    }
    .build()
```

## Dependency Injection

### Koin

```kotlin
single<ClipboardMonitor> {
    ClipboardMonitor.Builder()
        .setListener(get())
        .setDebounceDelay(100)
        .build()
}
```

### Hilt

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object ClipboardModule {
    @Provides
    @Singleton
    fun provideClipboardMonitor(
        listener: ClipboardListener,
        @ApplicationContext context: Context
    ): ClipboardMonitor {
        ClipboardMonitorFactory.init(context)
        return ClipboardMonitor.Builder()
            .setListener(listener)
            .setDebounceDelay(100)
            .build()
    }
}
```

## License

```
Copyright 2025 Sk Niyaj Ali

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## Contributing

Contributions are welcome. Please ensure:

- Code follows existing style conventions
- Platform-specific code goes in appropriate source sets
- Public API is documented with professional KDoc
- Changes maintain backward compatibility
- Tests are added for new features

## Links

- Repository: https://github.com/niyajali/clipboard-manager
- Issues: https://github.com/niyajali/clipboard-manager/issues

## Author

Sk Niyaj Ali - https://github.com/niyajali
