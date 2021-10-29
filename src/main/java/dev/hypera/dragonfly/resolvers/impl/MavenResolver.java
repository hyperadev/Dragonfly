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

package dev.hypera.dragonfly.resolvers.impl;

import dev.hypera.dragonfly.Dragonfly;
import dev.hypera.dragonfly.dependency.impl.MavenDependency;
import dev.hypera.dragonfly.exceptions.DownloadFailureException;
import dev.hypera.dragonfly.exceptions.ResolveFailureException;
import dev.hypera.dragonfly.resolvers.IResolver;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Maven resolver.
 *
 * @author Joshua Sing <joshua@hypera.dev>
 */
public class MavenResolver implements IResolver<MavenDependency> {

	private static final String FORMAT = "%s%s/%s/%s/%s-%s.jar";

	@Override
	public @Nullable String resolve(@NotNull Dragonfly dragonfly, @NotNull MavenDependency dependency) throws ResolveFailureException {
		Set<String> urls = getUrls(dragonfly, dependency);
		if (urls.isEmpty()) {
			throw new ResolveFailureException("Cannot resolve dependency: " + dependency);
		}

		String resolvedUrl = null;
		for (String url : urls) {
			if (get(url, dragonfly.getTimeout()) != null) {
				resolvedUrl = url;
				break;
			}
		}

		if (null == resolvedUrl) {
			throw new ResolveFailureException("Cannot resolve dependency: " + dependency);
		}

		return resolvedUrl;
	}

	private Set<String> getUrls(@NotNull Dragonfly dragonfly, @NotNull MavenDependency dependency) {
		return dragonfly.getRepositories().stream().map(repo -> String.format(
				FORMAT, repo,
				dependency.getGroupId().replace(".", "/"),
				dependency.getArtifactId(),
				dependency.getVersion(),
				dependency.getArtifactId(),
				dependency.getVersion())
		).collect(Collectors.toSet());
	}

}
