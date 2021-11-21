package Main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import Transactions.Transaction;
import Transactions.TransactionManager;

public class Main {

	static HashMap<Integer, Transaction> transactionTableHashMap = new HashMap<Integer, Transaction>();

	public static void main(String[] args) {
		// ***Change this line to try with different input transactions***
		String fileName = "./inputFiles/input6.txt";
		String line = null;

		int timestamp = 0;

		try {
			FileReader readFile = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(readFile);

			while ((line = bufferedReader.readLine()) != null) {
				String processedLine = line.replace(" ", "");

				// Transaction begins
				if (processedLine.charAt(0) == 'b') {
					timestamp += 1;
					transactionTableHashMap = TransactionManager.beginTransaction(processedLine, timestamp,
							Integer.parseInt(processedLine.substring(1, processedLine.indexOf(";"))));
				}

				// Read and Write operations are handled
				if (processedLine.charAt(0) == 'r' || processedLine.charAt(0) == 'w') {
					transactionTableHashMap = TransactionManager.requestLock(processedLine,
							processedLine.substring(processedLine.indexOf('(') + 1, processedLine.indexOf(')')),
							Integer.parseInt(processedLine.substring(1, processedLine.indexOf('('))),
							processedLine.charAt(0) + "");
				}

				// Commits the transaction
				if (processedLine.charAt(0) == 'e') {
					TransactionManager.commitTransaction(processedLine, transactionTableHashMap
							.get(Integer.parseInt(processedLine.substring(1, processedLine.indexOf(";")))));
				}
			}
			bufferedReader.close();

		} catch (FileNotFoundException ex) {
			System.out.print("Cannot open the file '" + fileName + "'");
		} catch (IOException io) {
			io.printStackTrace();
		}
	}

}
