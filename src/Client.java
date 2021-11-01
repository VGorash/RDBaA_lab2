import java.io.*;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        int port = 555;
        String ip = "127.0.0.1";

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        try {
            Socket socket = new Socket(ip, port);
            InputStream in = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            OutputStream out = socket.getOutputStream();
            PrintWriter pr = new PrintWriter(out, true);

            System.out.println("""
                    Usage:\s
                    when a first symbol on line is ‘>’ – enter operand (number)
                    when a first symbol on line is ‘@’ – enter operation
                    operation is one of ‘+’, ‘-‘, ‘/’, ‘*’ or
                    ‘#’ followed with number of evaluation step
                    ‘q’ to exit\s""");
            String inp, res;
            String regex = "\\d+";
            System.out.print("> ");
            inp = input.readLine();
            while (!inp.matches(regex)) {
                System.out.println("Please enter only an integer number without spaces or other characters");
                System.out.print("> ");
                inp = input.readLine();
            }
            pr.println(inp);
            System.out.println(br.readLine());

            while (true) {
                processInput(input, pr, br, socket);
                res = br.readLine();
                System.out.println(res);
            }
        } catch (Exception e) {
            System.out.println("Some kind of error has occurred.");
            System.exit(0);
        }
    }

    public static void processInput(BufferedReader input, PrintWriter pr, BufferedReader br, Socket socket) throws IOException {
        System.out.print("@: ");
        String option = input.readLine();
        while (!(option.matches("[-+*/q]") || option.matches("#[\\d+]"))) {
            System.out.println("Please enter an operation which is one of ‘+’, ‘-‘, ‘/’, ‘*’ or\n" +
                    "‘#’ followed with number of evaluation step" +
                    "‘q’ to exit");
            System.out.print("@: ");
            option = input.readLine();
        }
        pr.println(option);
        if (option.charAt(0) == '#') {
            System.out.println(br.readLine());
            processInput(input, pr, br, socket);
        } else if (option.charAt(0) == 'q') {
            socket.close();
            System.exit(0);
        } else {
            System.out.print("> ");
            String str = input.readLine();
            while (!str.matches("\\d+")) {
                System.out.println("Please enter an integer number without spaces or other characters");
                System.out.print("> ");
                str = input.readLine();
            }
            pr.println(str);
        }
    }
}