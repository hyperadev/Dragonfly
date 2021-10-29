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

package dev.hypera.dragonfly.resolvers;

import dev.hypera.dragonfly.Dragonfly;
import dev.hypera.dragonfly.dependency.Dependency;
import dev.hypera.dragonfly.exceptions.ResolveFailureException;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Resolver interface.
 *
 * @param <T> Dependency type.
 * @author Joshua Sing <joshua@hypera.dev>
 */
public interface IResolver<T extends Dependency> {

	/**
	 * Resolve dependency.
	 *
	 * @param dragonfly  {@link Dragonfly} instance.
	 * @param dependency {@link T} to be resolved.
	 * @return Resolved dependency url, if found, otherwise null.
	 * @throws ResolveFailureException if something went wrong while resolving the dependency.
	 */
	@Nullable String resolve(@NotNull Dragonfly dragonfly, @NotNull T dependency) throws ResolveFailureException;

	/**
	 * Attempts to get data from a URL.
	 *
	 * @param downloadUrl URL to get data from.
	 * @param timeout     Http timeout.
	 * @return Data from url or null.
	 * @throws ResolveFailureException if something went wrong while retrieving the data.
	 */
	@Internal
	default @Nullable String get(@NotNull String downloadUrl, int timeout) throws ResolveFailureException {
		try {
			URL url = new URL(downloadUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("User-Agent", "Dragonfly/" + Dragonfly.getVersion());
			connection.setInstanceFollowRedirects(true);
			connection.setConnectTimeout(timeout);
			connection.setReadTimeout(timeout);

			BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(connection.getInputStream())));
			String data = reader.lines().collect(Collectors.joining());
			reader.close();

			return data;
		} catch (IOException ex) {
			return null;
		} catch (Exception ex) {
			throw new ResolveFailureException();
		}
	}

}
