import java.io.*;
import java.net.*;
import java.util.*;

public class Worker extends Thread {
    Socket socket;
    List<Integer> list = new ArrayList<>();

    public Worker(Socket s) {
        socket = s;
    }

    public void run() {
        try {
            InputStream in = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            OutputStream out = socket.getOutputStream();
            PrintWriter pr = new PrintWriter(out, true);

            int i = 0;
            int firstInt = Integer.parseInt(br.readLine());
            list.add(firstInt);
            pr.println("[#" + (i + 1) + "] = " + list.get(i));
            i++;

            while (true) {
                String option = br.readLine();
                char chOp = option.charAt(0);
                int result;

                if (chOp == 'q') {
                    break;
                } else if (chOp == '#') {
                    firstInt = list.get(Character.getNumericValue(option.charAt(1)) - 1);
                    list.add(firstInt);
                } else {
                    int secondInt = Integer.parseInt(br.readLine());
                    result = switch (chOp) {
                        case '+' -> firstInt + secondInt;
                        case '-' -> firstInt - secondInt;
                        case '*' -> firstInt * secondInt;
                        case '/' -> firstInt / secondInt;
                        default -> throw new IllegalStateException("Incorrect");
                    };
                    list.add(result);
                    firstInt = result;
                }
                pr.println("[#" + (i + 1) + "] = " + list.get(i));
                i++;
            }
            socket.close();
        } catch (Exception e) {
            System.out.println("Error has occurred in Worker.");
        }
    }
}