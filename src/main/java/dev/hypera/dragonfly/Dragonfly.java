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

package dev.hypera.dragonfly;

import dev.hypera.dragonfly.dependency.Dependency;
import dev.hypera.dragonfly.downloaders.DependencyDownloader;
import dev.hypera.dragonfly.loading.DependencyLoader;
import dev.hypera.dragonfly.loading.IClassLoader;
import dev.hypera.dragonfly.objects.Status;
import dev.hypera.dragonfly.relocation.DependencyRelocator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.jetbrains.annotations.ApiStatus.Internal;

/**
 * Main Dragonfly class.
 * @author Joshua Sing <joshua@hypera.dev>
 */
public class Dragonfly {

	private static final String VERSION = "0.3.0-SNAPSHOT";

	private final int timeout;
	private final Path directory;
	private final Set<String> repositories;
	private final Consumer<Status> statusHandler;

	private final DependencyDownloader dependencyDownloader = new DependencyDownloader(this);
	private final DependencyRelocator dependencyRelocator;
	private final DependencyLoader dependencyLoader;


	@Internal
	protected Dragonfly(int timeout, IClassLoader classLoader, Path directory, Set<String> repositories, boolean delete, Consumer<Status> statusHandler) throws IOException {
		this.timeout = timeout;
		this.directory = directory;
		this.repositories = repositories;
		this.statusHandler = statusHandler;

		this.dependencyRelocator = new DependencyRelocator(this, delete);
		this.dependencyLoader = new DependencyLoader(this, classLoader);

		if (!Files.exists(directory)) {
			Files.createDirectories(directory);
		}
	}

	public static String getVersion() {
		return VERSION;
	}

	/**
	 * Download, relocate and load dependencies.
	 *
	 * @param dependencies Dependencies to be loaded.
	 * @return If the load was successful, in the form of a {@link CompletableFuture<Boolean>}.
	 */
	public CompletableFuture<Boolean> load(Dependency... dependencies) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				statusHandler.accept(Status.STARTING);
				List<Dependency> dependencyList = Arrays.stream(dependencies)
						.sorted(Comparator.comparingInt(Dependency::getPriority)).collect(Collectors.toList());

				if (dependencyList.size() > 0) {
					List<Dependency> downloadList = dependencyList.stream().filter(d -> !isDownloaded(d))
							.collect(Collectors.toList());
					if (downloadList.size() > 0) {
						statusHandler.accept(Status.DOWNLOADING);
						dependencyDownloader.download(downloadList);

						if (downloadList.stream().anyMatch(d -> d.getRelocations().size() > 0)) {
							statusHandler.accept(Status.RELOCATING);
							dependencyRelocator.relocate(downloadList);
						}
					}

					dependencyList.stream().filter(d -> d.getRelocations().size() > 0 && !d.isRelocated())
							.forEach(d -> d.setFileName(dependencyRelocator.getRelocatedFileName(d)));

					statusHandler.accept(Status.LOADING);
					dependencyLoader.load(dependencyList);
				}

				statusHandler.accept(Status.FINISHED);
				return true;
			} catch (Exception ex) {
				throw new IllegalStateException(ex);
			}
		});
	}

	/**
	 * If a dependency has been downloaded or not.
	 *
	 * @param dependency Dependency.
	 * @return If the given dependency has been downloaded or not.
	 */
	private boolean isDownloaded(Dependency dependency) {
		if (dependencyRelocator.isRelocated(dependency)) {
			return true;
		} else {
			return Files.exists(directory.resolve(dependency.getFileName()));
		}
	}

	public int getTimeout() {
		return timeout;
	}

	public Path getDirectory() {
		return directory;
	}

	public Set<String> getRepositories() {
		return repositories;
	}

	/**
	 * Get {@link DependencyDownloader}, for internal use only.
	 *
	 * @return Stored instance of {@link DependencyDownloader}.
	 */
	@Internal
	public DependencyDownloader getDependencyDownloader() {
		return dependencyDownloader;
	}

}
