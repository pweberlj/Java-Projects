package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Merges files from the given branch into the current branch.
 * @author Preston Weber
 */
public class Merge {

    /** The commit ID at the given commit. **/
    private String _givenCommitID;
    /** The commit ID at the current commit. **/
    private String _currentCommitID;

    /** The new files for the merged commit. **/
    private HashMap<String, String> _files;

    /** The name merged file names. **/
    private ArrayList<String> _fileNames;

    /**
     * Merges files from the given branch into the current branch.
     * @param repo the repository
     * @param branchName the given branch to merge with current
     * @throws IOException
     * @throws ClassNotFoundException
     */
    Merge(Repo repo, String branchName)
            throws IOException, ClassNotFoundException {
        Branch head = repo.getHead();
        Commit cHead = head.getHeadCommit();
        _currentCommitID = head.getHeadCommit().id();
        ArrayList<Branch> branches = repo.getBranchs();
        Branch branch = null;
        _fileNames = new ArrayList<String>();
        _files = new HashMap<String, String>();


        for (int i = 0; i < branches.size(); i++) {
            if (branches.get(i).getName().equals(branchName)) {
                branch = branches.get(i);
            }
        }
        _givenCommitID = branch.getHeadCommit().id();
        failureCases(repo, branchName);
        String splitPoint = getSplitPoint(repo, repo.getHead(), branchName);
        if (branch.getHeadCommit().id().equals(splitPoint)) {
            Utils.message("Given branch is an ancestor of the current branch.");
            System.exit(0);
        } else if (head.getHeadCommit().id().equals(splitPoint)) {
            head.setHeadCommitTo(branch.getHeadCommit());
            Utils.message("Current branch fast-forwarded.");
            System.exit(0);
        }
        Commit split = findCommit(splitPoint, repo);
        HashMap<String, String> splitFiles = split.getFiles();
        HashMap<String, String> currentC = head.getHeadCommit().getFiles();
        HashMap<String, String> givenC = branch.getHeadCommit().getFiles();

        mergeHelperOne(splitFiles, currentC, givenC, repo);

        String description = "Merged " + branch.getName()
                + " into " + head.getName() + ".";
        ArrayList<Commit> parents = new ArrayList<Commit>();
        Commit firstParent = head.getHeadCommit();
        Commit secondParent = branch.getHeadCommit();
        parents.add(firstParent);
        parents.add(secondParent);
        repo.createCommit(description, _files, _fileNames,
                cHead.getFileList(), false, parents, repo);
    }

    /**
     * Merges files from the given branch into the current branch.
     * @param splitFiles the files at the split commit
     * @param currentCommitFiles the files at the current commit
     * @param givenCommitFiles the files at the given commit
     * @param repo the repository
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void mergeHelperOne(HashMap<String, String> splitFiles,
                                HashMap<String, String> currentCommitFiles,
                                HashMap<String, String> givenCommitFiles,
                                Repo repo) throws IOException,
            ClassNotFoundException {
        for (String file : splitFiles.keySet()) {
            boolean presentInGiven = givenCommitFiles.containsKey(file);
            boolean presentInCurrent = currentCommitFiles.containsKey(file);
            boolean modifiedInCurrent = checkModified(file,
                    splitFiles, currentCommitFiles);
            boolean modifiedInGiven = checkModified(file,
                    splitFiles, givenCommitFiles);

            if (modifiedInGiven && !modifiedInCurrent) {
                if (presentInGiven) {
                    Checkout.process(file, _givenCommitID, repo);
                    new Add(repo, file);
                    File f = new File(Utils.getCWD() + "/" + file);
                    _files.put(file, Utils.sha1(Utils.readContentsAsString(f)));
                    _fileNames.add(file);
                }
            }
            if (!modifiedInCurrent && !presentInGiven) {
                new Remove(repo, file);
                repo.addMark(file);
            }
            if (modifiedInCurrent && modifiedInGiven) {
                if (checkModified(file, givenCommitFiles, currentCommitFiles)) {
                    mergeHelperTwo(splitFiles, currentCommitFiles,
                            givenCommitFiles, repo, file);
                }
            }
        }
        for (String file : givenCommitFiles.keySet()) {
            boolean presentInCurrent = currentCommitFiles.containsKey(file);
            boolean presentInSplit = splitFiles.containsKey(file);
            boolean modifiedInCurrent = checkModified(file,
                    splitFiles, currentCommitFiles);
            boolean modifiedInGiven = checkModified(file,
                    splitFiles, givenCommitFiles);

            if (!presentInSplit) {
                if (!presentInCurrent) {
                    Checkout.process(file, _givenCommitID, repo);
                    File f = new File(Utils.getCWD() + "/" + file);
                    _files.put(file, Utils.sha1(Utils.readContentsAsString(f)));
                    _fileNames.add(file);
                    new Add(repo, file);
                }
                if (modifiedInCurrent && modifiedInGiven) {
                    if (checkModified(file, givenCommitFiles,
                            currentCommitFiles)) {
                        mergeHelperTwo(splitFiles, currentCommitFiles,
                                givenCommitFiles, repo, file);
                    }
                }
            }
        }
    }

    /**
     * Merges files from the given branch into the current branch.
     * @param splitFiles the files at the split commit
     * @param currentCommitFiles the files at the current commit
     * @param givenCommitFiles the files at the given commit
     * @param repo the repository
     * @param file the file we are comparing at the three different areas
     *             (split, current, and given)
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void mergeHelperTwo(HashMap<String, String> splitFiles,
                                HashMap<String, String> currentCommitFiles,
                                HashMap<String, String> givenCommitFiles,
                                Repo repo, String file)
            throws IOException, ClassNotFoundException {
        String location = Utils.getCWD() + "/.gitlet/commits/";
        String currentLocation;
        File current;
        String givenLocation;
        File given;

        String conflictedContents = "<<<<<<< HEAD\n";
        if (currentCommitFiles.containsKey(file)) {
            currentLocation = location + _currentCommitID + "/" + file;
            current = new File(currentLocation);
            conflictedContents += Utils.readContentsAsString(current) + "\n";
        } else {
            conflictedContents += "";
        }
        conflictedContents += "=======\n";
        if (givenCommitFiles.containsKey(file)) {
            givenLocation = location + _givenCommitID + "/" + file;
            given = new File(givenLocation);
            conflictedContents += Utils.readContentsAsString(given) + "\n";
        } else {
            conflictedContents += "";
        }
        conflictedContents += ">>>>>>>\n";

        File fileOver = new File(Utils.getCWD() + "/" + file);
        Utils.writeContents(fileOver, conflictedContents);
        new Add(repo, fileOver.getName());
        Utils.message("Encountered a merge conflict.");
        _fileNames.add(fileOver.getName());
        _files.put(file, Utils.sha1(Utils.readContentsAsString(fileOver)));
    }

    /**
     * Checks for several provided failure cases within the project spec.
     * @param repo the repository
     * @param branchName the given branch
     */
    private void failureCases(Repo repo, String branchName) {

        File staged = new File(Utils.getCWD() + "/.gitlet/staging/");
        if (repo.getMarks().size() > 0 || staged.listFiles().length > 0) {
            Utils.message("You have uncommitted changes.");
            System.exit(0);
        }

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

        if (repo.getHead().getName().equals(branchName)) {
            Utils.message("Cannot merge a branch with itself.");
            System.exit(0);
        }

        checkUntracked(repo);
    }

    /**
     * Checks if there are any untracked files in the way before beginning
     * the merge process.
     * @param repo the repository
     */
    public static void checkUntracked(Repo repo) {
        Commit head = repo.getHead().getHeadCommit();
        HashMap<String, String> tracked = head.getFiles();

        File staged = new File(Utils.getCWD() + "/.gitlet/staging/");
        File[] stagedFiles = staged.listFiles();
        HashMap<String, String> stage = new HashMap<String, String>();
        for (int i = 0; i < stagedFiles.length; i++) {
            stage.put(stagedFiles[i].getName(),
                    Utils.sha1(Utils.readContentsAsString(stagedFiles[i])));
        }

        File cwd = new File(Utils.getCWD());
        for (File file : cwd.listFiles()) {
            if (file.getName().equals(".git")
                    || file.getName().equals(".DS_Store")) {
                continue;
            }
            if (tracked == null) {
                if (cwd.listFiles().length > 1) {
                    Utils.message("There is an untracked file "
                            + "in the way; delete it or add it first.");
                    System.exit(0);
                }
            } else {
                boolean inTracked = tracked.containsKey(file.getName());
                if (!inTracked
                        && !file.getName().equals(".gitlet")) {
                    Utils.message("There is an untracked file "
                            + "in the way; delete it or add it first.");
                    System.exit(0);
                }
            }
        }
    }

    /**
     * Returns the Commit ID of the latest comment ancestor of the two branches.
     * @param repo the repository
     * @param head the head branch
     * @param branchName the given branch
     * @return the ID of the split point
     */
    private String getSplitPoint(Repo repo, Branch head, String branchName) {
        ArrayList<Branch> branches = repo.getBranchs();
        Branch branch = null;
        for (int i = 0; i < branches.size(); i++) {
            if (branches.get(i).getName().equals(branchName)) {
                branch = branches.get(i);
            }
        }

        Commit headHead = head.getHeadCommit();
        ArrayList<String> headHeadIDs = new ArrayList<String>();
        while (headHead != null) {
            headHeadIDs.add(headHead.id());
            headHead = headHead.getParent();
        }

        Commit branchHead = branch.getHeadCommit();
        ArrayList<String> branchHeadIDs = new ArrayList<String>();
        while (branchHead != null) {
            branchHeadIDs.add(branchHead.id());
            branchHead = branchHead.getParent();
        }

        for (String id : branchHeadIDs) {
            if (headHeadIDs.contains(id)) {
                return id;
            }
        }
        return "";
    }

    /**
     * Finds a commit from an arraylist of commits based on the ID.
     * @param id the ID to search for
     * @param repo the repository
     * @return the found commit
     */
    private Commit findCommit(String id, Repo repo) {
        for (int i = 0; i < repo.getCommits().size(); i++) {
            if (repo.getCommits().get(i).id().equals(id)) {
                return repo.getCommits().get(i);
            }
        }
        return null;
    }

    /**
     * Checks if the respective file has been modified in either list.
     * @param file the file to check
     * @param fileListOne list one
     * @param fileListTwo list two
     * @return true if modified
     */
    private boolean checkModified(String file, HashMap<String,
            String> fileListOne, HashMap<String, String> fileListTwo) {
        if (fileListOne.containsKey(file) && fileListTwo.containsKey(file)) {
            if (!fileListOne.get(file).equals(fileListTwo.get(file))) {
                return true;
            }
            return false;
        } else if (fileListOne.containsKey(file)
                || fileListTwo.containsKey(file)) {
            return true;
        }
        return false;
    }
}
