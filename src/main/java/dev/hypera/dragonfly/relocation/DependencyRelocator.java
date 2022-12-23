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

package dev.hypera.dragonfly.relocation;

import dev.hypera.dragonfly.Dragonfly;
import dev.hypera.dragonfly.dependency.Dependency;
import dev.hypera.dragonfly.exceptions.DownloadFailureException;
import dev.hypera.dragonfly.exceptions.LoadFailureException;
import dev.hypera.dragonfly.exceptions.RelocationFailureException;
import dev.hypera.dragonfly.loading.DependencyLoader;
import dev.hypera.dragonfly.loading.DragonflyClassLoader;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Dependency relocator.
 *
 * @author Joshua Sing <joshua@hypera.dev>
 */
public class DependencyRelocator {

	private static final @NotNull String RELOCATED_FILENAME = "%s-relocated.jar";

	private final @NotNull Dragonfly dragonfly;
	private final boolean deleteOld;

	private final @NotNull DragonflyClassLoader classLoader;
	private final @NotNull DependencyLoader dependencyLoader;

	private boolean dependenciesLoaded;

	private @Nullable Constructor<?> constructor;
	private @Nullable Method method;

	@Internal
	public DependencyRelocator(@NotNull Dragonfly dragonfly, boolean deleteOld) {
		this.dragonfly = dragonfly;
		this.deleteOld = deleteOld;
		this.classLoader = new DragonflyClassLoader(getClass().getClassLoader());
		this.dependencyLoader = new DependencyLoader(dragonfly, classLoader::addURL);
	}


	/**
	 * Initialise relocator.
	 *
	 * @throws RelocationFailureException if something went wrong while initialising.
	 */
	private void init() throws RelocationFailureException {
		if (null == constructor && null == method) {
			try {
				Class<?> clazz = classLoader.loadClass("me.lucko.jarrelocator.JarRelocator");

				constructor = clazz.getDeclaredConstructor(File.class, File.class, Map.class);
				method = clazz.getDeclaredMethod("run");

				constructor.setAccessible(true);
				method.setAccessible(true);
			} catch (Exception ex) {
				throw new RelocationFailureException(ex);
			}
		}
	}


	/**
	 * Attempt to relocate a list of dependencies.
	 *
	 * @param dependencies Dependencies to be relocated.
	 * @throws RelocationFailureException if something went wrong while relocating the dependencies.
	 * @throws DownloadFailureException   if something went wrong while downloading internal dependencies.
	 * @throws LoadFailureException       if something went wrong while loading the internal dependencies.
	 */
	public void relocate(@NotNull List<Dependency> dependencies) throws RelocationFailureException, DownloadFailureException, LoadFailureException {
		loadInternalDependencies(getDependencies());
		init();

		for (Dependency dependency : dependencies) {
			if (dependency.getRelocations().size() > 0) {
				relocate(dependency);
			}
		}
	}


	/**
	 * Attempt to relocate a dependency.
	 *
	 * @param dependency Dependency to be relocated.
	 * @throws RelocationFailureException if something went wrong while relocating the dependency.
	 */
	private void relocate(@NotNull Dependency dependency) throws RelocationFailureException {
		try {
			Path relocatedPath = getRelocatedPath(dependency);
			if (Files.exists(relocatedPath)) {
				dependency.setFileName(getRelocatedFileName(dependency));
				dependency.setRelocated(true);
				return;
			}

			Map<String, String> relocations = new HashMap<>();
			dependency.getRelocations().forEach(r -> relocations.put(r.getFrom(), r.getTo()));

			Object object = constructor.newInstance(dragonfly.getDirectory().resolve(dependency.getFileName())
					.toFile(), relocatedPath.toFile(), relocations);
			method.invoke(object);

			if (deleteOld) {
				Files.delete(dragonfly.getDirectory().resolve(dependency.getFileName()));
			}

			dependency.setFileName(getRelocatedFileName(dependency));
			dependency.setRelocated(true);
		} catch (Exception ex) {
			throw new RelocationFailureException(ex);
		}
	}

	/**
	 * If a dependency has been relocated or not.
	 *
	 * @param dependency Dependency.
	 * @return If the given dependency has been relocated or not.
	 */
	public boolean isRelocated(@NotNull Dependency dependency) {
		if (dependency.getRelocations().size() > 0) {
			return Files.exists(getRelocatedPath(dependency));
		} else {
			return false;
		}
	}

	/**
	 * Get the relocated path of a dependency.
	 *
	 * @param dependency Dependency to get the relocated path of.
	 * @return Relocated path of the given dependency.
	 */
	public @NotNull Path getRelocatedPath(@NotNull Dependency dependency) {
		return dragonfly.getDirectory().resolve(getRelocatedFileName(dependency));
	}

	/**
	 * Get the relocated filename of a dependency.
	 *
	 * @param dependency Dependency to get the relocated filename of.
	 * @return Relocated filename of the given dependency.
	 */
	public @NotNull String getRelocatedFileName(@NotNull Dependency dependency) {
		return String.format(RELOCATED_FILENAME, dependency.getFileName().split("\\.jar")[0]);
	}

	/**
	 * Attempt to download and load internal dependencies.
	 *
	 * @param dependencies Dependencies to be downloaded and loaded.
	 * @throws DownloadFailureException if something went wrong while downloading the dependencies.
	 * @throws LoadFailureException     if something went wrong while loading the dependencies.
	 */
	private void loadInternalDependencies(List<Dependency> dependencies) throws DownloadFailureException, LoadFailureException {
		if (!dependenciesLoaded) {
			dragonfly.getDependencyDownloader().download(dependencies);
			dependencyLoader.load(dependencies);
			dependenciesLoaded = true;
		}
	}

	/**
	 * Get internal dependencies.
	 *
	 * @return Internal dependencies.
	 */
	public @NotNull List<Dependency> getDependencies() {
		return Arrays.asList(
				Dependency.maven(-3, "org.ow2.asm", "asm", "9.2"),
				Dependency.maven(-2, "org.ow2.asm", "asm-commons", "9.2"),
				Dependency.maven(-1, "me.lucko", "jar-relocator", "1.5")
		);
	}

}
