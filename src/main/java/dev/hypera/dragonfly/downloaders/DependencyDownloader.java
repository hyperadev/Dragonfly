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

package dev.hypera.dragonfly.downloaders;

import dev.hypera.dragonfly.Dragonfly;
import dev.hypera.dragonfly.annotations.Downloader;
import dev.hypera.dragonfly.dependency.Dependency;
import dev.hypera.dragonfly.downloaders.impl.MavenDownloader;
import dev.hypera.dragonfly.downloaders.impl.URLDownloader;
import dev.hypera.dragonfly.exceptions.DownloadFailureException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

/**
 * Dependency downloader.
 *
 * @author Joshua Sing <joshua@hypera.dev>
 */
public class DependencyDownloader {

	private final @NotNull Dragonfly dragonfly;
	private final @NotNull Set<IDownloader<? extends Dependency>> downloaders = new HashSet<>();

	@Internal
	public DependencyDownloader(@NotNull Dragonfly dragonfly) {
		this.dragonfly = dragonfly;
		this.downloaders.add(new MavenDownloader());
		this.downloaders.add(new URLDownloader());
	}

	/**
	 * Attempt to download a list of Dependencies.
	 *
	 * @param dependencies Dependencies to be downloaded.
	 * @throws DownloadFailureException if something went wrong while downloading the dependencies.
	 */
	public void download(@NotNull List<Dependency> dependencies) throws DownloadFailureException {
		for (Dependency dependency : dependencies) {
			if (!Files.exists(dragonfly.getDirectory().resolve(dependency.getFileName()))) {
				download(dependency);
			}
		}
	}

	/**
	 * Attempt to download a dependency.
	 *
	 * @param dependency Dependency to be downloaded.
	 * @throws DownloadFailureException if something went wrong while downloading the dependency.
	 */
	private void download(@NotNull Dependency dependency) throws DownloadFailureException {
		Optional<IDownloader<Dependency>> downloader = getDownloader(dependency);
		if (downloader.isPresent()) {
			downloader.get().download(dragonfly, dependency);
		} else {
			throw new DownloadFailureException("Could not find downloader for " + dependency.getClass()
					.getCanonicalName());
		}
	}

	/**
	 * Get a downloader for the dependency type.
	 *
	 * @param dependency Dependency to get the downloader for.
	 * @param <T>        Dependency type.
	 * @return Dependency downloader.
	 */
	@SuppressWarnings("unchecked")
	private @NotNull <T extends Dependency> Optional<IDownloader<T>> getDownloader(@NotNull T dependency) {
		return downloaders.stream().filter(downloader -> downloader.getClass()
				.isAnnotationPresent(Downloader.class) && downloader.getClass().getAnnotation(Downloader.class).value()
				.equals(dependency.getClass())).map(d -> (IDownloader<T>) d).findFirst();
	}

}
