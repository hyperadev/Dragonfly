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

package dev.hypera.dragonfly.dependency;

import dev.hypera.dragonfly.dependency.impl.MavenDependency;
import dev.hypera.dragonfly.dependency.impl.URLDependency;
import dev.hypera.dragonfly.relocation.Relocation;
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

/**
 * Dependency interface.
 * Implementations:
 *  - {@link MavenDependency}
 *  - {@link URLDependency}
 *
 * @author Joshua Sing <joshua@hypera.dev>
 */
public interface Dependency {

	int DEFAULT_PRIORITY = 1000;
	String FILENAME_FORMAT = "%s-%s.jar";

	int getPriority();

	@NotNull Set<Relocation> getRelocations();
	boolean isRelocated();
	void setRelocated(boolean relocated);

	@NotNull String getFileName();
	void setFileName(@NotNull String fileName);

	/**
	 * Creates a new {@link MavenDependency}.
	 * @param groupId Dependency groupId.
	 * @param artifactId Dependency artifactId.
	 * @param version Dependency version.
	 * @return New {@link MavenDependency} instance.
	 */
	static MavenDependency maven(String groupId, String artifactId, String version) {
		return maven(DEFAULT_PRIORITY, groupId, artifactId, version);
	}

	/**
	 * Creates a new {@link MavenDependency}, with a priority.
	 * @param priority Load priority.
	 * @param groupId Dependency groupId.
	 * @param artifactId Dependency artifactId.
	 * @param version Dependency version.
	 * @return New {@link MavenDependency} instance.
	 */
	static MavenDependency maven(int priority, String groupId, String artifactId, String version) {
		return new MavenDependency(priority, groupId.replace("\\.", "."), artifactId.replace("\\.", "."), version, new HashSet<>(), String.format(FILENAME_FORMAT, artifactId, version));
	}

	/**
	 * Creates a new {@link URLDependency}.
	 * @param name Dependency name.
	 * @param version Dependency version.
	 * @param url Dependency URL.
	 * @return New {@link URLDependency} instance.
	 */
	static URLDependency url(String name, String version, String url) {
		return url(DEFAULT_PRIORITY, name, version, url);
	}

	/**
	 * Creates a new {@link URLDependency}, with a priority.
	 * @param priority Load priority.
	 * @param name Dependency name.
	 * @param version Dependency version.
	 * @param url Dependency URL.
	 * @return New {@link URLDependency} instance.
	 */
	static URLDependency url(int priority, String name, String version, String url) {
		return new URLDependency(priority, name, version, url, new HashSet<>(), String.format(FILENAME_FORMAT, name, version));
	}

}
