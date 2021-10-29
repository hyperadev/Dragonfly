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

import dev.hypera.dragonfly.annotations.Builder;
import dev.hypera.dragonfly.loading.IClassLoader;
import dev.hypera.dragonfly.objects.Status;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

/**
 * {@link Dragonfly} builder.
 * @author Joshua Sing <joshua@hypera.dev>
 */
@Builder
public class DragonflyBuilder {

	private int timeout = 5000;
	private final @NotNull IClassLoader classLoader;
	private final @NotNull Path directory;
	private final @NotNull Set<String> repositories = new HashSet<>();
	private boolean deleteOnRelocate = true;
	private @NotNull Consumer<Status> statusHandler = status -> {};

	private DragonflyBuilder(@NotNull IClassLoader classLoader, @NotNull Path directory) {
		this.classLoader = classLoader;
		this.directory = directory;
		this.repositories.add("https://repo1.maven.org/maven2/");
	}

	/**
	 * Create a new Dragonfly builder.
	 *
	 * @param classLoader Class loader, used for loading the dependencies into the class path.
	 * @param directory   Directory to save dependencies in.
	 * @return New {@link DragonflyBuilder} instance.
	 */
	public static @NotNull DragonflyBuilder create(@NotNull IClassLoader classLoader, @NotNull Path directory) {
		return new DragonflyBuilder(classLoader, directory);
	}

	/**
	 * Set http timeout.
	 *
	 * @param timeout Timeout in milliseconds.
	 * @return Current {@link DragonflyBuilder} instance.
	 */
	public @NotNull DragonflyBuilder setTimeout(int timeout) {
		this.timeout = timeout;
		return this;
	}

	/**
	 * Add repositories to resolve dependencies in.
	 *
	 * @param repositories Repositories to be added.
	 * @return Current {@link DragonflyBuilder} instance.
	 */
	public @NotNull DragonflyBuilder addRepositories(@NotNull String... repositories) {
		this.repositories.addAll(Arrays.asList(repositories));
		return this;
	}

	/**
	 * Set if a dependency with relocation's non-relocated jar should be deleted after the relocated version is
	 * created.
	 *
	 * @param deleteOnRelocate Should a relocated dependency's non-relocated jar be deleted after relocation?
	 * @return Current {@link DragonflyBuilder} instance.
	 */
	public @NotNull DragonflyBuilder setDeleteOnRelocate(boolean deleteOnRelocate) {
		this.deleteOnRelocate = deleteOnRelocate;
		return this;
	}

	/**
	 * Sets the status handler, which will be provided status updates as a load is in progress.
	 *
	 * @param statusHandler Status handler.
	 * @return Current {@link DragonflyBuilder} instance.
	 */
	public @NotNull DragonflyBuilder setStatusHandler(@NotNull Consumer<Status> statusHandler) {
		this.statusHandler = statusHandler;
		return this;
	}

	/**
	 * Builds a new Dragonfly instance using the provided settings.
	 *
	 * @return New {@link Dragonfly} instance.
	 */
	public @NotNull Dragonfly build() {
		try {
			return new Dragonfly(timeout, classLoader, directory, repositories, deleteOnRelocate, statusHandler);
		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

}
