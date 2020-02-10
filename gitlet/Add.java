package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/** The Add class.
 * @author Preston Weber
 */
public class Add {

    /**
     * The Add class.
     * @param repo the repository
     * @param file the file to add
     * @throws IOException
     * @throws ClassNotFoundException
     */
    Add(Repo repo, String file) throws IOException, ClassNotFoundException {
        boolean exist = Files.exists(Paths.get(Utils.getCWD() + "/" + file));
        if (!exist) {
            Utils.message("File does not exist.");
            System.exit(0);
        }

        if (!repo.notMark(file)) {
            repo.removeMark(file);
        }

        String headID = repo.getHead().getHeadCommit().id();

        Files.copy(new File(Utils.getCWD() + "/" + file).toPath(),
                new File(Utils.getCWD() + "/.gitlet/staging/"
                        + file).toPath(),
                StandardCopyOption.REPLACE_EXISTING);

        String srcSHA = Utils.sha1(Utils.readContentsAsString(
                new File(Utils.getCWD() + "/.gitlet/staging/" + file)));
        String destSHA = null;
        if (Files.exists(Paths.get(Utils.getCWD() + "/.gitlet/commits/"
                + headID + "/" + file))) {
            destSHA = Utils.sha1(Utils.readContentsAsString(
                    new File(Utils.getCWD()
                            + "/.gitlet/commits/" + headID + "/" + file)));
        }

        if (srcSHA.equals(destSHA)) {
            File stageFile = new File(Utils.getCWD()
                    + "/.gitlet/staging/" + file);
            stageFile.delete();
        }
    }
}
