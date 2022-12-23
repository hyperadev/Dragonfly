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
import dev.hypera.dragonfly.exceptions.ResolveFailureException;
import dev.hypera.dragonfly.resolvers.IResolver;
import java.io.IOException;
import java.io.StringReader;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import sun.tools.jstat.ParserException;

/**
 * Maven snapshot resolver.
 *
 * @author Joshua Sing <joshua@hypera.dev>
 */
public class MavenSnapshotResolver implements IResolver<MavenDependency> {

	private static final @NotNull String FORMAT = "%s%s/%s/%s/maven-metadata.xml";
	private static final @NotNull String OUTPUT_FORMAT = "%s/%s-%s-%s-%s.jar";
	private final @NotNull DocumentBuilderFactory documentBuilderFactory;

	public MavenSnapshotResolver() {
		try {
			/* The below is an attempt to create an XML parser while preventing XML External Entity attacks */
			/* Read more: https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#java */
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			factory.setXIncludeAware(false);
			factory.setExpandEntityReferences(false);
			this.documentBuilderFactory = factory;
		} catch (ParserConfigurationException ex) {
			throw new RuntimeException("Failed to create DocumentBuilderFactory", ex);
		}
	}

	@Override
	public @Nullable String resolve(@NotNull Dragonfly dragonfly, @NotNull MavenDependency dependency) throws ResolveFailureException {
		if (!dependency.getVersion().contains("SNAPSHOT")) {
			throw new ResolveFailureException("Cannot resolve a dependency as a snapshot if it isn't a snapshot");
		} else {
			Set<String> urls = getUrls(dragonfly, dependency);
			if (urls.isEmpty()) {
				throw new ResolveFailureException("Cannot resolve dependency: " + dependency);
			}

			String data = null;
			String resolvedUrl = null;
			for (String url : urls) {
				if ((data = get(url, dragonfly.getTimeout())) != null) {
					resolvedUrl = url;
					break;
				}
			}

			if (null == data) {
				throw new ResolveFailureException("Cannot resolve dependency: " + dependency);
			}

			try {
				DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
				Document document = builder.parse(new InputSource(new StringReader(data)));
				Element root = document.getDocumentElement();
				Element snapshotData = (Element) root.getElementsByTagName("snapshot").item(0);

				String timestamp = snapshotData.getElementsByTagName("timestamp").item(0).getTextContent();
				String buildNumber = snapshotData.getElementsByTagName("buildNumber").item(0).getTextContent();

				return String.format(
						OUTPUT_FORMAT,
						resolvedUrl.replace("/maven-metadata.xml", ""),
						dependency.getArtifactId(),
						dependency.getVersion().replace("-SNAPSHOT", ""),
						timestamp,
						buildNumber
				);
			} catch (Exception ex) {
				throw new ResolveFailureException("Cannot resolve dependency: " + dependency, ex);
			}
		}
	}


	private Set<String> getUrls(@NotNull Dragonfly dragonfly, @NotNull MavenDependency dependency) {
		return dragonfly.getRepositories().stream().map(repo -> String.format(
				FORMAT, repo,
				dependency.getGroupId().replace(".", "/"),
				dependency.getArtifactId(),
				dependency.getVersion()
		)).collect(Collectors.toSet());
	}

}
