package org.wildfly.test.extension.rts;

import java.io.File;
import java.lang.String;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.star.util.TxStatus;
import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.narayana.rest.integration.api.Aborted;
import org.jboss.narayana.rest.integration.api.HeuristicType;
import org.jboss.narayana.rest.integration.api.ParticipantsManagerFactory;
import org.jboss.narayana.rest.integration.api.Prepared;
import org.jboss.narayana.rest.integration.api.ReadOnly;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.omg.CORBA.PRIVATE_MEMBER;
import org.wildfly.test.extension.rts.common.Constants;
import org.wildfly.test.extension.rts.common.LoggingParticipant;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
@RunWith(Arquillian.class)
public final class ParticipantTestCase {

    private static final String APPLICATION_ID = "org.wildfly.test.extension.rts";

    private static final String DEPENDENCIES = "Dependencies: org.jboss.narayana.rts\n";

    private TxSupport txSupport;

    @Deployment
    public static WebArchive getDeployment() {
        return ShrinkWrap.create(WebArchive.class, Constants.DEPLOYMENT_NAME + ".war")
                .addClasses(LoggingParticipant.class)
                .addAsWebInfResource(new File("../test-classes", "web.xml"), "web.xml")
                .addAsManifestResource(new StringAsset(DEPENDENCIES), "MANIFEST.MF");
    }

    @Before
    public void before() {
        txSupport = new TxSupport(Constants.TRANSACTION_MANAGER_URL);
    }

    @After
    public void after() {
        try {
            txSupport.rollbackTx();
        } catch (Throwable t){
        }
    }

    @Test
    public void testCommit() {
        txSupport.startTx();

        LoggingParticipant participant1 = new LoggingParticipant(new Prepared());
        LoggingParticipant participant2 = new LoggingParticipant(new Prepared());

        ParticipantsManagerFactory.getInstance().enlist(APPLICATION_ID, txSupport.getDurableParticipantEnlistmentURI(),
                participant1);
        ParticipantsManagerFactory.getInstance().enlist(APPLICATION_ID, txSupport.getDurableParticipantEnlistmentURI(),
                participant2);

        txSupport.commitTx();

        Assert.assertEquals(Arrays.asList(new String[] { "prepare", "commit" }), participant1.getInvocations());
        Assert.assertEquals(Arrays.asList(new String[] { "prepare", "commit" }), participant2.getInvocations());
    }

    @Test
    public void testCommitOnePhase() {
        txSupport.startTx();

        LoggingParticipant participant = new LoggingParticipant(new Prepared());

        ParticipantsManagerFactory.getInstance().enlist(APPLICATION_ID, txSupport.getDurableParticipantEnlistmentURI(),
                participant);

        txSupport.commitTx();

        Assert.assertEquals(Arrays.asList(new String[] { "commitOnePhase" }), participant.getInvocations());
    }

    @Test
    public void testReadOnly() {
        txSupport.startTx();

        final List<LoggingParticipant> participants = Arrays.asList(new LoggingParticipant[] {
                new LoggingParticipant(new ReadOnly()), new LoggingParticipant(new Prepared()),
                new LoggingParticipant(new Prepared()) });

        for (LoggingParticipant p : participants) {
            ParticipantsManagerFactory.getInstance().enlist(APPLICATION_ID,
                    txSupport.getDurableParticipantEnlistmentURI(), p);
        }

        txSupport.commitTx();

        // One of the participants was only prepared, while other two were prepared and commited.
        Assert.assertEquals(5, participants.get(0).getInvocations().size() + participants.get(1).getInvocations().size()
                + participants.get(2).getInvocations().size());

        for (LoggingParticipant p : participants) {
            if (p.getInvocations().size() == 1) {
                Assert.assertEquals(Arrays.asList(new String[] { "prepare" }), p.getInvocations());
            } else {
                Assert.assertEquals(Arrays.asList(new String[] { "prepare", "commit" }), p.getInvocations());
            }
        }
    }

    @Test
    public void testRollback() {
        txSupport.startTx();

        LoggingParticipant participant1 = new LoggingParticipant(new Prepared());
        LoggingParticipant participant2 = new LoggingParticipant(new Prepared());

        ParticipantsManagerFactory.getInstance().enlist(APPLICATION_ID, txSupport.getDurableParticipantEnlistmentURI(),
                participant1);
        ParticipantsManagerFactory.getInstance().enlist(APPLICATION_ID, txSupport.getDurableParticipantEnlistmentURI(),
                participant2);

        txSupport.rollbackTx();

        Assert.assertEquals(Arrays.asList(new String[] { "rollback" }), participant1.getInvocations());
        Assert.assertEquals(Arrays.asList(new String[] { "rollback" }), participant2.getInvocations());
    }

    @Test
    public void testRollbackByParticipant() {
        txSupport.startTx();

        final List<LoggingParticipant> participants = Arrays.asList(new LoggingParticipant[] {
                new LoggingParticipant(new Aborted()), new LoggingParticipant(new Aborted()), });

        for (LoggingParticipant p : participants) {
            ParticipantsManagerFactory.getInstance().enlist(APPLICATION_ID,
                    txSupport.getDurableParticipantEnlistmentURI(), p);
        }

        txSupport.commitTx();

        // One of the participants was prepared and then decided to rollback, the other was rolledback straight away.
        Assert.assertEquals(3, participants.get(0).getInvocations().size() + participants.get(1).getInvocations().size());

        for (LoggingParticipant p : participants) {
            if (p.getInvocations().size() == 1) {
                Assert.assertEquals(Arrays.asList(new String[] { "rollback" }), p.getInvocations());
            } else {
                Assert.assertEquals(Arrays.asList(new String[] { "prepare", "rollback" }), p.getInvocations());
            }
        }
    }

    @Test
    public void testHeuristicRollbackBeforePrepare() throws JAXBException {
        txSupport.startTx();

        final List<LoggingParticipant> participants = Arrays.asList(new LoggingParticipant[] {
                new LoggingParticipant(new Prepared()), new LoggingParticipant(new Prepared()) });

        String lastParticipantid = null;

        for (LoggingParticipant p : participants) {
            lastParticipantid = ParticipantsManagerFactory.getInstance().enlist(APPLICATION_ID,
                    txSupport.getDurableParticipantEnlistmentURI(), p);
        }

        ParticipantsManagerFactory.getInstance().reportHeuristic(lastParticipantid, HeuristicType.HEURISTIC_ROLLBACK);

        final String transactionStatus = TxSupport.getStatus(txSupport.commitTx());

        Assert.assertEquals(TxStatus.TransactionRolledBack.name(), transactionStatus);

        if (participants.get(0).getInvocations().size() == 1) {
            Assert.assertEquals(Arrays.asList(new String[] { "rollback" }), participants.get(0).getInvocations());
        } else {
            Assert.assertEquals(Arrays.asList(new String[] { "prepare", "rollback" }), participants.get(0).getInvocations());
        }
    }

    @Test
    public void testHeuristicCommitBeforePrepare() throws JAXBException {
        txSupport.startTx();

        final List<LoggingParticipant> participants = Arrays.asList(new LoggingParticipant[] {
                new LoggingParticipant(new Prepared()), new LoggingParticipant(new Prepared()) });

        String lastParticipantid = null;

        for (LoggingParticipant p : participants) {
            lastParticipantid = ParticipantsManagerFactory.getInstance().enlist(APPLICATION_ID,
                    txSupport.getDurableParticipantEnlistmentURI(), p);
        }

        ParticipantsManagerFactory.getInstance().reportHeuristic(lastParticipantid, HeuristicType.HEURISTIC_COMMIT);

        final String transactionStatus = TxSupport.getStatus(txSupport.commitTx());

        Assert.assertEquals(TxStatus.TransactionCommitted.name(), transactionStatus);
        Assert.assertEquals(Arrays.asList(new String[] { "prepare", "commit" }), participants.get(0).getInvocations());
        Assert.assertEquals(Collections.EMPTY_LIST, participants.get(1).getInvocations());
    }

}
