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

	public int getwriteLockTransactionId() {
		return writeLockTransactionId;
	}

	public void setwriteLockTransactionId(int writeLockTransactionId) {
		this.writeLockTransactionId = writeLockTransactionId;
	}

	public List<Integer> getreadLockTransactionId() {
		return readLockTransactionId;
	}

	public void setreadLockTransactionIds(List<Integer> readLockTransactionId) {
		List<Integer> readLockTransactionIds = new ArrayList<Integer>();
		readLockTransactionIds.addAll(readLockTransactionId);
	}
	
	public void setreadLockTransactionId(Integer readLockTransactionId) {
		List<Integer> readLockTransactionIds = new ArrayList<Integer>();
		readLockTransactionIds.add(readLockTransactionId);
	}

	public PriorityQueue<Integer> getwaitingOperationList() {
		return waitingOperationList;
	}

	public void setwaitingOperationListings(List<Integer> waitingOperationList) {
		PriorityQueue<Integer> waitListings = new PriorityQueue<Integer>();
		waitListings.addAll(waitingOperationList);
	}
	
	public void setwaitingOperationList(Integer waitingOperationList) {
		PriorityQueue<Integer> waitList = new PriorityQueue<Integer>();
		waitList.add(waitingOperationList);
	}

}
