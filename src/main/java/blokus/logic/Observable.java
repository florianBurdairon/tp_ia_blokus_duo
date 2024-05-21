package blokus.logic;

public interface Observable {
    void addListener(Observer observer);
    void removeListener(Observer observer);

    void updateObservers();
}
