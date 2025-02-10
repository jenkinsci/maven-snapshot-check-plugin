package jenkins.plugins.mvn_snapshot_check;

import hudson.model.*;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.ExtractResourceSCM;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.net.URL;

@WithJenkins
class MavenSnapshotCheckTest {

    @Test
    void testFreestyleBuildSuccess(JenkinsRule jenkins) throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.setScm(new ExtractResourceSCM(getClass().getResource("test-success.zip")));
        MavenSnapshotCheck mavenSnapshotCheck = new MavenSnapshotCheck(true,null);
        project.getBuildersList().add(mavenSnapshotCheck);
        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains("SNAPSHOT", build);
    }

    @Test
    void testFreestyleBuildFailure(JenkinsRule jenkins) throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.setScm(new ExtractResourceSCM(getClass().getResource("test-failure.zip")));
        MavenSnapshotCheck mavenSnapshotCheck = new MavenSnapshotCheck(true,null);
        project.getBuildersList().add(mavenSnapshotCheck);
        FreeStyleBuild build = jenkins.buildAndAssertStatus(Result.FAILURE, project);
        jenkins.assertLogContains("SNAPSHOT", build);
    }

    @Test
    void testFreestyleBuildSuccessNotChecked(JenkinsRule jenkins) throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.setScm(new ExtractResourceSCM(getClass().getResource("test-success.zip")));
        MavenSnapshotCheck mavenSnapshotCheck = new MavenSnapshotCheck(false,null);
        project.getBuildersList().add(mavenSnapshotCheck);
        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogNotContains("SNAPSHOT", build);
    }

    @Test
    void testPipelineBuildSuccess(JenkinsRule jenkins) throws Exception {
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
    void testPipelineBuildSuccessWithPomFiles(JenkinsRule jenkins) throws Exception {
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
    void testPipelineBuildSuccessWithNotChecked(JenkinsRule jenkins) throws Exception {
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
    void testPipelineBuildFailure(JenkinsRule jenkins) throws Exception {
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
