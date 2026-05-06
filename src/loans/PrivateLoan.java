package loans;

public class PrivateLoan extends Loan {
    public int getLoanSize() {
        return 10000;
    }

    public int getLoanRate() {
        return 10;
    }

    public int getPennyRate() {
        return 5;
    }
}
