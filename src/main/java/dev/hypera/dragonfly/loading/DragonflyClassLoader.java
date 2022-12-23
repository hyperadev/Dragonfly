/*
 * Dragonfly - Runtime dependency management library
 *  Copyright (c) 2021 Joshua Sing <joshua@hypera.dev>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package dev.hypera.dragonfly.loading;

import java.net.URL;
import java.net.URLClassLoader;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

/**
 * Dragonfly class loader, a child-first {@link URLClassLoader} used for loading Dragonfly's internal dependencies.
 *
 * @author Joshua Sing <joshua@hypera.dev>
 */
@Internal
public class DragonflyClassLoader extends URLClassLoader {

	public DragonflyClassLoader(@NotNull ClassLoader classLoader) {
		super(new URL[0], classLoader);
	}

	@Override
	public void addURL(@NotNull URL url) {
		super.addURL(url);
	}

	@Override
	protected @NotNull Class<?> loadClass(@NotNull String name, boolean resolve) throws ClassNotFoundException {
		Class<?> loadedClass = findLoadedClass(name);
		if (null == loadedClass) {
			try {
				loadedClass = findClass(name);
			} catch (ClassNotFoundException ex) {
				loadedClass = super.loadClass(name, resolve);
			}
		}

		if (resolve) {
			resolveClass(loadedClass);
		}

		return loadedClass;
	}

}
