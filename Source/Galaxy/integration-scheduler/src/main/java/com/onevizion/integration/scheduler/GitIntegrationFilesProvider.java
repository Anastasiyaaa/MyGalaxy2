/*
 * Copyright 2003-2020 OneVizion, Inc. All rights reserved.
 */

package com.onevizion.integration.scheduler;

import com.onevizion.integration.scheduler.exception.IntegrationFilesProviderException;
import com.onevizion.vo.GlobalSettings;
import com.onevizion.vo.integration.Integration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

@Component
public class GitIntegrationFilesProvider implements IntegrationFilesProvider {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String GITHUB_TOKEN = "IHubToken";

    @Autowired
    private GlobalSettings globalSettings;

    @Override
    public void loadIntegrationFiles(Integration integration, Path directory) {
        Assert.isTrue(Files.exists(directory), "Should exists");
        Assert.isTrue(Files.isDirectory(directory), "Should be a directory");

        logger.info("Load integration files to directory for [{}] Integration",
                integration.getIntegrationName());

        String version = integration.getVersion();

        CloneCommand cloneCommand = Git.cloneRepository()
                                       .setURI(integration.getGitRepoUrl())
                                       .setBranchesToClone(Collections.singleton(version))
                                       .setBranch(version)
                                       .setDirectory(directory.toFile());

        String token = globalSettings.getParam(GITHUB_TOKEN);

        if (StringUtils.isNotBlank(integration.getGitRepoLogin()) && StringUtils.isNotBlank(integration.getGitRepoPassword())) {
            UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(
                    integration.getGitRepoLogin(), integration.getGitRepoPassword());
            cloneCommand = cloneCommand.setCredentialsProvider(credentialsProvider);
        } else if (StringUtils.isNotBlank(token)) {
            cloneCommand = cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, ""));
        }

        try {
            cloneCommand.call();
        } catch (GitAPIException e) {
            throw new IntegrationFilesProviderException(
                    "Cannot load files for [{}] Integration from [{}] git repository",
                    e, integration.getIntegrationName(), integration.getGitRepoUrl());
        }
    }

    @Override
    public void updateIntegrationFiles(Integration integration, Path directory) {
        Assert.isTrue(Files.exists(directory), "Should exists");
        Assert.isTrue(Files.isDirectory(directory), "Should be a directory");

        String version = integration.getVersion();

        String localBranch;
        try (Git localGit = Git.open(directory.toFile())) {
            localBranch = localGit.getRepository().getBranch();

            String localRepositoryUrl = localGit.getRepository()
                                                .getConfig()
                                                .getString("remote", "origin", "url");

            if (localRepositoryUrl.equals(integration.getGitRepoUrl())) {
                reset(localGit, localBranch);
                fetch(localGit, integration);

                if (localGit.getRepository().getTags().containsKey(version)) {
                    checkout(localGit, version);
                } else {
                    if (!localBranch.equals(version) && !isBranchExists(localGit, version)) {
                        createLocalBranch(localGit, version);
                    }

                    checkout(localGit, version);
                    pull(localGit, integration);
                }
            } else {
                cleanIntegrationDirectory(directory);
                loadIntegrationFiles(integration, directory);
            }
        } catch (IOException e) {
            logger.error("Cannot open git file", e);
            throw new IntegrationFilesProviderException("Cannot open git file. [{}] git repository", e, integration.getGitRepoUrl());
        }
    }

    @Override
    public void cleanIntegrationDirectory(Path directory) {
        try {
            FileUtils.cleanDirectory(directory.toFile());
        } catch (IOException e) {
            logger.error("Cannot delete integration files", e);
            throw new IntegrationFilesProviderException("Cannot delete integration files", e);
        }
    }

    private Ref createLocalBranch(Git localGit, String originBranchName) {
        try {
            return localGit.branchCreate()
                           .setName(originBranchName)
                           .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
                           .setStartPoint("origin/" + originBranchName)
                           .call();
        } catch (GitAPIException ex) {
            logger.error("Cannot switch branch", ex);
            throw new IntegrationFilesProviderException("Cannot create branch", ex);
        }
    }

    private PullResult pull(Git localGit, Integration integration) {
        PullCommand pullCommand = localGit.pull();

        String token = globalSettings.getParam(GITHUB_TOKEN);
        if (StringUtils.isNotBlank(integration.getGitRepoLogin()) && StringUtils.isNotBlank(integration.getGitRepoPassword())) {
            UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(
                    integration.getGitRepoLogin(), integration.getGitRepoPassword());
            pullCommand = pullCommand.setCredentialsProvider(credentialsProvider);
        } else if (StringUtils.isNotBlank(token)) {
            pullCommand = pullCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, ""));
        }

        try {
            PullResult result = pullCommand.call();
            if (result.getMergeResult().getMergeStatus() == MergeResult.MergeStatus.FAILED) {
                logger.error("Cannot pull from git repository. Merge status is failed");
                throw new IntegrationFilesProviderException("Cannot pull from git repository. Merge status is Failed");
            }

            return result;
        } catch (GitAPIException e) {
            logger.error("Cannot pull from git repository", e);
            throw new IntegrationFilesProviderException("Cannot pull from git repository", e);
        }
    }

    private Ref checkout(Git localGit, String branchName) {
        try {
            return localGit.checkout().setName(branchName).call();
        } catch (GitAPIException e) {
            logger.error("Cannot switch branch", e);
            throw new IntegrationFilesProviderException("Cannot switch branch", e);
        }
    }

    private boolean isBranchExists(Git localGit, String branchName) {
        try {
            localGit.branchList().setContains(branchName).call();
        } catch (GitAPIException e) {
            return false;
        }
        return true;
    }

    private Ref reset(Git localGit, String localBranch) {
        try {
            return localGit.reset().setMode(ResetCommand.ResetType.HARD).setRef(localBranch).call();
        } catch (GitAPIException e) {
            logger.error("Cannot reset branch", e);
            throw new IntegrationFilesProviderException("Cannot reset branch", e);
        }
    }

    private void fetch(Git localGit, Integration integration) {
        FetchCommand fetchCommand = localGit.fetch();

        String token = globalSettings.getParam(GITHUB_TOKEN);
        if (StringUtils.isNotBlank(integration.getGitRepoLogin()) && StringUtils.isNotBlank(integration.getGitRepoPassword())) {
            UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(
                    integration.getGitRepoLogin(), integration.getGitRepoPassword());
            fetchCommand = fetchCommand.setCredentialsProvider(credentialsProvider);
        } else if (StringUtils.isNotBlank(token)) {
            fetchCommand = fetchCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, ""));
        }

        try {
            fetchCommand.call();
        } catch (GitAPIException e) {
            logger.error("Cannot fetch from git repository", e);
            throw new IntegrationFilesProviderException("Cannot fetch from git repository", e);
        }
    }

}
