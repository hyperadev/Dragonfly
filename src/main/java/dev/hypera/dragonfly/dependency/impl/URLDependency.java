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

package dev.hypera.dragonfly.dependency.impl;

import dev.hypera.dragonfly.dependency.Dependency;
import dev.hypera.dragonfly.relocation.Relocation;
import java.util.Arrays;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

/**
 * URL dependency.
 *
 * @author Joshua Sing <joshua@hypera.dev>
 */
public class URLDependency implements Dependency {

	private final int priority;
	private final @NotNull String name;
	private final @NotNull String version;
	private final @NotNull String url;
	private final @NotNull Set<Relocation> relocations;
	private boolean relocated;
	private @NotNull String fileName;

	public URLDependency(int priority, @NotNull String name, @NotNull String version, @NotNull String url, @NotNull Set<Relocation> relocations, @NotNull String fileName) {
		this.priority = priority;
		this.name = name;
		this.version = version;
		this.url = url;
		this.relocations = relocations;
		this.fileName = fileName;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	public @NotNull String getName() {
		return name;
	}

	public @NotNull String getVersion() {
		return version;
	}

	public @NotNull String getUrl() {
		return url;
	}

	@Override
	public @NotNull Set<Relocation> getRelocations() {
		return relocations;
	}

	@Override
	public boolean isRelocated() {
		return relocated;
	}

	@Override
	public void setRelocated(boolean relocated) {
		this.relocated = relocated;
	}

	public @NotNull URLDependency addRelocations(@NotNull Relocation... relocations) {
		this.relocations.addAll(Arrays.asList(relocations));
		return this;
	}

	@Override
	public @NotNull String getFileName() {
		return fileName;
	}

	@Override
	public void setFileName(@NotNull String fileName) {
		this.fileName = fileName;
	}

	@Override
	public String toString() {
		return name + ":" + version + ":" + url;
	}

}
