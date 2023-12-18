package com.numpy.scraping.team17;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;

import com.numpy.scraping.team17.model.Recipe;
import com.numpy.scraping.team17.utils.ExcelReader;
import com.numpy.scraping.team17.utils.ExcelWriter;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * 
 * Generic implementation to extract recipes based on morbidity
 * 
 * @author vbodepu
 *
 */
public class BaseRecipeExtractor {

	private static final String PAGEINDEX_PARAM_NAME = "?pageindex=";
	private static final String ALLERGIES_SUFFIX = "_Allergies";
	private static final int ALLERGIES_COLUMN_INDEX = 1;
	private static final String TO_ADD = "_To_Add";
	private static final String XLSX_EXTENSION = ".xlsx";
	private static final String TEAM_PREFIX_FOR_XLSX = "SmartScrappers_Team17_";
	private static final String ALL_SUFFIX = "_All";
	private static final String SRC_TEST_RESOURCES_TEST_DATA = "src/test/resources/TestData/";
	private static final String SRC_TEST_RESOURCES_TEST_DATA_OUTPUT = SRC_TEST_RESOURCES_TEST_DATA + "output/";
	private static final String SRC_TEST_RESOURCES_TEST_DATA_INPUT = SRC_TEST_RESOURCES_TEST_DATA + "input/";
	private static final String FILE_PATH_INGREDIENTS_AND_COMORBIDITIES = SRC_TEST_RESOURCES_TEST_DATA_INPUT
			+ "IngredientsAndComorbidities.xlsx";
	private static final String HOME_PAGE = "https://www.tarladalal.com/";
	private static final String INGREDIENTS_WORK_SHEET_NAME = "Diabetes-Hypothyroidism-Hyperte";
	private static final String ALLERGIES_WORK_SHEET_NAME = "Allergies";
	private static final String FILE_PATH_ALLERGIES = SRC_TEST_RESOURCES_TEST_DATA_INPUT + "Allergies.xlsx";

	private static final int MAX_RECEIPES_PER_MORBIDITY = 1200;
	private static final int MAX_PAGES_TO_FETCH = 50;
	protected WebDriver driver = null;

	public BaseRecipeExtractor() {
		super();
	}

	@BeforeClass
	protected void loadHomePage() throws InterruptedException {
		WebDriverManager.chromedriver().setup();
		// Configure Chrome options for headless mode
		ChromeOptions options = new ChromeOptions();
		// options.addArguments("--headless");
		options.addArguments("--headless", "--disable-gpu");

		driver = new ChromeDriver(options);
		driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);

		driver.get(HOME_PAGE);
		driver.manage().window().maximize();
		// driver.manage().timeouts().implicitlyWait(10); // Adjust the timeout
		// as needed

		Thread.sleep(5000);
	}

	@AfterClass
	protected void closeBrower() {
		driver.quit();
	}

	@AfterMethod
	public void getRunTime(ITestResult tr) {

		long time = tr.getEndMillis() - tr.getStartMillis();
		System.out.println("Total time taken for test :: " + tr.getName() + " :: in (secs) : " + (((time / 1000))));
	}

	/**
	 * @param links
	 * @param allRecipes
	 */
	protected void exportRecipes(String morbidity, int eliminateIngredientColumn, int toAddIngredientColumn) {

		// arraylist to store all the links
		ArrayList<String> links = new ArrayList<>();

		fetchAllRecipeUrlsFromPagination(links, driver);
		System.out.println("Morbidity : " + morbidity + " :: Number of Urls to scrape : " + links.size());

		// load each recipe page from above list and populate the recipe object
		// accordingly
		List<Recipe> allRecipes = new ArrayList<Recipe>();

		for (String recipePageUrl : links) {

			Recipe recipe = new Recipe();
			recipe.setTargettedMorbidConditions(morbidity);
			recipe.setRecipeURL(recipePageUrl);

			// read xpaths and values for name, ingredients etc
			populateRecipe(recipe);

			// System.out.println("recipe : " + recipe);

			if (recipe.getIngredients() != null) {
				allRecipes.add(recipe);
			}
		}

//		System.out.println("allRecipes : " + allRecipes);
		System.out.println("Exporting recipes for modbidity : " + morbidity);

		// Export allRecipes that does not have eliminated ingredients
		// into excel <Morbidity>.xlsx . e.g., Hypothyroidism.xlsx
		List<Recipe> safeList = new ArrayList<Recipe>();
		List<String> eliminateFIlter = readFiltersForEachMorbidity(eliminateIngredientColumn);

		for (Recipe recipe : allRecipes) {
			// Check if any string from filter columnValues is present in
			// ingredients using Java Stream API- if ingredients contains filter
			// list of values
			boolean foundEliminatedIngredient = eliminateFIlter.stream().anyMatch(recipe.getIngredients()::contains);
//			System.out.println("foundEliminatedIngredient :  " + foundEliminatedIngredient);
			if (!foundEliminatedIngredient) {
				safeList.add(recipe); // safeList recipes doesn't have
										// ingredients from eliminated list
			}
		}

		// Write recipes that does not contain eliminate list into excel sheet
		// morbidity.xlsx. e.g, Hypothyroidism.xlsx
		ExcelWriter.writeRecipesToExcel(safeList, morbidity,
				SRC_TEST_RESOURCES_TEST_DATA_OUTPUT + TEAM_PREFIX_FOR_XLSX + morbidity + XLSX_EXTENSION);

		List<Recipe> nonAllergicRecipies = new ArrayList<Recipe>();
		List<String> alleriesList = readAllergiesList();

		// System.out.println("alleriesList : " + alleriesList);

		// Filter receipts that have Allergies
		for (Recipe recipe : safeList) {
			// Check if any string from filter columnValues is present in
			// ingredients using Stream API
			boolean foundAllergicIngredient = alleriesList.stream().anyMatch(recipe.getIngredients()::contains);

			if (!foundAllergicIngredient) {
				nonAllergicRecipies.add(recipe);
			}
		}

		// Write recipes that contain ingredients from to add list into excel
		// sheet morbidity_Allergies.xlsx. e.g, Hypothyroidism_Allergies.xlsx
		String morbidityAllergiesFileName = TEAM_PREFIX_FOR_XLSX + morbidity + ALLERGIES_SUFFIX;
		ExcelWriter.writeRecipesToExcel(nonAllergicRecipies, morbidityAllergiesFileName,
				SRC_TEST_RESOURCES_TEST_DATA_OUTPUT + morbidityAllergiesFileName + XLSX_EXTENSION);

		List<Recipe> recipesWithToAddIngredients = new ArrayList<Recipe>();
		List<String> toAddIngredients = readFiltersForEachMorbidity(toAddIngredientColumn);

		for (Recipe recipe : safeList) {
			// Check if any string from filter columnValues is present in
			// ingredients using Stream API
			boolean foundToAddIngredient = toAddIngredients.stream().anyMatch(recipe.getIngredients()::contains);
			if (foundToAddIngredient) {
				recipesWithToAddIngredients.add(recipe);
			}
		}

		// Write recipes that contain ingredients from to add list into excel
		// sheet morbidity_To_Add.xlsx. e.g, Hypothyroidism_To_Add.xlsx
		String morbidityToAddFileName = TEAM_PREFIX_FOR_XLSX + morbidity + TO_ADD;
		ExcelWriter.writeRecipesToExcel(recipesWithToAddIngredients, morbidityToAddFileName,
				SRC_TEST_RESOURCES_TEST_DATA_OUTPUT + morbidityToAddFileName + XLSX_EXTENSION);

	}

	/**
	 * Reads the eliminate or to add list for each morbidity based on the column
	 * index. Used the Single excel document shared by the Hackathon organizers
	 * and reading data for each morbidity based on column. e.g., Eliminate List
	 * column for Hypothyroidism is 2.
	 */
	private List<String> readFiltersForEachMorbidity(int columnIndex) {
		List<String> columnValues = null;

		try (FileInputStream inputStream = new FileInputStream(FILE_PATH_INGREDIENTS_AND_COMORBIDITIES)) {
			Workbook workbook = WorkbookFactory.create(inputStream);
			Sheet sheet = workbook.getSheet(INGREDIENTS_WORK_SHEET_NAME);

			columnValues = ExcelReader.getColumnValues(sheet, columnIndex);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return columnValues;
	}

	/**
	 * Reads the filters for Allergies list shared by the Hackathon organisers
	 * 
	 * @return
	 */
	private List<String> readAllergiesList() {
		List<String> columnValues = null;

		try (FileInputStream inputStream = new FileInputStream(FILE_PATH_ALLERGIES)) {
			Workbook workbook = WorkbookFactory.create(inputStream);
			Sheet sheet = workbook.getSheet(ALLERGIES_WORK_SHEET_NAME);

			columnValues = ExcelReader.getColumnValues(sheet, ALLERGIES_COLUMN_INDEX);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return columnValues;
	}

	/**
	 * In each recipe page fetching the required data for expected output
	 * elements
	 * 
	 * @param recipe
	 */
	private void populateRecipe(Recipe recipe) {

		try {

			// populate all other recipe fields from the details page.
			driver.get(recipe.getRecipeURL());

			// Recipe Id
			String tempRecipeId = recipe.getRecipeURL().substring(recipe.getRecipeURL().lastIndexOf("-") + 1,
					(recipe.getRecipeURL().length() - 1));
			recipe.setRecipeID(tempRecipeId);

			try {
				// Recipe Name
				WebElement recipeName = driver.findElement(By.xpath("//span[@id='ctl00_cntrightpanel_lblRecipeName']"));
				recipe.setRecipeName(recipeName.getText());
			} catch (NoSuchElementException e) {
			}

			// Recipe URL - already populated

			// //Recipe Category
			// WebElement recipeCategory = driver.findElement(By.xpath(""));

			// food category
			// WebElement foodCategory= driver.findElement(By.xpath(""));

			// ingredients
			try {
				List<WebElement> ingredients = driver.findElements(By.xpath("//*[@itemprop=\"recipeIngredient\"]"));
				StringBuffer ingredientsBuffer = new StringBuffer(256);
				for (WebElement ingredient : ingredients) {
					ingredientsBuffer.append(ingredient.getText());
					ingredientsBuffer.append("\n");
				}
				recipe.setIngredients(ingredientsBuffer.toString());
			} catch (NoSuchElementException e) {
			}

			// Preparation Time
			try {
				WebElement preparationTime = driver.findElement(By.xpath("//time[@itemprop='prepTime']"));
				recipe.setPreparationTime(preparationTime.getText());
			} catch (NoSuchElementException e) {
			}
			// cooking Time
			try {
				WebElement cookingTime = driver.findElement(By.xpath("//time[@itemprop='cookTime']"));
				recipe.setCookingTime(cookingTime.getText());
			} catch (NoSuchElementException e) {
			}
			// Preparation Method
			try {
				WebElement preparationMethod = driver.findElement(By.xpath("//div[@id='recipe_small_steps']"));
				recipe.setPreparationmethod(preparationMethod.getText());
			} catch (NoSuchElementException e) {
			}

			// Nutrient Values
			StringBuffer nutritionValues = new StringBuffer();
			// TODO: make this table dynamic
			try {
				for (int i = 1; i <= 3; i++) {

					nutritionValues.append(
							driver.findElement(By.xpath("//*[@id=\"rcpnutrients\"]/tbody/tr[" + i + "]")).getText());
					nutritionValues.append("\n");

				}
			} catch (NoSuchElementException e) {
			}

			recipe.setNutrientValues(nutritionValues.toString());

			// Targeted Morbid Conditions - Already populated
			// WebElement targettedMorbidConditions = driver
			// .findElement(By.xpath("//a[@id='ctl00_cntleftpanel_ttlhealthtree_tvTtlHealtht211']"));

		} catch (Exception e) {

			System.err.println(
					"Error while scraping pages for URL : " + recipe.getRecipeURL() + " :: Error : " + e.getMessage());
		}

	}

	/**
	 * fetch URLs for each recipe that is stored in the <a> tag of each recipe
	 * name in the results card
	 * 
	 * @param links
	 *            - this stores URLs to recipes
	 * 
	 */
	private void populateRecipeLinks(ArrayList<String> links) {
		// list of WebElements that store all the links
		List<WebElement> recipeUrlsList = driver.findElements(By.className("rcc_recipename"));
		Iterator<WebElement> recipeUrlsListIter = recipeUrlsList.iterator();

		int counter = 0;
		// loop through recipeUrlsList to fill the links arraylist
		while ((recipeUrlsListIter.hasNext()) && (counter < MAX_RECEIPES_PER_MORBIDITY)) {
			WebElement link = (WebElement) recipeUrlsListIter.next();
			// .findElement -----> finds the tag <a> inside the current
			// WebElement
			// .getAttribute ----> returns the href attribute of the <a> tag in
			// the current WebElement
			links.add(link.findElement(By.tagName("a")).getAttribute("href"));
			counter++;
		}

	}

	/**
	 * fetches recipe URLs based on the pagination found in the morbidity
	 * landing page
	 * 
	 * @param allLInks
	 * @param driver
	 */
	private void fetchAllRecipeUrlsFromPagination(ArrayList<String> allLInks, WebDriver driver) {
		// list of WebElements that store all the links
		WebElement pagination = driver.findElement(By.xpath("//div[@id='pagination']"));
		List<WebElement> links = pagination.findElements(By.tagName("a"));

		// loop through page links to fill the all links
		Iterator<WebElement> linksIter = links.iterator();
		int maxPageSize = 1;

		while ((linksIter.hasNext()) && (maxPageSize < MAX_PAGES_TO_FETCH)) {

			WebElement paginationLink = (WebElement) linksIter.next();
			try {
				int tempPageNumber = Integer.valueOf(paginationLink.getText());
				if (tempPageNumber > maxPageSize) {
					maxPageSize = tempPageNumber;
				}
			} catch (NumberFormatException e) {
			}
		}

		System.out.println("Total Pages : " + maxPageSize);

		// Generate pagination URLs
		String basePaginationUrl = driver.getCurrentUrl() + PAGEINDEX_PARAM_NAME;
		List<String> pageLinks = new ArrayList<String>();

		for (int i = 1; i <= maxPageSize; i++) {
			// System.out.println("loading page : " + basePaginationUrl + i);
			driver.get(basePaginationUrl + i);
			populateRecipeLinks(allLInks);
		}

	}

}