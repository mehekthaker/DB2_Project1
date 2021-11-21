package waitDie;

import java.util.HashMap;
import java.util.List;
import java.util.Queue;

import Locks.Lock;
import Locks.LockManager;
import Operations.Operation;
import Transactions.Transaction;
import Transactions.TransactionManager;

public class RigorousTwoPhaseLocking {

	static HashMap<Integer, Transaction> transactionTableHashMap = new HashMap<Integer, Transaction>();

	static HashMap<String, Lock> lockTableHashMap = new HashMap<String, Lock>();
	
	//Adds the remaining operations of the blocking transaction in waiting list of operations

	public static HashMap<Integer, Transaction> blockTransaction(String processedLine, String dataItem, Transaction t,
			String operation) {

		if (lockTableHashMap.containsKey(dataItem)) {
			Lock lock = lockTableHashMap.get(dataItem);
			lock.getwaitingOperationList().add(t.getTransactionId());
			lockTableHashMap.put(dataItem, lock);
		}
		if (!t.getDataItems().contains(dataItem))
			t.getDataItems().add(dataItem);
		t.getblockedOperations().add(new Operation(operation, dataItem));
		transactionTableHashMap.put(t.getTransactionId(), t);
		System.out.println(processedLine + "  Transaction T" + t.getTransactionId() + " is in blocked state " + operation
				+ " operation on data item " + dataItem
				+ " has been added to the blocked operation queue of transaction table and the transaction T"
				+ t.getTransactionId() + " has been added to the waiting list of lock table." + "\n");
		return transactionTableHashMap;
	}

	// Handles the processing of transactions that are in active state 
	public static HashMap<Integer, Transaction> activeTransaction(String processedLine, String dataItem, Transaction trans,
			String operation) {

		if (lockTableHashMap.containsKey(dataItem)) {
			Lock record = lockTableHashMap.get(dataItem);
			if (record.getLockState().equals("Read") && operation.equals("Read")) {
				record = LockManager.read_read(processedLine, dataItem, trans, record);
			} else if (record.getLockState().equals("Write") && operation.equals("Read")) {
				record = LockManager.write_read(processedLine, dataItem, trans, record);
			} else if (record.getLockState().equals("Read") && operation.equals("Write")) {
				record = LockManager.read_write(processedLine, dataItem, trans, record);
			} else if (record.getLockState().equals("Write") && operation.equals("Write")) {
				record = LockManager.write_write(processedLine, dataItem, trans, record);
			} else if (record.getLockState().equals("") && operation.equals("Read")) {
				record.setLockState("Read");
				record.readLockTransactionId.add(trans.getTransactionId());
				System.out.println(processedLine + "  Transaction T" + trans.getTransactionId() + " has procured Read Lock on data item "
						+ dataItem + "\n");
			} else if (record.getLockState().equals("") && operation.equals("Write")) {
				record.setLockState("Write");
				record.setwriteLockTransactionId(trans.getTransactionId());
				System.out.print(processedLine + "  Transaction T" + trans.getTransactionId() + " has procured Write Lock on data item "
						+ dataItem + "\n");
			}
			lockTableHashMap.put(dataItem, record);

		} else {
			Lock record = null;
			if (operation.equals("Read")) {
				record = new Lock(dataItem, operation, 0);
				record.readLockTransactionId.add(trans.getTransactionId());
				System.out.println(processedLine + "  The transaction state for transaction T" + trans.getTransactionId()
						+ " is Active so entry for data item " + dataItem + " has been made in the lock table and transaction T" 
						+ trans.getTransactionId() + " has procured Read Lock on it." + "\n");
			} else if (operation.equals("Write")) {
				record = new Lock(dataItem, operation, trans.getTransactionId());
				System.out.println(processedLine + "  The transaction state for transaction T" + trans.getTransactionId()
						+ " is Active so entry for data item " + dataItem + " has been enter in the lock table and transaction T" 
						+ trans.getTransactionId() + " has procured Write Lock on it." + "\n");
			}
			if (!trans.getDataItems().contains(dataItem))
				trans.setDataItems(dataItem);
			transactionTableHashMap.put(trans.getTransactionId(), trans);
			lockTableHashMap.put(dataItem, record);
		}
		return transactionTableHashMap;
	}
	
	// Releases locks before aborting or committing the transaction
	public static void releaseLock(String processedLine, Transaction trans, String dataItem) {

		Lock lockRecord = lockTableHashMap.get(dataItem);

		if (lockRecord.getLockState().equals("Write") || lockRecord.readLockTransactionId.size() == 1) {
			Queue<Integer> wt = lockRecord.getwaitingOperationList();
			lockRecord.setLockState("");

			if (lockRecord.readLockTransactionId.size() == 1) {
				lockRecord.readLockTransactionId.remove(0);
				System.out
						.println( "Transaction T" + trans.getTransactionId() + " has released read lock on " + dataItem);
			} else {
				System.out
						.println("Transaction T" + trans.getTransactionId() + " has released write lock on " + dataItem);
			}
			lockTableHashMap.put(dataItem, lockRecord);
			if (wt.isEmpty()) {
				lockTableHashMap.remove(dataItem);

			} else {
				while (!lockRecord.getwaitingOperationList().isEmpty()) {
					int tid = lockRecord.getwaitingOperationList().remove();
					Transaction t = transactionTableHashMap.get(tid);

					t = procureLock(processedLine, t, dataItem, lockRecord);
					transactionTableHashMap.put(tid, t);
					if (!t.getTransactionState().equals("Commit")) {
						return;
					}
				}
			}
			lockTableHashMap.remove(dataItem);
		} else if (lockRecord.getLockState().equals("Read")) {
			List<Integer> rtids = lockRecord.readLockTransactionId;
			for (int i = 0; i < rtids.size(); ++i) {
				if (rtids.get(i) == trans.getTransactionId()) {
					rtids.remove(i);
				}
			}
			System.out.println("Transaction T" + trans.getTransactionId() + " has released read lock on " + dataItem);
			lockTableHashMap.put(dataItem, lockRecord);
		}

	}
	
	// Acquires lock on a data item when requested by the transaction
	public static Transaction procureLock(String processedLine, Transaction trans, String dataItem, Lock lockRecord) {
		Queue<Operation> wo = trans.getblockedOperations();
		trans.setTransactionState("Active");
		transactionTableHashMap.put(trans.getTransactionId(), trans);

		if (!wo.isEmpty()) {
			System.out.println(processedLine + "  Transaction T" + trans.getTransactionId()
					+ " has been changed from Block to Active State");
			System.out.println(processedLine + "  Running its blocked operations...");
		}
		while (!wo.isEmpty()) {
			
			Operation o = wo.remove();
			if (o.getOperation().equals("Read")) {
				TransactionManager.requestLock(processedLine, o.getDataItem(), trans.getTransactionId(), "r");
			} else if (o.getOperation().equals("Write")) {
				TransactionManager.requestLock(processedLine, o.getDataItem(), trans.getTransactionId(), "w");
			} else if (o.getOperation().equals("Commit")) {
				TransactionManager.commitTransaction(processedLine, trans);

			}
		}
		lockTableHashMap.put(dataItem, lockRecord);

		return trans;
	}
}