package uk.co.pols.bamboo.gitplugin;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.StringUtils;
import com.atlassian.bamboo.repository.Repository;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.ww2.actions.build.admin.create.BuildConfiguration;
import com.atlassian.bamboo.commit.CommitFile;
import com.atlassian.bamboo.commit.Commit;
import com.opensymphony.util.UrlUtils;

import java.io.Serializable;
import java.util.List;

import static uk.co.pols.bamboo.gitplugin.GitRepositoryConfig.AvailableConfig.REPOSITORY;

public class GitRepositoryConfig implements Serializable {
    public static final String REPO_PREFIX = "repository.github.";

    public static final String GIT_REPO_URL = REPO_PREFIX + "repositoryUrl";
    public static final String GIT_BRANCH = REPO_PREFIX + "branch";
    public static final String WEB_REPO_URL = REPO_PREFIX + "webRepositoryUrl";
    private static final String DEFAULT_BRANCH = "master";

    enum AvailableConfig {REPOSITORY, WEB_REPOSITORY}

    private String repositoryUrl;
    private String branch;
    private String webRepositoryUrl;

    public void populateFromConfig(HierarchicalConfiguration config, AvailableConfig availableConfig) {
        if (availableConfig.equals(REPOSITORY)) {
            repositoryUrl = config.getString(GIT_REPO_URL);
            branch = config.getString(GIT_BRANCH);
        } else {
            webRepositoryUrl = config.getString(WEB_REPO_URL);
        }
    }

    public HierarchicalConfiguration toConfiguration(HierarchicalConfiguration configuration, AvailableConfig availableConfig) {
        if (availableConfig.equals(REPOSITORY)) {
            configuration.setProperty(GIT_REPO_URL, getRepositoryUrl());
            configuration.setProperty(GIT_BRANCH, getBranch());
        } else {
            configuration.setProperty(WEB_REPO_URL, getWebRepositoryUrl());
        }

        return configuration;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = StringUtils.trim(repositoryUrl);
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getHost() {
        if (repositoryUrl == null) {
            return Repository.UNKNOWN_HOST;
        }

        return "github.com";
    }

    public String getWebRepositoryUrl() {
        return webRepositoryUrl;
    }

    public void setWebRepositoryUrl(String webRepositoryUrl) {
        this.webRepositoryUrl = StringUtils.trim(webRepositoryUrl);
    }

    public boolean hasWebBasedRepositoryAccess() {
        return StringUtils.isNotBlank(webRepositoryUrl) && webRepositoryUrl.contains("github.com");
    }

    public ErrorCollection validate(ErrorCollection errorCollection, BuildConfiguration buildConfiguration, AvailableConfig availableConfig) {
        if (availableConfig.equals(REPOSITORY)) {
            validateMandatoryField(buildConfiguration, errorCollection, GIT_REPO_URL, "Please specify where the repository is located");
            validateMandatoryField(buildConfiguration, errorCollection, GIT_BRANCH, "Please specify which branch you want to build");
        } else {
            String webRepoUrl = buildConfiguration.getString(WEB_REPO_URL);
            if (!StringUtils.isBlank(webRepoUrl) && !UrlUtils.verifyHierachicalURI(webRepoUrl)) {
                errorCollection.addError(WEB_REPO_URL, "This is not a valid url");
            }
        }

        return errorCollection;
    }

    private void validateMandatoryField(BuildConfiguration buildConfiguration, ErrorCollection errorCollection, String fieldKey, String errorMessage) {
        if (StringUtils.isEmpty(buildConfiguration.getString(fieldKey))) {
            errorCollection.addError(fieldKey, errorMessage);
        }
    }

    public void addDefaultValues(BuildConfiguration buildConfiguration, AvailableConfig availableConfig) {
        if (availableConfig.equals(REPOSITORY)) {
            buildConfiguration.setProperty(GIT_BRANCH, DEFAULT_BRANCH);
        }
    }

    public String getWebRepositoryUrlForFile(CommitFile commitFile) {
        return webRepositoryUrl + "/blob/" + commitFile.getRevision() + "/" + commitFile.getName();
    }

    public String getWebRepositoryUrlForCommit(Commit commit) {
        return webRepositoryUrl + "/commit/" + commitIdFor(commit);
    }

    public String getWebRepositoryUrlForRevision(CommitFile file) {
        return getWebRepositoryUrlForFile(file);
    }

    public String getWebRepositoryUrlForDiff(CommitFile file) {
        // wish I had access to the commit then I'd know the order of the files and could link directly to the diff (diff-<order_number>)
        return webRepositoryUrl + "/commit/" + file.getRevision();
    }

    private String commitIdFor(Commit commit) {
        List<CommitFile> files = commit.getFiles();
        if (files.isEmpty()) {
            return "UNKNOWN";
        }
        return files.get(0).getRevision();
    }
}