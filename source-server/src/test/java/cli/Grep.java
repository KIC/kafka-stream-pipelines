package cli;


import java.io.IOException;

public class Grep {
    public static void main(String[] args) throws IOException {
        // read std in, apply regex, print to std out
        //Scanner scanner = new Scanner(System.in);
        new Thread(){
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                System.err.println("No data on stdin");
                System.exit(-1);
            }
        }.start();

        System.out.println("das" + System.in.read());


    }
}
