package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/** The Status class to call the Status method.
 * @author Preston Weber
 */
public class Status {

    /**
     * The Status class to call the Status method.
     * @param repo the repository
     */
    Status(Repo repo) {
        ArrayList<Branch> branches = repo.getBranchs();
        ArrayList<String> branchNames = new ArrayList<String>();
        for (int i = 0; i < branches.size(); i++) {
            branchNames.add(branches.get(i).getName());
        }
        Collections.sort(branchNames);

        String head = repo.getHead().getName();

        Utils.message("=== Branches ===");
        for (int i = 0; i < branchNames.size(); i++) {
            if (branchNames.get(i) == head) {
                Utils.message("*" +  branchNames.get(i));
            } else {
                Utils.message(branchNames.get(i));
            }
        }
        Utils.message("");


        ArrayList<String> stagingFiles = new ArrayList<String>();
        File folder = new File(Utils.getCWD() + "/.gitlet/staging/");
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                stagingFiles.add(file.getName());
            }
        }

        Collections.sort(stagingFiles);

        Utils.message("=== Staged Files ===");
        for (int i = 0; i < stagingFiles.size(); i++) {
            Utils.message(stagingFiles.get(i));
        }
        Utils.message("");

        ArrayList<String> marks = new ArrayList<String>();
        for (int i = 0; i < repo.getMarks().size(); i++) {
            marks.add(repo.getMarks().get(i));
        }

        Collections.sort(marks);
        Utils.message("=== Removed Files ===");
        for (int i = 0; i < marks.size(); i++) {
            Utils.message(marks.get(i));
        }
        Utils.message("");

        Utils.message("=== Modifications Not Staged For Commit ===");
        Utils.message("");

        Utils.message("=== Untracked Files ===");
        Utils.message("");
    }
}
