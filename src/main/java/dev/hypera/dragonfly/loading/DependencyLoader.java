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

import dev.hypera.dragonfly.Dragonfly;
import dev.hypera.dragonfly.dependency.Dependency;
import dev.hypera.dragonfly.exceptions.LoadFailureException;
import java.net.MalformedURLException;
import java.util.List;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

/**
 * Dependency loader.
 *
 * @author Joshua Sing <joshua@hypera.dev>
 */
public class DependencyLoader {

	private final @NotNull Dragonfly dragonfly;
	private final @NotNull IClassLoader classLoader;

	@Internal
	public DependencyLoader(@NotNull Dragonfly dragonfly, @NotNull IClassLoader classLoader) {
		this.dragonfly = dragonfly;
		this.classLoader = classLoader;
	}

	/**
	 * Attempt to load a list of dependencies.
	 *
	 * @param dependencies Dependencies to be loaded.
	 * @throws LoadFailureException if something went wrong while loading the dependencies.
	 */
	public void load(@NotNull List<Dependency> dependencies) throws LoadFailureException {
		for (Dependency dependency : dependencies) {
			load(dependency);
		}
	}

	/**
	 * Attempt to load a dependency.
	 *
	 * @param dependency Dependency to be loaded.
	 * @throws LoadFailureException if something went wrong while loading the dependency.
	 */
	private void load(@NotNull Dependency dependency) throws LoadFailureException {
		try {
			classLoader.addURL(dragonfly.getDirectory().resolve(dependency.getFileName()).toUri().toURL());
		} catch (MalformedURLException ex) {
			throw new LoadFailureException(ex);
		}
	}

}
