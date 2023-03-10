package window;

import controller.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.*;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import persistenceController.AccountingSystemHib;
import persistenceController.CategoryHibController;
import persistenceController.IncomeHibController;
import persistenceController.UserHibController;
import service.CategoryService;
import service.ExpenseService;
import service.IncomeService;


import javax.persistence.EntityManagerFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;

public class ManageCategoryWindow implements Initializable {
    public ListView subCategoryList;
    public Label messageToUser;
    public Button backBtn;
    public Button manageSubCatBtn;
    public Label systemNameField;
    public Label systemIncomeField;
    public Label systemExpenseField;
    public Label systemDateField;
    public Button descriptionBtn;
    public TextField titleField;
    public Button updateCategoryTitleBtn;
    public Button deleteCategoryBtn;
    public Button showUsersBtn;
    public Button addSubCatBtn;
    public TextField editSubCatNameField;
    public Label errorMessage;
    public ListView incomeList;
    public ListView expenseList;
    public Label title;
    public TextField responsibleUserNameField;
    public Label messageToAddUser;
    public Label parentTitle;
    public ListView responsibleUserList;
    public TextField delIncNameField;
    public TextField delExpNameField;
    public Button createReportPatternBtn;
    public Button createReportBtn;
    private AccountingSystem accountingSystem;
    private Category category;
    private User activeUser;
    private Category parentCategory;
    private EntityManagerFactory entityManagerFactory;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void setDisplayInformation(
            AccountingSystem accountingSystem, Category category, User activeUser) {
        setAccountingSystem(accountingSystem);
        setActiveUser(activeUser);
        setCategory(category);
        setSubCategoryList(category);
        setExpenseList(category);
        setIncomeList(category);
        setParentCategory(category);
        setResponsibleUserList(category);
        if (category.getParentCategory() != null) {
            parentTitle.setText("'" + category.getParentCategory().getTitle() + "'");
        } else parentTitle.setText("Is a parent");

        if (activeUser.getType().equals(UserType.ADMIN)) {
            showUsersBtn.setDisable(false);
        }
    }

    public void setAccountingSystem(AccountingSystem accountingSystem) {
        this.accountingSystem = accountingSystem;
    }

    public void setCategory(Category category) {
        this.category = category;
        titleField.setText(category.getTitle());
        title.setText("'" + category.getTitle() + "'");
    }

    public void setParentCategory(Category category) {
        this.parentCategory = category.getParentCategory();
    }

    public void setActiveUser(User activeUser) {
        this.activeUser = activeUser;
    }

    public void setSubCategoryList(Category category) {
        subCategoryList.getItems().clear();
        if (!category.getSubCategories().isEmpty()) {
            for (Category subcategory : category.getSubCategories()) {
                subCategoryList
                        .getItems()
                        .add(subcategory.getTitle());
            }
        }
    }

    public void setIncomeList(Category category) {
        incomeList.getItems().clear();
        for (Income income : category.getIncomes()) {
            incomeList.getItems().add("| " + income.getName() + " |  " + income.getAmount() + "eur   " + income.getCreationDate());
        }
    }

    public void setExpenseList(Category category) {
        expenseList.getItems().clear();
        for (Expense expense : category.getExpenses()) {
            expenseList
                    .getItems()
                    .add("| " + expense.getName() + " |  " + expense.getAmount() + " eur   " + expense.getCreationDate());
        }
    }

    public void setResponsibleUserList(Category category) {
        responsibleUserList.getItems().clear();
        for (User user : category.getResponsibleUsers()) {
            responsibleUserList.getItems().add("'" + user.getName() + "'");
        }
    }

    public void descriptionBtnClick(ActionEvent actionEvent) {
        messageToUser.setText("");
        errorMessage.setText("");
        Stage popUpWindow = new Stage();

        popUpWindow.initModality(Modality.APPLICATION_MODAL);
        popUpWindow.setTitle("Category description");

        TextField categoryDescription = new TextField(category.getDescription());

        Button backBtn = new Button("Back");
        Button updateCategoryBtn = new Button("Update");
        backBtn.setOnAction(e -> popUpWindow.close());
        updateCategoryBtn.setOnAction(e -> updateDescriptionInformation(categoryDescription.getText()));
        VBox layout = new VBox(10);

        layout.getChildren().addAll(categoryDescription, backBtn, updateCategoryBtn);

        layout.setAlignment(Pos.CENTER);

        Scene scene1 = new Scene(layout, 500, 300);

        popUpWindow.setScene(scene1);

        popUpWindow.showAndWait();
    }

    private void updateDescriptionInformation(String text) {
        if (!emptyField(text)) {
            category.setDescription(text);
            CategoryHibController categoryHibController = new CategoryHibController(entityManagerFactory);
            categoryHibController.update(category);
            messageToUser.setText("Category description updated");
        }
    }

    public void backBtnClick(ActionEvent actionEvent) throws IOException {
        if (parentCategory == null) loadAccountingWindow();
        else loadParentCategoryWindow();
    }

    private void loadParentCategoryWindow() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/ManageCategoryWindow.fxml"));
        Parent root = loader.load();
        ManageCategoryWindow manageCategoryWindow = loader.getController();
        manageCategoryWindow.setEntityManagerFactory(entityManagerFactory);
        AccountingSystemHib accountingSystemHib = new AccountingSystemHib(entityManagerFactory);
        manageCategoryWindow.setDisplayInformation(accountingSystemHib.getById(accountingSystem.getId()), parentCategory, activeUser);

        Stage stage = (Stage) backBtn.getScene().getWindow();
        stage.setTitle("Accounting System. User " + activeUser.getName());
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }

    private void loadSubCategoryWindow(Category subcategory) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/ManageCategoryWindow.fxml"));
        Parent root = loader.load();
        ManageCategoryWindow manageCategoryWindow = loader.getController();

        AccountingSystemHib accountingSystemHib = new AccountingSystemHib(entityManagerFactory);
        CategoryHibController categoryHibController = new CategoryHibController(entityManagerFactory);
        manageCategoryWindow.setEntityManagerFactory(entityManagerFactory);
        manageCategoryWindow.setDisplayInformation(accountingSystemHib.getById(accountingSystem.getId()), categoryHibController.getById(subcategory.getId()), activeUser);

        Stage stage = (Stage) backBtn.getScene().getWindow();
        stage.setTitle("Accounting System. User " + activeUser.getName());
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }

    public void manageSubCatBtnClick(ActionEvent actionEvent) throws IOException {
        try {
            Category subcategory =
                    CategoryController.getSubcategoryByName(
                            category, subCategoryList.getSelectionModel().getSelectedItem().toString());
            loadSubCategoryWindow(subcategory);
        } catch (RuntimeException e) {
            errorMessage.setText("Category not selected");
        }
    }

    public void updateCategoryTitleBtnClick(ActionEvent actionEvent) {
        if (!emptyField(titleField.getText())) {
            if (AccountingSystemController.getCategoryByTitle(accountingSystem, titleField.getText())
                    != null) {
                messageToUser.setText("Category with this title already exists");
            } else {
                category.setTitle(titleField.getText());
                title.setText("'" + category.getTitle() + "'");
                CategoryHibController categoryHibController = new CategoryHibController(entityManagerFactory);
                categoryHibController.update(category);
                messageToUser.setText("Title updated");
            }
        } else {
            messageToUser.setText("Title cannot be empty");
        }
    }

    public void deleteCategoryBtnClick(ActionEvent actionEvent) {
        messageToUser.setText("");
        errorMessage.setText("");
        Stage popUpWindow = new Stage();


        popUpWindow.initModality(Modality.APPLICATION_MODAL);
        popUpWindow.setTitle("Delete category");

        Label question = new Label("Are you sure you want to delete this category?");
        Button backBtn = new Button("Go back");
        Button deleteBtn = new Button("Delete. I am sure. Yes. Bye.");
        backBtn.setOnAction(e -> popUpWindow.close());
        deleteBtn.setOnAction(
                e -> {
                    try {
                        new CategoryHibController(entityManagerFactory).delete(category.getId());
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                    try {
                        deleteCategory();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                    popUpWindow.close();
                });
        VBox layout = new VBox(10);

        layout.getChildren().addAll(question, backBtn, deleteBtn);

        layout.setAlignment(Pos.CENTER);

        Scene scene1 = new Scene(layout, 500, 300);

        popUpWindow.setScene(scene1);
//
//        if(!category.getSubCategories().isEmpty()){
//            errorMessage.setText("Cannot delete category with subcategories");
//            popUpWindow.close();
//        } else {
            popUpWindow.showAndWait();
//        }
    }

    private void deleteCategory() throws Exception {

        if (parentCategory == null) {
            AccountingSystemController.removeCategory(accountingSystem, category);
            AccountingSystemHib accountingSystemHib = new AccountingSystemHib(entityManagerFactory);
            accountingSystemHib.updateExpenseIncome(accountingSystem.getId());
            loadAccountingWindow();
        } else {
            CategoryController.removeSubCategory(parentCategory, category);
            AccountingSystemHib accountingSystemHib = new AccountingSystemHib(entityManagerFactory);
            accountingSystemHib.updateExpenseIncome(accountingSystem.getId());
            loadParentCategoryWindow();
        }
    }

    public void showUsersBtnClick(ActionEvent actionEvent) {
        loadUserList();
    }

    private void loadUserList() {
        messageToUser.setText("");
        Stage popUpWindow = new Stage();

        popUpWindow.initModality(Modality.APPLICATION_MODAL);
        popUpWindow.setTitle("User list");

        ListView users = new ListView();
        for (User user : accountingSystem.getUsers()) {
            users.getItems().add(user.getName() + ": " + user.getType());
        }

        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> popUpWindow.close());
        VBox layout = new VBox(10);

        layout.getChildren().addAll(backBtn, users);

        layout.setAlignment(Pos.CENTER);

        Scene scene1 = new Scene(layout, 500, 300);

        popUpWindow.setScene(scene1);

        popUpWindow.showAndWait();
    }

    public void addSubCatBtnClick(ActionEvent actionEvent) throws IOException {
        if (activeUser.getType().equals(UserType.ADMIN))
            errorMessage.setText("Admin cannot add categories");
        else loadCreateCategoryWindow();
    }

    private void loadCreateCategoryWindow() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/CreateCategoryWindow.fxml"));
        Parent root = loader.load();
        CreateCategoryWindow createCategoryWindow = loader.getController();
        AccountingSystemHib accountingSystemHib = new AccountingSystemHib(entityManagerFactory);
        createCategoryWindow.setAccountingSystem(accountingSystemHib.getById(accountingSystem.getId()));
        createCategoryWindow.setEntityManagerFactory(entityManagerFactory);
        createCategoryWindow.setActiveUser(activeUser);
        createCategoryWindow.setParentCategory(category);

        Stage stage = (Stage) addSubCatBtn.getScene().getWindow();
        stage.setTitle("Accounting System. User " + activeUser.getName());
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }

    public void addIncomeBtnClick(ActionEvent actionEvent) {
        messageToUser.setText("");
        errorMessage.setText("");
        Stage popUpWindow = new Stage();

        popUpWindow.initModality(Modality.APPLICATION_MODAL);
        popUpWindow.setTitle("Add income");

        Label labelIncomeName = new Label("Income name:");
        TextField incomeNameField = new TextField();

        Label labelIncomeAmount = new Label("Income amount:");
        TextField incomeAmountField = new TextField();

        Button backBtn = new Button("Back");
        Button createIncomeBtn = new Button("Create");
        backBtn.setOnAction(e -> popUpWindow.close());
        createIncomeBtn.setOnAction(
                e -> createIncome(incomeNameField.getText(), incomeAmountField.getText()));
        VBox layout = new VBox(10);

        layout
                .getChildren()
                .addAll(
                        labelIncomeName,
                        incomeNameField,
                        labelIncomeAmount,
                        incomeAmountField,
                        backBtn,
                        createIncomeBtn);

        layout.setAlignment(Pos.CENTER);

        Scene scene1 = new Scene(layout, 500, 300);

        popUpWindow.setScene(scene1);

        popUpWindow.showAndWait();
    }

    private void createIncome(String incomeNameField, String incomeAmountField) {
        if (emptyField(incomeNameField) || emptyField(incomeAmountField)) {
            Popup.display("Error", "Required field is empty", "Okay");
        } else if (IncomeController.incomeExists(category.getIncomes(), incomeNameField)) {
            Popup.display("Error", "Income with this name exists", "Okay");
        } else {
            try {
                Income income = new Income(incomeNameField, Integer.parseInt(incomeAmountField), category, LocalDate.now());
                IncomeService.create(entityManagerFactory, accountingSystem, income, category);
                AccountingSystemHib accountingSystemHib = new AccountingSystemHib(entityManagerFactory);
                accountingSystem = accountingSystemHib.getById(accountingSystem.getId());
                CategoryHibController categoryHibController = new CategoryHibController(entityManagerFactory);
                category = categoryHibController.getById(category.getId());
                Popup.display("Income added", "Income added", "Okay");
                setIncomeList(category);
            } catch (NumberFormatException e) {
                Popup.display("Error", "Amount must be a number", "Okay");
            }
        }
    }

    public void addExpenseBtnClick(ActionEvent actionEvent) {
        messageToUser.setText("");
        errorMessage.setText("");
        Stage popUpWindow = new Stage();

        popUpWindow.initModality(Modality.APPLICATION_MODAL);
        popUpWindow.setTitle("Add expense");

        Label labelExpenseName = new Label("Expense name:");
        TextField expenseNameField = new TextField();

        Label labelExpenseAmount = new Label("Expense amount:");
        TextField expenseAmountField = new TextField();

        Button backBtn = new Button("Back");
        Button createExpenseBtn = new Button("Create");
        backBtn.setOnAction(e -> popUpWindow.close());
        createExpenseBtn.setOnAction(
                e -> createExpense(expenseNameField.getText(), expenseAmountField.getText()));
        VBox layout = new VBox(10);

        layout
                .getChildren()
                .addAll(
                        labelExpenseName,
                        expenseNameField,
                        labelExpenseAmount,
                        expenseAmountField,
                        backBtn,
                        createExpenseBtn);

        layout.setAlignment(Pos.CENTER);

        Scene scene1 = new Scene(layout, 500, 300);

        popUpWindow.setScene(scene1);

        popUpWindow.showAndWait();
    }

    private void createExpense(String expenseNameField, String expenseAmountField) {
        if (emptyField(expenseNameField) || emptyField(expenseAmountField)) {
            Popup.display("Error", "Required field is empty", "Okay");
        } else if (ExpenseController.expenseExists(category.getExpenses(), expenseNameField)) {
            Popup.display("Error", "Expense with this name exists", "Okay");
        } else {
            try {
                Integer.parseInt(expenseAmountField);
                Expense expense = new Expense(expenseNameField, Integer.parseInt(expenseAmountField), category, LocalDate.now());
                ExpenseService.create(entityManagerFactory, accountingSystem, expense, category);
                AccountingSystemHib accountingSystemHib = new AccountingSystemHib(entityManagerFactory);
                accountingSystem = accountingSystemHib.getById(accountingSystem.getId());
                CategoryHibController categoryHibController = new CategoryHibController(entityManagerFactory);
                category = categoryHibController.getById(category.getId());
                Popup.display("Expense added", "Expense added", "Okay");
                setExpenseList(category);
            } catch (NumberFormatException e) {
                Popup.display("Error", "Amount must be a number", "Okay");
            }
        }
    }

    public void addResponsibleBtnClick(ActionEvent actionEvent) throws Exception {
        if (emptyField(responsibleUserNameField.getText())) {
            messageToAddUser.setText("Empty field");
        } else {
            addResponsibleUser();
        }
    }

    private void addResponsibleUser() throws Exception {
        User responsibleUser =
                UserController.getUserByName(accountingSystem, responsibleUserNameField.getText());
        if (responsibleUser == null) {
            messageToAddUser.setText("User with this name does not exist");
        } else {
            if (CategoryController.responsibleUserExists(
                    category.getResponsibleUsers(), responsibleUser)) {
                messageToAddUser.setText("User is already responsible");
            } else {
                messageToAddUser.setText(CategoryController.addResponsibleUser(category, responsibleUser, entityManagerFactory));
                CategoryHibController categoryHibController = new CategoryHibController(entityManagerFactory);
                category = categoryHibController.getById(category.getId());
                setResponsibleUserList(category);
            }
        }
    }

    public void removeRespBtnClick(ActionEvent actionEvent) throws Exception {
        if (emptyField(responsibleUserNameField.getText())) {
            messageToAddUser.setText("Empty field");
        } else {
            removeResponsibleUser();
        }
    }

    private void removeResponsibleUser() throws Exception {
        UserHibController userHibController = new UserHibController(entityManagerFactory);
        User responsibleUser = userHibController.getByNameInSystem(accountingSystem, responsibleUserNameField.getText());
        if (responsibleUser == null) {
            messageToAddUser.setText("User with this name does not exist");
        } else {
            if (CategoryController.responsibleUserExists(
                    category.getResponsibleUsers(), responsibleUser)) {
                CategoryHibController categoryHibController = new CategoryHibController(entityManagerFactory);
                categoryHibController.removeUserFromCategory(category.getId(), responsibleUser.getId());
                category = categoryHibController.getById(category.getId());

                messageToAddUser.setText("User removed from list");
                setResponsibleUserList(category);
            } else {
                messageToAddUser.setText("User is not responsible");
            }
        }
    }

    private void loadAccountingWindow() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/AccountingWindow.fxml"));
        Parent root = loader.load();
        AccountingWindow accounting = loader.getController();
        AccountingSystemHib accountingSystemHib = new AccountingSystemHib(entityManagerFactory);
        accounting.setAccountingSystem(accountingSystemHib.getById(accountingSystem.getId()));
        accounting.setEntityManagerFactory(entityManagerFactory);
        accounting.setUser(activeUser);
        accounting.setCategoryList(accountingSystem);

        Stage stage = (Stage) backBtn.getScene().getWindow();
        stage.setTitle("Accounting System. User " + activeUser.getName());
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }

    private boolean emptyField(String text) {
        return text.replaceAll("\\s", "").isEmpty();
    }

    public void deleteIncBtnClick(ActionEvent actionEvent) {
        if (validateIncomeDelete()) {
            popUpConfirmDeleteIncome();
        }
    }

    private boolean validateIncomeDelete() {
        if (emptyField(delIncNameField.getText())) {
            errorMessage.setText("Income name to delete is missing");
            return false;
        }
        if (CategoryController.getIncomeByName(category, delIncNameField.getText()) == null) {
            errorMessage.setText("Income with entered name does not exist");
            return false;
        }
        return true;
    }

    private void popUpConfirmDeleteIncome() {
        messageToUser.setText("");
        errorMessage.setText("");
        Stage popUpWindow = new Stage();

        popUpWindow.initModality(Modality.APPLICATION_MODAL);
        popUpWindow.setTitle("Delete Income");

        Label question =
                new Label("Are you sure you want to delete income '" + delIncNameField.getText() + "'?");
        Button backBtn = new Button("Go back");
        Button deleteBtn = new Button("Delete. I am sure. Yes. Bye.");
        backBtn.setOnAction(e -> popUpWindow.close());
        deleteBtn.setOnAction(
                e -> {
                    try {
                        deleteIncome();
                        popUpWindow.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
        VBox layout = new VBox(10);

        layout.getChildren().addAll(question, backBtn, deleteBtn);

        layout.setAlignment(Pos.CENTER);

        Scene scene1 = new Scene(layout, 500, 300);

        popUpWindow.setScene(scene1);

        popUpWindow.showAndWait();
    }

    private void deleteIncome() throws Exception {
        Income income = CategoryController.getIncomeByName(category, delIncNameField.getText());
        if (IncomeService.delete(entityManagerFactory,
                accountingSystem,
                category,
                income)) {
            errorMessage.setText("Income deleted");

            AccountingSystemHib accountingSystemHib = new AccountingSystemHib(entityManagerFactory);
            accountingSystem = accountingSystemHib.getById(accountingSystem.getId());
            accountingSystem.decreaseIncome(income.getAmount());
            accountingSystemHib.update(accountingSystem);

            CategoryHibController categoryHibController = new CategoryHibController(entityManagerFactory);
            category = categoryHibController.getById(category.getId());
            setIncomeList(category);
        } else {
            errorMessage.setText("Something went wrong");
        }
    }

    private boolean validateExpenseDelete() {
        if (emptyField(delExpNameField.getText())) {
            errorMessage.setText("Expense name to delete is missing");
            return false;
        }
        if (CategoryController.getExpenseByName(category, delExpNameField.getText()) == null) {
            errorMessage.setText("Expense with entered name does not exist");
            return false;
        }
        return true;
    }

    private void popUpConfirmDeleteExpense() {
        messageToUser.setText("");
        errorMessage.setText("");
        Stage popUpWindow = new Stage();

        popUpWindow.initModality(Modality.APPLICATION_MODAL);
        popUpWindow.setTitle("Delete Expense");

        Label question =
                new Label("Are you sure you want to delete expense '" + delExpNameField.getText() + "'?");
        Button backBtn = new Button("Go back");
        Button deleteBtn = new Button("Delete. I am sure. Yes. Bye.");
        backBtn.setOnAction(e -> popUpWindow.close());
        deleteBtn.setOnAction(
                e -> {
                    try {
                        deleteExpense();
                        popUpWindow.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
        VBox layout = new VBox(10);

        layout.getChildren().addAll(question, backBtn, deleteBtn);

        layout.setAlignment(Pos.CENTER);

        Scene scene1 = new Scene(layout, 500, 300);

        popUpWindow.setScene(scene1);

        popUpWindow.showAndWait();
    }

    private void deleteExpense() throws Exception {
        Expense expense = CategoryController.getExpenseByName(category, delExpNameField.getText());
        if (ExpenseService.delete(entityManagerFactory,
                accountingSystem,
                category,
                expense)) {
            errorMessage.setText("Expense deleted");


            AccountingSystemHib accountingSystemHib = new AccountingSystemHib(entityManagerFactory);
            accountingSystem = accountingSystemHib.getById(accountingSystem.getId());
            accountingSystem.decreaseExpense(expense.getAmount());
            accountingSystemHib.update(accountingSystem);

            CategoryHibController categoryHibController = new CategoryHibController(entityManagerFactory);
            category = categoryHibController.getById(category.getId());
            setExpenseList(category);
        } else {
            errorMessage.setText("Something went wrong");
        }
    }

    public void deleteExpBtnClick(ActionEvent actionEvent) {
        if (validateExpenseDelete()) {
            popUpConfirmDeleteExpense();
        }
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public void createReportPattern(ActionEvent actionEvent) throws IOException {
        Report.createPattern("pattern");
    }

    public void createReport(ActionEvent actionEvent) throws IOException {

      String patternPath = Report.getFolderPath();

        HSSFWorkbook workbook = new HSSFWorkbook();

        // spreadsheet object
        HSSFSheet spreadsheet
                = workbook.createSheet(" report ");

        // creating a row object
        HSSFRow row;
        row = spreadsheet.createRow(0);
        int cellnum = 0;
        Cell cell = row.createCell(cellnum++);
        cell.setCellValue("Name");
        cell = row.createCell(cellnum++);
        cell.setCellValue("Amount");
        cell = row.createCell(cellnum++);
        cell.setCellValue("Creation date");
        cell = row.createCell(cellnum++);
        cell.setCellValue("Type");
        cell = row.createCell(cellnum++);
        cell.setCellValue("Sum");

        int sum = 0;
        int rownum = 1;
        for (Income income : category.getIncomes()) {
            row = spreadsheet.createRow(rownum++);

            cellnum = 0;
            cell = row.createCell(cellnum++);
            cell.setCellValue(income.getName());
            cell = row.createCell(cellnum++);
            cell.setCellValue(income.getAmount());
            cell = row.createCell(cellnum++);
            cell.setCellValue(income.getCreationDate().toString());
            cell = row.createCell(cellnum++);
            cell.setCellValue("Income");
            sum+=income.getAmount();
        }

        for (Expense expense : category.getExpenses()) {
            row = spreadsheet.createRow(rownum++);

            cellnum = 0;
            cell = row.createCell(cellnum++);
            cell.setCellValue(expense.getName());
            cell = row.createCell(cellnum++);
            cell.setCellValue(expense.getAmount());
            cell = row.createCell(cellnum++);
            cell.setCellValue(expense.getCreationDate().toString());
            cell = row.createCell(cellnum++);
            cell.setCellValue("Expense");
            sum-=expense.getAmount();
        }

        row = spreadsheet.getRow(1);
        cell = row.createCell(4);
        cell.setCellValue(sum);

        FileOutputStream out = new FileOutputStream(
                new File(patternPath + category.getTitle() + " report.csv"));

        workbook.write(out);
        out.close();
    }
}
