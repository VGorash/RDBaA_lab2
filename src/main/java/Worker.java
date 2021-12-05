import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.util.*;

public class Worker extends Thread {

    private static final String jdbcUrl = "jdbc:postgresql://localhost:5432/studs";
    private static final String jdbcUser = "s264470";
    private static final String jdbcPassword = "npo508";

    private final Socket socket;
    private List<Integer> list;
    private final MessageDigest messageDigest;
    private final Connection connection;
    private final BufferedReader input;
    private final PrintWriter output;

    private String username;

    public Worker(Socket s) throws Exception{
        socket = s;
        messageDigest = MessageDigest.getInstance("SHA-512");
        Class.forName("org.postgresql.Driver");
        connection = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream(), true);
        list = new ArrayList<>();
    }

    private void initList() throws SQLException {
        String query = "SELECT record FROM rdba_calc WHERE user_id = ? ORDER BY seq_num";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, username);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()){
            list.add(resultSet.getInt(1));
        }
    }

    private void register() throws Exception {
        String username = input.readLine();
        String password = input.readLine();
        String query = "INSERT INTO rdba_user(username, password) VALUES (?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, username);
        preparedStatement.setBytes(2, messageDigest.digest(password.getBytes(StandardCharsets.UTF_8)));
        try {
            preparedStatement.execute();
            this.username = username;
            output.println("true");
            preparedStatement.close();
        }
        catch (SQLException e){
            output.println("false");
            preparedStatement.close();
        }
    }

    private void login() throws Exception{
        String username = input.readLine();
        String password = input.readLine();
        String query = "SELECT password FROM rdba_user WHERE username = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, username);
        ResultSet resultSet = preparedStatement.executeQuery();
        if(!resultSet.next()){
            output.println("false");
            resultSet.close();
            preparedStatement.close();
            return;
        }
        if(!Arrays.equals(messageDigest.digest(password.getBytes(StandardCharsets.UTF_8)), resultSet.getBytes(1))){
            output.println("false");
            resultSet.close();
            preparedStatement.close();
            return;
        }
        output.println("true");
        resultSet.close();
        preparedStatement.close();
        this.username = username;
    }

    private void addRecord(int value) throws SQLException{
        String query = "INSERT INTO rdba_calc(user_id, record) VALUES (?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, username);
        preparedStatement.setInt(2, value);
        preparedStatement.execute();
    }

    public void run() {
        try {
            if(input.readLine().equals("register")){
                while (username == null){
                    register();
                }
            }
            else{
                while (username == null){
                    login();
                }
            }
            initList();
            output.println(list.size());

            int i = 0;
            int firstInt;
            if(list.size() == 0){
                firstInt = Integer.parseInt(input.readLine());
                list.add(firstInt);
                addRecord(firstInt);
                output.println("[#" + (i + 1) + "] = " + list.get(i));
                i++;
            }
            else{
                i = list.size();
                firstInt = list.get(i-1);
                output.println("[#" + i + "] = " + firstInt);
            }

            while (true) {
                String option = input.readLine();
                char chOp = option.charAt(0);
                int result;

                if (chOp == 'q') {
                    break;
                } else if (chOp == '#') {
                    firstInt = list.get(Integer.parseInt(option.substring(1)) - 1);
                    list.add(firstInt);
                    addRecord(firstInt);
                } else {
                    int secondInt = Integer.parseInt(input.readLine());
                    result = switch (chOp) {
                        case '+' -> firstInt + secondInt;
                        case '-' -> firstInt - secondInt;
                        case '*' -> firstInt * secondInt;
                        case '/' -> firstInt / secondInt;
                        default -> throw new IllegalStateException("Incorrect");
                    };
                    list.add(result);
                    addRecord(result);
                    firstInt = result;
                }
                output.println("[#" + (i + 1) + "] = " + list.get(i));
                i++;
            }
            socket.close();
            connection.close();

        } catch (Exception e) {
            System.out.println("Error has occurred in Worker.");
            try {
                socket.close();
                connection.close();
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
}