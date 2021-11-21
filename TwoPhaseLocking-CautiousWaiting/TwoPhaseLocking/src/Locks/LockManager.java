package Locks;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import Operations.Operation;
import Transactions.Transaction;
import Transactions.TransactionManager;
import cautiousWaiting.RigorousTwoPhaseLocking;

public class LockManager {

	static HashMap<Integer, Transaction> transactionTableHashMap = new HashMap<Integer, Transaction>();

	// Resolves the read-read conflict between the transactions
	public static Lock read_read(String processedLine, String dataItem, Transaction trans, Lock lock) {
		lock.readLockTransactionId.add(trans.getTransactionId());
		if (!trans.getDataItems().contains(dataItem))
			trans.setDataItems(dataItem);
		transactionTableHashMap.put(trans.getTransactionId(), trans);
		System.out.println(processedLine + "  " + dataItem + " read locked by T" + trans.getTransactionId()
				+ ": Lock Table record for " + dataItem + " is created with mode " + lock.getLockState() + " (T"
				+ trans.getTransactionId() + " holds lock).\n");

		return lock;
	}

	// Resolves the read-write conflict between the transactions
	public static Lock read_write(String processedLine, String dataItem, Transaction trans, Lock lock) {
		if (lock.getReadLockTransId().size() == 1
				&& lock.getReadLockTransId().get(0).equals(trans.getTransactionId())) {
			lock.setLockState("Write");
			lock.getReadLockTransId().remove(0);
			lock.setWriteLockTransactionId(trans.getTransactionId());
			System.out.println(processedLine + "   Read lock upgraded to write lock for item " + dataItem + " by T"
					+ trans.getTransactionId() + ", lock table updated to mode W.\n");
		} else if (lock.getReadLockTransId().size() == 1
				&& !lock.getReadLockTransId().get(0).equals(trans.getTransactionId())) {
			Transaction t1 = transactionTableHashMap.get(lock.writeLockTransactionId);
			if (t1.getDataItems().equals(trans.getDataItems())) {
				t1.setTransactionState("Abort");
				transactionTableHashMap.put(t1.getTransactionId(), t1);
				lock.setLockState("Write");
				lock.setWriteLockTransactionId(t1.getTransactionId());
				lock.getReadLockTransId().remove(0);
				System.out.println(processedLine + "  Transaction T" + t1.getTransactionId()
						+ " is aborted because of accessing same data item " + dataItem + " as the transaction T"
						+ trans.getTransactionId() + " which is waiting for another operation. Transaction"
						+ t1.getTransactionId() + " procures Write lock for data item" + dataItem);
				RigorousTwoPhaseLocking.releaseLock(processedLine, t1, dataItem);

			} else {
				t1.setTransactionState("Block");
				t1.getBlockedOperations().add(new Operation("Write", dataItem));
				transactionTableHashMap.put(t1.getTransactionId(), t1);
				lock.waitingOperationList.add(t1.getTransactionId());
				System.out.println(processedLine + "Transaction T" + t1.getTransactionId()
						+ " is blocked because of lock on same data item and write operation for " + dataItem
						+ " has been added to the blocked operation queue of transaction table and the transaction T"
						+ t1.getTransactionId() + " has been added to the waiting list of lock table." + "\n");
			}

		} else if (lock.getReadLockTransId().size() > 1) {
			List<Integer> readTransId = lock.getReadLockTransId();
			Collections.sort(readTransId);
			int first = readTransId.get(0);
			if (first == trans.getTransactionId()) {
				System.out.println(processedLine + "  For transaction T" + trans.getTransactionId()
						+ " lock for data item " + dataItem + " has been upgraded to write.");
				for (int i = 1; i < readTransId.size(); i++) {
					Transaction t1 = transactionTableHashMap.get(readTransId.get(i));
					TransactionManager.abortTransaction(processedLine, t1);
					System.out.println("Aborting transaction " + trans.getTransactionId() + "...");
				}
				lock.readLockTransactionId.clear();
				lock.setWriteLockTransactionId(first);
			} else if (trans.getTransactionId() < first) {
				System.out.println(processedLine + "Transaction T" + trans.getTransactionId()
						+ " has procured write lock for data item " + dataItem);

				for (int i = 0; i < readTransId.size(); i++) {
					Transaction t1 = transactionTableHashMap.get(readTransId.get(i));
					TransactionManager.abortTransaction(processedLine, t1);
					System.out.println(processedLine + "Aborting transaction T" + t1.getTransactionId() + "...");
				}
				lock.getReadLockTransId().clear();
				lock.setWriteLockTransactionId(first);
			} else {
				trans.setTransactionState("Block");
				trans.getBlockedOperations().add(new Operation(dataItem, "Write"));
				System.out.println(processedLine + "  Transaction T" + trans.getTransactionId()
						+ " has been blocked because it has applied Write lock on the same data item.");
				int i = 0;
				while (i < readTransId.size()) {
					if (trans.getTransactionId() >= readTransId.get(i))
						i++;
					else {
						readTransId.remove(i - 1);
						Transaction t1 = transactionTableHashMap.get(readTransId.get(i - 1));
						TransactionManager.abortTransaction(processedLine, t1);
						System.out.println("Aborting transaction T" + t1.getTransactionId() + "...");
					}
				}
				lock.readLockTransactionId = readTransId;
			}

		}
		if (!trans.getDataItems().contains(dataItem)) {
			trans.getDataItems().add(dataItem);
			transactionTableHashMap.put(trans.getTransactionId(), trans);
		}
		return lock;
	}

	// Resolves the write-read conflict between the transactions
	public static Lock write_read(String processedLine, String dataItem, Transaction trans, Lock lock) {

		if (lock.writeLockTransactionId == trans.getTransactionId()) {
			lock.setLockState("Read");
			lock.setWriteLockTransactionId(0);
			lock.readLockTransactionId.add(trans.getTransactionId());
			if (!trans.getDataItems().contains(dataItem))
				trans.setDataItems(dataItem);
			transactionTableHashMap.put(trans.getTransactionId(), trans);
			System.out.println(processedLine + "  For the data item " + dataItem + " and transaction T"
					+ trans.getTransactionId() + " lock has been downgraded to Read Lock." + "\n");
		} else {
			Transaction t1 = transactionTableHashMap.get(lock.writeLockTransactionId);
			if (t1.getDataItems().equals(trans.getDataItems())) {
				t1.setTransactionState("Abort");
				transactionTableHashMap.put(t1.getTransactionId(), t1);
				lock.writeLockTransactionId = 0;
				lock.lockState = "Read";
				lock.readLockTransactionId.add(t1.getTransactionId());
				if (!t1.getDataItems().contains(dataItem))
					t1.getDataItems().add(dataItem);
				transactionTableHashMap.put(t1.getTransactionId(), t1);

				System.out.println("Transaction " + t1.getTransactionId()
						+ " aborts because it tries to procure lock on same data item as another transaction T"
						+ trans.getTransactionId() + ". T" + t1.getTransactionId() + " procures Read Lock on data item "
						+ dataItem + "\n");
				RigorousTwoPhaseLocking.releaseLock(processedLine, t1, dataItem);
			} else {
				t1.setTransactionState("Block");
				t1.getBlockedOperations().add(new Operation("Read", dataItem));

				lock.setWaitingOperationList(t1.getTransactionId());

				if (!t1.getDataItems().contains(dataItem))
					t1.setDataItems(dataItem);
				transactionTableHashMap.put(t1.getTransactionId(), t1);
				System.out.println(processedLine + "  Transaction T" + trans.getTransactionId()
						+ " has been blocked and Read operation on " + dataItem
						+ " has been added to the blocked operation queue in the transaction table and transaction T"
						+ trans.getTransactionId() + " has been added to the waiting list in the lock table."
						+ "\n");
			}
		}
		return lock;
	}

	// Resolves the write-write conflict between the transactions
	public static Lock write_write(String processedLine, String dataItem, Transaction trans, Lock lock) {

		Transaction t1 = transactionTableHashMap.get(lock.writeLockTransactionId);
		if (t1.getDataItems().equals(trans.getDataItems())) {
			t1.setTransactionState("Abort");
			transactionTableHashMap.put(t1.getTransactionId(), t1);
			lock.writeLockTransactionId = t1.getTransactionId();
			System.out.println(processedLine + "  Transaction T" + t1.getTransactionId()
					+ " is aborted because of accessing same data item " + dataItem + " as the transaction T"
					+ trans.getTransactionId() + " which is waiting for another operation. Transaction T"
					+ t1.getTransactionId() + " has procured write lock on data item" + dataItem);
			RigorousTwoPhaseLocking.releaseLock(processedLine, t1, dataItem);
		} else {
			t1.setTransactionState("Block");
			t1.setBlockedOperations(new Operation("Write", dataItem));
			transactionTableHashMap.put(t1.getTransactionId(), t1);
			lock.setWaitingOperationList(t1.getTransactionId());
			System.out.println(processedLine + "  Transaction T" + t1.getTransactionId()
					+ " has been blocked because of procuring lock on same data item " + dataItem + " and write operation for " + dataItem
					+ " has been added to the blocked operation queue of transaction table and the transaction T"
					+ t1.getTransactionId() + " has been added to the waiting list of lock table." + "\n");
		}
		if (!t1.getDataItems().contains(dataItem)) {
			t1.setDataItems(dataItem);
			transactionTableHashMap.put(t1.getTransactionId(), t1);
		}
		return lock;
	}

}
