package org.jpos.util;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DirPollOperationTest {

    volatile boolean fileProcessed;

    private static final int LITTLE_SLEEP = 10;
    private static final String RELATIVE_ARCHIVE_DIR = "archive";
    private static final String RELATIVE_BAD_DIR = "bad";
    private static final String RELATIVE_REQUEST_DIR = "request";

    private static final long DIRPOLL_CHECK_INTERVAL = 500L;
    private DirPoll dirPoll;
    private File testIncomingFile;
    private String basePath;
    public static final String DATE_FORMAT_STRING = "yyyyMMddHHmmss";

    @Before
    public void createDirPoll() throws Exception {
        basePath = createTempDir();
        dirPoll = new DirPoll();
        dirPoll.setPath(basePath);
        dirPoll.setPollInterval(DIRPOLL_CHECK_INTERVAL);
        dirPoll.setProcessor(new DirPoll.FileProcessor() {
            @Override
            public void process(File name) {
                fileProcessed = true;
            }
        });
		dirPoll.createDirs();
        emptyDirectories();
        new Thread(dirPoll).start();
    }

    private String createTempDir() throws IOException {
        File temp = File.createTempFile("dir_poll", "tmp");
        temp.delete(); // delete the file, we want a dir
        temp.mkdirs();
        return temp.getAbsolutePath();
    }

	@After
    public void destroyDirPoll() throws Exception {
        dirPoll.destroy();
        emptyDirectories();
        dirPoll = null;
        testIncomingFile = null;
    }

	@Test
    public void testBadFile() throws Exception {
        String filename = "dodgyTestFile.test";
        dirPoll.setProcessor(new DirPoll.FileProcessor() {
            @Override
            public void process(File name) throws DirPoll.DirPollException {
                fileProcessed = true;
                throw new DirPoll.DirPollException();
            }
        });
        dirPoll.setShouldArchive(true);
        dirPoll.setArchiveDateFormat(DATE_FORMAT_STRING);
        dirPoll.setShouldTimestampArchive(true);
        testIncomingFile = new File(absolutePathTo(RELATIVE_REQUEST_DIR, filename));
        FileOutputStream fileOutputStream = new FileOutputStream(testIncomingFile);
        fileOutputStream.write("test".getBytes());
        fileOutputStream.flush();
        assertThat(testIncomingFile.exists(), is(true));

        File badDirectory = new File(absolutePathTo(RELATIVE_BAD_DIR));
        waitForFileProcessed();
        waitForNumFilesInDirOrTimeout(1, badDirectory, 200);
        assertThat(badDirectory.listFiles().length, is(1));
        assertThat(badDirectory.listFiles()[0].getName().length(), is(DATE_FORMAT_STRING.length() + filename.length() + 1));
    }

	@Test
    public void testArchiveFile() throws IOException, InterruptedException {
        String filename = "dodgyTestFile.test";

        dirPoll.setShouldArchive(true);
        dirPoll.setArchiveDateFormat(DATE_FORMAT_STRING);
        dirPoll.setShouldTimestampArchive(true);
        testIncomingFile = new File(absolutePathTo(RELATIVE_REQUEST_DIR, filename));
        FileOutputStream fileOutputStream = new FileOutputStream(testIncomingFile);
        fileOutputStream.write("test".getBytes());
        fileOutputStream.flush();
        assertThat(testIncomingFile.exists(), is(true));

        File archiveDirectory = new File(absolutePathTo(RELATIVE_ARCHIVE_DIR));
        waitForFileProcessed();
        waitForNumFilesInDirOrTimeout(1, archiveDirectory, 200);
        assertThat(archiveDirectory.listFiles().length, is(1));
        assertThat(archiveDirectory.listFiles()[0].getName().length(), is(DATE_FORMAT_STRING.length() + filename.length() + 1));
    }

	@Test
    public void testCompressArchiveFile() throws IOException, InterruptedException {
        String filename = "dodgyTestFile.test";

        dirPoll.setShouldArchive(true);
        dirPoll.setShouldCompressArchive(true);
        dirPoll.setArchiveDateFormat(DATE_FORMAT_STRING);
        dirPoll.setShouldTimestampArchive(false);
        testIncomingFile = new File(absolutePathTo(RELATIVE_REQUEST_DIR, filename));
        FileOutputStream fileOutputStream = new FileOutputStream(testIncomingFile);
        fileOutputStream.write("test".getBytes());
        fileOutputStream.flush();
        assertThat(testIncomingFile.exists(), is(true));

        File archiveDirectory = new File(absolutePathTo(RELATIVE_ARCHIVE_DIR));
        waitForFileProcessed();
        waitForNumFilesInDirOrTimeout(1, archiveDirectory, 200);
        assertThat(archiveDirectory.listFiles().length, is(1));
        assertThat(archiveDirectory.listFiles()[0].getName(), is(testIncomingFile.getName() + ".zip"));
    }

	@Test
    public void testDoNotArchiveFile() throws IOException, InterruptedException {
        dirPoll.setShouldArchive(false);
        testIncomingFile = new File(absolutePathTo(RELATIVE_REQUEST_DIR, "dodgyTestFile2.test"));
        FileOutputStream fileOutputStream = new FileOutputStream(testIncomingFile);
        fileOutputStream.write("test".getBytes());
        fileOutputStream.flush();
        assertThat(testIncomingFile.exists(), is(true));

        waitForFileProcessed();
        Thread.sleep(200);
        File archiveDirectory = new File(absolutePathTo(RELATIVE_ARCHIVE_DIR));
        assertThat(archiveDirectory.listFiles().length, is(0));
    }

	@Test
    public void testArchiveNoTimestamp() throws Exception {
        String filename = "dodgyTestFile3.test";

        dirPoll.setShouldArchive(true);
        dirPoll.setShouldTimestampArchive(false);
        testIncomingFile = new File(absolutePathTo(RELATIVE_REQUEST_DIR, filename));
        FileOutputStream fileOutputStream = new FileOutputStream(testIncomingFile);
        fileOutputStream.write("test".getBytes());
        fileOutputStream.flush();
        assertThat(testIncomingFile.exists(), is(true));


        waitForFileProcessed();
        File archiveDirectory = new File(absolutePathTo(RELATIVE_ARCHIVE_DIR));
        waitForNumFilesInDirOrTimeout(1, archiveDirectory, 200);
        assertThat(archiveDirectory.listFiles().length, is(1));
        assertThat(archiveDirectory.listFiles()[0].getName().length(), is(filename.length()));
        assertThat(archiveDirectory.listFiles()[0].getName(), is(filename));
    }

	@Test
    public void testPause() throws Exception {
        dirPoll.setShouldArchive(false);
        dirPoll.pause();
        testIncomingFile = new File(absolutePathTo(RELATIVE_REQUEST_DIR, "dodgyTestFile3.test"));
        FileOutputStream fileOutputStream = new FileOutputStream(testIncomingFile);
        fileOutputStream.write("test".getBytes());
        fileOutputStream.flush();
        fileOutputStream.close();
        assertThat(testIncomingFile.exists(), is(true));

        // Sleep for enough time for dirpoll to pick up the file, if it wasn't paused
        Thread.sleep(DIRPOLL_CHECK_INTERVAL);
        assertThat(testIncomingFile.exists(), is(true));
        assertThat(dirPoll.isPaused(), is(true));
        dirPoll.unpause();
        waitForFileProcessed();
        assertThat(testIncomingFile.exists(), is(false));
    }

    private void waitForFileProcessed() throws InterruptedException {
        while (!fileProcessed) {
            Thread.sleep(LITTLE_SLEEP);
        }
    }

    private void waitForNumFilesInDirOrTimeout(int numFiles, File dir, int timeout) throws InterruptedException {
        long waitedFor = 0;
        while (dir.listFiles().length != numFiles || waitedFor < timeout) {
            waitedFor += LITTLE_SLEEP;
            Thread.sleep(LITTLE_SLEEP);
        }
    }

    private void emptyDirectories() {
        File archiveDir = new File(absolutePathTo(RELATIVE_ARCHIVE_DIR));
        File[] files = archiveDir.listFiles();
        for (File file : files) {
            file.delete();
        }

        File badDir = new File(absolutePathTo(RELATIVE_BAD_DIR));
        files = badDir.listFiles();
        for (File file : files) {
            file.delete();
        }
    }

	private String absolutePathTo(String... relativePaths) {
		String path = basePath;
		for (String relativePath : relativePaths) {
            path = path + File.separator + relativePath;
		}
		return path;
	}
}
