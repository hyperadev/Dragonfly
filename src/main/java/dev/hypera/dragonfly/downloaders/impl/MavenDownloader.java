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

package dev.hypera.dragonfly.downloaders.impl;

import dev.hypera.dragonfly.annotations.Downloader;
import dev.hypera.dragonfly.downloaders.IDownloader;
import dev.hypera.dragonfly.dependency.impl.MavenDependency;
import dev.hypera.dragonfly.exceptions.ResolveFailureException;
import dev.hypera.dragonfly.resolvers.impl.MavenResolver;
import dev.hypera.dragonfly.resolvers.impl.MavenSnapshotResolver;
import dev.hypera.dragonfly.Dragonfly;
import dev.hypera.dragonfly.exceptions.DownloadFailureException;
import org.jetbrains.annotations.NotNull;

/**
 * Maven dependency downloader.
 *
 * @author Joshua Sing <joshua@hypera.dev>
 */
@Downloader(MavenDependency.class)
public class MavenDownloader implements IDownloader<MavenDependency> {

	private final @NotNull MavenResolver resolver = new MavenResolver();
	private final @NotNull MavenSnapshotResolver snapshotResolver = new MavenSnapshotResolver();

	@Override
	public void download(@NotNull Dragonfly dragonfly, @NotNull MavenDependency dependency) throws DownloadFailureException {
		String url;
		if (dependency.getVersion().contains("-SNAPSHOT")) {
			url = snapshotResolver.resolve(dragonfly, dependency);
		} else {
			url = resolver.resolve(dragonfly, dependency);
		}

		if (null == url) {
			throw new ResolveFailureException("Cannot resolve dependency: " + dependency);
		}

		download(url, dragonfly.getTimeout(), dragonfly.getDirectory().resolve(dependency.getFileName()));
	}

}
