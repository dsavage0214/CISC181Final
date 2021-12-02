package app.controller;

import app.StudentCalc;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import pkgLogic.Loan;
import pkgLogic.Payment;

import java.net.URL;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;

public class LoanCalcViewController implements Initializable {

	private NumberFormat fmtCurrency = NumberFormat.getCurrencyInstance(Locale.US);
	
	private StudentCalc SC = null;

	@FXML
	private ComboBox cmbLoanType;

	@FXML
	private TextField LoanAmount;

	@FXML
	private TextField InterestRate;

	@FXML
	private TextField NbrOfYears;

	@FXML
	private DatePicker PaymentStartDate;

	@FXML
	private TextField AdditionalPayment;

	@FXML
	private TextField EscrowAmount;

	@FXML
	private Label lblEscrow;

	@FXML
	private Label lblTotalPayemnts;

	@FXML
	private Label lblTotalInterest;

	@FXML
	private Label lblInterestSaved;

	@FXML
	private Label lblMonthlyPayment;

	@FXML
	private Label lblPaymentsSaved;
	
	@FXML
	private Label lblTotalEscrow;

	@FXML
	private TableView<Payment> tvResults;

	@FXML
	private TableColumn<Payment, Integer> colPaymentNumber;

	@FXML
	private TableColumn<Payment, Double> colPaymentAmount;

	@FXML
	private TableColumn<Payment, LocalDate> colDueDate;

	@FXML
	private TableColumn<Payment, Double> colAdditionalPayment;

	@FXML
	private TableColumn<Payment, Double> colInterest;

	@FXML
	private TableColumn<Payment, Double> colPrinciple;
	@FXML
	private TableColumn<Payment, Double> colEscrow;

	@FXML
	private TableColumn<Payment, Double> colBalance;

	private ObservableList<Payment> paymentList = FXCollections.observableArrayList();

	@FXML
	private AnchorPane apAreaChart;

	@FXML
	private AreaChart<Number, Number> areaChartAmortization = null;

	@FXML
	private HBox hbChart;

	@FXML
	private AnchorPane barAreaChart;

	@FXML
	private AreaChart<Number, Number> stackedBarAmortization = null;

	@FXML
	private HBox stackedBarChart;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		
		LoanAmount.setText("269229");
		InterestRate.setText("2.99");
		NbrOfYears.setText("15");
		
		
		
		cmbLoanType.getItems().addAll("Home", "Auto", "School");

		cmbLoanType.getSelectionModel().selectFirst();

		cmbLoanType.valueProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue ov, String t, String t1) {
				toggleEscrow(t1);
			}
		});

		colPaymentNumber.setCellValueFactory(new PropertyValueFactory<>("PaymentNbr"));

		colPaymentAmount.setCellValueFactory(new PropertyValueFactory<>("PaymentFmt"));
		colPaymentAmount.setStyle("-fx-alignment: CENTER-RIGHT;");

		colDueDate.setCellValueFactory(new PropertyValueFactory<>("DueDate"));

		colAdditionalPayment.setCellValueFactory(new PropertyValueFactory<>("AdditionalPaymentFmt"));
		colAdditionalPayment.setStyle("-fx-alignment: CENTER-RIGHT;");

		colInterest.setCellValueFactory(new PropertyValueFactory<>("InterestPaymentFmt"));
		colInterest.setStyle("-fx-alignment: CENTER-RIGHT;");

		colPrinciple.setCellValueFactory(new PropertyValueFactory<>("PrincipleFmt"));
		colPrinciple.setStyle("-fx-alignment: CENTER-RIGHT;");

		colEscrow.setCellValueFactory(new PropertyValueFactory<>("EscrowPaymentFmt"));
		colEscrow.setStyle("-fx-alignment: CENTER-RIGHT;");

		colBalance.setCellValueFactory(new PropertyValueFactory<>("EndingBalanceFmt"));
		colBalance.setStyle("-fx-alignment: CENTER-RIGHT;");

		tvResults.setItems(paymentList);

		// PaintChart();
	}

	public void setMainApp(StudentCalc sc) {
		this.SC = sc;
	}

	@FXML
	private void btnClearFields(ActionEvent event) {

		btnClearResults(event);
		LoanAmount.clear();
		InterestRate.clear();
		NbrOfYears.clear();
		EscrowAmount.clear();
		PaymentStartDate.getEditor().clear();
		PaymentStartDate.setValue(null);
		AdditionalPayment.clear();
	}

	private void toggleEscrow(String strLoanType) {
		EscrowAmount.setVisible((strLoanType == "Home"));
		lblEscrow.setVisible((strLoanType == "Home"));
	}

	@FXML
	private void btnClearResultsKeyPress(KeyEvent event) {
		this.btnClearResults(null);

	}

	/**
	 * btnClearFields - Clear the input fields and output chart
	 * 
	 * @param event
	 */

	@FXML
	private void btnClearResults(ActionEvent event) {

		paymentList.clear();
		hbChart.getChildren().clear();
		stackedBarChart.getChildren().clear();

		lblInterestSaved.setText("");
		lblMonthlyPayment.setText("");
		lblPaymentsSaved.setText("");
		lblTotalInterest.setText("");
		lblTotalPayemnts.setText("");
		lblTotalEscrow.setText("");

	}

	private boolean ValidateData() {
		StringBuilder contentText = new StringBuilder();
		// create new boolean to check if an error needs to be shown
		boolean goodtogo = true;
		// Validate LoanAmount isn't empty
		if (LoanAmount.getText().trim().isEmpty() || !(Double.parseDouble(LoanAmount.getText().trim()) > 0)) {
			contentText.append("Loan Amount must be a positive double. \n");
			goodtogo = false;
		}
		if (InterestRate.getText().trim().isEmpty() || Double.parseDouble(InterestRate.getText().trim()) < 1
				|| Double.parseDouble(InterestRate.getText().trim()) > 30) {
			contentText.append("Interest Rate must be a positive value between 1 and 30 (inclusive). \n");
			goodtogo = false;
		}
		if (NbrOfYears.getText().trim().isEmpty() || !(Integer.parseInt(NbrOfYears.getText().trim()) > 0)) {
			contentText.append("Number of Years must be a positive integer. \n");
			goodtogo = false;
		}
		if (!EscrowAmount.getText().trim().isEmpty() && !(Double.parseDouble(EscrowAmount.getText().trim()) >= 0)) {
			contentText.append("Escrow Amount must be a positive double. \n");
			goodtogo = false;
		}
		if (!AdditionalPayment.getText().trim().isEmpty()
				&& !(Double.parseDouble(AdditionalPayment.getText().trim()) >= 0)) {
			contentText.append("Additional Payment must be a positive double. \n");
			goodtogo = false;
		}
		if (!goodtogo) {
			Alert fail = new Alert(AlertType.ERROR);
			fail.setHeaderText("Error!");
			fail.setContentText(contentText.toString());
			fail.showAndWait();
			return false;
		}
		return true;
	}

	/**
	 * btnCalcLoan - Fire this event when the button clicks
	 * 
	 * @version 1.0
	 * @param event
	 */
	@FXML
	private void btnCalcLoan(ActionEvent event) {

		this.btnClearResults(event);
		// Validate the data. If the method returns 'false', exit the method
		if (ValidateData() == false)
			return;

		// Examples- how to read data from the form
		double dLoanAmount = Double.parseDouble(LoanAmount.getText());
		double dInterestRate = Double.parseDouble(InterestRate.getText()) / 100;
		int dNbrOfYears = Integer.parseInt(NbrOfYears.getText());
		double dAdditionalPayment = (this.AdditionalPayment.getText().isEmpty() ? 0
				: Double.parseDouble(AdditionalPayment.getText()));
		double dEscrow = (this.EscrowAmount.getText().isEmpty() ? 0 : Double.parseDouble(this.EscrowAmount.getText()));
		LocalDate localDate = PaymentStartDate.getValue();

		Loan loanExtra = new Loan(dLoanAmount, dInterestRate, dNbrOfYears, localDate, dAdditionalPayment, dEscrow);
		Loan loanNoExtra = new Loan(dLoanAmount, dInterestRate, dNbrOfYears, localDate, 0, dEscrow);

		for (Payment p : loanExtra.getLoanPayments()) {
			paymentList.add(p);
		}

		
		lblTotalPayemnts.setText(fmtCurrency.format(loanExtra.getTotalPayments()));

		lblTotalInterest.setText(fmtCurrency.format(loanExtra.getTotalInterest()));

		lblInterestSaved.setText(fmtCurrency.format(loanNoExtra.getTotalInterest() - loanExtra.getTotalInterest()));
		lblPaymentsSaved
				.setText(String.valueOf(loanNoExtra.getLoanPayments().size() - loanExtra.getLoanPayments().size()));

		lblMonthlyPayment.setText(fmtCurrency.format(
				loanExtra.GetPMT() + 
				//loanExtra.getLoanPayments().get(0).getPayment()
				+ loanExtra.getAdditionalPayment() + loanExtra.getEscrow()));		
		lblTotalEscrow.setText(fmtCurrency.format(loanExtra.getTotalEscrow()));
		
		XYChart.Series seriesExtra = new XYChart.Series();
		XYChart.Series seriesNoExtra = new XYChart.Series();

		int MaxLoanPayments;
		if (loanExtra.getLoanPayments().size() >= loanNoExtra.getLoanPayments().size()) {
			MaxLoanPayments = loanExtra.getLoanPayments().size();
		} else {
			MaxLoanPayments = loanNoExtra.getLoanPayments().size();
		}

		double MaxLoanAmount;
		if (loanExtra.getLoanAmount() >= loanNoExtra.getLoanAmount()) {
			MaxLoanAmount = loanExtra.getLoanAmount();
		} else {
			MaxLoanAmount = loanNoExtra.getLoanAmount();
		}

		CreateAreaChartAmortization(MaxLoanPayments, MaxLoanAmount);

		for (Payment p : loanExtra.getLoanPayments()) {
			PlotData(seriesExtra, p.getPaymentNbr(), p.getEndingBalance());
		}

		for (Payment p : loanNoExtra.getLoanPayments()) {
			PlotData(seriesNoExtra, p.getPaymentNbr(), p.getEndingBalance());
		}

		areaChartAmortization.getData().addAll(seriesExtra, seriesNoExtra);

		
		
		
		for (final Series<Number, Number> series : areaChartAmortization.getData()) {
			for (final Data<Number, Number> data : series.getData()) {
				Tooltip tooltip = new Tooltip();				
				
				tooltip.setText(fmtCurrency.format(data.getYValue()));
				Tooltip.install(data.getNode(), tooltip);
			}
		}
		
		hbChart.getChildren().add(areaChartAmortization);

		createStackedBar();
	}

	private void createStackedBar() {
		CategoryAxis xAxis = new CategoryAxis();
		ArrayList<String> dates = new ArrayList<String>();
		// Prepare XYChart.Series objects
		XYChart.Series<String, Number> principalPayments = new XYChart.Series<>();
		principalPayments.setName("Principle");
		XYChart.Series<String, Number> interestPayments = new XYChart.Series<>();
		interestPayments.setName("Interest");
		
		XYChart.Series<String, Number> escrowPayments = new XYChart.Series<>();
		escrowPayments.setName("Escrow");
		
		for (Payment P : paymentList) {
			if (!dates.contains(Integer.toString(P.getDueDate().getYear()))) {
				dates.add(Integer.toString(P.getDueDate().getYear()));
			}
			principalPayments.getData().add(new XYChart.Data<String, Number>(
					Integer.toString(P.getDueDate().getYear()), 
					P.getPrinciple()+P.getAdditionalPayment()));
			interestPayments.getData().add(new XYChart.Data<String, Number>(
					Integer.toString(P.getDueDate().getYear()), 
					P.getInterestPayment()));
			
			escrowPayments.getData().add(new XYChart.Data<String, Number>(
					Integer.toString(P.getDueDate().getYear()), 
					P.getEscrowPayment()));
		}
		
		xAxis.setCategories(FXCollections
				.<String>observableArrayList(dates));
		xAxis.setLabel("Year");
		NumberAxis yAxis = new NumberAxis();
		yAxis.setLabel("Amount (USD)");
		// Creating the Bar chart
		StackedBarChart<String, Number> mystackedBarChart = new StackedBarChart<>(xAxis, yAxis);
		mystackedBarChart.setTitle("Monthly Payments");
		// Setting the data to bar chart
		mystackedBarChart.getData().addAll(principalPayments, interestPayments, escrowPayments);
		stackedBarChart.getChildren().add(mystackedBarChart);
		
	}

	/**
	 * CreateAreaChartAmortization - Create an Area Chart
	 * 
	 * @version 1.0
	 * @param MaxPayments
	 * @param MaxAmount
	 */
	private void CreateAreaChartAmortization(double MaxPayments, double MaxAmount) {

		double tickAmount = (MaxAmount > 100000 ? 10000 : 1000);

		final NumberAxis xAxis = new NumberAxis(1, MaxPayments, MaxPayments / 12);
		final NumberAxis yAxis = new NumberAxis(0, MaxAmount, tickAmount);

		xAxis.setLabel("Remaining Balance");
		xAxis.setLabel("Months");

		areaChartAmortization = new AreaChart<Number, Number>(xAxis, yAxis);
		areaChartAmortization.setTitle("Amortization Payment Chart");
		areaChartAmortization.setLegendVisible(false);
	}

	/**
	 * PlotData - Plot a data point for a given series
	 * 
	 * @version 1.0
	 * @param series
	 * @param PaymentNbr
	 * @param Balance
	 */
	private void PlotData(XYChart.Series series, int PaymentNbr, double Balance) {
		series.getData().add(new XYChart.Data(PaymentNbr, Balance));
	}


}
