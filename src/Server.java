import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        System.out.println("Server är nu Redo");

        //Init stuff
        ServerSocket serverSocket;
        Socket socket;
        InputStreamReader inputSR;
        OutputStreamWriter outputSW;
        BufferedReader bReader;
        BufferedWriter bWriter;

        //Starta Servern

        try {
            //Kontrollera att Socket nummer är ledig. Avbryt om socket är upptagen
            serverSocket = new ServerSocket(4321);
            System.out.println(serverSocket.getInetAddress());
            System.out.println(serverSocket.getLocalSocketAddress());
        } catch (IOException e) {
            System.out.println(e);
            return;
        }

        try {
            //Väntar på specifik socket efter trafik
            socket = serverSocket.accept();

            //Initiera Reader och Writer och koppla dem till socket
            inputSR = new InputStreamReader(socket.getInputStream());
            outputSW = new OutputStreamWriter(socket.getOutputStream());

            bReader = new BufferedReader(inputSR);
            bWriter = new BufferedWriter(outputSW);

            while (true) {
                //Hämta klientens meddelande och skicka den till openUpData()
                //Returnerar ett färdigt JSON objekt som skall tillbaka till klienten
                String message = bReader.readLine();
                String returnData = openUpData(message);

                System.out.println("Message Recieved and sent back");

                //Skicka acknoledgement eller svar tillbaka
                bWriter.write(returnData);
                bWriter.newLine();
                bWriter.flush();

                //Avsluta om QUIT
                if (message.equalsIgnoreCase("quit")) break;
            }
            //Stäng kopplingar
            socket.close();
            inputSR.close();
            outputSW.close();
            bReader.close();
            bWriter.close();

        } catch (IOException e) {
            System.out.println(e);
        } catch (ParseException e) {
            System.out.println(e);
        } finally {
            System.out.println("Server Avslutas");
        }
    }
    static void fetchJsonFromFile() throws IOException, ParseException {


        //Hämta data från JSON fil
        JSONObject fetchData = (JSONObject) new JSONParser().parse(new FileReader("data/data.json"));

        //Konvertera data till ett JSONObject
        JSONObject p1 = (JSONObject) fetchData.get("student1");
        JSONObject p2 = (JSONObject) fetchData.get("student2");

        //Hämta och skriv ut data
        String nameP1=p1.get("name").toString(), nameP2=p2.get("name").toString();
        int ageP1= Integer.parseInt(p1.get("age").toString()) , ageP2=Integer.parseInt(p2.get("age").toString());

        System.out.println("Mitt namn är " + nameP1 + " och jag är " + ageP1 + " år gammal.");
        System.out.println("Mitt namn är " + nameP2 + " och jag är " + ageP2 + " år gammal.");
    }
    static String openUpData(String message) throws ParseException, IOException {
        System.out.println(message);
        //Steg 1. Bygg upp JSON Obejct basserat på inkommande string
        JSONParser parser = new JSONParser();
        JSONObject jsonOb = (JSONObject) parser.parse(message);

        //Steg 2. Läs av URL och HTTP-metod för att veta vad klienten vill
        String url = jsonOb.get("httpURL").toString();
        String method = jsonOb.get("httpMethod").toString();

        //Steg 2.5. Dela upp URL med .split() metod
        String[] urls = url.split("/");

        //Steg 3. Använd en SwitchCase för att kolla vilken data som skall användas
        if (urls[0].equals("persons")) {
            if (method.equals("get")) {
                //VIll hämta data om personer
                //TODO lägg till logik om det är specfik person som skall hämtas

                //Skapa JSONReturn objektet
                JSONObject jsonReturn = new JSONObject();

                //Hämta data från JSON fil
                jsonReturn.put("data", parser.parse(new FileReader("data/data.json")).toString());

                //Inkluderat HTTP status code
                jsonReturn.put("httpStatusCode", 200);

                //Return
                return jsonReturn.toJSONString();
            }
        }
        return "message Recieved";
    }
}