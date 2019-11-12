package jenkins.plugins.mvn_snapshot_check;

public class MavenSnapshotCheckException extends RuntimeException {
    public MavenSnapshotCheckException() {
        super();
    }

    public MavenSnapshotCheckException(String message) {
        super(message);
    }

    public MavenSnapshotCheckException(String message, Throwable cause) {
        super(message, cause);
    }

    public MavenSnapshotCheckException(Throwable cause) {
        super(cause);
    }

    public MavenSnapshotCheckException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
