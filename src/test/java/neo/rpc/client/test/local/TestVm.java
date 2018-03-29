package neo.rpc.client.test.local;

import java.sql.Timestamp;
import java.text.NumberFormat;

import org.apache.commons.lang3.time.FastDateFormat;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import neo.model.core.Block;
import neo.model.core.Transaction;
import neo.model.db.BlockDb;
import neo.model.util.ConfigurationUtil;
import neo.model.util.VerifyScriptUtil;
import neo.network.LocalControllerNode;
import neo.network.model.LocalNodeData;

/**
 * tests the RPC server.
 *
 * @author coranos
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestVm {

	/**
	 * the integer format.
	 */
	private static final NumberFormat INTEGER_FORMAT = NumberFormat.getIntegerInstance();

	/**
	 * the date format.
	 */
	private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyy.MM.dd");

	/**
	 * the controller.
	 */
	private static final LocalControllerNode CONTROLLER;

	/**
	 * the logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(TestVm.class);

	static {
		final JSONObject controllerNodeConfig = ConfigurationUtil.getConfiguration();
		CONTROLLER = new LocalControllerNode(controllerNodeConfig);
	}

	/**
	 * method for after class disposal.
	 */
	@AfterClass
	public static void afterClass() {
		LOG.debug("afterClass");
	}

	/**
	 * method for before class setup.
	 */
	@BeforeClass
	public static void beforeClass() {
		LOG.debug("beforeClass");
	}

	/**
	 * first test, blank, so beforeClass() time doesnt throw off the metrics.
	 */
	@Test
	public void aaaFirstTest() {
	}

	@Test
	public void test001Vm() {
		LOG.debug("STARTED vm");
		final LocalNodeData localNodeData = CONTROLLER.getLocalNodeData();
		final BlockDb blockDb = localNodeData.getBlockDb();
		final long maxIndex = blockDb.getHeaderOfBlockWithMaxIndex().getIndexAsLong();
		long startMs = -1;
		for (long blockIx = 0; blockIx <= maxIndex; blockIx++) {
			LOG.debug("STARTED png {} of {} ", blockIx, maxIndex);
			final Block block = blockDb.getFullBlockFromHeight(blockIx);

			for (final Transaction tx : block.getTransactionList()) {
				if (!VerifyScriptUtil.VerifyScripts(blockDb, tx)) {
					throw new RuntimeException("script failed.");
				}
			}

			final Timestamp blockTs = block.getTimestamp();
			if (startMs < 0) {
				startMs = blockTs.getTime();
			}
			final long ms = blockTs.getTime() - startMs;
			if (ms > (86400 * 1000)) {
				final String targetDateStr = DATE_FORMAT.format(blockTs);

				LOG.info("INTERIM vm {} of {}, date {}", INTEGER_FORMAT.format(blockIx),
						INTEGER_FORMAT.format(maxIndex), targetDateStr);

				startMs = blockTs.getTime();
			}
		}
		LOG.debug("SUCCESS vm");
	}

	/**
	 * last test, blank, so afterClass() time doesnt throw off the metrics.
	 */
	@Test
	public void zzzLastTest() {
	}
}
