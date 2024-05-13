package blokus.logic;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PieceTypeAdapter extends TypeAdapter<Piece> {

    @Override
    public void write(JsonWriter out, Piece piece) throws IOException {
        out.beginObject();
        out.name("case_number").value(piece.getCaseNumber());
        out.name("cases");
        Type listOfPositionObject = new TypeToken<ArrayList<Position>>() {}.getType();
        new Gson().toJson(piece.getCases(), listOfPositionObject, out);
        out.endObject();
    }

    @Override
    public Piece read(JsonReader in) throws IOException {
        in.beginObject();
        int caseNumber = 0;
        List<Position> cases = null;
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "case_number":
                    caseNumber = in.nextInt();
                    break;
                case "cases":
                    Type listOfPositionObject = new TypeToken<ArrayList<Position>>() {}.getType();
                    cases = new Gson().fromJson(in, listOfPositionObject);
                    break;
            }
        }
        in.endObject();
        return new Piece(caseNumber, cases);
    }
}