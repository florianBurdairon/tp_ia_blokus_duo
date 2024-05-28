package blokus.logic;

import blokus.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Piece implements Serializable {
    private final int id;
    private final int caseNumber;
    private final ArrayList<Position> cases;
    private final ArrayList<Position> corners;
    private final ArrayList<Position> sides;
    private final Map<List<Position>, Transform> transformations = new HashMap<>();

    public Piece(int id, int caseNumber, List<Position> cases, List<Position> corners, List<Position> sides) {
        this.id = id;
        this.caseNumber = caseNumber;
        this.cases = new ArrayList<>(cases);
        this.corners = new ArrayList<>(corners);
        this.sides = new ArrayList<>(sides);

        for(Grid.Angle angle : Grid.Angle.values()) {
            for (int i = 0; i < 2; i++) {
                List<Position> transformation = Utils.transform(cases, angle, i==1);
                boolean contains = false;
                for (List<Position> transf : transformations.keySet())
                {
                    if (Utils.areSimilar(transf, transformation)) {
                        contains = true;
                        break;
                    }
                }
                if (!contains) {
                    transformations.put(transformation, new Transform(angle, i==1));
                }
            }
        }
    }

    public int getId() {
        return id;
    }

    public int getCaseNumber() {
        return caseNumber;
    }

    public List<Position> getCases() {
        List<Position> clonedCases = new ArrayList<>();
        for (Position c : cases) {
            clonedCases.add(c.clone());
        }
        return clonedCases;
    }

    public List<Position> getCorners() {
        List<Position> clonedCases = new ArrayList<>();
        for (Position c : corners) {
            clonedCases.add(c.clone());
        }
        return clonedCases;
    }

    public List<Position> getSides() {
        List<Position> clonedCases = new ArrayList<>();
        for (Position c : sides) {
            clonedCases.add(c.clone());
        }
        return clonedCases;
    }

    public Map<List<Position>, Transform> getTransformations() {
        return transformations;
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

    @Override
    public String toString() {
        return "Piece{" +
                "cases=" + cases +
                '}';
    }
}
