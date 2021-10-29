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
import dev.hypera.dragonfly.exceptions.DownloadFailureException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

/**
 * Downloader interface.
 *
 * @param <T> Dependency type.
 * @author Joshua Sing <joshua@hypera.dev>
 */
public interface IDownloader<T> {

	/**
	 * Download dependency.
	 *
	 * @param dragonfly  {@link Dragonfly} instance.
	 * @param dependency {@link T} to be downloaded.
	 * @throws DownloadFailureException if something went wrong while downloading the dependency.
	 */
	void download(@NotNull Dragonfly dragonfly, @NotNull T dependency) throws DownloadFailureException;

	/**
	 * Attempts to download a dependency from a URL.
	 *
	 * @param downloadUrl URL to download from.
	 * @param timeout     Http timeout.
	 * @param path        Path to save the downloaded content.
	 * @return true if the download was a success, otherwise false.
	 * @throws DownloadFailureException if something went wrong while downloading from the url.
	 */
	@Internal
	default boolean download(@NotNull String downloadUrl, int timeout, @NotNull Path path) throws DownloadFailureException {
		try {
			URL url = new URL(downloadUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("User-Agent", "Dragonfly/" + Dragonfly.getVersion());
			connection.setInstanceFollowRedirects(true);
			connection.setConnectTimeout(timeout);
			connection.setReadTimeout(timeout);

			BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream());
			Files.copy(inputStream, path);
			inputStream.close();

			return true;
		} catch (IOException ex) {
			return false;
		} catch (Exception ex) {
			throw new DownloadFailureException();
		}
	}

}
