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
 * Defines how to create clipboard monitors for specific platforms.
 *
 * Each platform (Android, Windows, iOS, etc.) provides its own implementation
 * that knows how to create and configure the appropriate clipboard monitor.
 * The factory automatically selects the best available implementation based
 * on priority and platform compatibility.
 *
 * @since 1.0.0
 */
public interface PlatformStrategy {
    /**
     * The priority level for this platform implementation.
     *
     * When multiple implementations are available, the one with the highest
     * priority is selected. Use higher values for native implementations and
     * lower values for fallback implementations.
     *
     * **Recommended values:**
     * - Native implementations: 100
     * - Generic implementations: 50
     * - Fallback implementations: 0-49
     *
     * @return Priority value where higher numbers indicate higher priority
     */
    public val priority: Int get() = 50

    /**
     * Determines if this implementation can be used on the current platform.
     *
     * Implementations should check for required dependencies, APIs, or platform
     * features before returning true. This method should be fast as it's called
     * during initialization.
     *
     * @return true if this implementation can create monitors on the current platform
     */
    public fun isApplicable(): Boolean

    /**
     * Creates a new clipboard monitor using the provided configuration.
     *
     * Called by the factory after confirming this implementation is applicable
     * and has the highest priority. The returned monitor should be fully
     * initialized but not yet started.
     *
     * @param config The configuration settings for the monitor
     * @return A new clipboard monitor ready to be started
     * @throws IllegalStateException if required resources are unavailable
     */
    public fun createMonitor(config: ClipboardConfig): ClipboardMonitor
}
