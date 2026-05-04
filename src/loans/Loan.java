package loans;

public abstract class Loan {
    protected int paymentLeft;

    public abstract int getLoanSize();

    public int getPaymentLeft() {
        return paymentLeft;
    }

    public abstract int getLoanRate();

    public void payOutLoan(int money) {
        paymentLeft -= money;
    }
}
