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

	@Override
	public void initialize(URL location, ResourceBundle resources) {

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

		
		lblEscrow.setText("");
		lblInterestSaved.setText("");
		lblMonthlyPayment.setText("");
		lblPaymentsSaved.setText("");
		lblTotalInterest.setText("");
		lblTotalPayemnts.setText("");
		
	}

	private boolean ValidateData() {
		StringBuilder contentText = new StringBuilder();
		//create new boolean to check if an error needs to be shown
		boolean goodtogo = true;
		// Validate LoanAmount isn't empty
		if (LoanAmount.getText().trim().isEmpty() || 
				!(Double.parseDouble(LoanAmount.getText().trim())>0)) {
			contentText.append("Loan Amount must be a positive double. \n");
			goodtogo = false;
		}
		if (InterestRate.getText().trim().isEmpty() || 
				Double.parseDouble(InterestRate.getText().trim())<1 || 
				Double.parseDouble(InterestRate.getText().trim())>30) {
			contentText.append("Interest Rate must be a positive value between 1 and 30 (inclusive). \n");
			goodtogo = false;
		}
		if (NbrOfYears.getText().trim().isEmpty() || !(Integer.parseInt(NbrOfYears.getText().trim())>0)) {
			contentText.append("Number of Years must be a positive integer. \n");
			goodtogo = false;
		}
		if (!EscrowAmount.getText().trim().isEmpty() && !(Double.parseDouble(EscrowAmount.getText().trim())>0)) {
			contentText.append("Escrow Amount must be a positive double. \n");
			goodtogo = false;
		}
		if (!AdditionalPayment.getText().trim().isEmpty() && !(Double.parseDouble(AdditionalPayment.getText().trim())>0)) {
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

		NumberFormat fmtCurrency = NumberFormat.getCurrencyInstance(Locale.US);
		lblTotalPayemnts.setText(fmtCurrency.format(loanExtra.getTotalPayments()));
		
		lblTotalInterest.setText(fmtCurrency.format(loanExtra.getTotalInterest()));
		
		lblTotalPayemnts.setText(fmtCurrency.format(loanExtra.GetPMT()));
		
		lblInterestSaved.setText(fmtCurrency.format(loanNoExtra.getTotalInterest() -  loanExtra.getTotalInterest()));
		lblPaymentsSaved
				.setText(String.valueOf(loanNoExtra.getLoanPayments().size() - loanExtra.getLoanPayments().size()));

		lblMonthlyPayment.setText(fmtCurrency.format(loanExtra.getLoanPayments().get(0).getPayment()));
		
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
				tooltip.setText(data.getYValue().toString());
				Tooltip.install(data.getNode(), tooltip);
			}
		}
		hbChart.getChildren().add(areaChartAmortization);
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

	private void PaintChart() {
		final NumberAxis xAxis = new NumberAxis(1, 360, 10);
		final NumberAxis yAxis = new NumberAxis(0, 300000, 20000);

		xAxis.setLabel("Remaining Balance");
		xAxis.setLabel("Months");

		final AreaChart<Number, Number> ac = new AreaChart<Number, Number>(xAxis, yAxis);

		ac.setTitle("Amortization Schedule");

		XYChart.Series seriesApril = new XYChart.Series();
		seriesApril.setName("April");
		seriesApril.getData().add(new XYChart.Data(1, 268645.8));
		seriesApril.getData().add(new XYChart.Data(2, 268290.2));
		seriesApril.getData().add(new XYChart.Data(3, 267933.3));
		seriesApril.getData().add(new XYChart.Data(4, 267575.1));
		seriesApril.getData().add(new XYChart.Data(5, 267215.5));
		seriesApril.getData().add(new XYChart.Data(6, 266854.6));
		seriesApril.getData().add(new XYChart.Data(7, 266492.3));
		seriesApril.getData().add(new XYChart.Data(8, 266128.7));
		seriesApril.getData().add(new XYChart.Data(9, 265763.7));
		seriesApril.getData().add(new XYChart.Data(10, 265397.3));
		seriesApril.getData().add(new XYChart.Data(11, 265029.5));
		seriesApril.getData().add(new XYChart.Data(12, 264660.4));
		seriesApril.getData().add(new XYChart.Data(13, 264289.9));
		seriesApril.getData().add(new XYChart.Data(14, 263918));
		seriesApril.getData().add(new XYChart.Data(15, 263544.7));
		seriesApril.getData().add(new XYChart.Data(16, 263170));
		seriesApril.getData().add(new XYChart.Data(17, 262793.9));
		seriesApril.getData().add(new XYChart.Data(18, 262416.4));
		seriesApril.getData().add(new XYChart.Data(19, 262037.5));
		seriesApril.getData().add(new XYChart.Data(20, 261657.2));
		seriesApril.getData().add(new XYChart.Data(21, 261275.4));
		seriesApril.getData().add(new XYChart.Data(22, 260892.2));
		seriesApril.getData().add(new XYChart.Data(23, 260507.6));
		seriesApril.getData().add(new XYChart.Data(24, 260121.5));
		seriesApril.getData().add(new XYChart.Data(25, 259734));
		seriesApril.getData().add(new XYChart.Data(26, 259345));
		seriesApril.getData().add(new XYChart.Data(27, 258954.5));
		seriesApril.getData().add(new XYChart.Data(28, 258562.6));
		seriesApril.getData().add(new XYChart.Data(29, 258169.3));
		seriesApril.getData().add(new XYChart.Data(30, 257774.4));
		seriesApril.getData().add(new XYChart.Data(31, 257378.1));
		seriesApril.getData().add(new XYChart.Data(32, 256980.3));
		seriesApril.getData().add(new XYChart.Data(33, 256581));
		seriesApril.getData().add(new XYChart.Data(34, 256180.1));
		seriesApril.getData().add(new XYChart.Data(35, 255777.8));
		seriesApril.getData().add(new XYChart.Data(36, 255374));
		seriesApril.getData().add(new XYChart.Data(37, 254968.7));
		seriesApril.getData().add(new XYChart.Data(38, 254561.8));
		seriesApril.getData().add(new XYChart.Data(39, 254153.5));
		seriesApril.getData().add(new XYChart.Data(40, 253743.6));
		seriesApril.getData().add(new XYChart.Data(41, 253332.1));
		seriesApril.getData().add(new XYChart.Data(42, 252919.1));
		seriesApril.getData().add(new XYChart.Data(43, 252504.6));
		seriesApril.getData().add(new XYChart.Data(44, 252088.5));
		seriesApril.getData().add(new XYChart.Data(45, 251670.8));
		seriesApril.getData().add(new XYChart.Data(46, 251251.6));
		seriesApril.getData().add(new XYChart.Data(47, 250830.8));
		seriesApril.getData().add(new XYChart.Data(48, 250408.5));
		seriesApril.getData().add(new XYChart.Data(49, 249984.5));
		seriesApril.getData().add(new XYChart.Data(50, 249559));
		seriesApril.getData().add(new XYChart.Data(51, 249131.8));
		seriesApril.getData().add(new XYChart.Data(52, 248703.1));
		seriesApril.getData().add(new XYChart.Data(53, 248272.8));
		seriesApril.getData().add(new XYChart.Data(54, 247840.8));
		seriesApril.getData().add(new XYChart.Data(55, 247407.2));
		seriesApril.getData().add(new XYChart.Data(56, 246972));
		seriesApril.getData().add(new XYChart.Data(57, 246535.2));
		seriesApril.getData().add(new XYChart.Data(58, 246096.7));
		seriesApril.getData().add(new XYChart.Data(59, 245656.6));
		seriesApril.getData().add(new XYChart.Data(60, 245214.8));
		seriesApril.getData().add(new XYChart.Data(61, 244771.4));
		seriesApril.getData().add(new XYChart.Data(62, 244326.3));
		seriesApril.getData().add(new XYChart.Data(63, 243879.5));
		seriesApril.getData().add(new XYChart.Data(64, 243431.1));
		seriesApril.getData().add(new XYChart.Data(65, 242981));
		seriesApril.getData().add(new XYChart.Data(66, 242529.2));
		seriesApril.getData().add(new XYChart.Data(67, 242075.7));
		seriesApril.getData().add(new XYChart.Data(68, 241620.5));
		seriesApril.getData().add(new XYChart.Data(69, 241163.6));
		seriesApril.getData().add(new XYChart.Data(70, 240704.9));
		seriesApril.getData().add(new XYChart.Data(71, 240244.6));
		seriesApril.getData().add(new XYChart.Data(72, 239782.5));
		seriesApril.getData().add(new XYChart.Data(73, 239318.7));
		seriesApril.getData().add(new XYChart.Data(74, 238853.2));
		seriesApril.getData().add(new XYChart.Data(75, 238385.9));
		seriesApril.getData().add(new XYChart.Data(76, 237916.9));
		seriesApril.getData().add(new XYChart.Data(77, 237446.1));
		seriesApril.getData().add(new XYChart.Data(78, 236973.5));
		seriesApril.getData().add(new XYChart.Data(79, 236499.2));
		seriesApril.getData().add(new XYChart.Data(80, 236023.1));
		seriesApril.getData().add(new XYChart.Data(81, 235545.2));
		seriesApril.getData().add(new XYChart.Data(82, 235065.5));
		seriesApril.getData().add(new XYChart.Data(83, 234584));
		seriesApril.getData().add(new XYChart.Data(84, 234100.7));
		seriesApril.getData().add(new XYChart.Data(85, 233615.6));
		seriesApril.getData().add(new XYChart.Data(86, 233128.7));
		seriesApril.getData().add(new XYChart.Data(87, 232639.9));
		seriesApril.getData().add(new XYChart.Data(88, 232149.3));
		seriesApril.getData().add(new XYChart.Data(89, 231656.9));
		seriesApril.getData().add(new XYChart.Data(90, 231162.6));
		seriesApril.getData().add(new XYChart.Data(91, 230666.5));
		seriesApril.getData().add(new XYChart.Data(92, 230168.5));
		seriesApril.getData().add(new XYChart.Data(93, 229668.7));
		seriesApril.getData().add(new XYChart.Data(94, 229167));
		seriesApril.getData().add(new XYChart.Data(95, 228663.4));
		seriesApril.getData().add(new XYChart.Data(96, 228157.9));
		seriesApril.getData().add(new XYChart.Data(97, 227650.5));
		seriesApril.getData().add(new XYChart.Data(98, 227141.2));
		seriesApril.getData().add(new XYChart.Data(99, 226630));
		seriesApril.getData().add(new XYChart.Data(100, 226116.8));
		seriesApril.getData().add(new XYChart.Data(101, 225601.8));
		seriesApril.getData().add(new XYChart.Data(102, 225084.8));
		seriesApril.getData().add(new XYChart.Data(103, 224565.9));
		seriesApril.getData().add(new XYChart.Data(104, 224045));
		seriesApril.getData().add(new XYChart.Data(105, 223522.2));
		seriesApril.getData().add(new XYChart.Data(106, 222997.5));
		seriesApril.getData().add(new XYChart.Data(107, 222470.7));
		seriesApril.getData().add(new XYChart.Data(108, 221942));
		seriesApril.getData().add(new XYChart.Data(109, 221411.3));
		seriesApril.getData().add(new XYChart.Data(110, 220878.6));
		seriesApril.getData().add(new XYChart.Data(111, 220343.9));
		seriesApril.getData().add(new XYChart.Data(112, 219807.2));
		seriesApril.getData().add(new XYChart.Data(113, 219268.5));
		seriesApril.getData().add(new XYChart.Data(114, 218727.8));
		seriesApril.getData().add(new XYChart.Data(115, 218185));
		seriesApril.getData().add(new XYChart.Data(116, 217640.2));
		seriesApril.getData().add(new XYChart.Data(117, 217093.4));
		seriesApril.getData().add(new XYChart.Data(118, 216544.5));
		seriesApril.getData().add(new XYChart.Data(119, 215993.6));
		seriesApril.getData().add(new XYChart.Data(120, 215440.6));
		seriesApril.getData().add(new XYChart.Data(121, 214885.5));
		seriesApril.getData().add(new XYChart.Data(122, 214328.3));
		seriesApril.getData().add(new XYChart.Data(123, 213769.1));
		seriesApril.getData().add(new XYChart.Data(124, 213207.7));
		seriesApril.getData().add(new XYChart.Data(125, 212644.3));
		seriesApril.getData().add(new XYChart.Data(126, 212078.7));
		seriesApril.getData().add(new XYChart.Data(127, 211511));
		seriesApril.getData().add(new XYChart.Data(128, 210941.2));
		seriesApril.getData().add(new XYChart.Data(129, 210369.3));
		seriesApril.getData().add(new XYChart.Data(130, 209795.2));
		seriesApril.getData().add(new XYChart.Data(131, 209218.9));
		seriesApril.getData().add(new XYChart.Data(132, 208640.5));
		seriesApril.getData().add(new XYChart.Data(133, 208059.9));
		seriesApril.getData().add(new XYChart.Data(134, 207477.1));
		seriesApril.getData().add(new XYChart.Data(135, 206892.2));
		seriesApril.getData().add(new XYChart.Data(136, 206305.1));
		seriesApril.getData().add(new XYChart.Data(137, 205715.7));
		seriesApril.getData().add(new XYChart.Data(138, 205124.2));
		seriesApril.getData().add(new XYChart.Data(139, 204530.4));
		seriesApril.getData().add(new XYChart.Data(140, 203934.4));
		seriesApril.getData().add(new XYChart.Data(141, 203336.2));
		seriesApril.getData().add(new XYChart.Data(142, 202735.7));
		seriesApril.getData().add(new XYChart.Data(143, 202133));
		seriesApril.getData().add(new XYChart.Data(144, 201528));
		seriesApril.getData().add(new XYChart.Data(145, 200920.8));
		seriesApril.getData().add(new XYChart.Data(146, 200311.2));
		seriesApril.getData().add(new XYChart.Data(147, 199699.4));
		seriesApril.getData().add(new XYChart.Data(148, 199085.3));
		seriesApril.getData().add(new XYChart.Data(149, 198468.9));
		seriesApril.getData().add(new XYChart.Data(150, 197850.2));
		seriesApril.getData().add(new XYChart.Data(151, 197229.1));
		seriesApril.getData().add(new XYChart.Data(152, 196605.7));
		seriesApril.getData().add(new XYChart.Data(153, 195980));
		seriesApril.getData().add(new XYChart.Data(154, 195352));
		seriesApril.getData().add(new XYChart.Data(155, 194721.6));
		seriesApril.getData().add(new XYChart.Data(156, 194088.8));
		seriesApril.getData().add(new XYChart.Data(157, 193453.6));
		seriesApril.getData().add(new XYChart.Data(158, 192816.1));
		seriesApril.getData().add(new XYChart.Data(159, 192176.2));
		seriesApril.getData().add(new XYChart.Data(160, 191533.8));
		seriesApril.getData().add(new XYChart.Data(161, 190889.1));
		seriesApril.getData().add(new XYChart.Data(162, 190242));
		seriesApril.getData().add(new XYChart.Data(163, 189592.4));
		seriesApril.getData().add(new XYChart.Data(164, 188940.4));
		seriesApril.getData().add(new XYChart.Data(165, 188285.9));
		seriesApril.getData().add(new XYChart.Data(166, 187629));
		seriesApril.getData().add(new XYChart.Data(167, 186969.6));
		seriesApril.getData().add(new XYChart.Data(168, 186307.8));
		seriesApril.getData().add(new XYChart.Data(169, 185643.5));
		seriesApril.getData().add(new XYChart.Data(170, 184976.6));
		seriesApril.getData().add(new XYChart.Data(171, 184307.3));
		seriesApril.getData().add(new XYChart.Data(172, 183635.5));
		seriesApril.getData().add(new XYChart.Data(173, 182961.1));
		seriesApril.getData().add(new XYChart.Data(174, 182284.3));
		seriesApril.getData().add(new XYChart.Data(175, 181604.8));
		seriesApril.getData().add(new XYChart.Data(176, 180922.9));
		seriesApril.getData().add(new XYChart.Data(177, 180238.3));
		seriesApril.getData().add(new XYChart.Data(178, 179551.3));
		seriesApril.getData().add(new XYChart.Data(179, 178861.6));
		seriesApril.getData().add(new XYChart.Data(180, 178169.3));
		seriesApril.getData().add(new XYChart.Data(181, 177474.5));
		seriesApril.getData().add(new XYChart.Data(182, 176777));
		seriesApril.getData().add(new XYChart.Data(183, 176077));
		seriesApril.getData().add(new XYChart.Data(184, 175374.3));
		seriesApril.getData().add(new XYChart.Data(185, 174668.9));
		seriesApril.getData().add(new XYChart.Data(186, 173961));
		seriesApril.getData().add(new XYChart.Data(187, 173250.3));
		seriesApril.getData().add(new XYChart.Data(188, 172537));
		seriesApril.getData().add(new XYChart.Data(189, 171821.1));
		seriesApril.getData().add(new XYChart.Data(190, 171102.4));
		seriesApril.getData().add(new XYChart.Data(191, 170381.1));
		seriesApril.getData().add(new XYChart.Data(192, 169657));
		seriesApril.getData().add(new XYChart.Data(193, 168930.2));
		seriesApril.getData().add(new XYChart.Data(194, 168200.7));
		seriesApril.getData().add(new XYChart.Data(195, 167468.5));
		seriesApril.getData().add(new XYChart.Data(196, 166733.5));
		seriesApril.getData().add(new XYChart.Data(197, 165995.8));
		seriesApril.getData().add(new XYChart.Data(198, 165255.3));
		seriesApril.getData().add(new XYChart.Data(199, 164512));
		seriesApril.getData().add(new XYChart.Data(200, 163766));
		seriesApril.getData().add(new XYChart.Data(201, 163017.1));
		seriesApril.getData().add(new XYChart.Data(202, 162265.4));
		seriesApril.getData().add(new XYChart.Data(203, 161511));
		seriesApril.getData().add(new XYChart.Data(204, 160753.6));
		seriesApril.getData().add(new XYChart.Data(205, 159993.5));
		seriesApril.getData().add(new XYChart.Data(206, 159230.5));
		seriesApril.getData().add(new XYChart.Data(207, 158464.6));
		seriesApril.getData().add(new XYChart.Data(208, 157695.9));
		seriesApril.getData().add(new XYChart.Data(209, 156924.2));
		seriesApril.getData().add(new XYChart.Data(210, 156149.7));
		seriesApril.getData().add(new XYChart.Data(211, 155372.3));
		seriesApril.getData().add(new XYChart.Data(212, 154592));
		seriesApril.getData().add(new XYChart.Data(213, 153808.7));
		seriesApril.getData().add(new XYChart.Data(214, 153022.5));
		seriesApril.getData().add(new XYChart.Data(215, 152233.3));
		seriesApril.getData().add(new XYChart.Data(216, 151441.2));
		seriesApril.getData().add(new XYChart.Data(217, 150646.2));
		seriesApril.getData().add(new XYChart.Data(218, 149848.1));
		seriesApril.getData().add(new XYChart.Data(219, 149047));
		seriesApril.getData().add(new XYChart.Data(220, 148243));
		seriesApril.getData().add(new XYChart.Data(221, 147435.9));
		seriesApril.getData().add(new XYChart.Data(222, 146625.8));
		seriesApril.getData().add(new XYChart.Data(223, 145812.7));
		seriesApril.getData().add(new XYChart.Data(224, 144996.5));
		seriesApril.getData().add(new XYChart.Data(225, 144177.2));
		seriesApril.getData().add(new XYChart.Data(226, 143354.9));
		seriesApril.getData().add(new XYChart.Data(227, 142529.5));
		seriesApril.getData().add(new XYChart.Data(228, 141701));
		seriesApril.getData().add(new XYChart.Data(229, 140869.4));
		seriesApril.getData().add(new XYChart.Data(230, 140034.7));
		seriesApril.getData().add(new XYChart.Data(231, 139196.8));
		seriesApril.getData().add(new XYChart.Data(232, 138355.9));
		seriesApril.getData().add(new XYChart.Data(233, 137511.7));
		seriesApril.getData().add(new XYChart.Data(234, 136664.4));
		seriesApril.getData().add(new XYChart.Data(235, 135813.9));
		seriesApril.getData().add(new XYChart.Data(236, 134960.2));
		seriesApril.getData().add(new XYChart.Data(237, 134103.3));
		seriesApril.getData().add(new XYChart.Data(238, 133243.2));
		seriesApril.getData().add(new XYChart.Data(239, 132379.9));
		seriesApril.getData().add(new XYChart.Data(240, 131513.4));
		seriesApril.getData().add(new XYChart.Data(241, 130643.5));
		seriesApril.getData().add(new XYChart.Data(242, 129770.5));
		seriesApril.getData().add(new XYChart.Data(243, 128894.1));
		seriesApril.getData().add(new XYChart.Data(244, 128014.5));
		seriesApril.getData().add(new XYChart.Data(245, 127131.6));
		seriesApril.getData().add(new XYChart.Data(246, 126245.3));
		seriesApril.getData().add(new XYChart.Data(247, 125355.8));
		seriesApril.getData().add(new XYChart.Data(248, 124462.9));
		seriesApril.getData().add(new XYChart.Data(249, 123566.6));
		seriesApril.getData().add(new XYChart.Data(250, 122667));
		seriesApril.getData().add(new XYChart.Data(251, 121764));
		seriesApril.getData().add(new XYChart.Data(252, 120857.7));
		seriesApril.getData().add(new XYChart.Data(253, 119947.9));
		seriesApril.getData().add(new XYChart.Data(254, 119034.7));
		seriesApril.getData().add(new XYChart.Data(255, 118118.1));
		seriesApril.getData().add(new XYChart.Data(256, 117198.1));
		seriesApril.getData().add(new XYChart.Data(257, 116274.6));
		seriesApril.getData().add(new XYChart.Data(258, 115347.6));
		seriesApril.getData().add(new XYChart.Data(259, 114417.2));
		seriesApril.getData().add(new XYChart.Data(260, 113483.3));
		seriesApril.getData().add(new XYChart.Data(261, 112545.9));
		seriesApril.getData().add(new XYChart.Data(262, 111604.9));
		seriesApril.getData().add(new XYChart.Data(263, 110660.5));
		seriesApril.getData().add(new XYChart.Data(264, 109712.4));
		seriesApril.getData().add(new XYChart.Data(265, 108760.9));
		seriesApril.getData().add(new XYChart.Data(266, 107805.8));
		seriesApril.getData().add(new XYChart.Data(267, 106847));
		seriesApril.getData().add(new XYChart.Data(268, 105884.7));
		seriesApril.getData().add(new XYChart.Data(269, 104918.8));
		seriesApril.getData().add(new XYChart.Data(270, 103949.3));
		seriesApril.getData().add(new XYChart.Data(271, 102976.1));
		seriesApril.getData().add(new XYChart.Data(272, 101999.3));
		seriesApril.getData().add(new XYChart.Data(273, 101018.8));
		seriesApril.getData().add(new XYChart.Data(274, 100034.6));
		seriesApril.getData().add(new XYChart.Data(275, 99046.8));
		seriesApril.getData().add(new XYChart.Data(276, 98055.2));
		seriesApril.getData().add(new XYChart.Data(277, 97060));
		seriesApril.getData().add(new XYChart.Data(278, 96060.9));
		seriesApril.getData().add(new XYChart.Data(279, 95058.2));
		seriesApril.getData().add(new XYChart.Data(280, 94051.7));
		seriesApril.getData().add(new XYChart.Data(281, 93041.4));
		seriesApril.getData().add(new XYChart.Data(282, 92027.3));
		seriesApril.getData().add(new XYChart.Data(283, 91009.4));
		seriesApril.getData().add(new XYChart.Data(284, 89987.7));
		seriesApril.getData().add(new XYChart.Data(285, 88962.2));
		seriesApril.getData().add(new XYChart.Data(286, 87932.8));
		seriesApril.getData().add(new XYChart.Data(287, 86899.6));
		seriesApril.getData().add(new XYChart.Data(288, 85862.5));
		seriesApril.getData().add(new XYChart.Data(289, 84821.5));
		seriesApril.getData().add(new XYChart.Data(290, 83776.6));
		seriesApril.getData().add(new XYChart.Data(291, 82727.8));
		seriesApril.getData().add(new XYChart.Data(292, 81675));
		seriesApril.getData().add(new XYChart.Data(293, 80618.3));
		seriesApril.getData().add(new XYChart.Data(294, 79557.6));
		seriesApril.getData().add(new XYChart.Data(295, 78493));
		seriesApril.getData().add(new XYChart.Data(296, 77424.4));
		seriesApril.getData().add(new XYChart.Data(297, 76351.7));
		seriesApril.getData().add(new XYChart.Data(298, 75275));
		seriesApril.getData().add(new XYChart.Data(299, 74194.3));
		seriesApril.getData().add(new XYChart.Data(300, 73109.6));
		seriesApril.getData().add(new XYChart.Data(301, 72020.8));
		seriesApril.getData().add(new XYChart.Data(302, 70927.9));
		seriesApril.getData().add(new XYChart.Data(303, 69830.9));
		seriesApril.getData().add(new XYChart.Data(304, 68729.7));
		seriesApril.getData().add(new XYChart.Data(305, 67624.5));
		seriesApril.getData().add(new XYChart.Data(306, 66515.1));
		seriesApril.getData().add(new XYChart.Data(307, 65401.5));
		seriesApril.getData().add(new XYChart.Data(308, 64283.8));
		seriesApril.getData().add(new XYChart.Data(309, 63161.9));
		seriesApril.getData().add(new XYChart.Data(310, 62035.8));
		seriesApril.getData().add(new XYChart.Data(311, 60905.4));
		seriesApril.getData().add(new XYChart.Data(312, 59770.8));
		seriesApril.getData().add(new XYChart.Data(313, 58632));
		seriesApril.getData().add(new XYChart.Data(314, 57488.9));
		seriesApril.getData().add(new XYChart.Data(315, 56341.5));
		seriesApril.getData().add(new XYChart.Data(316, 55189.8));
		seriesApril.getData().add(new XYChart.Data(317, 54033.8));
		seriesApril.getData().add(new XYChart.Data(318, 52873.4));
		seriesApril.getData().add(new XYChart.Data(319, 51708.7));
		seriesApril.getData().add(new XYChart.Data(320, 50539.6));
		seriesApril.getData().add(new XYChart.Data(321, 49366.2));
		seriesApril.getData().add(new XYChart.Data(322, 48188.3));
		seriesApril.getData().add(new XYChart.Data(323, 47006));
		seriesApril.getData().add(new XYChart.Data(324, 45819.3));
		seriesApril.getData().add(new XYChart.Data(325, 44628.1));
		seriesApril.getData().add(new XYChart.Data(326, 43432.5));
		seriesApril.getData().add(new XYChart.Data(327, 42232.4));
		seriesApril.getData().add(new XYChart.Data(328, 41027.8));
		seriesApril.getData().add(new XYChart.Data(329, 39818.7));
		seriesApril.getData().add(new XYChart.Data(330, 38605));
		seriesApril.getData().add(new XYChart.Data(331, 37386.8));
		seriesApril.getData().add(new XYChart.Data(332, 36164));
		seriesApril.getData().add(new XYChart.Data(333, 34936.6));
		seriesApril.getData().add(new XYChart.Data(334, 33704.7));
		seriesApril.getData().add(new XYChart.Data(335, 32468.1));
		seriesApril.getData().add(new XYChart.Data(336, 31226.8));
		seriesApril.getData().add(new XYChart.Data(337, 29981));
		seriesApril.getData().add(new XYChart.Data(338, 28730.4));
		seriesApril.getData().add(new XYChart.Data(339, 27475.2));
		seriesApril.getData().add(new XYChart.Data(340, 26215.2));
		seriesApril.getData().add(new XYChart.Data(341, 24950.5));
		seriesApril.getData().add(new XYChart.Data(342, 23681.1));
		seriesApril.getData().add(new XYChart.Data(343, 22406.9));
		seriesApril.getData().add(new XYChart.Data(344, 21128));
		seriesApril.getData().add(new XYChart.Data(345, 19844.2));
		seriesApril.getData().add(new XYChart.Data(346, 18555.7));
		seriesApril.getData().add(new XYChart.Data(347, 17262.3));
		seriesApril.getData().add(new XYChart.Data(348, 15964));
		seriesApril.getData().add(new XYChart.Data(349, 14660.9));
		seriesApril.getData().add(new XYChart.Data(350, 13352.9));
		seriesApril.getData().add(new XYChart.Data(351, 12040));
		seriesApril.getData().add(new XYChart.Data(352, 10722.1));
		seriesApril.getData().add(new XYChart.Data(353, 9399.4));
		seriesApril.getData().add(new XYChart.Data(354, 8071.6));
		seriesApril.getData().add(new XYChart.Data(355, 6738.9));
		seriesApril.getData().add(new XYChart.Data(356, 5401.2));
		seriesApril.getData().add(new XYChart.Data(357, 4058.5));
		seriesApril.getData().add(new XYChart.Data(358, 2710.7));
		seriesApril.getData().add(new XYChart.Data(359, 1357.9));
		seriesApril.getData().add(new XYChart.Data(360, 0));

		XYChart.Series seriesMay = new XYChart.Series();
		seriesMay.setName("May");
		seriesMay.getData().add(new XYChart.Data(1, 268445.8));
		seriesMay.getData().add(new XYChart.Data(2, 267889.5));
		seriesMay.getData().add(new XYChart.Data(3, 267331.1));
		seriesMay.getData().add(new XYChart.Data(4, 266770.6));
		seriesMay.getData().add(new XYChart.Data(5, 266208));
		seriesMay.getData().add(new XYChart.Data(6, 265643.3));
		seriesMay.getData().add(new XYChart.Data(7, 265076.4));
		seriesMay.getData().add(new XYChart.Data(8, 264507.5));
		seriesMay.getData().add(new XYChart.Data(9, 263936.4));
		seriesMay.getData().add(new XYChart.Data(10, 263363.2));
		seriesMay.getData().add(new XYChart.Data(11, 262787.8));
		seriesMay.getData().add(new XYChart.Data(12, 262210.3));
		seriesMay.getData().add(new XYChart.Data(13, 261630.6));
		seriesMay.getData().add(new XYChart.Data(14, 261048.7));
		seriesMay.getData().add(new XYChart.Data(15, 260464.7));
		seriesMay.getData().add(new XYChart.Data(16, 259878.4));
		seriesMay.getData().add(new XYChart.Data(17, 259290));
		seriesMay.getData().add(new XYChart.Data(18, 258699.4));
		seriesMay.getData().add(new XYChart.Data(19, 258106.5));
		seriesMay.getData().add(new XYChart.Data(20, 257511.4));
		seriesMay.getData().add(new XYChart.Data(21, 256914.1));
		seriesMay.getData().add(new XYChart.Data(22, 256314.5));
		seriesMay.getData().add(new XYChart.Data(23, 255712.7));
		seriesMay.getData().add(new XYChart.Data(24, 255108.7));
		seriesMay.getData().add(new XYChart.Data(25, 254502.3));
		seriesMay.getData().add(new XYChart.Data(26, 253893.7));
		seriesMay.getData().add(new XYChart.Data(27, 253282.9));
		seriesMay.getData().add(new XYChart.Data(28, 252669.7));
		seriesMay.getData().add(new XYChart.Data(29, 252054.2));
		seriesMay.getData().add(new XYChart.Data(30, 251436.4));
		seriesMay.getData().add(new XYChart.Data(31, 250816.3));
		seriesMay.getData().add(new XYChart.Data(32, 250193.9));
		seriesMay.getData().add(new XYChart.Data(33, 249569.2));
		seriesMay.getData().add(new XYChart.Data(34, 248942.1));
		seriesMay.getData().add(new XYChart.Data(35, 248312.6));
		seriesMay.getData().add(new XYChart.Data(36, 247680.8));
		seriesMay.getData().add(new XYChart.Data(37, 247046.6));
		seriesMay.getData().add(new XYChart.Data(38, 246410.1));
		seriesMay.getData().add(new XYChart.Data(39, 245771.1));
		seriesMay.getData().add(new XYChart.Data(40, 245129.8));
		seriesMay.getData().add(new XYChart.Data(41, 244486));
		seriesMay.getData().add(new XYChart.Data(42, 243839.9));
		seriesMay.getData().add(new XYChart.Data(43, 243191.3));
		seriesMay.getData().add(new XYChart.Data(44, 242540.3));
		seriesMay.getData().add(new XYChart.Data(45, 241886.8));
		seriesMay.getData().add(new XYChart.Data(46, 241230.9));
		seriesMay.getData().add(new XYChart.Data(47, 240572.5));
		seriesMay.getData().add(new XYChart.Data(48, 239911.7));
		seriesMay.getData().add(new XYChart.Data(49, 239248.4));
		seriesMay.getData().add(new XYChart.Data(50, 238582.6));
		seriesMay.getData().add(new XYChart.Data(51, 237914.3));
		seriesMay.getData().add(new XYChart.Data(52, 237243.5));
		seriesMay.getData().add(new XYChart.Data(53, 236570.2));
		seriesMay.getData().add(new XYChart.Data(54, 235894.3));
		seriesMay.getData().add(new XYChart.Data(55, 235215.9));
		seriesMay.getData().add(new XYChart.Data(56, 234535));
		seriesMay.getData().add(new XYChart.Data(57, 233851.5));
		seriesMay.getData().add(new XYChart.Data(58, 233165.5));
		seriesMay.getData().add(new XYChart.Data(59, 232476.9));
		seriesMay.getData().add(new XYChart.Data(60, 231785.7));
		seriesMay.getData().add(new XYChart.Data(61, 231091.9));
		seriesMay.getData().add(new XYChart.Data(62, 230395.5));
		seriesMay.getData().add(new XYChart.Data(63, 229696.5));
		seriesMay.getData().add(new XYChart.Data(64, 228994.9));
		seriesMay.getData().add(new XYChart.Data(65, 228290.6));
		seriesMay.getData().add(new XYChart.Data(66, 227583.7));
		seriesMay.getData().add(new XYChart.Data(67, 226874.2));
		seriesMay.getData().add(new XYChart.Data(68, 226162));
		seriesMay.getData().add(new XYChart.Data(69, 225447.1));
		seriesMay.getData().add(new XYChart.Data(70, 224729.6));
		seriesMay.getData().add(new XYChart.Data(71, 224009.3));
		seriesMay.getData().add(new XYChart.Data(72, 223286.4));
		seriesMay.getData().add(new XYChart.Data(73, 222560.7));
		seriesMay.getData().add(new XYChart.Data(74, 221832.3));
		seriesMay.getData().add(new XYChart.Data(75, 221101.2));
		seriesMay.getData().add(new XYChart.Data(76, 220367.4));
		seriesMay.getData().add(new XYChart.Data(77, 219630.8));
		seriesMay.getData().add(new XYChart.Data(78, 218891.4));
		seriesMay.getData().add(new XYChart.Data(79, 218149.2));
		seriesMay.getData().add(new XYChart.Data(80, 217404.3));
		seriesMay.getData().add(new XYChart.Data(81, 216656.6));
		seriesMay.getData().add(new XYChart.Data(82, 215906.1));
		seriesMay.getData().add(new XYChart.Data(83, 215152.7));
		seriesMay.getData().add(new XYChart.Data(84, 214396.6));
		seriesMay.getData().add(new XYChart.Data(85, 213637.6));
		seriesMay.getData().add(new XYChart.Data(86, 212875.7));
		seriesMay.getData().add(new XYChart.Data(87, 212111));
		seriesMay.getData().add(new XYChart.Data(88, 211343.5));
		seriesMay.getData().add(new XYChart.Data(89, 210573));
		seriesMay.getData().add(new XYChart.Data(90, 209799.7));
		seriesMay.getData().add(new XYChart.Data(91, 209023.5));
		seriesMay.getData().add(new XYChart.Data(92, 208244.3));
		seriesMay.getData().add(new XYChart.Data(93, 207462.3));
		seriesMay.getData().add(new XYChart.Data(94, 206677.3));
		seriesMay.getData().add(new XYChart.Data(95, 205889.3));
		seriesMay.getData().add(new XYChart.Data(96, 205098.4));
		seriesMay.getData().add(new XYChart.Data(97, 204304.5));
		seriesMay.getData().add(new XYChart.Data(98, 203507.7));
		seriesMay.getData().add(new XYChart.Data(99, 202707.9));
		seriesMay.getData().add(new XYChart.Data(100, 201905));
		seriesMay.getData().add(new XYChart.Data(101, 201099.2));
		seriesMay.getData().add(new XYChart.Data(102, 200290.3));
		seriesMay.getData().add(new XYChart.Data(103, 199478.4));
		seriesMay.getData().add(new XYChart.Data(104, 198663.5));
		seriesMay.getData().add(new XYChart.Data(105, 197845.5));
		seriesMay.getData().add(new XYChart.Data(106, 197024.5));
		seriesMay.getData().add(new XYChart.Data(107, 196200.3));
		seriesMay.getData().add(new XYChart.Data(108, 195373.1));
		seriesMay.getData().add(new XYChart.Data(109, 194542.7));
		seriesMay.getData().add(new XYChart.Data(110, 193709.3));
		seriesMay.getData().add(new XYChart.Data(111, 192872.7));
		seriesMay.getData().add(new XYChart.Data(112, 192033));
		seriesMay.getData().add(new XYChart.Data(113, 191190.1));
		seriesMay.getData().add(new XYChart.Data(114, 190344.1));
		seriesMay.getData().add(new XYChart.Data(115, 189494.9));
		seriesMay.getData().add(new XYChart.Data(116, 188642.6));
		seriesMay.getData().add(new XYChart.Data(117, 187787));
		seriesMay.getData().add(new XYChart.Data(118, 186928.2));
		seriesMay.getData().add(new XYChart.Data(119, 186066.2));
		seriesMay.getData().add(new XYChart.Data(120, 185201));
		seriesMay.getData().add(new XYChart.Data(121, 184332.5));
		seriesMay.getData().add(new XYChart.Data(122, 183460.7));
		seriesMay.getData().add(new XYChart.Data(123, 182585.7));
		seriesMay.getData().add(new XYChart.Data(124, 181707.5));
		seriesMay.getData().add(new XYChart.Data(125, 180825.9));
		seriesMay.getData().add(new XYChart.Data(126, 179941));
		seriesMay.getData().add(new XYChart.Data(127, 179052.8));
		seriesMay.getData().add(new XYChart.Data(128, 178161.2));
		seriesMay.getData().add(new XYChart.Data(129, 177266.4));
		seriesMay.getData().add(new XYChart.Data(130, 176368.1));
		seriesMay.getData().add(new XYChart.Data(131, 175466.5));
		seriesMay.getData().add(new XYChart.Data(132, 174561.5));
		seriesMay.getData().add(new XYChart.Data(133, 173653.2));
		seriesMay.getData().add(new XYChart.Data(134, 172741.4));
		seriesMay.getData().add(new XYChart.Data(135, 171826.2));
		seriesMay.getData().add(new XYChart.Data(136, 170907.5));
		seriesMay.getData().add(new XYChart.Data(137, 169985.5));
		seriesMay.getData().add(new XYChart.Data(138, 169059.9));
		seriesMay.getData().add(new XYChart.Data(139, 168130.9));
		seriesMay.getData().add(new XYChart.Data(140, 167198.4));
		seriesMay.getData().add(new XYChart.Data(141, 166262.4));
		seriesMay.getData().add(new XYChart.Data(142, 165322.9));
		seriesMay.getData().add(new XYChart.Data(143, 164379.9));
		seriesMay.getData().add(new XYChart.Data(144, 163433.4));
		seriesMay.getData().add(new XYChart.Data(145, 162483.2));
		seriesMay.getData().add(new XYChart.Data(146, 161529.6));
		seriesMay.getData().add(new XYChart.Data(147, 160572.3));
		seriesMay.getData().add(new XYChart.Data(148, 159611.5));
		seriesMay.getData().add(new XYChart.Data(149, 158647.1));
		seriesMay.getData().add(new XYChart.Data(150, 157679));
		seriesMay.getData().add(new XYChart.Data(151, 156707.3));
		seriesMay.getData().add(new XYChart.Data(152, 155732));
		seriesMay.getData().add(new XYChart.Data(153, 154753));
		seriesMay.getData().add(new XYChart.Data(154, 153770.3));
		seriesMay.getData().add(new XYChart.Data(155, 152784));
		seriesMay.getData().add(new XYChart.Data(156, 151793.9));
		seriesMay.getData().add(new XYChart.Data(157, 150800.2));
		seriesMay.getData().add(new XYChart.Data(158, 149802.7));
		seriesMay.getData().add(new XYChart.Data(159, 148801.5));
		seriesMay.getData().add(new XYChart.Data(160, 147796.5));
		seriesMay.getData().add(new XYChart.Data(161, 146787.8));
		seriesMay.getData().add(new XYChart.Data(162, 145775.2));
		seriesMay.getData().add(new XYChart.Data(163, 144758.9));
		seriesMay.getData().add(new XYChart.Data(164, 143738.8));
		seriesMay.getData().add(new XYChart.Data(165, 142714.8));
		seriesMay.getData().add(new XYChart.Data(166, 141687));
		seriesMay.getData().add(new XYChart.Data(167, 140655.3));
		seriesMay.getData().add(new XYChart.Data(168, 139619.8));
		seriesMay.getData().add(new XYChart.Data(169, 138580.4));
		seriesMay.getData().add(new XYChart.Data(170, 137537.1));
		seriesMay.getData().add(new XYChart.Data(171, 136489.9));
		seriesMay.getData().add(new XYChart.Data(172, 135438.7));
		seriesMay.getData().add(new XYChart.Data(173, 134383.6));
		seriesMay.getData().add(new XYChart.Data(174, 133324.6));
		seriesMay.getData().add(new XYChart.Data(175, 132261.6));
		seriesMay.getData().add(new XYChart.Data(176, 131194.6));
		seriesMay.getData().add(new XYChart.Data(177, 130123.6));
		seriesMay.getData().add(new XYChart.Data(178, 129048.6));
		seriesMay.getData().add(new XYChart.Data(179, 127969.5));
		seriesMay.getData().add(new XYChart.Data(180, 126886.4));
		seriesMay.getData().add(new XYChart.Data(181, 125799.2));
		seriesMay.getData().add(new XYChart.Data(182, 124708));
		seriesMay.getData().add(new XYChart.Data(183, 123612.7));
		seriesMay.getData().add(new XYChart.Data(184, 122513.2));
		seriesMay.getData().add(new XYChart.Data(185, 121409.7));
		seriesMay.getData().add(new XYChart.Data(186, 120302));
		seriesMay.getData().add(new XYChart.Data(187, 119190.1));
		seriesMay.getData().add(new XYChart.Data(188, 118074.1));
		seriesMay.getData().add(new XYChart.Data(189, 116953.9));
		seriesMay.getData().add(new XYChart.Data(190, 115829.5));
		seriesMay.getData().add(new XYChart.Data(191, 114700.9));
		seriesMay.getData().add(new XYChart.Data(192, 113568));
		seriesMay.getData().add(new XYChart.Data(193, 112430.9));
		seriesMay.getData().add(new XYChart.Data(194, 111289.6));
		seriesMay.getData().add(new XYChart.Data(195, 110143.9));
		seriesMay.getData().add(new XYChart.Data(196, 108994));
		seriesMay.getData().add(new XYChart.Data(197, 107839.7));
		seriesMay.getData().add(new XYChart.Data(198, 106681.1));
		seriesMay.getData().add(new XYChart.Data(199, 105518.2));
		seriesMay.getData().add(new XYChart.Data(200, 104350.9));
		seriesMay.getData().add(new XYChart.Data(201, 103179.2));
		seriesMay.getData().add(new XYChart.Data(202, 102003.2));
		seriesMay.getData().add(new XYChart.Data(203, 100822.7));
		seriesMay.getData().add(new XYChart.Data(204, 99637.8));
		seriesMay.getData().add(new XYChart.Data(205, 98448.5));
		seriesMay.getData().add(new XYChart.Data(206, 97254.7));
		seriesMay.getData().add(new XYChart.Data(207, 96056.4));
		seriesMay.getData().add(new XYChart.Data(208, 94853.6));
		seriesMay.getData().add(new XYChart.Data(209, 93646.3));
		seriesMay.getData().add(new XYChart.Data(210, 92434.5));
		seriesMay.getData().add(new XYChart.Data(211, 91218.2));
		seriesMay.getData().add(new XYChart.Data(212, 89997.3));
		seriesMay.getData().add(new XYChart.Data(213, 88771.8));
		seriesMay.getData().add(new XYChart.Data(214, 87541.7));
		seriesMay.getData().add(new XYChart.Data(215, 86307));
		seriesMay.getData().add(new XYChart.Data(216, 85067.6));
		seriesMay.getData().add(new XYChart.Data(217, 83823.7));
		seriesMay.getData().add(new XYChart.Data(218, 82575));
		seriesMay.getData().add(new XYChart.Data(219, 81321.7));
		seriesMay.getData().add(new XYChart.Data(220, 80063.7));
		seriesMay.getData().add(new XYChart.Data(221, 78800.9));
		seriesMay.getData().add(new XYChart.Data(222, 77533.4));
		seriesMay.getData().add(new XYChart.Data(223, 76261.2));
		seriesMay.getData().add(new XYChart.Data(224, 74984.2));
		seriesMay.getData().add(new XYChart.Data(225, 73702.4));
		seriesMay.getData().add(new XYChart.Data(226, 72415.8));
		seriesMay.getData().add(new XYChart.Data(227, 71124.4));
		seriesMay.getData().add(new XYChart.Data(228, 69828.1));
		seriesMay.getData().add(new XYChart.Data(229, 68527));
		seriesMay.getData().add(new XYChart.Data(230, 67221));
		seriesMay.getData().add(new XYChart.Data(231, 65910.1));
		seriesMay.getData().add(new XYChart.Data(232, 64594.2));
		seriesMay.getData().add(new XYChart.Data(233, 63273.5));
		seriesMay.getData().add(new XYChart.Data(234, 61947.8));
		seriesMay.getData().add(new XYChart.Data(235, 60617.1));
		seriesMay.getData().add(new XYChart.Data(236, 59281.4));
		seriesMay.getData().add(new XYChart.Data(237, 57940.8));
		seriesMay.getData().add(new XYChart.Data(238, 56595.1));
		seriesMay.getData().add(new XYChart.Data(239, 55244.3));
		seriesMay.getData().add(new XYChart.Data(240, 53888.5));
		seriesMay.getData().add(new XYChart.Data(241, 52527.6));
		seriesMay.getData().add(new XYChart.Data(242, 51161.6));
		seriesMay.getData().add(new XYChart.Data(243, 49790.4));
		seriesMay.getData().add(new XYChart.Data(244, 48414.2));
		seriesMay.getData().add(new XYChart.Data(245, 47032.7));
		seriesMay.getData().add(new XYChart.Data(246, 45646.1));
		seriesMay.getData().add(new XYChart.Data(247, 44254.3));
		seriesMay.getData().add(new XYChart.Data(248, 42857.3));
		seriesMay.getData().add(new XYChart.Data(249, 41455));
		seriesMay.getData().add(new XYChart.Data(250, 40047.5));
		seriesMay.getData().add(new XYChart.Data(251, 38634.7));
		seriesMay.getData().add(new XYChart.Data(252, 37216.6));
		seriesMay.getData().add(new XYChart.Data(253, 35793.2));
		seriesMay.getData().add(new XYChart.Data(254, 34364.4));
		seriesMay.getData().add(new XYChart.Data(255, 32930.3));
		seriesMay.getData().add(new XYChart.Data(256, 31490.8));
		seriesMay.getData().add(new XYChart.Data(257, 30045.9));
		seriesMay.getData().add(new XYChart.Data(258, 28595.6));
		seriesMay.getData().add(new XYChart.Data(259, 27139.8));
		seriesMay.getData().add(new XYChart.Data(260, 25678.6));
		seriesMay.getData().add(new XYChart.Data(261, 24211.9));
		seriesMay.getData().add(new XYChart.Data(262, 22739.8));
		seriesMay.getData().add(new XYChart.Data(263, 21262.1));
		seriesMay.getData().add(new XYChart.Data(264, 19778.8));
		seriesMay.getData().add(new XYChart.Data(265, 18290));
		seriesMay.getData().add(new XYChart.Data(266, 16795.6));
		seriesMay.getData().add(new XYChart.Data(267, 15295.6));
		seriesMay.getData().add(new XYChart.Data(268, 13790));
		seriesMay.getData().add(new XYChart.Data(269, 12278.7));
		seriesMay.getData().add(new XYChart.Data(270, 10761.8));
		seriesMay.getData().add(new XYChart.Data(271, 9239.1));
		seriesMay.getData().add(new XYChart.Data(272, 7710.8));
		seriesMay.getData().add(new XYChart.Data(273, 6176.7));
		seriesMay.getData().add(new XYChart.Data(274, 4636.9));
		seriesMay.getData().add(new XYChart.Data(275, 3091.3));
		seriesMay.getData().add(new XYChart.Data(276, 1539.9));
		seriesMay.getData().add(new XYChart.Data(277, 5.8));
		seriesMay.getData().add(new XYChart.Data(278, 0));

		ac.setMaxWidth(1500);
		ac.getData().addAll(seriesApril, seriesMay);

		for (final Series<Number, Number> series : ac.getData()) {
			for (final Data<Number, Number> data : series.getData()) {
				Tooltip tooltip = new Tooltip();
				tooltip.setText(data.getYValue().toString());
				Tooltip.install(data.getNode(), tooltip);
			}
		}

		apAreaChart.getChildren().add(ac);

	}

}
