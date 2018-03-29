package neo.model.util;

import neo.model.bytes.UInt160;
import neo.model.core.Transaction;
import neo.model.db.BlockDb;
import neo.vm.ScriptBuilder;
import neo.vm.contract.ApplicationEngine;
import neo.vm.contract.CachedScriptTable;
import neo.vm.contract.ContractState;
import neo.vm.contract.StateReader;
import neo.vm.contract.TriggerType;

/**
 * the utility for verifying scripts.
 *
 * @author coranos
 *
 */
public class VerifyScriptUtil {

	/**
	 * verify a script's execution.
	 *
	 * @param blockDb
	 *            the blockdb to use.
	 * @param tx
	 *            the transaction to use.
	 * @return true if the script was executed, and did not halt, and left nothing
	 *         on the stack.
	 */
	public static boolean VerifyScripts(final BlockDb blockDb, final Transaction tx) {
		final UInt160[] hashes = tx.getScriptHashesForVerifying(blockDb);

		if (hashes.length != tx.scripts.size()) {
			return false;
		}
		for (int i = 0; i < hashes.length; i++) {
			byte[] verification = tx.scripts.get(i).getCopyOfVerificationScript();
			if (verification.length == 0) {
				final ScriptBuilder sb = new ScriptBuilder();
				sb.emitAppCall(hashes[i].toByteArray(), false);
				verification = sb.toByteArray();
			} else {
				if (hashes[i] != tx.scripts.get(i).getScriptHash()) {
					return false;
				}
			}
			final StateReader service = new StateReader();
			final CachedScriptTable table = new CachedScriptTable(
					blockDb.getStates(UInt160.class, ContractState.class));
			final ApplicationEngine engine = new ApplicationEngine(TriggerType.Verification, tx, table, service,
					ModelUtil.FIXED8_ZERO, false);
			engine.loadScript(verification, false);
			engine.loadScript(tx.scripts.get(i).getCopyOfInvocationScript(), true);
			if (!engine.Execute()) {
				return false;
			}
			if ((engine.evaluationStack.getCount() != 1) || !engine.evaluationStack.pop().getBoolean()) {
				return false;
			}
		}
		return true;
	}
}
