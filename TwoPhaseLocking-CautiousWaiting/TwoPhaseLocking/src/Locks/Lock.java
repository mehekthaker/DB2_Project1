package Locks;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Lock {
	String dataItemName;
	String lockState;
	int writeLockTransactionId;
	public List<Integer> readLockTransactionId;
	PriorityQueue<Integer> waitingOperationList;

	public Lock() {
		super();

	}

	public Lock(String dataItemName, String lockState, int writeLockTransactionId) {
		super();
		this.dataItemName = dataItemName;
		this.lockState = lockState;
		this.writeLockTransactionId = writeLockTransactionId;
		readLockTransactionId = new ArrayList<Integer>();
		waitingOperationList = new PriorityQueue<Integer>();
	}

	public String getDataItemName() {
		return dataItemName;
	}

	public void setDataItemName(String dataItemName) {
		this.dataItemName = dataItemName;
	}

	public String getLockState() {
		return lockState;
	}

	public void setLockState(String lockState) {
		this.lockState = lockState;
	}

	public int getWriteLockTransactionId() {
		return writeLockTransactionId;
	}

	public void setWriteLockTransactionId(int writeLockTransId) {
		this.writeLockTransactionId = writeLockTransId;
	}

	public List<Integer> getReadLockTransId() {
		return readLockTransactionId;
	}

	public void setReadLockTransactionIds(List<Integer> readLockTransactionId) {
		List<Integer> readLockTransIds = new ArrayList<Integer>();
		readLockTransIds.addAll(readLockTransactionId);
	}

	public void setReadLockTransactionId(Integer readLockTransactionId) {
		List<Integer> readLockTransIds = new ArrayList<Integer>();
		readLockTransIds.add(readLockTransactionId);
	}

	public PriorityQueue<Integer> getWaitingOperationList() {
		return waitingOperationList;
	}

	public void setWaitingOperationListings(List<Integer> waitingOperationList) {
		PriorityQueue<Integer> waitListings = new PriorityQueue<Integer>();
		waitListings.addAll(waitingOperationList);
	}

	public void setWaitingOperationList(Integer waitingOperationList) {
		PriorityQueue<Integer> waitListings = new PriorityQueue<Integer>();
		waitListings.add(waitingOperationList);
	}

}
