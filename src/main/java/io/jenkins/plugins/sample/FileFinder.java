package io.jenkins.plugins.sample;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.jenkinsci.remoting.RoleChecker;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FileFinder extends Object implements FilePath.FileCallable<List<String>> {
    private final String includesPattern;

    private final String exIncludesPattern;

    public FileFinder(String includesPattern, String exIncludesPattern) {
        this.includesPattern = includesPattern;
        this.exIncludesPattern = exIncludesPattern;
    }

    public List<String> invoke(File directory, VirtualChannel channel) throws IOException, InterruptedException {
        return find(directory);
    }

    public List<String> find(File directory) {
        try {
            FileSet fileSet = new FileSet();
            Project antProject = new Project();
            fileSet.setProject(antProject);
            fileSet.setDir(directory);
            fileSet.setExcludes(this.exIncludesPattern);
            fileSet.setIncludes(this.includesPattern);
            String[] files = fileSet.getDirectoryScanner(antProject).getIncludedFiles();
            if (files == null) {
                return Collections.emptyList();
            }
            return Arrays.asList(files);
        } catch (BuildException buildException) {
            return Collections.emptyList();
        }
    }

    @Override
    public void checkRoles(RoleChecker roleChecker) throws SecurityException {

    }
}
