package org.wildfly.test.extension.rts;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.star.util.TxMediaType;
import org.jboss.jbossts.star.util.TxStatusMediaType;
import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.test.extension.rts.common.Constants;
import org.wildfly.test.extension.rts.common.Work;
import org.wildfly.test.extension.rts.common.RestfulParticipant;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 * @author <a href="mailto:mmusgrov@redhat.com">Michael Musgrove</a>
 *
 */
@RunAsClient
@RunWith(Arquillian.class)
public final class CoordinatorTestCase {

    private static final String DEPENDENCIES = "Dependencies: org.jboss.narayana.rts\n";

    @Deployment
    public static WebArchive getDeployment() {
        return ShrinkWrap.create(WebArchive.class, Constants.DEPLOYMENT_NAME + ".war")
                .addClasses(RestfulParticipant.class, Constants.class, Work.class)
                .addAsWebInfResource(new File("../test-classes", "web.xml"), "web.xml")
                .addAsManifestResource(new StringAsset(DEPENDENCIES), "MANIFEST.MF");
    }

    @BeforeClass
    public static void beforeClass() {
        TxSupport.setTxnMgrUrl(Constants.TRANSACTION_MANAGER_URL);
    }

    @Before
    public void before() throws Exception {
        RestfulParticipant.clearFaults();
    }

    @Test
    public void testListTransactions() {
        TxSupport[] txns = { new TxSupport(), new TxSupport() };
        int txnCount = new TxSupport().txCount();

        for (TxSupport txn : txns) {
            txn.startTx();
        }

        // there should be txns.length more transactions
        Assert.assertEquals(txnCount + txns.length, txns[0].txCount());

        for (TxSupport txn : txns) {
            txn.commitTx();
        }

        // the number of transactions should be back to the original number
        Assert.assertEquals(txnCount, txns[0].txCount());
    }

    @Test
    public void test1PCAbort() throws Exception {
        TxSupport txn = new TxSupport();
        String pUrl = Constants.RESTFUL_PARTICIPANT_URL;
        String pid = null;
        String pVal;

        pid = modifyResource(txn, pUrl, pid, "p1", "v1");
        pVal = getResourceProperty(txn, pUrl, pid, "p1");
        Assert.assertEquals(pVal, "v1");

        txn.startTx();
        pid = enlistResource(txn, pUrl + "?pId=" + pid);

        modifyResource(txn, pUrl, pid, "p1", "v2");
        pVal = getResourceProperty(txn, pUrl, pid, "p1");
        Assert.assertEquals(pVal, "v2");

        txn.rollbackTx();

        pVal = getResourceProperty(txn, pUrl, pid, "p1");
        Assert.assertEquals(pVal, "v1");
    }

    @Test
    public void test1PCCommit() throws Exception {
        TxSupport txn = new TxSupport();
        String pUrl = Constants.RESTFUL_PARTICIPANT_URL;
        String pid = null;
        String pVal;

        pid = modifyResource(txn, pUrl, pid, "p1", "v1");
        pVal = getResourceProperty(txn, pUrl, pid, "p1");
        Assert.assertEquals(pVal, "v1");

        txn.startTx();
        pid = enlistResource(txn, pUrl + "?pId=" + pid);

        modifyResource(txn, pUrl, pid, "p1", "v2");
        pVal = getResourceProperty(txn, pUrl, pid, "p1");
        Assert.assertEquals(pVal, "v2");

        txn.commitTx();

        pVal = getResourceProperty(txn, pUrl, pid, "p1");
        Assert.assertEquals(pVal, "v2");
    }

    @Test
    public void test2PC() throws Exception {
        TxSupport txn = new TxSupport();
        String pUrl = Constants.RESTFUL_PARTICIPANT_URL;
        String[] pid = new String[2];
        String[] pVal = new String[2];

        for (int i = 0; i < pid.length; i++) {
            pid[i] = modifyResource(txn, pUrl, null, "p1", "v1");
            pVal[i] = getResourceProperty(txn, pUrl, pid[i], "p1");

            Assert.assertEquals(pVal[i], "v1");
        }

        txn.startTx();

        for (int i = 0; i < pid.length; i++) {
            enlistResource(txn, pUrl + "?pId=" + pid[i]);

            modifyResource(txn, pUrl, pid[i], "p1", "v2");
            pVal[i] = getResourceProperty(txn, pUrl, pid[i], "p1");

            Assert.assertEquals(pVal[i], "v2");
        }

        txn.rollbackTx();

        for (int i = 0; i < pid.length; i++) {
            pVal[i] = getResourceProperty(txn, pUrl, pid[i], "p1");
            Assert.assertEquals(pVal[i], "v1");
        }
    }

    @Test
    public void testCommitInvalidTx() throws IOException {
        TxSupport txn = new TxSupport().startTx();

        String terminator = txn.getTerminatorURI();
        terminator += "/_dead";
        // an attempt to commit on this URI should fail:
        txn.httpRequest(new int[] { HttpURLConnection.HTTP_NOT_FOUND }, terminator, "PUT", TxMediaType.TX_STATUS_MEDIA_TYPE,
                TxStatusMediaType.TX_COMMITTED);
        // commit it properly
        txn.commitTx();
    }

    @Test
    public void testTimeoutCleanup() throws InterruptedException {
        TxSupport txn = new TxSupport();
        int txnCount = txn.txCount();
        txn.startTx(1000);
        txn.enlistTestResource(Constants.RESTFUL_PARTICIPANT_URL, false);

        // Let the txn timeout
        Thread.sleep(2000);

        Assert.assertEquals(txnCount, txn.txCount());
    }

    private String enlistResource(final TxSupport txn, final String pUrl) {
        return txn.enlistTestResource(pUrl, false);
    }

    private String modifyResource(final TxSupport txn, final String pUrl, final String pid, final String name,
            final String value) {

        final int[] expected = new int[] { HttpURLConnection.HTTP_OK };
        final String url = getResourceUpdateUrl(pUrl, pid, name, value).toString();
        final String method = "GET";

        return txn.httpRequest(expected, url, method, TxMediaType.PLAIN_MEDIA_TYPE);
    }

    private String getResourceProperty(final TxSupport txn, final String pUrl, final String pid, final String name) {
        final int[] expected = new int[] { HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_NO_CONTENT };
        final String updateUrl = getResourceUpdateUrl(pUrl, pid, name, null).toString();
        final String method = "GET";

        return txn.httpRequest(expected, updateUrl, method, TxMediaType.PLAIN_MEDIA_TYPE);
    }

    private StringBuilder getResourceUpdateUrl(final String pUrl, final String pid, final String name, final String value) {
        final StringBuilder sb = new StringBuilder(pUrl);

        if (pid != null) {
            sb.append("?pId=").append(pid).append("&name=");
        } else {
            sb.append("?name=");
        }

        sb.append(name);

        if (value != null) {
            sb.append("&value=").append(value);
        }

        return sb;
    }

}
