package Transactions;

import java.util.LinkedList;
import java.util.Queue;

import Operations.Operation;

public class Transaction {
	int transactionId;
	int timestamp;
	String transactionState;
	public Queue<String> dataItems;
	Queue<Operation> blockedOperations;

	public Transaction() {
		super();
	}

	public Transaction(int transactionId, int timestamp, String transactionState) {
		super();
		this.transactionId = transactionId;
		this.timestamp = timestamp;
		this.transactionState = transactionState;
		dataItems=new LinkedList<String>();
		blockedOperations=new LinkedList<Operation>();
	}

	public Queue<String> getDataItems() {
		return dataItems;
	}

	public void setDataItems(String dataItems) {
		Queue<String> dataItemQueue = new LinkedList<String>();
		dataItemQueue.add(dataItems);
	}

	public Queue<Operation> getblockedOperations() {
		return blockedOperations;
	}

	public void setblockedOperations(Operation blockedOperations) {
		Queue<Operation> waitOperation = new LinkedList<Operation>();
		waitOperation.add(blockedOperations);
	}

	public int getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(int transactionId) {
		this.transactionId = transactionId;
	}

	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	public String getTransactionState() {
		return transactionState;
	}

	public void setTransactionState(String transactionState) {
		this.transactionState = transactionState;
	}

}
