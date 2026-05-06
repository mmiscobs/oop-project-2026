package loans;

import java.util.Map;

import utils.Reactive;
import utils.SerializedBlob;
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

    public static Loan fromBlob(SerializedBlob blob) {
        return switch (blob.map().get("type").string()) {
            case "FederalLoan" -> new FederalLoan();
            case "PrivateLoan" -> new PrivateLoan();
            default -> null;
        };
    }

    public SerializedBlob toBlob() {
        return SerializedBlob.fromMap(Map.of("type", SerializedBlob.string(getClass().getSimpleName())));
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
