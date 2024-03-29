import hudson.model.*;
import jenkins.plugins.mvn_snapshot_check.MavenSnapshotCheck;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.ExtractResourceSCM;
import org.jvnet.hudson.test.JenkinsRule;

import java.net.URL;

public class MavenSnapshotCheckTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testFreestyleBuildSuccess() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.setScm(new ExtractResourceSCM(getClass().getResource("test-success.zip")));
        MavenSnapshotCheck mavenSnapshotCheck = new MavenSnapshotCheck(true,null);
        project.getBuildersList().add(mavenSnapshotCheck);
        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains("SNAPSHOT", build);
    }

    @Test
    public void testFreestyleBuildFailure() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.setScm(new ExtractResourceSCM(getClass().getResource("test-failure.zip")));
        MavenSnapshotCheck mavenSnapshotCheck = new MavenSnapshotCheck(true,null);
        project.getBuildersList().add(mavenSnapshotCheck);
        FreeStyleBuild build = jenkins.buildAndAssertStatus(Result.FAILURE, project);
        jenkins.assertLogContains("SNAPSHOT", build);
    }

    @Test
    public void testFreestyleBuildSuccessNotChecked() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.setScm(new ExtractResourceSCM(getClass().getResource("test-success.zip")));
        MavenSnapshotCheck mavenSnapshotCheck = new MavenSnapshotCheck(false,null);
        project.getBuildersList().add(mavenSnapshotCheck);
        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogNotContains("SNAPSHOT", build);
    }

    @Test
    public void testPipelineBuildSuccess() throws Exception {
        URL zipFile = getClass().getResource("test-success.zip");
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-pipeline-success");
        String pipelineScript =
                "node {\n" +
                "  unzip '" + zipFile.getPath() + "'\n" +
                "  mavenSnapshotCheck check: 'true' \n" +
                "}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun run = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        jenkins.assertLogContains("SNAPSHOT", run);
    }

    @Test
    public void testPipelineBuildSuccessWithPomFiles() throws Exception {
        URL zipFile = getClass().getResource("test-success.zip");
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-pipeline-success");
        String pipelineScript =
                "node {\n" +
                        "  unzip '" + zipFile.getPath() + "'\n" +
                        "  mavenSnapshotCheck check: 'true', pomFiles:'pom.xml', excludePomFiles: '' \n" +
                        "}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun run = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        jenkins.assertLogContains("SNAPSHOT", run);
    }

    @Test
    public void testPipelineBuildSuccessWithNotChecked() throws Exception {
        URL zipFile = getClass().getResource("test-success.zip");
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-pipeline-success");
        String pipelineScript =
                "node {\n" +
                        "  unzip '" + zipFile.getPath() + "'\n" +
                        "  mavenSnapshotCheck check: 'false' \n" +
                        "}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
    }

    @Test
    public void testPipelineBuildFailure() throws Exception {
        URL zipFile = getClass().getResource("test-failure.zip");

        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-pipeline-failure");
        String pipelineScript =
                "node {\n" +
                        "  unzip '" + zipFile.getPath() + "'\n" +
                        "  mavenSnapshotCheck check: 'true' \n" +
                        "}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun run = job.scheduleBuild2(0).waitForStart();
        jenkins.waitForCompletion(run);
        jenkins.assertBuildStatus(Result.FAILURE, run);
        jenkins.assertLogContains("SNAPSHOT", run);
    }

}
