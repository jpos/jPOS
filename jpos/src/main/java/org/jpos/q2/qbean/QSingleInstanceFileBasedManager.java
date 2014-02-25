package org.jpos.q2.qbean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import org.jpos.q2.QBeanSupport;

public class QSingleInstanceFileBasedManager extends QBeanSupport {

    File             lockFile;
    FileChannel      lockChannel;
    FileLock         lock;
    FileOutputStream lockFileOS;

    /*
     * (non-Javadoc)
     *
     * @see org.jpos.q2.QBeanSupport#initService()
     */
    @Override
    protected void initService() throws Exception {

        try {
            lockFile = new File("./" + getServer().getDeployDir(), "instance.lock");
            if (lockFile.exists()) {
                // Either an orphan lock or lock from another instance. Orphan
                // lock will get deleted, the latter will not get a lock on it.
                lockFile.delete();
            }
            lockFileOS = new FileOutputStream(lockFile);
            lockFileOS.close();
            lockChannel = new RandomAccessFile(lockFile, "rw").getChannel();
            lock = lockChannel.tryLock();
            if (lock == null) {
                throw new Exception("Unable to obtain lock");
            }
        }
        catch (Exception e) {
            getLog().error("An instance of Q2 is already running. Shutting this instance");
            if (lock != null) {
                lock.release();
            }
            if (lockChannel != null) {
                lockChannel.close();
            }
            if (lockFile != null) {
                lockFile.delete();
            }
            getServer().shutdown();
        }

    }

    @Override
    protected void stopService() throws Exception {

        lock.release();
        lockChannel.close();
        lockFile.delete();
    }

}
