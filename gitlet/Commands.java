package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/** The commands list.
 * @author Preston Weber **/
public class Commands {

    /** The repository. **/
    private static Repo repo = null;

    /** The commands list. **/
    Commands() {
    }

    /**
     * Creates a new Gitlet version-control system in the current directory.
     * @throws IOException
     */
    public static void init() throws IOException {
        if (Files.exists(Paths.get(Utils.getCWD() + "/.gitlet"))) {
            Utils.message("A Gitlet version-control system "
                    + "already exists in the current directory.");
            System.exit(0);
        }

        try {
            Files.createDirectories(Paths.get(Utils.getCWD()
                    + "/.gitlet"));
            Files.createDirectories(Paths.get(Utils.getCWD()
                    + "/.gitlet/staging"));
            Files.createDirectories(Paths.get(Utils.getCWD()
                    + "/.gitlet/commits"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        repo = new Repo();
        Branch branch = repo.createBranch("master");
        branch.setHead(true);

        HashMap<String, String> files = new HashMap<>();
        repo.createCommit("initial commit", files, null,
                null, null, repo, true);

        Utils.writeObject(new File(Utils.getCWD() + "/.gitlet/REPO"), repo);
    }

    /**
     * Starting at the current head commit, display information
     * about each commit backwards along the commit tree until
     * the initial commit, following the first parent commit links,
     * ignoring any second parents found in merge commits.
     */
    public static void log() {
        if (!Files.exists(Paths.get(Utils.getCWD() + "/.gitlet"))) {
            Utils.message("Not in an initialized Gitlet directory.");
            System.exit(0);
        }

        repo = Utils.readObject(new File(Utils.getCWD() + "/.gitlet/REPO"),
                Repo.class);
        new Log(repo);
    }

    /**
     * Adds a copy of the file as it currently exists to the staging area.
     * @param file the file to add
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static void add(String file) throws IOException,
            ClassNotFoundException {
        if (!Files.exists(Paths.get(Utils.getCWD() + "/.gitlet"))) {
            Utils.message("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        repo = Utils.readObject(new File(Utils.getCWD() + "/.gitlet/REPO"),
                Repo.class);
        new Add(repo, file);
        Utils.writeObject(new File(Utils.getCWD() + "/.gitlet/REPO"), repo);
    }

    /**
     * Saves a snapshot of certain files in the current commit
     * and staging area so they can be restored at a later time,
     * creating a new commit.
     * @param desc the commit description
     * @throws IOException
     */
    public static void commit(String desc) throws IOException {
        if (!Files.exists(Paths.get(Utils.getCWD() + "/.gitlet"))) {
            Utils.message("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        File folder = new File(Utils.getCWD() + "/.gitlet/staging");
        File[] listOfFiles = folder.listFiles();

        repo = Utils.readObject(new File(Utils.getCWD() + "/.gitlet/REPO"),
                Repo.class);

        if (listOfFiles.length == 0 && repo.getMarks().size() == 0) {
            Utils.message("No changes added to the commit.");
            System.exit(0);
        }

        Commit parent = repo.getHead().getHeadCommit();

        HashMap<String, String> files = new HashMap<String, String>();
        ArrayList<String> fileNames = new ArrayList<String>();
        for (int i = 0; i < listOfFiles.length; i++) {
            fileNames.add(listOfFiles[i].getName());
            files.put(listOfFiles[i].getName(),
                    Utils.sha1(Utils.readContentsAsString(listOfFiles[i])));
        }

        repo.createCommit(desc, files, fileNames,
                listOfFiles, parent, repo, false);
        repo.removeAllMarks();
        Utils.writeObject(new File(Utils.getCWD() + "/.gitlet/REPO"), repo);
    }

    /**
     * Like log, except displays information about all commits ever made.
     * The order of the commits does not matter.
     */
    public static void globalLog() {
        if (!Files.exists(Paths.get(Utils.getCWD() + "/.gitlet"))) {
            Utils.message("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        repo = Utils.readObject(new File(Utils.getCWD() + "/.gitlet/REPO"),
                Repo.class);
        new GlobalLog(repo);
    }

    /**
     * Takes the version of the file as it exists in the head commit,
     * the front of the current branch, and puts it in the working directory,
     * overwriting the version of the file that's already there if there is one.
     * @param fileName the file to checkout
     * @throws IOException
     */
    public static void checkout(String fileName) throws IOException {
        if (!Files.exists(Paths.get(Utils.getCWD() + "/.gitlet"))) {
            Utils.message("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        repo = Utils.readObject(new File(Utils.getCWD() + "/.gitlet/REPO"),
                Repo.class);
        Checkout.process(fileName, repo);
    }

    /**
     * Takes the version of the file as it exists in the commit
     * with the given id, and puts it in the working directory,
     * overwriting the version of the file that's already there
     * if there is one.
     * @param fileName the file to checkout
     * @param commitID at the given commitID
     * @throws IOException
     */
    public static void checkout(String fileName, String commitID)
            throws IOException {
        if (!Files.exists(Paths.get(Utils.getCWD() + "/.gitlet"))) {
            Utils.message("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        repo = Utils.readObject(new File(Utils.getCWD() + "/.gitlet/REPO"),
                Repo.class);

        if (commitID.length() < Utils.UID_LENGTH) {
            File commits = new File(Utils.getCWD() + "/.gitlet/commits/");
            File[] ids = commits.listFiles();
            for (File file : ids) {
                if (file.getName().contains(commitID)) {
                    commitID = file.getName();
                }
            }
        }
        Checkout.process(fileName, commitID, repo);
    }

    /**
     * Takes all files in the commit at the head of the given branch,
     * and puts them in the working directory, overwriting the versions
     * of the files that are already there if they exist.
     * @param branchName the branch to checkout
     * @throws IOException
     */
    public static void checkoutBranch(String branchName) throws IOException {
        if (!Files.exists(Paths.get(Utils.getCWD() + "/.gitlet"))) {
            Utils.message("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        repo = Utils.readObject(new File(Utils.getCWD() + "/.gitlet/REPO"),
                Repo.class);
        Checkout.processBranch(repo, branchName);
        Utils.writeObject(new File(Utils.getCWD() + "/.gitlet/REPO"), repo);
    }

    /**
     * Unstage the file if it is currently staged.
     * If the file is tracked in the current commit, mark it
     * to indicate that it is not to be included in the next commit
     * and remove the file from the working directory.
     * @param fileName the file to remove
     */
    public static void remove(String fileName) {
        if (!Files.exists(Paths.get(Utils.getCWD() + "/.gitlet"))) {
            Utils.message("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        repo = Utils.readObject(new File(Utils.getCWD() + "/.gitlet/REPO"),
                Repo.class);
        new Remove(repo, fileName);
        Utils.writeObject(new File(Utils.getCWD() + "/.gitlet/REPO"), repo);
    }

    /**
     * Prints out the ids of all commits that have the given commit message,
     * one per line. If there are multiple such commits, it prints the
     * ids out on separate lines.
     * @param commitMessage the commit message to be found
     */
    public static void find(String commitMessage) {
        if (!Files.exists(Paths.get(Utils.getCWD() + "/.gitlet"))) {
            Utils.message("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        repo = Utils.readObject(new File(Utils.getCWD() + "/.gitlet/REPO"),
                Repo.class);
        new Find(repo, commitMessage);
    }

    /**
     * Displays what branches currently exist, and marks the current
     * branch with a *. Also displays what files have been staged or
     * marked for untracking.
     */
    public static void status() {
        if (!Files.exists(Paths.get(Utils.getCWD() + "/.gitlet"))) {
            Utils.message("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        repo = Utils.readObject(new File(Utils.getCWD() + "/.gitlet/REPO"),
                Repo.class);
        new Status(repo);
    }

    /**
     * Creates a new branch with the given name,
     * and points it at the current head node.
     * @param branchName the name of branch
     */
    public static void branch(String branchName) {
        if (!Files.exists(Paths.get(Utils.getCWD() + "/.gitlet"))) {
            Utils.message("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        repo = Utils.readObject(new File(Utils.getCWD() + "/.gitlet/REPO"),
                Repo.class);

        Branch branch = repo.createBranch(branchName);

        Utils.writeObject(new File(Utils.getCWD() + "/.gitlet/REPO"), repo);
    }

    /**
     * Deletes the branch with the given name.
     * @param branchName the given branch name
     */
    public static void removeBranch(String branchName) {
        if (!Files.exists(Paths.get(Utils.getCWD() + "/.gitlet"))) {
            Utils.message("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        repo = Utils.readObject(new File(Utils.getCWD() + "/.gitlet/REPO"),
                Repo.class);

        repo.removeBranch(branchName);
        Utils.writeObject(new File(Utils.getCWD() + "/.gitlet/REPO"), repo);
    }

    /**
     * Checks out all the files tracked by the given commit.
     * Removes tracked files that are not present in that commit.
     * Also moves the current branch's head to that commit node.
     * @param commitID the given commit
     */
    public static void reset(String commitID) throws IOException {
        if (!Files.exists(Paths.get(Utils.getCWD() + "/.gitlet"))) {
            Utils.message("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        repo = Utils.readObject(new File(Utils.getCWD() + "/.gitlet/REPO"),
                Repo.class);
        Checkout.reset(repo, commitID);
        Utils.writeObject(new File(Utils.getCWD() + "/.gitlet/REPO"), repo);
    }

    /**
     *  Merges files from the given branch into the current branch.
     * @param branchName The branch to merge into the current branch.
     */
    public static void merge(String branchName)
            throws IOException, ClassNotFoundException {
        if (!Files.exists(Paths.get(Utils.getCWD() + "/.gitlet"))) {
            Utils.message("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        repo = Utils.readObject(new File(Utils.getCWD() + "/.gitlet/REPO"),
                Repo.class);
        ArrayList<Branch> branches = repo.getBranchs();
        boolean branchFound = false;
        for (int i = 0; i < branches.size(); i++) {
            if (branches.get(i).getName().equals(branchName)) {
                branchFound = true;
                break;
            }
        }
        if (!branchFound) {
            Utils.message("A branch with that name does not exist.");
            System.exit(0);
        }
        new Merge(repo, branchName);
        Utils.writeObject(new File(Utils.getCWD() + "/.gitlet/REPO"), repo);
    }
}
