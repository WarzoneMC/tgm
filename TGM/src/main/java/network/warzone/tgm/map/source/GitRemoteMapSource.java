package network.warzone.tgm.map.source;

import java.io.File;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import com.google.common.collect.Iterables;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FS;

import lombok.AllArgsConstructor;

public class GitRemoteMapSource extends MapSource {
    private final String remoteURI;
    private String branch = null;

    public GitRemoteMapSource(final String name, final File destinationDirectory, final String remoteURI) {
        this(name, destinationDirectory, remoteURI, null);
    }

    public GitRemoteMapSource(final String name, final File destinationDirectory, final String remoteURI, final String branch) {
        super(name, destinationDirectory);
        this.remoteURI = remoteURI;
        this.branch = branch;
    }

	@Override
	public void refreshMaps() {
        if (this.repositoryAlreadyExists()) {
            System.out.println("Pulling changes from remote repository...");

            Git localGitRepo = this.getLocalRepositoryFromDestination();
            if (localGitRepo == null) {
                System.out.println("Error updating remote " + this.name);
                localGitRepo.close();
                return;
            }

            RevCommit prePullHead = this.getRepoHead(localGitRepo);
            if (prePullHead == null) {
                System.out.println("Error updating remote " + this.name);
                localGitRepo.close();
                return;
            }
            try {
                PullCommand pullCmd = localGitRepo.pull();
                if (this.branch != null) pullCmd.setRemoteBranchName("refs/heads/" + this.branch);
                this.chainCredentialsFromURI(pullCmd, this.remoteURI);
                PullResult pullResult = pullCmd.call();
                if (pullResult.isSuccessful()) {
                    RevCommit postPullHead = this.getRepoHead(localGitRepo);
                    int addedCommits = this.getCommitDifference(localGitRepo, prePullHead, postPullHead);
                    System.out.println("Pulled " + addedCommits + " commits!");
                } else {
                    System.out.println("There was an error pulling from the remote");
                }
            } catch (Exception e) {
                System.out.println("There was an error pulling from the remote");
            }
            localGitRepo.close();
        } else {
            if (this.destinationDirectory.exists() && this.destinationDirectory.listFiles().length > 0) {
                System.out.println("Destination directory is not empty. Skipping clone.");
                return;
            }
            try {
                System.out.println("Cloning remote repository...");
                CloneCommand cloneCommand = Git.cloneRepository()
                    .setURI(this.remoteURI)
                    .setDirectory(destinationDirectory)
                    .setCloneAllBranches(false)
                    .setCloneSubmodules(false);
                this.chainCredentialsFromURI(cloneCommand, this.remoteURI);
                if (this.branch != null) {
                    cloneCommand.setBranch("refs/heads/" + branch);
                    cloneCommand.setBranchesToClone(Collections.singleton("refs/heads/" + branch));
                }
                cloneCommand.call();
            } catch (InvalidRemoteException exception) {
                System.out.println("The specified remote was not valid, so there was an error cloning it"); 
                return;
            } catch (GitAPIException exception) {
                System.out.println("Error in cloning the repository"); 
                exception.printStackTrace();
                return;
            }
            System.out.println("Successfully cloned maps from remote '" + this.name + "'");
        }
	}

    private void chainCredentialsFromURI(TransportCommand<?, ?> transportCommand, String remoteURI) {
        URI parsedURI = null;
        try {
            parsedURI = new URI(remoteURI);
        } catch (URISyntaxException e) {
            return;
        }
        final String userCreds = parsedURI.getUserInfo();
        if (userCreds == null) return;
        final String[] credentials = userCreds.split(":", 2);
        final String username = credentials[0];
        final String password = credentials[1];
        transportCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password));
    }

    private RevCommit getRepoHead(Git gitRepo) {
        RevCommit latestCommit = null;
        try {
            Iterable<RevCommit> revCommits = gitRepo.log().setMaxCount(1).call();
            latestCommit = revCommits.iterator().next();
        } catch (GitAPIException e) {
            System.out.println("Error updating remote " + this.name);
            return null;
        }
        return latestCommit;
    }

    private int getCommitDifference(Git gitRepo, RevCommit since, RevCommit until) {
        try {
            Iterable<RevCommit> diffCommits = gitRepo.log().addRange(since, until).call();
            return Iterables.size(diffCommits);
        } catch (IncorrectObjectTypeException | MissingObjectException | GitAPIException e) {
            return 0;
        }
    }

    private boolean repositoryAlreadyExists() {
        return RepositoryCache.FileKey.isGitRepository(destinationDirectory, FS.DETECTED) || new File(destinationDirectory, ".git").exists();
    }

    private Git getLocalRepositoryFromDestination() {
        Repository localRepository = null;
        try {
            localRepository = new FileRepository(new File(this.destinationDirectory, ".git"));
        } catch (IOException exception) { return null; }
        return new Git(localRepository);
    }

    public RepoData getRepoData() {
        if (!this.repositoryAlreadyExists()) return null;
        Git localGitRepo = this.getLocalRepositoryFromDestination();
        if (localGitRepo == null) return null;
        RevCommit head = this.getRepoHead(localGitRepo);
        return new RepoData(
            this.name,
            head.getId().getName(),
            head.getShortMessage(),
            this.branch == null ? "Default Branch" : this.branch
        );
    }

    @AllArgsConstructor
    public static class RepoData {
        public final String NAME;
        public final String HEAD_ID;
        public final String HEAD_MESSAGE;
        public final String BRANCH;
    }
}
