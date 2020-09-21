import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class ExtractQuery {

    public static void main(String[] args) {


        boolean foundQuote = false, foundConcat = false, foundSQuote = false, putSQuote = false;
        String newQuery = "";
        // String changeList = "";

        /*
         * try { FileWriter myWriter = new FileWriter(args[2]);
         * myWriter.write(changeList); myWriter.close(); } catch (IOException e) {
         * e.printStackTrace(); }
         */

        try {
            File myObj = new File(args[0]);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String str = myReader.nextLine();

                // System.out.println("-" + str + "-");
                str = str.replace("`", "'");
                str = str.replace("\"", " \" ");
                str = str.replace("+", " + ");
                str = str.replace(";", " ; ");
                str = str.replace("     ", " ");
                str = str.replace("    ", " ");
                str = str.replace("   ", " ");
                str = str.replace("  ", " ");
                // str = str.replace("("," ( ");
                // str = str.replace(")"," ) ");
                // System.out.println(data);

                String[] strings = str.split(" ");
                String data = "";

                // System.out.println(strings.length);

                // for(int i = 0; i < strings.length; i++) System.out.println(strings[i]);
                // System.out.println(data);
                for (int i = 0; i < strings.length; i++) {
                    data = strings[i];
                    // System.out.println("-" + data + "-");

                    /*
                     * if (Pattern.matches("[^\'].*[.].*[(].*[)]", data)) { data = "'" + data + "'";
                     * // System.out.println("******" + data); } else if
                     * (Pattern.matches("[^\'].*[(][)]", data)) { data = "'" + data + "'"; //
                     * System.out.println("******" + data); }
                     */

                    if (data.contains(";"))
                        break;
                    if (data.contains("\"")) {
                        foundQuote = !foundQuote;
                        // if (foundQuote)
                        foundConcat = false;
                        // newQuery += " " + data.replace("\"", "");
                        continue;
                        // System.out.println(data.replace("\"", ""));
                    }
                    if (foundQuote) {
                        newQuery += " " + data;
                        continue;
                    }
                    if (data.contains("+")) {
                        if (foundConcat && putSQuote) {
                            newQuery += "'";
                            putSQuote = false;
                        }
                        if (!newQuery.trim().endsWith("'") && !strings[i + 1].contains("\"")
                                && (strings[i + 1].contains("(") || strings[i + 1].contains("[")
                                        || (i + 2 < strings.length && !strings[i + 2].contains("+")))) {
                            putSQuote = true;
                            newQuery += " '";
                        }
                        foundConcat = true;
                        continue;
                    }
                    if (foundConcat) {
                        newQuery += " " + data;
                        // foundConcat = false;
                        continue;
                    }
                }
            }

            myReader.close();

        } catch (FileNotFoundException e) {
            System.out.println("*******An error occurred.");
            e.printStackTrace();
        }

        String[] strings = newQuery.split("'");
        newQuery = strings[0];
        for (int i = 1; i < strings.length; i += 2) {
            newQuery += "'" + strings[i].trim() + "'";
            if (i + 1 < strings.length)
                newQuery += strings[i + 1];
        }

        // System.out.println(newQuery);

        int lp, rp;

        lp = rp = 0;
        for (int i = 0; i < newQuery.length(); i++) {
            if (newQuery.charAt(i) == '(')
                lp++;
            else if (newQuery.charAt(i) == ')')
                rp++;
        }
        if (rp > lp) {
            newQuery = newQuery.substring(0, newQuery.length() - 1);
            // System.out.println("******" + newQuery);
        }

        // newQuery = newQuery.replace("''", "'");
        System.out.println(newQuery);
        try {
            FileWriter myWriter = new FileWriter(args[1]);
            myWriter.write(newQuery);
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
