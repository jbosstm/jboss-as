package org.wildfly.test.extension.rts.common;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public final class Constants {

    public static final String PORT = "8080";

    public static final String BASE_URL = "http://localhost:" + PORT + "/";

    public static final String TRANSACTION_MANAGER_URL = BASE_URL + "rest-at-coordinator/tx/transaction-manager";

    public static final String DEPLOYMENT_NAME = "test-deployment";

    public static final String RESTFUL_PARTICIPANT_PATH_SEGMENT = "txresource";

    public static final String RESTFUL_PARTICIPANT_URL = Constants.BASE_URL + Constants.DEPLOYMENT_NAME + "/"
            + RESTFUL_PARTICIPANT_PATH_SEGMENT;

}
