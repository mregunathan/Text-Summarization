import java.io.BufferedReader;
import java.io.FileReader;

public class SummaryGenerator{

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println(" Enter the input file name    :");
        String file_name = br.readLine();
        Measures m = new Measures("E:\\Project\\Inputs\\" + file_name + ".txt");
    }
}
