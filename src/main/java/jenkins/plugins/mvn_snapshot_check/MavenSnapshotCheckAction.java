package jenkins.plugins.mvn_snapshot_check;

import hudson.model.Run;
import jenkins.model.RunAction2;

/**
 * @author donghui 2019/5/11.
 */
public class MavenSnapshotCheckAction  implements RunAction2{

    private transient Run run;

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public MavenSnapshotCheckAction(String message){
        this.message = message;
    }

    @Override
    public String getIconFileName() {
        return "document.png";
    }

    @Override
    public String getDisplayName() {
        return "Maven SNAPSHOT Check";
    }

    @Override
    public String getUrlName() {
        return "maven-snapshot-check";
    }

    @Override
    public void onAttached(Run<?, ?> run) {
        this.run = run;
    }

    @Override
    public void onLoad(Run<?, ?> run) {
        this.run = run;
    }

    public Run getRun() {
        return run;
    }
}
