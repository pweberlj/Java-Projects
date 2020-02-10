package gitlet;

import java.io.File;

/**
 * The Remove class to remove files.
 * @author Preston Weber
 */
public class Remove {

    /**
     * The Remove class to remove files.
     * @param repo the repository
     * @param fileName the file name
     */
    Remove(Repo repo, String fileName) {

        Commit head =  repo.getHead().getHeadCommit();

        if (!new File(Utils.getCWD() + "/.gitlet/staging/"
                + fileName).exists()
                && !head.getFiles().containsKey(fileName)) {
            Utils.message("No reason to remove the file.");
            System.exit(0);
        }

        if (new File(Utils.getCWD() + "/.gitlet/staging/"
                + fileName).exists()) {
            File stageFile = new File(Utils.getCWD()
                    + "/.gitlet/staging/" + fileName);
            stageFile.delete();
        }
        if (head.getFiles().containsKey(fileName)) {
            repo.addMark(fileName);
            if (new File(Utils.getCWD() + "/" + fileName).exists()) {
                File remove = new File(Utils.getCWD() + "/" + fileName);
                remove.delete();
            }
        }
    }
}
