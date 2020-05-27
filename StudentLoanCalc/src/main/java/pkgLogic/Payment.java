package pkgLogic;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

public class Payment {

	private int PaymentNbr;
	private LocalDate DueDate;
	private double Payment;
	private String PaymentFmt;

	private double AdditionalPayment;
	private String AdditionalPaymentFmt;

	private double EscrowPayment;
	private String EscrowPaymentFmt;

	private double InterestPayment;
	private String InterestPaymentFmt;

	private double Principle;
	private String PrincipleFmt;

	private double EndingBalance;
	private String EndingBalanceFmt;

	private NumberFormat fmtCurrency = NumberFormat.getCurrencyInstance();

	public Payment(double beginningBalance, int paymentNbr, LocalDate dueDate, Loan loan, boolean bLastPayment) {

		this.PaymentNbr = paymentNbr;
		this.EscrowPayment = loan.getEscrow();
		this.DueDate = dueDate;
		this.InterestPayment = beginningBalance * (loan.getInterestRate() / 12);

		if (bLastPayment) {
			this.Payment = beginningBalance;// + (beginningBalance * (loan.getInterestRate() / 12));
			this.Principle = beginningBalance - this.InterestPayment;
			this.AdditionalPayment = 0;
			this.EndingBalance = 0;

		} else {
			this.Payment = loan.GetPMT() + loan.getAdditionalPayment();
			this.Principle = loan.GetPMT() + loan.getAdditionalPayment() - this.InterestPayment;
			this.AdditionalPayment = loan.getAdditionalPayment();
			this.EndingBalance = beginningBalance - this.Principle;
		}
	}

	public int getPaymentNbr() {
		return PaymentNbr;
	}

	public void setPaymentNbr(int paymentNbr) {
		PaymentNbr = paymentNbr;
	}

	public LocalDate getDueDate() {
		return DueDate;
	}

	public void setDueDate(LocalDate dueDate) {
		DueDate = dueDate;
	}

	public double getPayment() {
		return Payment;
	}

	public String getPaymentFmt() {
		return fmtCurrency.format(getPayment());
	}

	public void setPayment(double payment) {
		Payment = payment;
	}

	public double getAdditionalPayment() {
		return AdditionalPayment;
	}

	public String getAdditionalPaymentFmt() {
		return fmtCurrency.format(getAdditionalPayment());
	}

	public void setAdditionalPayment(double additionalPayment) {
		AdditionalPayment = additionalPayment;
	}

	public double getInterestPayment() {
		return InterestPayment;
	}

	public String getInterestPaymentFmt() {
		return fmtCurrency.format(getInterestPayment());

	}

	public void setInterestPayment(double interestPayment) {
		InterestPayment = interestPayment;
	}

	public double getPrinciple() {
		return Principle;
	}

	public String getPrincipleFmt() {
		return fmtCurrency.format(getPrinciple());
	}

	public void setPrinciple(double principle) {
		Principle = principle;
	}

	public double getEndingBalance() {
		return EndingBalance;
	}

	public String getEndingBalanceFmt() {
		return fmtCurrency.format(getEndingBalance());
	}

	public void setEndingBalance(double endingBalace) {
		EndingBalance = endingBalace;
	}

	public double getEscrowPayment() {
		return EscrowPayment;
	}

	public String getEscrowPaymentFmt() {
		return fmtCurrency.format(getEscrowPayment());
	}

}