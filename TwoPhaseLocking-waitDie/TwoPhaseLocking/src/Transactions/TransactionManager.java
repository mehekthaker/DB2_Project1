package Transactions;

import java.util.HashMap;
import java.util.Queue;

import Operations.Operation;
import waitDie.RigorousTwoPhaseLocking;

public class TransactionManager {

	static HashMap<Integer, Transaction> transactionTableMap = new HashMap<Integer, Transaction>();
	
	// Begins the transaction and the start timestamp is recorded at this point
	public static HashMap<Integer, Transaction> beginTransaction(String processedLine, int timestamp, int transactionId) {

		Transaction newTransaction = new Transaction(transactionId, timestamp, "Active");
		transactionTableMap.put(transactionId, newTransaction);
		
		System.out.print(processedLine+ "	" + " Begin T" + newTransaction.getTransactionId()
				+ ": Record added to the transaction table with Tid=" + newTransaction.getTransactionId() + "and TS(T"
				+ newTransaction.getTransactionId() + ")=" + newTransaction.getTimestamp() + ". T"
				+ newTransaction.getTransactionId() + " state=" + newTransaction.getTransactionState() + ".\n");
		return transactionTableMap;
	}
	
	
	//Requests for read or write lock as per the input file
	public static HashMap<Integer, Transaction> requestLock(String processedLine, String dataItem, int transactionId, String operation) {

		operation = operation.equals("r") ? "Read" : "Write";
		Transaction t = transactionTableMap.get(transactionId);

		if (t.getTransactionState().equals("Active")) {
			transactionTableMap = RigorousTwoPhaseLocking.activeTransaction(processedLine, dataItem, t, operation);
		} else if (t.getTransactionState().equals("Block")) {
			transactionTableMap = RigorousTwoPhaseLocking.blockTransaction(processedLine, dataItem, t, operation);
		} else if (t.getTransactionState().equals("Abort")) {
			System.out.print(processedLine+ " Transaction " + t.getTransactionId() + "is aborted.");
		} else if (t.getTransactionState().equals("Commit")) {
			System.out.print(processedLine+ " Transaction " + t.getTransactionId() + "is committed.");
		}
		return transactionTableMap;
	}
	
	//Performs the commit transaction
	public static void commitTransaction(String processedLine, Transaction t) {

		if (t.getTransactionState().equals("Active")) {
			t.setTransactionState("Commit");
			Queue<String> dataItems = t.dataItems;

			while (!dataItems.isEmpty()) {
				String item = dataItems.remove();
				RigorousTwoPhaseLocking.releaseLock(processedLine, t, item);
			}
			transactionTableMap.put(t.getTransactionId(), t);
			System.out.print(processedLine + " T" + t.getTransactionId() + " state=" + t.getTransactionState() + ". T"
					+ t.getTransactionId() + " releases all locks held by T" + t.getTransactionId() + "\n");
		} else if (t.getTransactionState().equals("Block")) {
			t.blockedOperations.add(new Operation("Commit", ""));
			transactionTableMap.put(t.getTransactionId(), t);
			System.out.println(processedLine+ "  Commit operation on transaction T" + t.getTransactionId()
					+ " has been added to the blocked operation queue");
		} else if (t.getTransactionState().equals("Abort")) {
			System.out.println(processedLine+ "  Transaction T" + t.getTransactionId()
					+ " cannot be committed because it has already been aborted.");
		}
	}
	
	//Performs the transaction abort
	public static void abortTransaction(String processedLine,Transaction t) {
		t.setTransactionState("Abort");
		Queue<String> dataItems = t.dataItems;

		System.out.println("Releasing locks procured by transaction T" + t.transactionId);

		while (!dataItems.isEmpty()) {
			String item = dataItems.remove();
			RigorousTwoPhaseLocking.releaseLock(processedLine, t, item);
		}
		transactionTableMap.put(t.transactionId, t);

	}

}
