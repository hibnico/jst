package org.hibnet.jst.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.generator.IGenerator;
import org.eclipse.xtext.generator.JavaIoFileSystemAccess;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;

import com.google.inject.Provider;

public class JstJavaFileGenerator {

	@Inject
	private Provider<ResourceSet> resourceSetProvider;
	@Inject
	private IResourceValidator validator;
	@Inject
	private IGenerator generator;
	@Inject
	private JavaIoFileSystemAccess fileAccess;

	public List<Issue> generate(List<File> sources, File output) {
		ResourceSet set = resourceSetProvider.get();

		List<Resource> resources = new ArrayList<>();
		List<Issue> issues = new ArrayList<>();

		for (File source : sources) {
			Resource resource = set.getResource(URI.createURI(source.toURI().toString()), true);
			resources.add(resource);
			issues.addAll(validator.validate(resource, CheckMode.ALL, CancelIndicator.NullImpl));
		}

		if (!issues.isEmpty()) {
			return issues;
		}

		fileAccess.setOutputPath(output.getAbsolutePath());

		for (Resource resource : resources) {
			generator.doGenerate(resource, fileAccess);
		}

		return null;
	}

}