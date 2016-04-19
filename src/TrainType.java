public enum TrainType {
    A(40),   // priority
    P(0),    // passenger
    F(-20);  // freight

    private final int timeCost;

    TrainType(int cost) {
        this.timeCost = cost;
    }

    public int timeCost() {
        return this.timeCost;
    }

}
