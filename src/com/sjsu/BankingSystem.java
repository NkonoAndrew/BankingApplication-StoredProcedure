package com.sjsu;

import java.io.FileInputStream;
import java.sql.*;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Manage connection to database and perform SQL statements.
 */
public class BankingSystem {
	//java -cp ";./db2jcc4.jar" BankingSystem.java ./db.properties
	// Connection properties
	private static String driver;
	private static String url;
	private static String username;
	private static String password;
	
	// JDBC Objects
	private static Connection con;

	private static Scanner in = new Scanner(System.in);
	private static Scanner input = new Scanner(System.in);

	private static final Pattern NUMBERS = Pattern.compile("\\d+");
	private static final Pattern LETTERS = Pattern.compile("\\p{Alpha}+");


	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Need database properties filename");
		} else {
			init(args[0]);
			try {
				Class.forName(driver);
				con = null;
				con = DriverManager.getConnection(url, username, password);
				welcomeScreen();
			} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Initialize database connection given properties file.
	 * @param filename name of properties file
	 */
	public static void init(String filename) {
		try {
			Properties props = new Properties();						// Create a new Properties object
			FileInputStream input = new FileInputStream(filename);	// Create a new FileInputStream object using our filename parameter
			props.load(input);										// Load the file contents into the Properties object
			driver = props.getProperty("jdbc.driver");				// Load the driver
			url = props.getProperty("jdbc.url");						// Load the url
			username = props.getProperty("jdbc.username");			// Load the username
			password = props.getProperty("jdbc.password");			// Load the password
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * User Welcome Screen
	 */
	public static void welcomeScreen() throws Exception {
		try {
		int input;
		boolean again = true;

		while (again) {
			System.out.println("Welcome!" +
					"\n 1. New Customer" +
					"\n 2. Login" +
					"\n 3. Cancel");
			System.out.print("Please make a choice:");
			input = Math.abs(in.nextInt());


			switch (input) {
				case 1:	//Prompt for Name, Gender, Age, and Pin.  System will return a customer ID if successful.
					String name;
					do {
						System.out.println("Enter name:");
						name = in.next();
					}while (isAlpha(name) == false);


					System.out.println("Enter a gender (M) or (F):");
					String gender = in.next();

					while ((!"M".equals(gender)) && (!"F".equals(gender))){
						System.out.println("Gender M or F!:");
						gender = in.nextLine();
					}

					String age1;
					do {
						System.out.println("Enter age:");
						age1 = in.nextLine();
					}while (isNumeric(age1) == false);

					int age = Integer.parseInt(age1);

					String pin1;
					do {
						System.out.println("Enter PIN:");
						pin1 = in.nextLine();
					}while (isNumeric(pin1) == false);

					int pin = Integer.parseInt(pin1);
					newCustomer(name, gender, age, pin);
					break;

				case 2: //, prompt for customer ID and pin to authenticate the customer.
						// If user enters 0 for both customer ID & pin, then you will go straight to Screen #4.
					String id;
					do {
						System.out.println("Enter ID:");
						id = in.next();
					}while (isNumeric(id) == false);
					int enterID = Integer.parseInt(id);


					String enterPin;
					do {
						System.out.println("Enter PIN:");
						enterPin = in.next();
					}while (isNumeric(enterPin) == false);
					int enterPin2 = Integer.valueOf(enterPin);

					int returnedPassword = userAuthentification(enterID, enterPin2);

					if (enterID == 0 && enterPin2 == 0){
						adminView();
					}else if (returnedPassword == 1) {
						customerMenu(enterID);
					}
					else {
						System.out.println("The password or userID is incorrect. Try again");
					}
					break;
				case 3:
					again = false;
					break;
			}
		}
		} catch (Exception e){
			System.out.println("Please enter correct input");
			e.printStackTrace();
			welcomeScreen();
		}

	}

	public static final boolean isNumeric(String text)
	{
		return NUMBERS.matcher(text).matches();
	}

	public static final boolean isAlpha(String text)
	{
		return LETTERS.matcher(text).matches();
	}



	public static void customerMenu(int ID) throws SQLException, Exception {
		try{
		int input2;
		boolean again = true;

		while (again) {
			System.out.println("Welcome!" +
					"\n 1. Open Account" +
					"\n 2. Close Account" +
					"\n 3. Deposit" +
					"\n 4. Withdraw" +
					"\n 5. Transfer" +
					"\n 6. Account Summary" +
					"\n 7. Exit");
			System.out.print("Please make a choice:");
			Scanner in = new Scanner(System.in);
			input2 = in.nextInt();


			switch (input2) {
				//new user needs to insert a name gender and pin
				case 1:  //prompt for customer ID, account type, and balance (Initial deposit).
					//System will return an account number if successful.
					String customerID;
					do {
						System.out.println("Enter ID:");
						customerID = in.next();
					}while (isNumeric(customerID) == false);


					System.out.println("Enter the account type C for Checking and S for Savings:");
					String accountType = in.next();

					while ((!"C".equals(accountType)) && (!"F".equals(accountType))){
						System.out.println("Enter Account Type C or S:");
						accountType = in.next();
					}


					String initialAmount;
					do {
						System.out.println("Enter initial amount:");
						initialAmount = in.next();
					}while (isNumeric(initialAmount) == false);


					openAccount(customerID, initialAmount, accountType);
					break;

				case 2:
					String closeThis;
					do {
						System.out.println("Enter the amount to close:");
						closeThis = in.next();
					}while (isNumeric(closeThis) == false);

					if (ID != isOwner(closeThis)){
						System.out.println("Sorry, this account belongs to another user");
					}
					else {
						closeAccount(closeThis);
						System.out.println("Account " + closeThis + " closed!");
					}
					break;

				case 3:    //prompt for account # and deposit amount
					String accountNo;
					do {
						System.out.println("Enter the account # to deposit to:");
						accountNo = in.next();
					}while (isNumeric(accountNo) == false);

//					System.out.println("Enter amount you wish to deposit:");
//					String depositAmount = in.next();

					String depositAmount;
					do {
						System.out.println("Enter amount you wish to deposit:");
						depositAmount = in.next();
					}while (isNumeric(depositAmount) == false);
					deposit(accountNo, depositAmount);
					break;

				case 4:	//prompt for account # and withdraw amount
					String withdrawFrom;
					do {
						System.out.println("Enter account number you wish to withdraw from:");
						withdrawFrom = in.next();
					}while (isNumeric(withdrawFrom) == false);


					String withdrawAmount;
					do {
						System.out.println("Enter amount you wish to withdraw:");
						withdrawAmount = in.next();
					}while (isNumeric(withdrawAmount) == false);

					if (ID != isOwner(withdrawFrom)){
						System.out.println("Can only withdraw from your account!");
					} else {
						withdraw(withdrawFrom, withdrawAmount);
					}
					break;

				case 5:	//prompt for the source and destination account #s and transfer amount.
					String withdrawFromAcc;
					do {
						System.out.println("Enter the account number you wish to withdraw from::");
						withdrawFromAcc = in.next();
					}while (isNumeric(withdrawFromAcc) == false);


					String transferTo;
					do {
						System.out.println("Enter the account number you wish to transfer to:");
						transferTo = in.next();
					}while (isNumeric(transferTo) == false);

					String transferAmount;
					do {
						System.out.println("Enter amount you wish to transfer:");
						transferAmount = in.next();
					}while (isNumeric(transferAmount) == false);

					if (ID != isOwner(withdrawFromAcc)){
						System.out.println("This account belongs to another user or does not exist");
					}
					else {
						transfer(withdrawFromAcc, transferTo, transferAmount);
					}
					break;
				case 6:	//display each account # and its balance for same customer
						// and the total balance of all accounts.
						accountSummary(String.valueOf(ID));
					break;
				case 7:
					again = false;
					break;
				default:
					System.out.println("Enter a valid number or enter 7 to exit");
					break;
			}
		}
		} catch (Exception e){
			System.out.println("Please enter data correctly:");

		}

	}

	public static void adminView() throws SQLException, Exception {
		try {
		int input3;
		boolean again = true;

		while (again) {
			System.out.println("Welcome!" +
					"\n 1. Account Summary for a customer" +
					"\n 2. Report A :: Customer Information with Total Balance" +
					"\n 3. Add Interest" +
					"\n 4. Exit");
			System.out.print("Please make a choice:");
			Scanner in = new Scanner(System.in);
			input3 = in.nextInt();


			switch (input3) {
				//new user needs to insert a name gender and pin
				case 1:
					String userID;
					do {
						System.out.println("Enter customer ID to view summary:");
						userID = in.nextLine();
					}while (isNumeric(userID) == false);
					accountSummary(userID);
					break;

				case 2:
					reportA();
					break;
				case 3:
					double userInput = 0;
					while (true) {
						System.out.println("Enter a Savings Rate:");
						try {
							userInput = Double.parseDouble(in.next());

							break;
						} catch (NumberFormatException ignore) {
							System.out.println("Invalid input");
						}
					}
					String savingsRate = String.valueOf(Math.abs(userInput));

					double userInput2 = 0;
					while (true) {
						System.out.println("Enter a Checking Rate:");
						try {
							userInput2 = Double.parseDouble(in.next());
							break;
						} catch (NumberFormatException ignore) {
							System.out.println("Invalid input");
						}
					}
					String checkingRate = String.valueOf(Math.abs(userInput2));

					System.out.println("Checking Rate:" + checkingRate);
					System.out.println("Savings Rate:" + savingsRate);
					addInterest(savingsRate, checkingRate);
					break;
				case 4:
					again = false;
					break;
				default:
					System.out.println("Enter a valid number or enter 4 to exit:");
					break;

			}
		}
		} catch (Exception e){
			System.out.println("Please enter correct input:");
			e.printStackTrace();
			welcomeScreen();
		}
	}

	/**
	 * Create a new customer.
	 * @param name customer name
	 * @param gender customer gender
	 * @param age customer age
	 * @param pin customer pin
	 */
	public static void newCustomer(String name, String gender, int age, int pin) throws SQLException {

		try {
			System.out.println(":: CREATE NEW CUSTOMER - RUNNING");
			CallableStatement callableStatement = con.prepareCall("{CALL P2.CUST_CRT(?, ?, ?, ?, ?, ?, ?)}");
			callableStatement.setString(1, name);
			callableStatement.setString(2, gender);
			callableStatement.setInt(3, age);
			callableStatement.setInt(4, pin);
			callableStatement.registerOutParameter(5, Types.INTEGER);
			callableStatement.registerOutParameter(6, Types.INTEGER);
			callableStatement.registerOutParameter(7, Types.VARCHAR);
			callableStatement.execute();

			System.out.println("Auto generated user ID is:" + callableStatement.getInt(5));

			System.out.println(callableStatement.getInt(6));
			System.out.println(callableStatement.getInt(7));
			System.out.println(":: CREATE NEW CUSTOMER - SUCCESS");
		} catch (SQLException e){
			e.getMessage();
		}


	}

	/**
	 * Open a new account.
	 * @param id customer id
	 * @param type type of account
	 * @param amount initial deposit amount
	 */
	public static void openAccount(String id, String amount, String type) throws SQLException {
		try{
			System.out.println(":: CREATE NEW ACCOUNT - RUNNING");
			CallableStatement callableStatement = con.prepareCall("{CALL P2.ACCT_OPN(?, ?, ?, ?, ?, ?)}");
			callableStatement.setString(1, id);
			callableStatement.setString(2, amount);
			callableStatement.setString(3,type);
			callableStatement.registerOutParameter(4, Types.INTEGER);
			callableStatement.registerOutParameter(5, Types.INTEGER);
			callableStatement.registerOutParameter(6, Types.VARCHAR);
			callableStatement.execute();

			System.out.println("Auto generated account ID is:" + callableStatement.getInt(4));
			System.out.println(":: CREATE NEW ACCOUNT - SUCCESS");

		} catch (SQLException e){
			e.getMessage();
			e.printStackTrace();
		}
	}


	public static int userAuthentification(int id, int pin) throws SQLException{
			CallableStatement callableStatement = con.prepareCall("{CALL P2.CUST_LOGIN(?, ?, ?, ?, ?)}");
			callableStatement.setInt(1, id);
			callableStatement.setInt(2, pin);
			callableStatement.registerOutParameter(3, Types.INTEGER);
			callableStatement.registerOutParameter(4, Types.INTEGER);
			callableStatement.registerOutParameter(5, Types.VARCHAR);

			callableStatement.execute();
			return  callableStatement.getInt(3);
	}

	/**
	 * Close an account.
	 * @param accNum account number
	 */
	public static void closeAccount(String accNum) throws SQLException {
		try{
			System.out.println(":: CLOSE ACCOUNT - RUNNING");

			CallableStatement callableStatement = con.prepareCall("{CALL P2.ACCT_CLS(?, ?, ?)}");
			callableStatement.setString(1, accNum);
			callableStatement.registerOutParameter(2, Types.INTEGER);
			callableStatement.registerOutParameter(3, Types.VARCHAR);
			callableStatement.execute();

		System.out.println(":: CLOSE ACCOUNT - SUCCESS");
		} catch (SQLException e){
			e.getMessage();
			e.printStackTrace();
		}
	}

	public static int isOwner(String accNum) throws SQLException {
		PreparedStatement preparedStatement = con.prepareStatement
				("SELECT id from p2.account where number = ?");

		preparedStatement.setString(1, accNum);
		ResultSet resultSet = preparedStatement.executeQuery();

		int id = 0;
		while (resultSet.next()){
			id = resultSet.getInt("id");
		}
		resultSet.close();
		return id;

	}


	/**
	 * Deposit into an account.
	 * @param accNum account number
	 * @param amount deposit amount
	 */
	public static void deposit(String accNum, String amount) throws SQLException {
		System.out.println(":: DEPOSIT - RUNNING");
			CallableStatement callableStatement = con.prepareCall("{CALL P2.ACCT_DEP(?, ?, ?, ?)}");
			callableStatement.setString(1, accNum);
			callableStatement.setString(2, amount);
			callableStatement.registerOutParameter(3, Types.INTEGER);
			callableStatement.registerOutParameter(4, Types.VARCHAR);
			callableStatement.execute();
		System.out.println(":: OPEN ACCOUNT - SUCCESS");
	}

	/**
	 * Withdraw from an account.
	 * @param accNum account number
	 * @param amount withdraw amount
	 */
	public static void withdraw(String accNum, String amount) throws SQLException {
		System.out.println(":: WITHDRAW - RUNNING");
			CallableStatement callableStatement = con.prepareCall("{CALL P2.ACCT_WTH(?, ?, ?, ?)}");
			callableStatement.setString(1, accNum);
			callableStatement.setString(2, amount);
			callableStatement.registerOutParameter(3, Types.INTEGER);
			callableStatement.registerOutParameter(4, Types.VARCHAR);
			callableStatement.execute();
		System.out.println(":: WITHDRAW - SUCCESS");

	}

	/**
	 * Transfer amount from source account to destination account.
	 * @param srcAccNum source account number
	 * @param destAccNum destination account number
	 * @param amount transfer amount
	 */
	public static void transfer(String srcAccNum, String destAccNum, String amount) throws SQLException {
		try {
			System.out.println(":: TRANSFER - RUNNING");
			CallableStatement callableStatement = con.prepareCall("{CALL P2.ACCT_TRX(?, ?, ?, ?, ?)}");
			callableStatement.setString(1, srcAccNum);
			callableStatement.setString(2, destAccNum);
			callableStatement.setString(3, amount);
			callableStatement.registerOutParameter(4, Types.INTEGER);
			callableStatement.registerOutParameter(5, Types.VARCHAR);
			callableStatement.execute();
			System.out.println(":: TRANSFER - SUCCESS");
		} catch (SQLException exception){
			System.out.println("Transfer not successful");
		}
	}

	/**
	 * Display account summary.
	 * @param cusID customer ID
	 */
	public static void accountSummary(String cusID) throws SQLException {
		System.out.println(":: ACCOUNT SUMMARY - RUNNING");
				try {
					PreparedStatement preparedStatement = con.prepareStatement
							("SELECT number, balance from p2.account WHERE id = ? AND status = 'A' ");
					preparedStatement.setString(1, cusID);
					ResultSet resultSet = preparedStatement.executeQuery();


					System.out.println("The account number(s) and balance of the current customer(" + cusID + ")");
					while (resultSet.next()) {
						System.out.print(resultSet.getInt("number") + "  ");
						System.out.print(resultSet.getInt("balance"));
						System.out.println();
					}
					resultSet.close();
				} catch (SQLException e){
					System.out.println("Cannot find the customer ID or the ID does not exist. Try again");
				}


				try {
					PreparedStatement preparedStatement = con.prepareStatement
							("SELECT sum(balance) as totalBalance from p2.account WHERE id = ? AND status = 'A' ");

					preparedStatement.setString(1, cusID);

					int total = 0;
					ResultSet resultSet1 = preparedStatement.executeQuery();

					System.out.print("Total balance of the current customer(" + cusID + ") =");

					while (resultSet1.next()) {
						total = resultSet1.getInt("totalBalance");
					}
					resultSet1.close();
					System.out.print(total);
				} catch (SQLException e){
					System.out.println("Cannot find the user balance");
				}
				System.out.println();
				System.out.println(":: ACCOUNT SUMMARY - SUCCESS");
	}

	/**
	 * Display Report A - Customer Information with Total Balance in Decreasing Order.
	 */
	public static void reportA() throws SQLException {
		System.out.println(":: REPORT A - RUNNING");
		try {
			PreparedStatement preparedStatement = con.prepareStatement
					("SELECT C.ID, name, age, gender, sum(balance) as totalBalance FROM p2.customer C, p2.account A WHERE C.id = A.id and STATUS = 'A' GROUP BY C.ID, name, name, age, gender ORDER BY totalBalance DESC");
			ResultSet resultSet = preparedStatement.executeQuery();

			String name, gender;
			int total = 0, age, id = 0;
			System.out.println("ID \t NAME \t AGE \t GENDER \t TOTAL BALANCE");
			while (resultSet.next()) {
				id = resultSet.getInt("ID");
				name = resultSet.getString("NAME");
				age = resultSet.getInt("AGE");
				gender = resultSet.getString("GENDER");
				total = resultSet.getInt("totalBalance");
				System.out.println(id + "\t" + name + "\t" + age + "\t" + gender + "\t\t\t" + total);

			}
			resultSet.close();
		} catch (SQLException e){
			System.out.println(e);
		}
		System.out.println(":: REPORT A - SUCCESS");
	}

	/**
	 * Add interest to all active accounts
	 */
	public static void addInterest(String savingsRate, String checkingRate){
		try{
			System.out.println(":: ADD INTEREST - RUNNING");
				CallableStatement callableStatement = con.prepareCall("{CALL P2.ADD_INTEREST(?, ?, ?, ?)}");
				callableStatement.setString(1, savingsRate);
				callableStatement.setString(2, checkingRate);
				callableStatement.registerOutParameter(3, Types.INTEGER);
				callableStatement.registerOutParameter(4, Types.VARCHAR);
				callableStatement.execute();
			System.out.println(":: ADD INTEREST - SUCCESS");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


}
