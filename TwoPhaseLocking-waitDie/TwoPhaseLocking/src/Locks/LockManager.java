package Locks;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import Operations.Operation;
import Transactions.Transaction;
import Transactions.TransactionManager;
import waitDie.RigorousTwoPhaseLocking;

public class LockManager {

	static HashMap<Integer, Transaction> transactionTableMap = new HashMap<Integer, Transaction>();
	//Resolves the read-read conflict between the transaction
	public static Lock read_read(String processedLine, String dataItem, Transaction trans, Lock lock) {
		lock.readLockTransactionId.add(trans.getTransactionId());
		if (!trans.getDataItems().contains(dataItem))
			trans.setDataItems(dataItem);
		transactionTableMap.put(trans.getTransactionId(), trans);
		System.out.println(processedLine + "  " + dataItem + " read locked by T" + trans.getTransactionId()
				+ ": Lock Table record for " + dataItem + " is created with mode " + lock.getLockState() + " (T"
				+ trans.getTransactionId() + " holds lock).\n");

		return lock;
	}
	
	//Resolves the read-write conflict between the transaction 	
	public static Lock read_write(String processedLine, String dataItem, Transaction trans, Lock lock) {
		if (lock.getreadLockTransactionId().size() == 1
				&& lock.getreadLockTransactionId().get(0).equals(trans.getTransactionId())) {
			lock.setLockState("Write");
			lock.getreadLockTransactionId().remove(0);
			lock.setwriteLockTransactionId(trans.getTransactionId());
			System.out.println(processedLine + "   Read lock upgraded to write lock for item " + dataItem + " by T"
					+ trans.getTransactionId() + ", lock table updated to mode W.\n");
		} else if (lock.getreadLockTransactionId().size() == 1
				&& !lock.getreadLockTransactionId().get(0).equals(trans.getTransactionId())) {
			Transaction t1 = transactionTableMap.get(lock.writeLockTransactionId);
			if (t1.getTimestamp()>trans.getTimestamp()) {
				t1.setTransactionState("Abort");
				transactionTableMap.put(t1.getTransactionId(), t1);
				lock.setLockState("Write");
				lock.setwriteLockTransactionId(t1.getTransactionId());
				lock.getreadLockTransactionId().remove(0);
				System.out.println(processedLine + "  Transaction T" + t1.getTransactionId()
						+ " aborts due to higher timestamp and transaction T" + t1.getTransactionId()
						+ " procures Write Lock on data item " + dataItem + ".\n");
				RigorousTwoPhaseLocking.releaseLock(processedLine, t1, dataItem);

			} else {
				t1.setTransactionState("Block");
				t1.getblockedOperations().add(new Operation("Write", dataItem));
				transactionTableMap.put(t1.getTransactionId(), t1);
				lock.waitingOperationList.add(t1.getTransactionId());
				System.out.println(processedLine + "  Transaction T" + t1.getTransactionId()
						+ " is blocked due to higher timestamp and write " + "operation for " + dataItem
						+ " has been added to the blocked operation queue of transaction table and the transaction T"
						+ t1.getTransactionId() + " has been added to the waiting list of lock table." + "\n");
			}

		} else if (lock.getreadLockTransactionId().size() > 1) {
			List<Integer> readTransId = lock.getreadLockTransactionId();
			Collections.sort(readTransId);
			int first = readTransId.get(0);
			if (first == trans.getTransactionId()) {
				System.out.println(processedLine + "  For transaction " + trans.getTransactionId()
						+ " lock for data item " + dataItem + " has been upgraded to write.");
				for (int i = 1; i < readTransId.size(); i++) {
					Transaction t1 = transactionTableMap.get(readTransId.get(i));
					TransactionManager.abortTransaction(processedLine, t1);
					System.out.println(processedLine+ "  Aborting transaction T" + trans.getTransactionId() + "...");
				}
				lock.readLockTransactionId.clear();
				lock.setwriteLockTransactionId(first);
			} else if (trans.getTransactionId() < first) {
				System.out.println(processedLine + "  Transaction T" + trans.getTransactionId()
						+ " has procured write lock for data item " + dataItem);

				for (int i = 0; i < readTransId.size(); i++) {
					Transaction t1 = transactionTableMap.get(readTransId.get(i));
					TransactionManager.abortTransaction(processedLine, t1);
					System.out.println("Aborting transaction T" + t1.getTransactionId() + "...");
				}
				lock.getreadLockTransactionId().clear();
				lock.setwriteLockTransactionId(first);
			} else {
				trans.setTransactionState("Block");
				trans.getblockedOperations().add(new Operation(dataItem, "Write"));
				System.out.println(processedLine + "  Transaction T" + trans.getTransactionId()
						+ " has been blocked because it has higher timestamp.");
				int i = 0;
				while (i < readTransId.size()) {
					if (trans.getTransactionId() >= readTransId.get(i))
						i++;
					else {
						readTransId.remove(i-1);
						Transaction t1 = transactionTableMap.get(readTransId.get(i-1));
						TransactionManager.abortTransaction(processedLine, t1);
						System.out.println("Aborting transaction T" + t1.getTransactionId() + "...");
					}
				}
				lock.readLockTransactionId.addAll(readTransId);
			}

		}
		if (!trans.getDataItems().contains(dataItem)) {
			trans.getDataItems().add(dataItem);
			transactionTableMap.put(trans.getTransactionId(), trans);
		}
		return lock;
	}
	
	//Resolves the write-read conflict between the transaction
	public static Lock write_read(String processedLine, String dataItem, Transaction trans, Lock lock) {

		if (lock.writeLockTransactionId == trans.getTransactionId()) {
			lock.setLockState("Read");
			lock.setwriteLockTransactionId(0);
			lock.readLockTransactionId.add(trans.getTransactionId());
			if (!trans.getDataItems().contains(dataItem))
				trans.setDataItems(dataItem);
			transactionTableMap.put(trans.getTransactionId(), trans);
			System.out.println(processedLine+ "  For the data item " + dataItem + " and transaction T" + trans.getTransactionId()
					+ " lock has been downgraded to Read Lock." + "\n");
		} else {
			Transaction t1 = transactionTableMap.get(lock.writeLockTransactionId);
			if (t1.getTimestamp()>trans.getTimestamp()) {
				t1.setTransactionState("Abort");
				transactionTableMap.put(t1.getTransactionId(), t1);
				lock.writeLockTransactionId = 0;
				lock.lockState = "Read";
				lock.readLockTransactionId.add(t1.getTransactionId());
				if (!t1.getDataItems().contains(dataItem))
					t1.getDataItems().add(dataItem);
				transactionTableMap.put(t1.getTransactionId(), t1);

				System.out.println(processedLine+ "  Transaction T" + t1.getTransactionId()
						+ " aborts because it has higher timestamp and transaction T" + t1.getTransactionId()
						+ " procures Read Lock on data item " + dataItem + "\n");
				RigorousTwoPhaseLocking.releaseLock(processedLine, t1, dataItem);
			} else {
				t1.setTransactionState("Block");
				t1.getblockedOperations().add(new Operation("Read", dataItem));

				lock.setwaitingOperationList(t1.getTransactionId());

				if (!t1.getDataItems().contains(dataItem))
					t1.setDataItems(dataItem);
				transactionTableMap.put(t1.getTransactionId(), t1);
				System.out.println(processedLine+  "  Transaction T" + trans.getTransactionId() + " has been blocked and Read operation on "
						+ dataItem
						+ " has been added to the blocked operation queue in the transaction table and transaction T"
						+ trans.getTransactionId() + " has been added to the waiting list in the lock table."
						+ "\n");
			}
		}
		return lock;
	}
	
	//Resolves the write-write conflict between the transaction
	public static Lock write_write(String processedLine, String dataItem, Transaction trans, Lock lock) {

		Transaction t1 = transactionTableMap.get(lock.writeLockTransactionId);
		if (t1.getTimestamp()>trans.getTimestamp()) {
			t1.setTransactionState("Abort");
			transactionTableMap.put(t1.getTransactionId(), t1);
			lock.writeLockTransactionId = t1.getTransactionId();
			System.out.println(processedLine+
					"  Transaction T" + t1.getTransactionId() + " is aborted due to higher timestamp and transaction T"
							+ t1.getTransactionId() + " has procured write lock on data item" + dataItem);
			RigorousTwoPhaseLocking.releaseLock(processedLine, t1, dataItem);
		} else {
			t1.setTransactionState("Block");
			t1.setblockedOperations(new Operation("Write", dataItem));
			transactionTableMap.put(t1.getTransactionId(), t1);
			lock.setwriteLockTransactionId(t1.getTransactionId());
			System.out.println(processedLine + "  Transaction T" + t1.getTransactionId()
					+ " has been blocked due to high timestamp and write operation for " + dataItem
					+ " has been added to the blocked operation queue of transaction table and the transaction T"
					+ t1.getTransactionId() + " has been added to the waiting list of lock table." + "\n");
		}
		if (!t1.getDataItems().contains(dataItem)) {
			t1.setDataItems(dataItem);
			transactionTableMap.put(t1.getTransactionId(), t1);
		}
		return lock;
	}

}