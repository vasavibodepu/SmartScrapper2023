package com.numpy.scraping.team17;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
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
	private static final int MAX_RECEIPES_PER_MORBIDITY = 10;
	private static final int MAX_PAGES_TO_FETCH = 1;
	protected WebDriver driver = null;

	private static final String INGREDIENTS_WORK_SHEET_NAME = "Diabetes-Hypothyroidism-Hyperte";
	private static final String ALLERGIES_WORK_SHEET_NAME = "Allergies";
	private static final String FILE_PATH_ALLERGIES = SRC_TEST_RESOURCES_TEST_DATA_INPUT + "Allergies.xlsx";

	public BaseRecipeExtractor() {
		super();
	}

	@BeforeClass
	protected void loadHomePage() {
		WebDriverManager.chromedriver().setup();
		driver = new ChromeDriver();
		driver.get(HOME_PAGE);
		driver.manage().window().maximize();
	}

	@AfterClass
	protected void closeBrower() {
		driver.quit();
	}

	/**
	 * @param links
	 * @param allRecipes
	 */
	protected void exportRecipes(String morbidity, int eliminateIngredientColumn, int toAddIngredientColumn) {

		// arraylist to store all the links
		ArrayList<String> links = new ArrayList<>(14);

		fetchAllRecipeUrls(links, driver);
		System.out.println("Morbidity : " + morbidity + " :: Number of Urls to scrape : " + links.size());

		// load each recipe page from above list and populate the recipe object
		// accordingly
		List<Recipe> allRecipes = new ArrayList<Recipe>();

		for (String recipePageUrl : links) {
			Recipe recipe = new Recipe();
			recipe.setTargettedMorbidConditions(morbidity);
			recipe.setRecipeURL(recipePageUrl);

			// populate all other recipe fields from the details page.
			driver.get(recipePageUrl);
//			System.out.println("Scraping the Page : " + recipePageUrl);

			// read xpaths and values for name, ingredients etc
			populateRecipe(recipe);
			// System.out.println(recipe);

			allRecipes.add(recipe);
		}

		// Export allRecipes that does not have eliminated ingredients
		// into excel <Morbidity>.xlsx . e.g., Hypothyroidism.xlsx
		List<Recipe> safeList = new ArrayList<Recipe>();
		List<String> eliminateFIlter = readFiltersForEachMorbidity(eliminateIngredientColumn);

		for (Recipe recipe : allRecipes) {
			// Check if any string from filter columnValues is present in
			// ingredients using Stream API
			boolean foundEliminatedIngredient = eliminateFIlter.stream().anyMatch(recipe.getIngredients()::contains);
			if (!foundEliminatedIngredient) {
				safeList.add(recipe);
			}
		}

		// Write recipes that does not contain eliminate list into excel sheet
		// morbidity.xlsx. e.g, Hypothyroidism.xlsx
		ExcelWriter.writeRecipesToExcel(safeList, morbidity,
				SRC_TEST_RESOURCES_TEST_DATA_OUTPUT + TEAM_PREFIX_FOR_XLSX + morbidity + XLSX_EXTENSION);

		List<Recipe> nonAllergicRecipies = new ArrayList<Recipe>();
		List<String> alleriesList = readAllergiesList();

//		System.out.println("alleriesList : " + alleriesList);

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
		// sheet morbidity_To_Add.xlsx. e.g, Hypothyroidism_To_Add.xlsx
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
	 * index set Used the Single excel document shared by the Hackathon
	 * organizers and reading data for each morbidity based on column. e.g.,
	 * Eliminate List column for Hypothyroidism is 2.
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
	 * Reads the eliminate or to add list for each morbidity based on the column
	 * index set Used the Single excel document shared by the Hackathon
	 * organizers and reading data for each morbidity based on column. e.g.,
	 * Eliminate List column for Hypothyroidism is 2.
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

	private void populateRecipe(Recipe recipe) {

		// Recipe Id
		String tempRecipeId = recipe.getRecipeURL().substring(recipe.getRecipeURL().lastIndexOf("-") + 1,
				(recipe.getRecipeURL().length() - 1));
		recipe.setRecipeID(tempRecipeId);

		// Recipe Name
		WebElement recipeName = driver.findElement(By.xpath("//span[@id='ctl00_cntrightpanel_lblRecipeName']"));
		recipe.setRecipeName(recipeName.getText());

		// Recipe URL - already populated

		// //Recipe Category
		// WebElement recipeCategory = driver.findElement(By.xpath(""));

		// food category
		// WebElement foodCategory= driver.findElement(By.xpath(""));

		// ingredients
		List<WebElement> ingredients = driver.findElements(By.xpath("//*[@itemprop=\"recipeIngredient\"]"));
		StringBuffer ingredientsBuffer = new StringBuffer(256);
		for (WebElement ingredient : ingredients) {
			ingredientsBuffer.append(ingredient.getText());
			ingredientsBuffer.append("\n");
		}
		recipe.setIngredients(ingredientsBuffer.toString());

		// Preparation Time
		WebElement preparationTime = driver.findElement(By.xpath("//time[@itemprop='prepTime']"));
		recipe.setPreparationTime(preparationTime.getText());

		// cooking Time
		WebElement cookingTime = driver.findElement(By.xpath("//time[@itemprop='cookTime']"));
		recipe.setCookingTime(cookingTime.getText());

		// Preparation Method
		// WebElement preparationMethod =
		// driver.findElement(By.xpath("//div[@id='recipe_small_steps']"));
		// recipe.setPreparationmethod(preparationMethod.getText());

		// Nutrient Values
		// WebElement nutrientValues = driver.findElement(By.xpath(""));
		// String nutrition =
		// driver.findElement(By.xpath("//*[@id=\"rcpnuts\"]/span/span")).getText();
		// System.out.println(nutrition);

		StringBuffer nutritionValues = new StringBuffer();
		// TODO: make this table dynamic
		try {
			for (int i = 1; i <= 3; i++) {

				nutritionValues.append(
						driver.findElement(By.xpath("//*[@id=\"rcpnutrients\"]/tbody/tr[" + i + "]")).getText());
				nutritionValues.append("\n");

			}
		} catch (Exception e) {
		}

		recipe.setNutrientValues(nutritionValues.toString());

		// Targeted Morbid Conditions - Already populated
		// WebElement targettedMorbidConditions = driver
		// .findElement(By.xpath("//a[@id='ctl00_cntleftpanel_ttlhealthtree_tvTtlHealtht211']"));

	}

	/**
	 * @return
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

	private void fetchAllRecipeUrls(ArrayList<String> allLInks, WebDriver driver) {
		// list of WebElements that store all the links
		WebElement pagination = driver.findElement(By.xpath("//div[@id='pagination']"));
		List<WebElement> links = pagination.findElements(By.tagName("a"));
		List<String> pageLinks = new ArrayList<String>();

		// loop through page links to fill the all links
		Iterator<WebElement> linksIter = links.iterator();
		int pageCounter = 0;
		while ((linksIter.hasNext()) && (pageCounter < MAX_PAGES_TO_FETCH)) {
			WebElement paginationLink = (WebElement) linksIter.next();
			String pageToLoad = paginationLink.getAttribute("href");
			pageLinks.add(pageToLoad);

			pageCounter++;

		}

		for (String pageToLoad : pageLinks) {
			driver.get(pageToLoad);
			populateRecipeLinks(allLInks);
		}

	}

}