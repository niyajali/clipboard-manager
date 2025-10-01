/**
 * Copyright 2025 Sk Niyaj Ali
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.niyajali.clipboard.manager

/**
 * WebAssembly JavaScript platform implementation.
 */
private class WasmJSStrategy : PlatformStrategy {
    override val priority: Int = 100

    override fun isApplicable(): Boolean = true

    override fun createMonitor(config: ClipboardConfig): ClipboardMonitor {
        return StubClipboardMonitor()
    }
}

/**
 * Creates clipboard monitors for WebAssembly JavaScript.
 *
 * **Experimental:** WasmJS support is limited and uses the same browser
 * Clipboard API as the regular JS target.
 *
 * For production use, prefer the JS target which has full clipboard monitoring support.
 *
 * @since 1.0.0
 */
public actual object ClipboardMonitorFactory {
    internal actual val registry: PlatformRegistry = PlatformRegistry()

    init {
        registry.register(WasmJSStrategy())
    }

    /**
     * Creates a clipboard monitor with the specified configuration.
     *
     * **Note:** WasmJS is experimental and not fully supported.
     * Use the JS target for browser clipboard monitoring.
     *
     * @param config The configuration for the monitor
     * @return A new clipboard monitor
     * @throws UnsupportedOperationException WasmJS is not yet fully supported
     */
    public actual fun create(config: ClipboardConfig): ClipboardMonitor {
        val strategy = registry.selectStrategy()
            ?: throw UnsupportedOperationException(
                "No applicable clipboard strategy found for WasmJS platform. " +
                    "Use the JS target for browser clipboard monitoring.",
            )

        return strategy.createMonitor(config)
    }

    /**
     * Creates a clipboard monitor with default configuration.
     *
     * @param listener The listener to receive clipboard change notifications
     * @return A new Android clipboard monitor with default settings
     * @throws IllegalStateException if the factory has not been initialized
     */
    public actual fun create(listener: ClipboardListener): ClipboardMonitor {
        return create(ClipboardConfig.default(listener))
    }
}
