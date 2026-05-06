package loans;

import utils.Reactive;
import utils.Reactive.Observable;

public abstract class Loan {
    protected Reactive<Integer> paymentLeft = new Reactive<>(0);
    public Observable<Integer> paymentLeftView = paymentLeft.readOnly();

    public abstract int getLoanSize();

    public int getPaymentLeft() {
        return paymentLeft.get();
    }

    Loan() {
        paymentLeft.set(getLoanSize());
    }

    public abstract int getLoanRate();

    public abstract int getPennyRate();

    public void payOutLoan(int money) {
        paymentLeft.set(paymentLeft.get() - money);
    }

    public boolean paidOutLoan() {
        return paymentLeft.get() <= 0;
    }

    public int getPerTickInterest() {
        final int TICKS_IN_INTEREST_PERIOD = 50;
        return (int) (getPaymentLeft() * ((double) getLoanRate() / 100) / TICKS_IN_INTEREST_PERIOD);
    }

    public void payPerTickInterest(int money) {
        if (money > getPerTickInterest()) {
            payOutLoan(money);
        } else {
            payOutLoan(money);
            paymentLeft
                    .set(paymentLeft.get() + (int) ((getPerTickInterest() - money) * ((double) getPennyRate() / 100)));
        }
    }
}
