import java.io.*;
import java.net.Socket;

public class Client {

    private static final int port = 555;
    private static final String ip = "127.0.0.1";

    private final BufferedReader serverInput;
    private final BufferedReader userInput;
    private final PrintWriter serverOutput;
    private final Socket socket;

    public static void main(String[] args) {
        try {
            new Client().run();
        }
        catch (Exception e) {
            System.out.println("Some kind of error has occurred.");
            System.exit(0);
        }
    }

    public Client() throws IOException{
        socket = new Socket(ip, port);
        serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        serverOutput = new PrintWriter(socket.getOutputStream(), true);
        userInput = new BufferedReader(new InputStreamReader(System.in));
    }

    private void run() throws IOException{
        System.out.println("Enter 'register' if you want to register, any string otherwise");
        serverOutput.println(userInput.readLine());
        boolean logged = false;
        while (!logged){
            login();
            logged = Boolean.parseBoolean(serverInput.readLine());
        }
        System.out.println("""
                    Usage:\s
                    when a first symbol on line is ‘>’ – enter operand (number)
                    when a first symbol on line is ‘@’ – enter operation
                    operation is one of ‘+’, ‘-‘, ‘/’, ‘*’ or
                    ‘#’ followed with number of evaluation step
                    ‘q’ to exit\s""");
        String inp, res;
        String regex = "-?\\d+";
        if(Integer.parseInt(serverInput.readLine()) == 0){
            System.out.print("> ");
            inp = userInput.readLine();
            while (!inp.matches(regex)) {
                System.out.println("Please enter only an integer number without spaces or other characters");
                System.out.print("> ");
                inp = userInput.readLine();
            }
            serverOutput.println(inp);
        }
        System.out.println(serverInput.readLine());

        while (true) {
            processInput();
            res = serverInput.readLine();
            System.out.println(res);
        }
    }

    private void login() throws IOException{
        System.out.println("Enter username");
        serverOutput.println(userInput.readLine());
        System.out.println("Enter password");
        serverOutput.println(userInput.readLine());
    }

    private void processInput() throws IOException {
        System.out.print("@: ");
        String option = userInput.readLine();
        while (!(option.matches("[-+*/q]") || option.matches("#[\\d+]"))) {
            System.out.println("Please enter an operation which is one of ‘+’, ‘-‘, ‘/’, ‘*’ or\n" +
                    "‘#’ followed with number of evaluation step" +
                    "‘q’ to exit");
            System.out.print("@: ");
            option = userInput.readLine();
        }
        serverOutput.println(option);
        if (option.charAt(0) == '#') {
            System.out.println(serverInput.readLine());
            processInput();
        } else if (option.charAt(0) == 'q') {
            socket.close();
            System.exit(0);
        } else {
            System.out.print("> ");
            String str = userInput.readLine();
            while (!str.matches("-?\\d+")) {
                System.out.println("Please enter an integer number without spaces or other characters");
                System.out.print("> ");
                str = userInput.readLine();
            }
            serverOutput.println(str);
        }
    }
}