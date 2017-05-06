package org.more.util;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;
/**
 * 当资源有改动时该类会调用Run方法。
 * @version : 2012-8-2
 * @author 赵永春 (zyc@hasor.net)
 */
public abstract class ResourceWatch extends Thread {
    private static Logger logger        = Logger.getLogger(ResourceWatch.class.getName());
    private        URI    resourceURI   = null;                                           //资源地址
    //
    private        long   lastHashCode  = 0;                                              //上一次检查资源的校验码，两次检查校验码不一致将执行reload
    private        long   checkSeepTime = 0;                                              //检查的时间间隔-15秒
    //
    public ResourceWatch() {
        this(null, 15 * 1000);
    }
    public ResourceWatch(final URI resourceURI) {
        this(resourceURI, 15 * 1000);
    }
    public ResourceWatch(final URI resourceURI, final long checkSeepTime) {
        this.resourceURI = resourceURI;
        this.checkSeepTime = checkSeepTime;
    }
    /**首次启动监听。*/
    public abstract void firstStart(URI resourceURI) throws IOException;

    /**当遇到资源改变之后调用。*/
    public abstract void onChange(URI resourceURI) throws IOException;

    /**检查资源是否修改，并且返回修改的时间戳。当两次检查不一致时会调用{@link #onChange(URI)}方法。*/
    public abstract long lastModify(URI resourceURI) throws IOException;
    /**首次启动会先执行load然后在启动线程*/
    @Override
    public synchronized void start() {
        try {
            this.firstStart(this.resourceURI);
            this.lastHashCode = this.lastModify(this.resourceURI);
        } catch (Exception e) {
            logger.warning(this.resourceURI + " lastModify error.");
        }
        super.start();
    }
    @Override
    public final void run() {
        if (this.resourceURI == null) {
            return;
        }
        String schema = this.resourceURI.getScheme();
        schema = schema == null ? "" : schema;
        schema = schema.toLowerCase();
        while (true) {
            try {
                long lastHashCode = this.lastModify(this.resourceURI);
                if (this.lastHashCode != lastHashCode) {
                    try {
                        this.onChange(this.resourceURI);
                        this.lastHashCode = lastHashCode;
                    } catch (Exception e) {
                        logger.warning(String.format("reload config error :%s", e));
                    }
                }
                Thread.sleep(this.checkSeepTime);
            } catch (Exception e) {
            }
        }
    }
    public URI getResourceURI() {
        return this.resourceURI;
    }
    public void setResourceURI(final URI resourceURI) {
        this.resourceURI = resourceURI;
    }
    public long getCheckSeepTime() {
        return this.checkSeepTime;
    }
    public void setCheckSeepTime(final long checkSeepTime) {
        this.checkSeepTime = checkSeepTime;
    }
}