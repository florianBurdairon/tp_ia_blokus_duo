package blokus.logic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Piece implements Serializable {
    private final int caseNumber;
    private final List<Position> cases;

    public Piece(int caseNumber, List<Position> cases) {
        this.caseNumber = caseNumber;
        this.cases = cases;
    }

    public int getCaseNumber() {
        return caseNumber;
    }

    public List<Position> getCases() {
        return cases;
    }

    //Deserialization of piece-set.json file to get all the pieces
    public static List<Piece> getPieces() {
        InputStream inputStream = Piece.class.getClassLoader().getResourceAsStream("piece-set.json");
        StringBuilder resultStringBuilder = new StringBuilder();
        try {
            assert inputStream != null;
            try (BufferedReader br
              = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = br.readLine()) != null) {
                    resultStringBuilder.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(Piece.class, new PieceTypeAdapter())
            .create();
        return gson.fromJson(resultStringBuilder.toString(), new TypeToken<ArrayList<Piece>>() {}.getType());
    }
}
