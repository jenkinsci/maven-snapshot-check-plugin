package jenkins.plugins.mvn_snapshot_check;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author donghui 2019/4/24.
 */
public class MavenCheck extends Builder {

    private final boolean check;

    @DataBoundConstructor
    public MavenCheck(boolean check) {
        this.check = check;
    }

    public boolean getCheck() {
        return check;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        FilePath workspace = build.getWorkspace();
        // workspace.getChannel().
        if (getCheck()) {
            listener.getLogger().println("SNAPSHOT check!");
        }
        return true;
    }

    @Override
    public MavenCheck.DescriptorImpl getDescriptor() {
        return (MavenCheck.DescriptorImpl)super.getDescriptor();
    }

    /**
     * Descriptor for {@link MavenCheck}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        /**
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        @Override
        public String getDisplayName() {
            return "Maven SNAPSHOT Check";
        }
    }
}
