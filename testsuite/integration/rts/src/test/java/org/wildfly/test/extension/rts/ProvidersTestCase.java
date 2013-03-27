package org.wildfly.test.extension.rts;

import javax.xml.bind.JAXBException;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.star.util.TxStatus;
import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.jbossts.star.util.media.txstatusext.TransactionManagerElement;
import org.jboss.jbossts.star.util.media.txstatusext.TransactionStatisticsElement;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.test.extension.rts.common.Constants;

/**
 * Test if all providers work as required.
 *
 * Some of the media types and XML elements are covered in another tests. Therefore, they were emitted here.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
@RunAsClient
@RunWith(Arquillian.class)
public final class ProvidersTestCase {

    private TxSupport txSupport;

    @Before
    public void before() {
        txSupport = new TxSupport(Constants.TRANSACTION_MANAGER_URL);
        txSupport.startTx();
    }

    @After
    public void after() {
        txSupport.rollbackTx();
    }

    @Test
    public void testTxStatusMediaType() {
        Assert.assertEquals(TxStatus.TransactionActive.name(), TxSupport.getStatus(txSupport.txStatus()));
    }

    @Test
    public void testTransactionManagerElement() throws JAXBException {
        TransactionManagerElement transactionManagerElement = txSupport.getTransactionManagerInfo();

        Assert.assertNotNull(transactionManagerElement);
        Assert.assertEquals(1, transactionManagerElement.getCoordinatorURIs().size());
    }

    @Test
    public void testTransactionStatisticsElement() throws JAXBException {
        TransactionStatisticsElement transactionStatisticsElement = txSupport.getTransactionStatistics();

        Assert.assertNotNull(transactionStatisticsElement);
        Assert.assertEquals(1, transactionStatisticsElement.getActive());
    }

}